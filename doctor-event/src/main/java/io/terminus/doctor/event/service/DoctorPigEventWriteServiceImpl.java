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

    public DoctorPigEventWriteServiceImpl(DoctorPigEventManager doctorPigEventManager){
        this.doctorPigEventManager = doctorPigEventManager;
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
    public Response<Boolean> vaccinationEvent(DoctorVaccinationDto doctorVaccinationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        return null;
    }

    @Override
    public Response<Boolean> conditionEvent(DoctorConditionDto doctorConditionDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        return null;
    }

    @Override
    public Response<Boolean> chgLocationEvent(DoctorChgLocationDto doctorChgLocationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        return null;
    }

    @Override
    public Response<Boolean> chgFarmEvent(DoctorChgFarmDto doctorChgFarmDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        return null;
    }

    @Override
    public Response<Boolean> removalEvent(DoctorRemovalDto doctorRemovalDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        return null;
    }

    @Override
    public Response<Boolean> boarSemenEvent(DoctorSemenDto doctorSemenDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        return null;
    }

    @Override
    public Response<Boolean> sowMatingEvent(DoctorMatingDto doctorMatingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        return null;
    }

    @Override
    public Response<Boolean> sowPregCheckEvent(DoctorPregChkResultDto doctorPregChkResultDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        return null;
    }

    @Override
    public Response<Boolean> sowFarrowingEvent(DoctorFarrowingDto doctorFarrowingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        return null;
    }

    @Override
    public Response<Boolean> sowPigletsChgEvent(DoctorPigletsChgDto doctorPigletsChgDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        return null;
    }

    @Override
    public Response<Boolean> sowFostersEvent(DoctorFostersDto doctorFostersDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        return null;
    }

    @Override
    public Response<Boolean> sowLitterWeightEvent(DoctorLitterWeightDto doctorLitterWeightDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        return null;
    }

    @Override
    public Response<Boolean> sowPartWeanEvent(DoctorPartWeanDto doctorPartWeanDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        return null;
    }

    @Override
    public Response<Boolean> sowWeanEvent(DoctorWeanDto doctorWeanDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
        return null;
    }
}
