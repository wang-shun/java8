package io.terminus.doctor.event.service;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.cache.DoctorPigInfoCache;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorPigInfoDetailDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.enums.DataRange;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-18
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Slf4j
@Service
@RpcProvider
public class DoctorPigReadServiceImpl implements DoctorPigReadService {

    private final DoctorPigDao doctorPigDao;

    private final DoctorPigTrackDao doctorPigTrackDao;

    private final DoctorPigEventReadService doctorPigEventReadService;

    private final DoctorBarnDao doctorBarnDao;

    private final DoctorPigInfoCache doctorPigInfoCache;

    private final DoctorPigEventDao doctorPigEventDao;

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM");

    @Autowired
    public DoctorPigReadServiceImpl(DoctorPigDao doctorPigDao, DoctorPigTrackDao doctorPigTrackDao,
                                    DoctorPigEventReadService doctorPigEventReadService,
                                    DoctorBarnDao doctorBarnDao, DoctorPigInfoCache doctorPigInfoCache,
                                    DoctorPigEventDao doctorPigEventDao){
        this.doctorPigDao = doctorPigDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorPigEventReadService = doctorPigEventReadService;
        this.doctorBarnDao = doctorBarnDao;
        this.doctorPigInfoCache = doctorPigInfoCache;
        this.doctorPigEventDao = doctorPigEventDao;
    }

    @Override
    public Response<Long> queryPigCount(Integer range, Long id, Integer pigType) {
        try{
            Map<String,Object> criteria = Maps.newHashMap();
            if(Objects.equals(range, DataRange.FARM.getKey())){
                criteria.put("farmId",id);
            }else if(Objects.equals(DataRange.ORG.getKey(), range)){
                criteria.put("orgId",id);
            }else {
                return Response.fail("range.input.error");
            }
            criteria.put("pigType",pigType);
            return Response.ok(doctorPigDao.count(criteria));
        }catch (Exception e){
            log.error(" query pig sow count fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.pigSowCount.fail");
        }
    }

    @Override
    public Response<DoctorPigInfoDetailDto> queryPigDetailInfoByPigId(Long pigId, Integer eventSize) {
        try{
            Integer dayAge = null;
            DoctorPig doctorPig = doctorPigDao.findById(pigId);
            checkState(!isNull(doctorPig), "query.doctorPigId.fail");

            if (doctorPig.getBirthDate() != null) {
                dayAge = (int)(DateTime.now()
                        .minus(doctorPig.getBirthDate().getTime()).getMillis() / (1000 * 60 * 60 * 24) + 1);
            }
            DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(pigId);

            List<DoctorPigEvent> doctorPigEvents = RespHelper.orServEx(
                    doctorPigEventReadService.queryPigDoctorEvents(doctorPig.getFarmId(), doctorPig.getId(), null, null, null, null)).getData();
            Long canRollback = null;
            DoctorPigEvent rollbackEvent = RespHelper.orServEx(doctorPigEventReadService.canRollbackEvent(doctorPig.getId()));
            if (rollbackEvent != null){
                canRollback = rollbackEvent.getId();
            }
            Integer targetEventSize = MoreObjects.firstNonNull(eventSize, 3);
            targetEventSize = targetEventSize > doctorPigEvents.size() ? doctorPigEvents.size() : targetEventSize;

            return Response.ok(DoctorPigInfoDetailDto.builder().doctorPig(doctorPig).doctorPigTrack(doctorPigTrack)
                    .doctorPigEvents(doctorPigEvents.subList(0, targetEventSize)).dayAge(dayAge).canRollback(canRollback).build());
        }catch (Exception e){
            log.error("query pig detail info fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.pigDetailInfo.fail");
        }
    }

    @Override
    public Response<DoctorPig> findPigById(Long pigId){
        try{
            return Response.ok(doctorPigDao.findById(pigId));
        }catch(Exception e){
            log.error("query pig fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.pig.fail");
        }
    }

    @Override
    public Response<Paging<DoctorPigInfoDto>> pagingDoctorInfoDtoByPig(DoctorPig doctorPig, Integer pageNo, Integer pageSize) {
        try{
            if(isNull(doctorPig.getFarmId())){
                return Response.fail("input.farmId.empty");
            }

            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            Paging<DoctorPig> paging = doctorPigDao.paging(pageInfo.getOffset(),pageInfo.getLimit(),doctorPig);
            if(paging.isEmpty()){
                return Response.ok(Paging.empty());
            }
            List<DoctorPig> doctorPigs = paging.getData();
            List<DoctorPigTrack> doctorPigTracks = doctorPigTrackDao.findByPigIds(doctorPigs.stream().map(s->s.getId()).collect(Collectors.toList()));

            Map<Long, List<DoctorPigEvent>> doctorPigEventMap = Maps.newHashMap();
            doctorPigTracks.forEach(doctorPigTrack -> {
                List<DoctorPigEvent> doctorPigEvents = doctorPigEventDao.queryAllEventsByPigId(doctorPigTrack.getPigId());
                doctorPigEventMap.put(doctorPigTrack.getPigId(), doctorPigEvents);
            });
            Map<Long, DoctorPigTrack> doctorPigTrackMap = doctorPigTracks.stream().collect(Collectors.toMap(k->k.getPigId(), v->v));

            return Response.ok(new Paging<>(paging.getTotal(),
                        doctorPigs.stream().map(s->DoctorPigInfoDto.buildDoctorPigInfoDto(s, doctorPigTrackMap.get(s.getId()), doctorPigEventMap.get(s.getId()))).collect(Collectors.toList())
                    ));
        }catch (Exception e){
            log.error("paging doctor info dto by pig, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("paging.doctorDto.fail");
        }
    }

    @Override
    public Response<Paging<DoctorPigInfoDto>> pagingDoctorInfoDtoByPigTrack(DoctorPigTrack doctorPigTrack, Integer pageNo, Integer pageSize) {
        try{
            if(isNull(doctorPigTrack.getFarmId())){
                return Response.fail("paging.doctorInfoDto.fail");
            }

            PageInfo pageInfo = new PageInfo(pageNo,pageSize);
            Paging<DoctorPigTrack> paging = doctorPigTrackDao.paging(pageInfo.getOffset(),pageInfo.getLimit(),doctorPigTrack);
            if(paging.isEmpty()){
                return Response.ok(Paging.empty());
            }
            List<DoctorPigTrack> doctorPigTracks = paging.getData();
            Map<Long, List<DoctorPigEvent>> doctorPigEventMap = Maps.newHashMap();
            doctorPigTracks.forEach(track -> {
                List<DoctorPigEvent> doctorPigEvents = doctorPigEventDao.queryAllEventsByPigId(doctorPigTrack.getPigId());
                doctorPigEventMap.put(track.getPigId(), doctorPigEvents);
            });
            List<DoctorPig> doctorPigs = doctorPigDao.findByIds(doctorPigTracks.stream().map(s->s.getPigId()).collect(Collectors.toList()));
            Map<Long, DoctorPigTrack> doctorPigTrackMap = doctorPigTracks.stream().collect(Collectors.toMap(k->k.getPigId(),v->v));

            return Response.ok(new Paging<>(paging.getTotal(),
                    doctorPigs.stream().map(s->DoctorPigInfoDto.buildDoctorPigInfoDto(s, doctorPigTrackMap.get(s.getId()), doctorPigEventMap.get(s.getId()))).collect(Collectors.toList())
                    ));
        }catch (Exception e){
            log.error("paging doctor info dto by track pig fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("paging.doctorInfoTrack.fail");
        }
    }

    @Override
    public Response<DoctorPigInfoDto> queryDoctorInfoDtoById(@NotNull(message = "input.pigId.empty") Long pigId) {
        try{
            DoctorPig doctorPig = doctorPigDao.findById(pigId);
            checkState(!isNull(doctorPig), "doctorPig.findById.empty");

            DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(pigId);
            List<DoctorPigEvent> doctorPigEvents = doctorPigEventDao.queryAllEventsByPigId(pigId);
            return Response.ok(DoctorPigInfoDto.buildDoctorPigInfoDto(doctorPig, doctorPigTrack, doctorPigEvents));
        }catch (Exception e){
            log.error(" fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.doctorInfoDto.fail");
        }
    }

    @Override
    public Response<String> generateFostersCode(String eventAt, Long farmId) {
        try{
            DateTime dateTime = DateUtil.DATE.parseDateTime(eventAt);
            Long farrowingCount =  doctorPigEventDao.countPigEventTypeDuration(
                    farmId, PigEvent.FARROWING.getKey(),
                    dateTime.withDayOfMonth(1).withTimeAtStartOfDay().toDate(),
                    dateTime.plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay().toDate());
            farrowingCount += 1;
            return Response.ok(dateTime.toString(DTF) + "-" + farrowingCount);
        }catch (IllegalStateException e){
            log.error(" input pigId not exist, farmId:{} ", farmId);
            return Response.fail(e.getMessage());
        }catch (Exception e){
            log.error("generate foster code fail,farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("generate.fostersCode.fail");
        }
    }

    @Override
    public Response<List<DoctorPigInfoDto>> queryDoctorPigInfoByBarnId(@NotNull(message = "input.barnId.empty") Long barnId) {
        try{
            List<DoctorPig> doctorPigs = doctorPigDao.list(ImmutableMap.of("barnId", barnId));
            if(isNull(doctorPigs) || Iterables.isEmpty(doctorPigs)){
                return Response.ok(Collections.emptyList());
            }

            List<DoctorPigTrack> tracks = doctorPigTrackDao.findByPigIds(doctorPigs.stream().map(d -> d.getId()).collect(Collectors.toList()));
            Map<Long, List<DoctorPigEvent>> doctorPigEventMap = Maps.newHashMap();
            tracks.forEach(track -> {
                List<DoctorPigEvent> doctorPigEvents = doctorPigEventDao.queryAllEventsByPigId(track.getPigId());
                doctorPigEventMap.put(track.getPigId(), doctorPigEvents);
            });
            Map<Long, DoctorPigTrack> trackMap = tracks.stream().collect(Collectors.toMap(k->k.getPigId(), v->v));

            List<DoctorPigInfoDto> dtos =  doctorPigs.stream()
                    .map(doc->DoctorPigInfoDto.buildDoctorPigInfoDto(doc, trackMap.get(doc.getId()), doctorPigEventMap.get(doc.getId())))
                    .collect(Collectors.toList());
        	return Response.ok(dtos);
        }catch (IllegalStateException se){
            log.warn("illegal state fail, cause:{}", Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        }catch (Exception e){
            log.error("query pig info by barn id fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.doctorPigInfo.fail");
        }
    }

    /**
     * 查询当前猪舍里的猪只列表(注意是当前猪舍)
     * @param barnId 猪舍id
     * @return 猪只列表
     */
    @Override
    public Response<List<DoctorPigTrack>> findActivePigTrackByCurrentBarnId(Long barnId) {
        try {
            List<DoctorPigTrack> pigTracks = doctorPigTrackDao.findByBarnId(barnId);

            //过滤掉公母猪已离场的
            return Response.ok(pigTracks.stream().filter(pig -> !pig.getStatus().equals(PigStatus.BOAR_LEAVE.getKey())
                    && !pig.getStatus().equals(PigStatus.Removal.getKey())).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("find active pig track by current barn id fail, barnId:{}, cause:{}", barnId, Throwables.getStackTraceAsString(e));
            return Response.fail("pig.track.find.fail");
        }
    }

    @Override
    public Response<List<DoctorPig>> findPigsByFarmId(Long farmId) {
        try {
            return Response.ok(doctorPigDao.findPigsByFarmId(farmId));
        } catch (Exception e) {
            log.error("find pigs by farmId failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("pig.find.fail");
        }
    }

    @Override
    public Response<DoctorPigTrack> findPigTrackByPigId(@NotNull(message = "input.pigId.empty") Long pigId) {
        try{
            return Response.ok(doctorPigTrackDao.findByPigId(pigId));
        } catch (Exception e) {
            log.error("find pig track by pig id failed, pig id is {}, cause by {}", pigId, Throwables.getStackTraceAsString(e));
            return Response.fail("pig.track.find.fail");
        }
    }

    @Override
    public Response<Boolean> validatePigCodeByFarmId(Long orgId, String pigCode) {
        try{
            return Response.ok(doctorPigInfoCache.judgePigCodeNotContain(orgId, pigCode));
        }catch (Exception e){
            log.error("validate pig code not in farm fail, farmId:{}, pigCode:{}, cause:{}", orgId, pigCode, Throwables.getStackTraceAsString(e));
            return Response.fail("validate.pigCode.fail");
        }
    }

    @Override
    public Response<Set<Integer>> findPigStatusByBarnId(Long barnId) {
        try {
            return Response.ok(Sets.newHashSet(doctorPigTrackDao.findStatusByBarnId(barnId)));
        } catch (Exception e) {
            log.error("find pig status by barnId failed, barnId:{}, cause:{}", barnId, Throwables.getStackTraceAsString(e));
            return Response.fail("pig.status.find.fail");
        }
    }

    @Override
    public Response<DoctorBarn> findBarnByPigId(Long pigId) {
        try {
            //从pigTrack查当前猪舍
            DoctorPigTrack pigTrack = doctorPigTrackDao.findByPigId(pigId);
            return Response.ok(doctorBarnDao.findById(pigTrack.getCurrentBarnId()));
        } catch (Exception e) {
            log.error("find barn by pig id failed, pigId:{}, cause:{}", pigId, Throwables.getStackTraceAsString(e));
            return Response.fail("pig.find.fail");
        }
    }

    @Override
    public Response<Integer> getCountOfMating(@NotNull(message = "pigId.not.null") Long pigId) {
        try {
            DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(pigId);
            return Response.ok(doctorPigTrack.getCurrentMatingCount() + 1);
        } catch (Exception e) {
            log.error("fail to get count of mating of pig id:{}, cause:{}", pigId, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        }
    }

    @Override
    public Response<Date> getFirstMatingTime(@NotNull(message = "pigId.not.null") Long pigId, @NotNull(message = "farmId.not.null") Long farmId) {
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(pigId);
        DateTime matingDate;
        if (doctorPigTrack.getCurrentMatingCount() == null) {
            log.error("fail to get first mating time by pig id:{}",pigId);
            return Response.fail("get.first.mating.time.fail");
        }

        if (doctorPigTrack.getCurrentMatingCount() > 0) {
            Map<String, Object> criteria = ImmutableMap.of("pigId", pigId, "farmId", farmId, "count", doctorPigTrack.getCurrentMatingCount()-1, "type", PigEvent.MATING.getKey(), "kind", DoctorPig.PIG_TYPE.SOW.getKey());
            DoctorPigEvent doctorPigEvent = doctorPigEventDao.getFirstMatingTime(criteria);
            matingDate = new DateTime(Long.valueOf(doctorPigEvent.getExtraMap().get("matingDate").toString()));
            return Response.ok(matingDate.plusDays(114).toDate());
        } else {
            return Response.ok(null);
        }
    }
}
