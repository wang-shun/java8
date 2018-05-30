package io.terminus.doctor.event.service;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.cache.DoctorPigInfoCache;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorChgFarmInfoDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigJoinDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorPigInfoDetailDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.IotPigDto;
import io.terminus.doctor.event.dto.search.DoctorPigCountDto;
import io.terminus.doctor.event.dto.search.SearchedPig;
import io.terminus.doctor.event.enums.KongHuaiPregCheckResult;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorChgFarmInfo;
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
import static io.terminus.common.utils.Arguments.notNull;
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

    private final DoctorPigJoinDao doctorPigJoinDao;

    private final DoctorChgFarmInfoDao doctorChgFarmInfoDao;

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyyMM");

    private static final JsonMapperUtil JSON_MAPPER = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER;

    @Autowired
    public DoctorPigReadServiceImpl(DoctorPigDao doctorPigDao, DoctorPigTrackDao doctorPigTrackDao,
                                    DoctorPigEventReadService doctorPigEventReadService,
                                    DoctorBarnDao doctorBarnDao, DoctorPigInfoCache doctorPigInfoCache,
                                    DoctorPigEventDao doctorPigEventDao, DoctorPigJoinDao doctorPigJoinDao, DoctorChgFarmInfoDao doctorChgFarmInfoDao){
        this.doctorPigDao = doctorPigDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorPigEventReadService = doctorPigEventReadService;
        this.doctorBarnDao = doctorBarnDao;
        this.doctorPigInfoCache = doctorPigInfoCache;
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorPigJoinDao = doctorPigJoinDao;
        this.doctorChgFarmInfoDao = doctorChgFarmInfoDao;
    }

    @Override
    public Response<Long> getPigCount(Long farmId, DoctorPig.PigSex pigSex) {
        try {
            return Response.ok(doctorPigDao.getPigCount(farmId, pigSex));
        } catch (Exception e) {
            log.error("get pig count failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("get.pig.count.fail");
        }
    }

    @Override
    public RespWithEx<DoctorPigInfoDetailDto> queryPigDetailInfoByPigId(Long farmId, Long pigId, Integer eventSize) {
        try {
            Integer dayAge = null;
            DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(pigId);
            DoctorPig doctorPig = doctorPigDao.findById(pigId);
            log.error("queryPigDetailInfoByPigId:1");
            DoctorChgFarmInfo doctorChgFarmInfo = null;
            if (!Objects.equals(doctorPigTrack.getFarmId(), farmId)) {
                doctorChgFarmInfo = doctorChgFarmInfoDao.findByFarmIdAndPigId(farmId, pigId);
                doctorPigTrack = JSON_MAPPER.fromJson(doctorChgFarmInfo.getTrack(), DoctorPigTrack.class);
                doctorPig = JSON_MAPPER.fromJson(doctorChgFarmInfo.getPig(), DoctorPig.class);
            }
            log.error("queryPigDetailInfoByPigId:2");
            if (doctorPig == null) {
                return RespWithEx.fail("pig.not.found");
            }

            if (doctorPig.getBirthDate() != null) {
                dayAge = (int) (DateTime.now()
                        .minus(doctorPig.getBirthDate().getTime()).getMillis() / (1000 * 60 * 60 * 24) + 1);
            }
            Integer targetEventSize = MoreObjects.firstNonNull(eventSize, 3);
            log.error("queryPigDetailInfoByPigId:3");
            List<DoctorPigEvent> doctorPigEvents;
            if (isNull(doctorChgFarmInfo)) {
                doctorPigEvents = RespHelper.orServEx(
                        doctorPigEventReadService.queryPigDoctorEvents(null, pigId, 1, targetEventSize, null, null)).getData();
            } else {
                doctorPigEvents = doctorPigEventDao.queryBeforeChgFarm(pigId, doctorChgFarmInfo.getEventId());
            }
            log.error("queryPigDetailInfoByPigId:4"+isNull(doctorChgFarmInfo));
            return RespWithEx.ok(DoctorPigInfoDetailDto.builder().doctorPig(doctorPig).doctorPigTrack(doctorPigTrack)
                    .doctorPigEvents(doctorPigEvents).dayAge(dayAge).isChgFarm(notNull(doctorChgFarmInfo)).build());
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (Exception e){
            e.printStackTrace();
            log.error("query pig detail info fail, cause:{}", Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("query.pigDetailInfo.fail");
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
            List<DoctorPigTrack> doctorPigTracks = doctorPigTrackDao.findByPigIds(doctorPigs.stream().map(DoctorPig::getId).collect(Collectors.toList()));

            Map<Long, List<DoctorPigEvent>> doctorPigEventMap = Maps.newHashMap();
            doctorPigTracks.forEach(doctorPigTrack -> {
                List<DoctorPigEvent> doctorPigEvents = doctorPigEventDao.queryAllEventsByPigId(doctorPigTrack.getPigId());
                doctorPigEventMap.put(doctorPigTrack.getPigId(), doctorPigEvents);
            });
            Map<Long, DoctorPigTrack> doctorPigTrackMap = doctorPigTracks.stream().collect(Collectors.toMap(DoctorPigTrack::getPigId, v->v));

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
            List<DoctorPig> doctorPigs = doctorPigDao.findByIds(doctorPigTracks.stream().map(DoctorPigTrack::getPigId).collect(Collectors.toList()));
            Map<Long, DoctorPigTrack> doctorPigTrackMap = doctorPigTracks.stream().collect(Collectors.toMap(DoctorPigTrack::getPigId, v->v));

            return Response.ok(new Paging<>(paging.getTotal(),
                    doctorPigs.stream().map(s->DoctorPigInfoDto.buildDoctorPigInfoDto(s, doctorPigTrackMap.get(s.getId()), doctorPigEventMap.get(s.getId()))).collect(Collectors.toList())
                    ));
        }catch (Exception e){
            log.error("paging doctor info dto by track pig fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("paging.doctorInfoTrack.fail");
        }
    }

    @Override
    public Response<Paging<SearchedPig>> pagingPig(Map<String, Object> params, Integer pageNo, Integer pageSize) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            Paging<SearchedPig> paging = doctorPigJoinDao.pigPagingWithJoin(params, pageInfo.getOffset(), pageInfo.getLimit());

            return Response.ok(new Paging<>(paging.getTotal(), mapSearchPig(paging.getData())));
        } catch (Exception e) {
            log.error("paging pig failed, params:{}, pageNo:{}, pageSize:{}, cause:{}", params, pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("paging.pig.fail");
        }
    }

    private List<SearchedPig> mapSearchPig(List<SearchedPig> pigs) {
        return pigs.stream()
                .map(pig -> {
                    if (pig.getBirthDate() != null) {
                        pig.setDayAge((int)(DateTime.now().minus(pig.getBirthDate().getTime()).getMillis() / (1000 * 60 * 60 * 24) + 1));
                    }

                    if(pig.getStatus() != null){
                        PigStatus pigStatus = PigStatus.from(pig.getStatus());
                        if(pigStatus != null){
                            pig.setStatusName(pigStatus.getName());
                        }
                    }

                    // 如果是待分娩状态, 获取妊娠检查的时间
                    if (Objects.equals(pig.getStatus(), PigStatus.Farrow.getKey()) || Objects.equals(pig.getStatus(), PigStatus.KongHuai.getKey())) {
                        DoctorPigEvent pregEvent = doctorPigEventDao.queryLastPregCheck(pig.getId());
                        if (pregEvent == null) {
                            return pig;
                        }
                        pig.getExtra().put("checkDate", pregEvent.getEventAt());

                        // 处理 KongHuaiPregCheckResult
                        if (Objects.equals(pig.getStatus(), PigStatus.KongHuai.getKey())) {
                            KongHuaiPregCheckResult result = getPreg(pregEvent.getPregCheckResult());
                            if (result != null) {
                                pig.setStatus(result.getKey());
                                pig.setStatusName(result.getName());
                            }
                        }
                    }
                    return pig;
                })
                .collect(Collectors.toList());
    }

    private static KongHuaiPregCheckResult getPreg(Integer pregCheckResult) {
        if (Objects.equals(pregCheckResult, PregCheckResult.FANQING.getKey())) {
            return KongHuaiPregCheckResult.FANQING;
        } else if (Objects.equals(pregCheckResult, PregCheckResult.YING.getKey())) {
            return KongHuaiPregCheckResult.YING;
        } else if (Objects.equals(pregCheckResult, PregCheckResult.LIUCHAN.getKey())) {
            return KongHuaiPregCheckResult.LIUCHAN;
        } else {
            return null;
        }
    }

    @Override
    public Response<DoctorPigInfoDto> queryDoctorInfoDtoById(@NotNull(message = "input.pigId.empty") Long pigId) {
        try{
            DoctorPig doctorPig = doctorPigDao.findById(pigId);
            checkState(notNull(doctorPig), "pig.not.found");

            DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(pigId);
            if (Objects.equals(doctorPigTrack.getStatus(), PigStatus.KongHuai.getKey())) {
                Integer result = (Integer) doctorPigTrack.getExtraMap().get("pregCheckResult");
                if (result != null) {
                    doctorPigTrack.setStatus(result);
                }
            }
            List<DoctorPigEvent> doctorPigEvents = doctorPigEventDao.queryAllEventsByPigId(pigId);
            return Response.ok(DoctorPigInfoDto.buildDoctorPigInfoDto(doctorPig, doctorPigTrack, doctorPigEvents));
        }catch (Exception e){
            log.error(" fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.doctorInfoDto.fail");
        }
    }

    @Override
    public Response<String> genNest(Long farmId, String eventAt, Integer size) {
        try {
            DateTime dateTime = Arguments.isEmpty(eventAt) ? DateTime.now() : DateUtil.DATE.parseDateTime(eventAt);
            Long farrowingCount =  doctorPigEventDao.countPigEventTypeDuration(
                    farmId,
                    PigEvent.FARROWING.getKey(),
                    dateTime.withDayOfMonth(1).withTimeAtStartOfDay().toDate(),
                    dateTime.plusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay().toDate());
            farrowingCount += MoreObjects.firstNonNull(size, 1);
            return Response.ok(dateTime.toString(DTF) + "-" + farrowingCount);
        } catch (Exception e) {
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

            List<DoctorPigTrack> tracks = doctorPigTrackDao.findByPigIds(doctorPigs.stream().map(DoctorPig::getId).collect(Collectors.toList()));
            Map<Long, List<DoctorPigEvent>> doctorPigEventMap = Maps.newHashMap();
            tracks.forEach(track -> {
                List<DoctorPigEvent> doctorPigEvents = doctorPigEventDao.queryAllEventsByPigId(track.getPigId());
                doctorPigEventMap.put(track.getPigId(), doctorPigEvents);
            });
            Map<Long, DoctorPigTrack> trackMap = tracks.stream().collect(Collectors.toMap(DoctorPigTrack::getPigId, v->v));

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
    public Response<Boolean> validatePigCodeByFarmId(Long farmId, String pigCode) {
        try{
            return Response.ok(doctorPigInfoCache.judgePigCodeNotContain(farmId, pigCode));
        }catch (Exception e){
            log.error("validate pig code not in farm fail, farmId:{}, pigCode:{}, cause:{}", farmId, pigCode, Throwables.getStackTraceAsString(e));
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
            DoctorPigEvent doctorPigEvent = doctorPigEventDao.getFirstMateEvent(doctorPigTrack.getPigId(), new Date());
            return Response.ok(doctorPigEvent.getEventAt());
        } else {
            return Response.ok(null);
        }
    }

    @Override
    public Response<Long> getPigCountByBarnPigTypes(Long farmId, List<Integer> pigTypes) {
        try {
            List<DoctorBarn> barns = doctorBarnDao.findByEnums(farmId, pigTypes, null, null, null);
            if (Arguments.isNullOrEmpty(barns)) {
                return Response.ok(0L);
            }
            return Response.ok(doctorPigTrackDao.countByBarnIds(barns.stream().map(DoctorBarn::getId).collect(Collectors.toList())));
        } catch (Exception e) {
            log.error("getPigCountByBarnPigTypes failed, farmId:{}, pigTypes:{}, cause:{}", farmId, pigTypes, Throwables.getStackTraceAsString(e));
            return Response.ok(0L);
        }
    }

    @Override
    public Response<DoctorPigCountDto> getPigCount(@NotNull(message = "farmId.not.null") Long farmId) {
        try {
            DoctorPigCountDto pigCountDto = doctorPigJoinDao.findPigCount(farmId);
            Integer sowTotalCount = pigCountDto.getFarrowCount() + pigCountDto.getKonghuaiCount() + pigCountDto.getPregCount();
            pigCountDto.setBoarCount(doctorPigJoinDao.findBoarPigCount(farmId));
            pigCountDto.setSowTotalCount(sowTotalCount);
            return Response.ok(pigCountDto);
        } catch (Exception e) {
            log.error("getPigCount.failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("get.pig.count.failed");
        }
    }

    @Override
    public Response<List<DoctorPigTrack>> queryCurrentStatus(List<Long> pigIds) {
        try {
            if (Arguments.isNullOrEmpty(pigIds)) {
                return Response.ok(Lists.newArrayList());
            }
            return Response.ok(doctorPigTrackDao.queryCurrentStatus(pigIds));
        } catch (Exception e) {
            log.error("query current status failed, pigIds:{}, cause:{}", pigIds, Throwables.getStackTraceAsString(e));
            return Response.fail("query.current.status.failed");
        }
    }

    @Override
    public Response<List<DoctorPig>> suggestSowPig(Long barnId, String name, Integer count) {
        try {
            return Response.ok(doctorPigJoinDao.suggestSowPig(barnId, name, count));
        } catch (Exception e) {
            log.error("suggest sow pig failed,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("suggest.sow.pig.failed");
        }
    }

    @Override
    public Response<IotPigDto> getIotPig(Long pigId) {
        try {
            DoctorPig doctorPig = doctorPigDao.findById(pigId);
            IotPigDto iotPigDto = new IotPigDto();
            iotPigDto.setPigId(doctorPig.getId());
            iotPigDto.setPigCode(doctorPig.getPigCode());
            iotPigDto.setRfid(doctorPig.getRfid());
            DoctorPigTrack pigTrack = doctorPigTrackDao.findByPigId(pigId);
            iotPigDto.setStatus(pigTrack.getStatus());
            PigStatus status = PigStatus.from(iotPigDto.getStatus());
            iotPigDto.setStatusName(status.getName());
            if (Objects.equals(pigTrack.getStatus(), PigStatus.KongHuai.getKey())) {
                iotPigDto.setStatus((Integer) pigTrack.getExtraMap().get("pregCheckResult"));
                KongHuaiPregCheckResult result = KongHuaiPregCheckResult.from(iotPigDto.getStatus());
                iotPigDto.setStatusName(result.getName());
            }
            Date statusDate = doctorPigEventDao.findEventAtLeadToStatus(pigId, pigTrack.getStatus());
            iotPigDto.setStatusDay(DateUtil.getDeltaDays(statusDate, new Date()));
            return Response.ok(iotPigDto);
        } catch (Exception e) {
            log.error("get iot pig failed, pigId:{}, cause:{}", pigId, Throwables.getStackTraceAsString(e));
            return Response.fail("get.iot.pig.failed");
        }
    }

    @Override
    public Response<List<DoctorPig>> findUnRemovalPigsBy(Long barnId) {
        try {
            return Response.ok(doctorPigJoinDao.findUnRemovalPigsBy(barnId));
        } catch (Exception e) {
            log.error("find unremoval pigs by failed,barnId:{}, cause:{}", barnId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.unremoval.pigs.by.failed");
        }
    }

    @Override
    public Response<DoctorChgFarmInfo> findByFarmIdAndPigId(Long farmId, Long pigId) {
        try {
            return Response.ok(doctorChgFarmInfoDao.findByFarmIdAndPigId(farmId, pigId));
        } catch (Exception e) {
            log.error("find by farmId and pigId, farmId:{}, pigId:{}, cause:{}",
                    farmId, pigId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.by.farmId.and.pigId");
        }
    }

    @Override
    public Response<Paging<SearchedPig>> pagingChgFarmPig(Map<String, Object> params, Integer pageNo, Integer pageSize) {
        try {
            PageInfo pageInfo = PageInfo.of(pageNo, pageSize);
            Paging<DoctorChgFarmInfo> paging = doctorChgFarmInfoDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), params);
            if (paging.isEmpty()) {
                return Response.ok(Paging.empty());
            }

            List<SearchedPig> list = paging.getData().stream().map(doctorChgFarmInfo -> {
                DoctorPig doctorPig = JSON_MAPPER.fromJson(doctorChgFarmInfo.getPig(), DoctorPig.class);
                DoctorPigTrack doctorPigTrack = JSON_MAPPER.fromJson(doctorChgFarmInfo.getTrack(), DoctorPigTrack.class);
                SearchedPig searchedPig = new SearchedPig();
                BeanMapper.copy(doctorPig, searchedPig);
                if (searchedPig.getBirthDate() != null) {
                    searchedPig.setDayAge((int)(DateTime.now().minus(searchedPig.getBirthDate().getTime()).getMillis() / (1000 * 60 * 60 * 24) + 1));
                }
                searchedPig.setStatus(doctorPigTrack.getStatus());
                searchedPig.setStatusName(PigStatus.from(doctorPigTrack.getStatus()).getName());
                searchedPig.setCurrentBarnId(doctorPigTrack.getCurrentBarnId());
                searchedPig.setCurrentBarnName(doctorPigTrack.getCurrentBarnName());
                searchedPig.setCurrentParity(doctorPigTrack.getCurrentParity());
                return searchedPig;
            }).collect(Collectors.toList());
            return Response.ok(new Paging<>(paging.getTotal(), list));
        } catch (Exception e) {
            log.error("paging chg farm pig failed,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("paging.chg.farm.pig.failed");
        }
    }
}
