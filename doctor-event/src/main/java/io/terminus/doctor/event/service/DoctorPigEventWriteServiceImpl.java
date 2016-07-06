package io.terminus.doctor.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
import io.terminus.doctor.event.dto.event.sow.DoctorAbortionDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPartWeanDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.dto.event.usual.DoctorConditionDto;
import io.terminus.doctor.event.dto.event.usual.DoctorDiseaseDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.dto.event.usual.DoctorRemovalDto;
import io.terminus.doctor.event.dto.event.usual.DoctorVaccinationDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.event.DoctorPigCountEvent;
import io.terminus.doctor.event.event.PigEventCreateEvent;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-19
 * Email:yaoqj@terminus.io
 * Descirbe: 猪事件信息录入方式
 */
@Service
@Slf4j
public class DoctorPigEventWriteServiceImpl implements DoctorPigEventWriteService{

    private final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    private final DoctorPigEventManager doctorPigEventManager;

    private final DoctorPigTrackDao doctorPigTrackDao;

    private final DoctorPigReadService doctorPigReadService;

    private final CoreEventDispatcher coreEventDispatcher;

    private final DoctorPigEventDao doctorPigEventDao;

    public static final List<Integer> NOT_ALLOW_ROLL_BACK_EVENTS =Lists.newArrayList(
            PigEvent.ENTRY.getKey(),PigEvent.FARROWING.getKey(),
            PigEvent.FOSTERS.getKey(),PigEvent.FOSTERS_BY.getKey());

    @Autowired(required = false)
    private Publisher publisher;

    @Autowired
    public DoctorPigEventWriteServiceImpl(
            DoctorPigEventManager doctorPigEventManager, CoreEventDispatcher coreEventDispatcher,
            DoctorPigTrackDao doctorPigTrackDao, DoctorPigReadService doctorPigReadService,
            DoctorPigEventDao doctorPigEventDao){
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorPigEventManager = doctorPigEventManager;
        this.coreEventDispatcher = coreEventDispatcher;
        this.doctorPigReadService = doctorPigReadService;
        this.doctorPigEventDao = doctorPigEventDao;
    }

    @Override
    public Response<Long> rollBackPigEvent(Long pigEventId,Integer revertPigType,Long staffId, String staffName) {
        try{
            // validate lastest event
            DoctorPigEvent doctorPigEvent = doctorPigEventDao.findById(pigEventId);
            checkState(!isNull(doctorPigEvent), "input.pigEventId.error");

            DoctorPigEvent doctorPigEventLast = doctorPigEventDao.queryLastPigEventById(doctorPigEvent.getPigId());
            checkState(Objects.equals(doctorPigEventLast.getId(), pigEventId), "pigRollBack.error.notLastly");

            checkState(!NOT_ALLOW_ROLL_BACK_EVENTS.contains(doctorPigEvent.getType()), "pigRollBack.eventType.notAllow");

            return Response.ok(doctorPigEventManager.rollBackPigEvent(pigEventId,revertPigType,staffId,staffName));
        }catch (IllegalStateException e){
            log.error("illegal state pig roll back info, doctorEventId:{}, cause:{}", pigEventId, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        }catch (Exception e){
            log.error("pig roll back fail, doctorEventId:{}, cause:{}",pigEventId, Throwables.getStackTraceAsString(e));
            return Response.fail("pig.rollBack.fail");
        }
    }

    @Override
    public Response<Long> pigEntryEvent(DoctorBasicInputInfoDto doctorBasicInputInfoDto, DoctorFarmEntryDto doctorFarmEntryDto) {
        try{
            Map<String,Object> extra = Maps.newHashMap();
            BeanMapper.copy(doctorFarmEntryDto, extra);
            Map<String,Object> result = Maps.newHashMap();
            result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, extra);

            // publish zk event
            publishEvent(result);

            coreEventDispatcher.publish(DoctorPigCountEvent.builder()
                    .farmId(doctorBasicInputInfoDto.getFarmId())
                    .orgId(doctorBasicInputInfoDto.getOrgId())
                    .pigType(doctorBasicInputInfoDto.getPigType()).build());

            return Response.ok(Params.getWithConvert(result,"doctorPigId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("pig entry event create fail,basicInfo:{}, doctorFarmEntryDto:{}, cause:{}",
                    doctorBasicInputInfoDto, doctorFarmEntryDto,
                    Throwables.getStackTraceAsString(e));
            return Response.fail("create.entryEvent.fail");
        }
    }

    @Override
    public Response<Long> diseaseEvent(DoctorDiseaseDto doctorDiseaseDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            doctorDiseaseDto.setDiseaseStaff(doctorBasicInputInfoDto.getStaffName());

            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorDiseaseDto, dto);

            Map<String, Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"doctorEventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("disease event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.diseaseEvent.fail");
        }
    }

    @Override
    public Response<Boolean> diseaseEvents(DoctorDiseaseDto doctorDiseaseDto, DoctorBasicInputInfoDto basicInputInfoDto) {
        try{
            Response<List<DoctorPigInfoDto>> listResponse = this.doctorPigReadService.queryDoctorPigInfoByBarnId(basicInputInfoDto.getBarnId());
            checkState(listResponse.isSuccess(), "query.pigByBarnId.fail");

            List<DoctorBasicInputInfoDto> basicInputInfoDtos = listResponse.getResult().stream()
                    .map(dto->basicInputInfoDto.buildSameBarnPigInfo(dto.getId(),dto.getPigType(),dto.getPigCode())).collect(Collectors.toList());

            Map<String,Object> beans = Maps.newHashMap();
            BeanMapper.copy(doctorDiseaseDto, beans);

            doctorPigEventManager.createCasualPigEvents(basicInputInfoDtos, beans);
        	return Response.ok(Boolean.TRUE);
        }catch (IllegalStateException se){
            log.warn("illegal state, create events disease fail, cause:{}", Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        }catch (Exception e){
            log.error("create diseases events fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.diseaseEvents.fail");
        }
    }

    @Override
    public Response<Long> vaccinationEvent(DoctorVaccinationDto doctorVaccinationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            doctorVaccinationDto.setVaccinationStaffId(doctorBasicInputInfoDto.getStaffId());
            doctorVaccinationDto.setVaccinationStaffName(doctorBasicInputInfoDto.getStaffName());

            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorVaccinationDto, dto);

            Map<String,Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"doctorEventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> vaccinationEvents(DoctorVaccinationDto doctorVaccinationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Response<List<DoctorPigInfoDto>> listResponse = this.doctorPigReadService.queryDoctorPigInfoByBarnId(doctorBasicInputInfoDto.getBarnId());
            checkState(listResponse.isSuccess(), "query.vaccinations.error");

            List<DoctorBasicInputInfoDto> basicInputInfoDtos = listResponse.getResult().stream()
                    .map(dto->doctorBasicInputInfoDto.buildSameBarnPigInfo(dto.getId(),dto.getPigType(),dto.getPigCode())).collect(Collectors.toList());

            Map<String,Object> beans = Maps.newHashMap();
            BeanMapper.copy(doctorVaccinationDto, beans);

            doctorPigEventManager.createCasualPigEvents(basicInputInfoDtos, beans);
        	return Response.ok(Boolean.TRUE);
        }catch (IllegalStateException se){
            log.warn("illegal state fail, cause:{}", Throwables.getStackTraceAsString(se));
            return Response.fail(se.getMessage());
        }catch (Exception e){
            log.error("vaccination events create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("vaccination.createEvents.fail");
        }
    }

    @Override
    public Response<Long> conditionEvent(DoctorConditionDto doctorConditionDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorConditionDto, dto);

            Map<String,Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"doctorEventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("condition event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.condition.fail");
        }
    }

    @Override
    public Response<Long> chgLocationEvent(DoctorChgLocationDto doctorChgLocationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorChgLocationDto, dto);

            Map<String,Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"doctorEventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Long> abortionEvent(DoctorAbortionDto doctorAbortionDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorAbortionDto, dto);
            Map<String,Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
            return Response.ok(Params.getWithConvert(result, "doctorEventId", a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("abortion create event fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.abortionEvent.fail");
        }
    }

    @Override
    public Response<Long> chgFarmEvent(DoctorChgFarmDto doctorChgFarmDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorChgFarmDto, dto);

            Map<String,Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"doctorEventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Long> removalEvent(DoctorRemovalDto doctorRemovalDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorRemovalDto, dto);

            Map<String,Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"doctorEventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Long> boarSemenEvent(DoctorSemenDto doctorSemenDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorSemenDto, dto);

            Map<String,Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"doctorEventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Long> sowMatingEvent(DoctorMatingDto doctorMatingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            doctorMatingDto.setMatingStaff(doctorBasicInputInfoDto.getStaffName());

            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorMatingDto, dto);

            Map<String,Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"doctorEventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Long> chgSowLocationEvent(DoctorChgLocationDto doctorChgLocationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorChgLocationDto, dto);

            doctorChgLocationDto.setChgLocationFromBarnId(doctorBasicInputInfoDto.getBarnId());
            doctorChgLocationDto.setChgLocationFromBarnName(doctorBasicInputInfoDto.getBarnName());

            Map<String,Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
            return Response.ok(Params.getWithConvert(result, "doctorEventId", a->Long.valueOf(a.toString())));
        }catch(IllegalStateException e){
            log.error("change sow location event illegal status, cause:{}", e.getMessage());
            return Response.fail(e.getMessage());
        }catch (Exception e){
            log.error("change sow location event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.chgLocation.fail");
        }
    }

    @Override
    public Response<Long> sowPregCheckEvent(DoctorPregChkResultDto doctorPregChkResultDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorPregChkResultDto, dto);

            Map<String,Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"doctorEventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Long> sowFarrowingEvent(DoctorFarrowingDto doctorFarrowingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            doctorFarrowingDto.setFarrowStaff1(doctorBasicInputInfoDto.getStaffName());
            doctorFarrowingDto.setFarrowStaff2(doctorBasicInputInfoDto.getStaffName());

            // validate count
            checkState(Objects.equals(
                            doctorFarrowingDto.getFarrowingLiveCount(),
                            doctorFarrowingDto.getHealthCount() + doctorFarrowingDto.getWeakCount()), "validate.farrowingCount.fail");
            checkState(Objects.equals(doctorFarrowingDto.getFarrowingLiveCount(),
                            doctorFarrowingDto.getLiveBoarCount() + doctorFarrowingDto.getLiveSowCount()),
                    "validate.farrowingCount.fail");

            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorFarrowingDto, dto);

            Map<String,Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"doctorEventId",a->Long.valueOf(a.toString())));
        }catch (IllegalStateException e){
            log.error("illegal state validate farrow count error, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    @Deprecated
    public Response<Long> sowPigletsChgEvent(DoctorPigletsChgDto doctorPigletsChgDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorPigletsChgDto, dto);

            Map<String,Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"doctorEventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Long> sowPartWeanEvent(DoctorPartWeanDto doctorPartWeanDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorPartWeanDto, dto);

            Map<String,Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"doctorEventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> sowPigsEventCreate(List<DoctorBasicInputInfoDto> basics, Map<String, Object> extra) {
        try{
            // 批量信息创建
            Map<String,Object> result = doctorPigEventManager.createSowEvents(basics, extra);

            // pub result info
            publishEvent(result);

            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("sow pigs event creates fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.pigsEvent.fail");
        }
    }

    @Override
    public Response<Boolean> casualPigsEventCreate(List<DoctorBasicInputInfoDto> basics, Map<String, Object> extra) {
        try{
            // 批量信息创建
            Map<String, Object> result = doctorPigEventManager.createCasualPigEvents(basics, extra);

            // pull result info
            publishEvent(result);

            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("casual events pigs event create fail, basics:{}, extra:{}, cause:{}",basics, extra, Throwables.getStackTraceAsString(e));
            return Response.fail("create.casualPigsEvent.fail");
        }
    }

    /**
     * 推送对应的事件信息
     * @param results
     */
    private void publishEvent (Map<String,Object> results){
        if(publisher == null){
            // coreEventDispatcher.publish(new PigEventCreateEvent(results));
            coreEventDispatcher.publish(DataEvent.make(DataEventType.PigEventCreate.getKey(), results));
        }else{
            try {
                publisher.publish(DataEvent.toBytes(DataEventType.PigEventCreate.getKey(), new PigEventCreateEvent(results)));
            }catch (Exception e){
                log.error("failed to publish event, cause:{}", e);
            }
        }
    }
}
