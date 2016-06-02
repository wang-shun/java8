package io.terminus.doctor.web.front.event.impl;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
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

import java.util.Map;

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
    public Response<Long> sowEventCreate(DoctorBasicInputInfoDto doctorBasicInputInfoDto, Map<String, Object> params) {
        try{

            PigEvent pigEvent = PigEvent.from(doctorBasicInputInfoDto.getEventType());

            switch (pigEvent){
                case MATING:
                    return doctorPigEventWriteService.sowMatingEvent(BeanMapper.map(params, DoctorMatingDto.class), doctorBasicInputInfoDto);
                case TO_PREG:
                    return doctorPigEventWriteService.chgLocationEvent(BeanMapper.map(params, DoctorChgLocationDto.class), doctorBasicInputInfoDto);
                case PREG_CHECK:
                    return doctorPigEventWriteService.sowPregCheckEvent(BeanMapper.map(params, DoctorPregChkResultDto.class), doctorBasicInputInfoDto);
                case TO_MATING:
                    return doctorPigEventWriteService.chgLocationEvent(BeanMapper.map(params, DoctorChgLocationDto.class), doctorBasicInputInfoDto);
                case ABORTION:
                    return doctorPigEventWriteService.abortionEvent(BeanMapper.map(params, DoctorAbortionDto.class), doctorBasicInputInfoDto);
                case TO_FARROWING:
                    return doctorPigEventWriteService.chgLocationEvent(BeanMapper.map(params, DoctorChgLocationDto.class), doctorBasicInputInfoDto);
                case FARROWING:
                    return doctorPigEventWriteService.sowFarrowingEvent(BeanMapper.map(params, DoctorFarrowingDto.class), doctorBasicInputInfoDto);
                case WEAN:
                    return doctorPigEventWriteService.sowPartWeanEvent(BeanMapper.map(params, DoctorPartWeanDto.class), doctorBasicInputInfoDto);
                case FOSTERS:
                    return doctorPigEventWriteService.sowFostersEvent(BeanMapper.map(params, DoctorFostersDto.class), doctorBasicInputInfoDto);
                case PIGLETS_CHG:
                    return doctorPigEventWriteService.sowPigletsChgEvent(BeanMapper.map(params, DoctorPigletsChgDto.class), doctorBasicInputInfoDto);
                default:
                    return Response.fail("create.sowEvent.fail");
            }
        }catch (Exception e){
            log.error("sow event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.sowEvent.fail");
        }
    }
}
