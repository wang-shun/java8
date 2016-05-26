package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.DataEvent;
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
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public Response<Boolean> rollBackPigEvent(Long pigEventId) {
        try{
            return Response.ok(doctorPigEventManager.rollBackPigEvent(pigEventId));
        }catch (Exception e){
            log.error("pig roll back fail, eventId:{}, cause:{}",pigEventId, Throwables.getStackTraceAsString(e));
            return Response.fail("pig.rollBack.fail");
        }
    }

    @Override
    public Response<Boolean> pigEntryEvent(DoctorBasicInputInfoDto doctorBasicInputInfoDto, DoctorFarmEntryDto doctorFarmEntryDto, Integer pigType) {
        try{
            publishEvent(doctorPigEventManager.pigEntryEvent(doctorBasicInputInfoDto, doctorFarmEntryDto, pigType));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("pig entry event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.entryEvent.fail");
        }
    }

    @Override
    public Response<Boolean> diseaseEvent(DoctorDiseaseDto doctorDiseaseDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto,Integer pigType) {
        try{
            publishEvent(doctorPigEventManager.createDiseaseEvent(doctorDiseaseDto,doctorBasicInputInfoDto,pigType));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("disease event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.diseaseEvent.fail");
        }
    }

    @Override
    public Response<Boolean> vaccinationEvent(DoctorVaccinationDto doctorVaccinationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            publishEvent(doctorPigEventManager.createVaccinationEvent(doctorVaccinationDto,doctorBasicInputInfoDto, pigType));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> conditionEvent(DoctorConditionDto doctorConditionDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            publishEvent(doctorPigEventManager.createConditionEvent(doctorConditionDto,doctorBasicInputInfoDto,pigType));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("condition event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.condition.fail");
        }
    }

    @Override
    public Response<Boolean> chgLocationEvent(DoctorChgLocationDto doctorChgLocationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            publishEvent(doctorPigEventManager.createChgLocationEvent(doctorChgLocationDto, doctorBasicInputInfoDto, pigType));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> chgFarmEvent(DoctorChgFarmDto doctorChgFarmDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            publishEvent(doctorPigEventManager.createChgFarmLocationEvent(doctorChgFarmDto, doctorBasicInputInfoDto, pigType));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> removalEvent(DoctorRemovalDto doctorRemovalDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            publishEvent(doctorPigEventManager.createRemovalEvent(doctorRemovalDto, doctorBasicInputInfoDto, pigType));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> boarSemenEvent(DoctorSemenDto doctorSemenDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            publishEvent(doctorPigEventManager.createBoarSemenEvent(doctorSemenDto, doctorBasicInputInfoDto, pigType));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> sowMatingEvent(DoctorMatingDto doctorMatingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{

            publishEvent(doctorPigEventManager.createPigEventOnlyExtra(doctorMatingDto, doctorBasicInputInfoDto, pigType));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> sowPregCheckEvent(DoctorPregChkResultDto doctorPregChkResultDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{

            publishEvent(doctorPigEventManager.createPigEventOnlyExtra(doctorPregChkResultDto, doctorBasicInputInfoDto, pigType));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> sowFarrowingEvent(DoctorFarrowingDto doctorFarrowingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            publishEvent(doctorPigEventManager.createPigEventOnlyExtra(doctorFarrowingDto, doctorBasicInputInfoDto, pigType));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> sowPigletsChgEvent(DoctorPigletsChgDto doctorPigletsChgDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            publishEvent(doctorPigEventManager.createPigEventOnlyExtra(doctorPigletsChgDto, doctorBasicInputInfoDto, pigType));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> sowFostersEvent(DoctorFostersDto doctorFostersDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            publishEvent(doctorPigEventManager.createPigEventOnlyExtra(doctorFostersDto, doctorBasicInputInfoDto, pigType));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> sowLitterWeightEvent(DoctorLitterWeightDto doctorLitterWeightDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{

            publishEvent(doctorPigEventManager.createPigEventOnlyExtra(doctorLitterWeightDto, doctorBasicInputInfoDto, pigType));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> sowPartWeanEvent(DoctorPartWeanDto doctorPartWeanDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            publishEvent(doctorPigEventManager.createPigEventOnlyExtra(doctorPartWeanDto, doctorBasicInputInfoDto, pigType));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> sowWeanEvent(DoctorWeanDto doctorWeanDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{

            publishEvent(doctorPigEventManager.createPigEventOnlyExtra(doctorWeanDto, doctorBasicInputInfoDto, pigType));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    /**
     * 推送对应的事件信息
     * @param eventId
     */
    public void publishEvent(Long eventId){
        if(publisher == null){
            eventBus.post(new PigEventCreateEvent(eventId));
        }else{
            try {
                publisher.publish(DataEvent.toBytes(DataEventType.PigEventCreate.getKey(), new PigEventCreateEvent(eventId)));
            }catch (Exception e){
                log.error("failed to publish event, cause:{}", e);
            }
        }
    }
}
