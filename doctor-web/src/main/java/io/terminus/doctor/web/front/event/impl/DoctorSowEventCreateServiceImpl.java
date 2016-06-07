package io.terminus.doctor.web.front.event.impl;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.sow.DoctorAbortionDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFostersDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPartWeanDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.service.DoctorPigEventWriteService;
import io.terminus.doctor.web.front.event.service.DoctorSowEventCreateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by yaoqijun.
 * Date:2016-06-02
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Service
@Slf4j
public class DoctorSowEventCreateServiceImpl implements DoctorSowEventCreateService{

    private final DoctorPigEventWriteService doctorPigEventWriteService;

    @Autowired
    public DoctorSowEventCreateServiceImpl(DoctorPigEventWriteService doctorPigEventWriteService){
        this.doctorPigEventWriteService = doctorPigEventWriteService;
    }

    @Override
    public Response<Long> sowEventCreate(DoctorBasicInputInfoDto doctorBasicInputInfoDto, String sowInfoDtoJson) {
        try{

            PigEvent pigEvent = PigEvent.from(doctorBasicInputInfoDto.getEventType());

            switch (pigEvent){
                case MATING:
                    return doctorPigEventWriteService.sowMatingEvent(JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorMatingDto.class), doctorBasicInputInfoDto);
                case TO_PREG:
                    return doctorPigEventWriteService.chgSowLocationEvent(JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorChgLocationDto.class), doctorBasicInputInfoDto);
                case PREG_CHECK:
                    return doctorPigEventWriteService.sowPregCheckEvent(JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorPregChkResultDto.class), doctorBasicInputInfoDto);
                case TO_MATING:
                    return doctorPigEventWriteService.chgSowLocationEvent(JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorChgLocationDto.class), doctorBasicInputInfoDto);
                case ABORTION:
                    return doctorPigEventWriteService.abortionEvent(JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorAbortionDto.class), doctorBasicInputInfoDto);
                case TO_FARROWING:
                    return doctorPigEventWriteService.chgSowLocationEvent(JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorChgLocationDto.class), doctorBasicInputInfoDto);
                case FARROWING:
                    return doctorPigEventWriteService.sowFarrowingEvent(JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorFarrowingDto.class), doctorBasicInputInfoDto);
                case WEAN:
                    return doctorPigEventWriteService.sowPartWeanEvent(JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorPartWeanDto.class), doctorBasicInputInfoDto);
                case FOSTERS:
                    return doctorPigEventWriteService.sowFostersEvent(JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorFostersDto.class), doctorBasicInputInfoDto);
                case PIGLETS_CHG:
                    return doctorPigEventWriteService.sowPigletsChgEvent(JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorPigletsChgDto.class), doctorBasicInputInfoDto);
                default:
                    return Response.fail("create.sowEvent.fail");
            }
        }catch (Exception e){
            log.error("sow event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.sowEvent.fail");
        }
    }
}
