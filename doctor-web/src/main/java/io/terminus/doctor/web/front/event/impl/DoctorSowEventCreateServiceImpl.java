package io.terminus.doctor.web.front.event.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.service.DoctorBasicWriteService;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPartWeanDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.service.DoctorPigEventWriteService;
import io.terminus.doctor.web.front.event.service.DoctorSowEventCreateService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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

    private final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    private final DoctorPigEventWriteService doctorPigEventWriteService;
    private final DoctorBasicWriteService doctorBasicWriteService;

    @Autowired
    public DoctorSowEventCreateServiceImpl(DoctorPigEventWriteService doctorPigEventWriteService,
                                           DoctorBasicWriteService doctorBasicWriteService){
        this.doctorPigEventWriteService = doctorPigEventWriteService;
        this.doctorBasicWriteService = doctorBasicWriteService;
    }

    @Override
    public Response<Long> sowEventCreate(DoctorBasicInputInfoDto doctorBasicInputInfoDto, String sowInfoDtoJson) {
        try {

            PigEvent pigEvent = PigEvent.from(doctorBasicInputInfoDto.getEventType());

            switch (pigEvent) {
                case MATING:
                    return doctorPigEventWriteService.sowMatingEvent(JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorMatingDto.class), doctorBasicInputInfoDto);
                case TO_PREG:
                    return doctorPigEventWriteService.chgSowLocationEvent(JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorChgLocationDto.class), doctorBasicInputInfoDto);
                case PREG_CHECK:
                    return doctorPigEventWriteService.sowPregCheckEvent(JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorPregChkResultDto.class), doctorBasicInputInfoDto);
                case TO_MATING:
                    return doctorPigEventWriteService.chgSowLocationEvent(JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorChgLocationDto.class), doctorBasicInputInfoDto);
                case TO_FARROWING:
                    return doctorPigEventWriteService.chgSowLocationEvent(JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorChgLocationDto.class), doctorBasicInputInfoDto);
                case FARROWING:
                    return doctorPigEventWriteService.sowFarrowingEvent(JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorFarrowingDto.class), doctorBasicInputInfoDto);
                case WEAN:
                    return doctorPigEventWriteService.sowPartWeanEvent(JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorPartWeanDto.class), doctorBasicInputInfoDto);
                case PIGLETS_CHG:
                    DoctorPigletsChgDto pigletsChg = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(sowInfoDtoJson, DoctorPigletsChgDto.class);

                    //新录入的客户要创建一把
                    Long customerId = RespHelper.orServEx(doctorBasicWriteService.addCustomerWhenInput(doctorBasicInputInfoDto.getFarmId(),
                            doctorBasicInputInfoDto.getFarmName(), pigletsChg.getPigletsCustomerId(), pigletsChg.getPigletsCustomerName(),
                            UserUtil.getUserId(), UserUtil.getCurrentUser().getName()));
                    pigletsChg.setPigletsCustomerId(customerId);
                    return doctorPigEventWriteService.sowPigletsChgEvent(pigletsChg, doctorBasicInputInfoDto);
                default:
                    return Response.fail("create.sowEvent.fail");
            }
        }catch (ServiceException e) {
            return Response.fail(e.getMessage());
        }catch (IllegalStateException e){
            log.error("sow event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        }catch (Exception e){
            log.error("sow event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.sowEvent.fail");
        }
    }

    @Override
    public Response<Boolean> sowEventsCreate(List<DoctorBasicInputInfoDto> dtoList, String sowInfoDtoJson) {
        Map<String, Object> extra;
        try{
            extra = OBJECT_MAPPER.readValue(sowInfoDtoJson, JacksonType.MAP_OF_OBJECT);
        }catch (Exception e){
            log.error("create sow events list fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.sowEvents.fail");
        }
        return doctorPigEventWriteService.sowPigsEventCreate(dtoList, extra);
    }

    @Override
    public Response<Boolean> casualEventsCreate(List<DoctorBasicInputInfoDto> dtoList, String sowInfoDtoJson) {
        try{
            Map<String,Object> extra = OBJECT_MAPPER.readValue(sowInfoDtoJson, JacksonType.MAP_OF_OBJECT);
            return doctorPigEventWriteService.casualPigsEventCreate(dtoList, extra);
        }catch (Exception e){
            log.error("casual event create fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.casualEvent.fail");
        }
    }
}
