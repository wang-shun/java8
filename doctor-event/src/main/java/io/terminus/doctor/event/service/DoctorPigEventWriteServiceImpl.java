package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
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
import io.terminus.doctor.event.manager.DoctorPigEventManager;
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

    @Autowired
    public DoctorPigEventWriteServiceImpl(DoctorPigEventManager doctorPigEventManager){
        this.doctorPigEventManager = doctorPigEventManager;
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
            return Response.ok(doctorPigEventManager.pigEntryEvent(doctorBasicInputInfoDto, doctorFarmEntryDto,pigType));
        }catch (Exception e){
            log.error("pig entry event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.entryEvent.fail");
        }
    }

    @Override
    public Response<Boolean> diseaseEvent(DoctorDiseaseDto doctorDiseaseDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto,Integer pigType) {
        try{
            return Response.ok(doctorPigEventManager.createDiseaseEvent(doctorDiseaseDto,doctorBasicInputInfoDto,pigType));
        }catch (Exception e){
            log.error("disease event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.diseaseEvent.fail");
        }
    }

    @Override
    public Response<Boolean> vaccinationEvent(DoctorVaccinationDto doctorVaccinationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            return Response.ok(doctorPigEventManager.createVaccinationEvent(doctorVaccinationDto,doctorBasicInputInfoDto, pigType));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> conditionEvent(DoctorConditionDto doctorConditionDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            return Response.ok(doctorPigEventManager.createConditionEvent(doctorConditionDto,doctorBasicInputInfoDto,pigType));
        }catch (Exception e){
            log.error("condition event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.condition.fail");
        }
    }

    @Override
    public Response<Boolean> chgLocationEvent(DoctorChgLocationDto doctorChgLocationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            return Response.ok(doctorPigEventManager.createChgLocationEvent(doctorChgLocationDto, doctorBasicInputInfoDto, pigType));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> chgFarmEvent(DoctorChgFarmDto doctorChgFarmDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            return Response.ok(doctorPigEventManager.createChgFarmLocationEvent(doctorChgFarmDto, doctorBasicInputInfoDto, pigType));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> removalEvent(DoctorRemovalDto doctorRemovalDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            return Response.ok(doctorPigEventManager.createRemovalEvent(doctorRemovalDto, doctorBasicInputInfoDto, pigType));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> boarSemenEvent(DoctorSemenDto doctorSemenDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            return Response.ok(doctorPigEventManager.createBoarSemenEvent(doctorSemenDto, doctorBasicInputInfoDto, pigType));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> sowMatingEvent(DoctorMatingDto doctorMatingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{

            return Response.ok(doctorPigEventManager.createPigEventOnlyExtra(doctorMatingDto, doctorBasicInputInfoDto, pigType));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> sowPregCheckEvent(DoctorPregChkResultDto doctorPregChkResultDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{

            return Response.ok(doctorPigEventManager.createPigEventOnlyExtra(doctorPregChkResultDto, doctorBasicInputInfoDto, pigType));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> sowFarrowingEvent(DoctorFarrowingDto doctorFarrowingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            return Response.ok(doctorPigEventManager.createPigEventOnlyExtra(doctorFarrowingDto, doctorBasicInputInfoDto, pigType));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> sowPigletsChgEvent(DoctorPigletsChgDto doctorPigletsChgDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            return Response.ok(doctorPigEventManager.createPigEventOnlyExtra(doctorPigletsChgDto, doctorBasicInputInfoDto, pigType));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> sowFostersEvent(DoctorFostersDto doctorFostersDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            return Response.ok(doctorPigEventManager.createPigEventOnlyExtra(doctorFostersDto, doctorBasicInputInfoDto, pigType));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> sowLitterWeightEvent(DoctorLitterWeightDto doctorLitterWeightDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{

            return Response.ok(doctorPigEventManager.createPigEventOnlyExtra(doctorLitterWeightDto, doctorBasicInputInfoDto, pigType));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> sowPartWeanEvent(DoctorPartWeanDto doctorPartWeanDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{
            return Response.ok(doctorPigEventManager.createPigEventOnlyExtra(doctorPartWeanDto, doctorBasicInputInfoDto, pigType));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }

    @Override
    public Response<Boolean> sowWeanEvent(DoctorWeanDto doctorWeanDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType) {
        try{

            return Response.ok(doctorPigEventManager.createPigEventOnlyExtra(doctorWeanDto, doctorBasicInputInfoDto, pigType));
        }catch (Exception e){
            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.vaccination.fail");
        }
    }
}
