package io.terminus.doctor.web.front.event.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.sow.DoctorAbortionDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPartWeanDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.service.DoctorPigEventWriteService;
import io.terminus.doctor.web.front.event.service.DoctorSowEventCreateService;
import io.terminus.zookeeper.pubsub.Subscriber;
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

    @Autowired(required = false)
    private Subscriber subscriber;

    @Autowired
    public DoctorSowEventCreateServiceImpl(DoctorPigEventWriteService doctorPigEventWriteService){
        this.doctorPigEventWriteService = doctorPigEventWriteService;

        // TODO 确认消耗领取相关字段信息
//        if(subscriber != null){
//            try{
//                subscriber.subscribe(data->{
//                    DataEvent dataEvent = DataEvent.fromBytes(data);
//                    if(!Objects.equals(dataEvent.getEventType(), DataEventType.VaccinationMedicalConsume.getKey())){
//                        return;
//                    }
//                    sowPigsEventCreateByConsume(dataEvent.getContent());
//                });
//            }catch (Exception e){
//                log.error("subscriber callback fail, cause:{}", Throwables.getStackTraceAsString(e));
//            }
//        }
    }

    @Override
    public Response<Long> sowEventCreate(DoctorBasicInputInfoDto doctorBasicInputInfoDto, String sowInfoDtoJson) {
        try{

            PigEvent pigEvent = PigEvent.from(doctorBasicInputInfoDto.getEventType());

            log.info("*************  sow json info  :{} ", sowInfoDtoJson);

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

    @Override
    public Response<Boolean> sowEventsCreate(List<DoctorBasicInputInfoDto> dtoList, String sowInfoDtoJson) {
        try{
            Map<String, Object> extra = OBJECT_MAPPER.readValue(sowInfoDtoJson, JacksonType.MAP_OF_OBJECT);
            RespHelper.orServEx(doctorPigEventWriteService.sowPigsEventCreate(dtoList, extra));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("create sow events list fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.sowEvents.fail");
        }
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

//    @Override
//    public Response<Boolean> sowPigsEventCreateByConsume(String paramsJson) {
//        try{
//            Map<String,Object> paramsMap = OBJECT_MAPPER.readValue(paramsJson, JacksonType.MAP_OF_OBJECT);
//
//            // basic input info
//            Long barnId = Long.valueOf(paramsMap.get("barnId").toString());
//
//
//            return Response.ok(Boolean.TRUE);
//        }catch (Exception e){
//            log.error("sow pigs event create fail, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("consume.eventCreate.fail");
//        }
//    }
}
