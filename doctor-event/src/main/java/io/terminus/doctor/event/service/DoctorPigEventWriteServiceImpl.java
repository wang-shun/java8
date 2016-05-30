package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFostersDto;
import io.terminus.doctor.event.dto.event.sow.DoctorLitterWeightDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPartWeanDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.dto.event.sow.DoctorWeanDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.dto.event.usual.DoctorConditionDto;
import io.terminus.doctor.event.dto.event.usual.DoctorDiseaseDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.dto.event.usual.DoctorRemovalDto;
import io.terminus.doctor.event.dto.event.usual.DoctorVaccinationDto;
import io.terminus.doctor.event.event.PigEventCreateEvent;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-19
 * Email:yaoqj@terminus.io
 * Descirbe: 猪事件信息录入方式
 */
@Service
@Slf4j
public class DoctorPigEventWriteServiceImpl implements DoctorPigEventWriteService{

    private final DoctorPigEventManager doctorPigEventManager;

    private final EventBus eventBus;

    @Autowired(required = false)
    private Publisher publisher;

    @Autowired
    public DoctorPigEventWriteServiceImpl(DoctorPigEventManager doctorPigEventManager, EventBus eventBus){
        this.doctorPigEventManager = doctorPigEventManager;
        this.eventBus = eventBus;
    }

    @Override
    public Response<Long> rollBackPigEvent(Long pigEventId,Integer revertPigType,Long staffId, String staffName) {
        try{
            return Response.ok(doctorPigEventManager.rollBackPigEvent(pigEventId,revertPigType,staffId,staffName));
        }catch (Exception e){
            log.error("pig roll back fail, eventId:{}, cause:{}",pigEventId, Throwables.getStackTraceAsString(e));
            return Response.fail("pig.rollBack.fail");
        }
    }

    @Override
    public Response<Long> pigEntryEvent(DoctorBasicInputInfoDto doctorBasicInputInfoDto, DoctorFarmEntryDto doctorFarmEntryDto) {
        try{
            Map<String,Object> extra = Maps.newHashMap();
            BeanMapper.copy(doctorFarmEntryDto, extra);
            Map<String,Object> result = Maps.newHashMap();
            if(Objects.equals(doctorBasicInputInfoDto.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())){
                result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, extra);
            }else {
                result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, extra);
            }
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"eventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("pig entry event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.entryEvent.fail");
        }
    }

    @Override
    public Response<Long> diseaseEvent(DoctorDiseaseDto doctorDiseaseDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorDiseaseDto, dto);

            Map<String, Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"eventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("disease event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.diseaseEvent.fail");
        }
    }

    @Override
    public Response<Long> vaccinationEvent(DoctorVaccinationDto doctorVaccinationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorVaccinationDto, dto);

            Map<String,Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"eventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Long> conditionEvent(DoctorConditionDto doctorConditionDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorBasicInputInfoDto, dto);

            Map<String,Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"eventId",a->Long.valueOf(a.toString())));
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
            return Response.ok(Params.getWithConvert(result,"eventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Long> chgFarmEvent(DoctorChgFarmDto doctorChgFarmDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorChgFarmDto, dto);

            Map<String,Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"eventId",a->Long.valueOf(a.toString())));
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
            return Response.ok(Params.getWithConvert(result,"eventId",a->Long.valueOf(a.toString())));
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
            return Response.ok(Params.getWithConvert(result,"eventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Long> sowMatingEvent(DoctorMatingDto doctorMatingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorMatingDto, dto);

            Map<String,Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"eventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Long> sowPregCheckEvent(DoctorPregChkResultDto doctorPregChkResultDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorPregChkResultDto, dto);

            Map<String,Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"eventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Long> sowFarrowingEvent(DoctorFarrowingDto doctorFarrowingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorFarrowingDto, dto);

            Map<String,Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"eventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Long> sowPigletsChgEvent(DoctorPigletsChgDto doctorPigletsChgDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorPigletsChgDto, dto);

            Map<String,Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"eventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Long> sowFostersEvent(DoctorFostersDto doctorFostersDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorFostersDto, dto);

            Map<String,Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"eventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Long> sowLitterWeightEvent(DoctorLitterWeightDto doctorLitterWeightDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorLitterWeightDto, dto);

            Map<String,Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"eventId",a->Long.valueOf(a.toString())));
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
            return Response.ok(Params.getWithConvert(result,"eventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Long> sowWeanEvent(DoctorWeanDto doctorWeanDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        try{
            Map<String,Object> dto = Maps.newHashMap();
            BeanMapper.copy(doctorWeanDto, dto);

            Map<String,Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
            publishEvent(result);
            return Response.ok(Params.getWithConvert(result,"eventId",a->Long.valueOf(a.toString())));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    /**
     * 推送对应的事件信息
     * @param results
     */
    private void publishEvent (Map<String,Object> results){
        if(publisher == null){
            eventBus.post(new PigEventCreateEvent(results));
        }else{
            try {
                publisher.publish(DataEvent.toBytes(DataEventType.PigEventCreate.getKey(), new PigEventCreateEvent(results)));
            }catch (Exception e){
                log.error("failed to publish event, cause:{}", e);
            }
        }
    }
}
