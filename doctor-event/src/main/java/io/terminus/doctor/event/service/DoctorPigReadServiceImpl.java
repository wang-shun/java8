package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorPigInfoDetailDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.enums.DataRange;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
public class DoctorPigReadServiceImpl implements DoctorPigReadService{

    private final DoctorPigDao doctorPigDao;

    private final DoctorPigTrackDao doctorPigTrackDao;

    private final DoctorPigEventReadService doctorPigEventReadService;

    @Autowired
    public DoctorPigReadServiceImpl(DoctorPigDao doctorPigDao, DoctorPigTrackDao doctorPigTrackDao,
                                    DoctorPigEventReadService doctorPigEventReadService){
        this.doctorPigDao = doctorPigDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorPigEventReadService = doctorPigEventReadService;
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
    public Response<DoctorPigInfoDetailDto> queryPigDetailInfoByPigId(Long pigId) {
        try{
            DoctorPig doctorPig = doctorPigDao.findById(pigId);
            checkState(!isNull(doctorPig), "query.doctorPigId.fail");
            
            DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(pigId);

            List<DoctorPigEvent> doctorPigEvents = RespHelper.orServEx(
                    doctorPigEventReadService.queryPigDoctorEvents(doctorPig.getFarmId(), doctorPig.getId(), null, null, null, null)).getData();

            return Response.ok(DoctorPigInfoDetailDto.builder().doctorPig(doctorPig).doctorPigTrack(doctorPigTrack).doctorPigEvents(doctorPigEvents).build());
        }catch (Exception e){
            log.error("query pig detail info fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.pigDetailInfo.fail");
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

            Map<Long, DoctorPigTrack> doctorPigTrackMap = doctorPigTracks.stream().collect(Collectors.toMap(k->k.getPigId(), v->v));

            return Response.ok(new Paging<>(paging.getTotal(),
                        doctorPigs.stream().map(s->DoctorPigInfoDto.buildDoctorPigInfoDto(s, doctorPigTrackMap.get(s.getId()))).collect(Collectors.toList())
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
            List<DoctorPig> doctorPigs = doctorPigDao.findByIds(doctorPigTracks.stream().map(s->s.getPigId()).collect(Collectors.toList()));
            Map<Long, DoctorPigTrack> doctorPigTrackMap = doctorPigTracks.stream().collect(Collectors.toMap(k->k.getPigId(),v->v));

            return Response.ok(new Paging<>(paging.getTotal(),
                    doctorPigs.stream().map(s->DoctorPigInfoDto.buildDoctorPigInfoDto(s, doctorPigTrackMap.get(s.getId()))).collect(Collectors.toList())
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

            return Response.ok(DoctorPigInfoDto.buildDoctorPigInfoDto(doctorPig, doctorPigTrack));
        }catch (Exception e){
            log.error(" fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.doctorInfoDto.fail");
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
            Map<Long, DoctorPigTrack> trackMap = tracks.stream().collect(Collectors.toMap(k->k.getPigId(), v->v));

            List<DoctorPigInfoDto> dtos =  doctorPigs.stream()
                    .map(doc->DoctorPigInfoDto.buildDoctorPigInfoDto(doc, trackMap.get(doc.getId())))
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
}
