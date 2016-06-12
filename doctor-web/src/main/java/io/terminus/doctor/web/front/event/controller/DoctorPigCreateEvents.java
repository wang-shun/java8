package io.terminus.doctor.web.front.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Lists;
import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.dto.event.usual.DoctorConditionDto;
import io.terminus.doctor.event.dto.event.usual.DoctorDiseaseDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.dto.event.usual.DoctorRemovalDto;
import io.terminus.doctor.event.dto.event.usual.DoctorVaccinationDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.service.DoctorPigEventWriteService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.web.front.event.service.DoctorSowEventCreateService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-26
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Slf4j
@Controller
@RequestMapping("/api/doctor/events/create")
public class DoctorPigCreateEvents {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    private final DoctorPigEventWriteService doctorPigEventWriteService;

    private final DoctorFarmReadService doctorFarmReadService;

    private final DoctorPigReadService doctorPigReadService;

    private final UserReadService userReadService;

    private final DoctorSowEventCreateService doctorSowEventCreateService;

    @Autowired
    public DoctorPigCreateEvents(DoctorPigEventWriteService doctorPigEventWriteService,
                                 DoctorFarmReadService doctorFarmReadService,
                                 DoctorPigReadService doctorPigReadService,
                                 UserReadService userReadService,
                                 DoctorSowEventCreateService doctorSowEventCreateService){
        this.doctorPigEventWriteService = doctorPigEventWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorPigReadService = doctorPigReadService;
        this.userReadService =userReadService;
        this.doctorSowEventCreateService = doctorSowEventCreateService;
    }

    @RequestMapping(value = "/createChgLocation", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createChangeLocationEvent(@RequestParam("pigId") Long pigId,
                                          @RequestParam("farmId") Long farmId,
                                          @RequestParam("doctorChgLocationDtoJson") String doctorChgLocationDtoJson){
        DoctorChgLocationDto doctorChgLocationDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(
                doctorChgLocationDtoJson, DoctorChgLocationDto.class);

        if (isNull(doctorChgLocationDto)){
            throw new JsonResponseException("create.chgLocation.fail");
        }

        return RespHelper.or500(doctorPigEventWriteService.chgLocationEvent(doctorChgLocationDto,
                buildBasicInputInfoDto(farmId, pigId, PigEvent.CHG_LOCATION)));
    }

    @RequestMapping(value = "/createChgFarm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createChangeFarmEvent(@RequestParam("doctorChgFarmDtoJson") String doctorChgFarmDtoJson,
                                      @RequestParam("pigId") Long pigId, @RequestParam("farmId") Long farmId){

        DoctorChgFarmDto doctorChgFarmDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorChgFarmDtoJson, DoctorChgFarmDto.class);
        if (isNull(doctorChgFarmDto))
            throw new JsonResponseException("create.chgFarm.error");
        return RespHelper.or500(doctorPigEventWriteService.chgFarmEvent(doctorChgFarmDto, buildBasicInputInfoDto(farmId, pigId, PigEvent.CHG_FARM)));
    }

    @RequestMapping(value = "/createRemovalEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createRemovalEvent(@RequestParam("doctorRemovalDtoJson") String doctorRemovalDtoJson,
                                   @RequestParam("pigId") Long pigId, @RequestParam("farmId") Long farmId){

        DoctorRemovalDto doctorRemovalDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorRemovalDtoJson, DoctorRemovalDto.class);
        if(isNull(doctorRemovalDto))
            throw new JsonResponseException("create.removalEvent.fail");

        return RespHelper.or500(doctorPigEventWriteService.removalEvent(doctorRemovalDto, buildBasicInputInfoDto(farmId,pigId, PigEvent.REMOVAL)));
    }

    @RequestMapping(value = "/createDiseaseEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createDiseaseEvent(@RequestParam("doctorDiseaseDtoJson") String doctorDiseaseDtoJson,
                                   @RequestParam("pigId") Long pigId, @RequestParam("farmId") Long farmId){

        DoctorDiseaseDto doctorDiseaseDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorDiseaseDtoJson, DoctorDiseaseDto.class);
        if(isNull(doctorDiseaseDto))
            throw new JsonResponseException("create.diseaseEvent.fail");
        return RespHelper.or500(doctorPigEventWriteService.diseaseEvent(doctorDiseaseDto, buildBasicInputInfoDto(farmId, pigId, PigEvent.DISEASE)));
    }

    @RequestMapping(value = "/createVaccinationEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createVaccinationEvent(@RequestParam("doctorVaccinationDtoJson") String doctorVaccinationDtoJson,
                                       @RequestParam("pigId") Long pigId, @RequestParam("farmId") Long farmId){
        DoctorVaccinationDto doctorVaccinationDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorVaccinationDtoJson, DoctorVaccinationDto.class);
        if(isNull(doctorVaccinationDto))
            throw new JsonResponseException("create.diseaseEvent.fail");
        return RespHelper.or500(doctorPigEventWriteService.vaccinationEvent(doctorVaccinationDto, buildBasicInputInfoDto(farmId, pigId, PigEvent.VACCINATION)));
    }

    @RequestMapping(value = "/createConditionEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createConditionEvent(@RequestParam("doctorConditionDtoJson") String doctorConditionDtoJson,
                                     @RequestParam("pigId") Long pigId, @RequestParam("farmId") Long farmId){
        DoctorConditionDto doctorConditionDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorConditionDtoJson, DoctorConditionDto.class);
        if(isNull(doctorConditionDto))
            throw new JsonResponseException("create.conditionEvent.fail");
        return RespHelper.or500(doctorPigEventWriteService.conditionEvent(doctorConditionDto, buildBasicInputInfoDto(farmId, pigId, PigEvent.CONDITION)));
    }

    @RequestMapping(value = "/createSemen", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createSemenEvent(@RequestParam("farmId") Long farmId,
                                 @RequestParam("pigId") Long pigId,
                                 @RequestParam("doctorSemenDtoJson") String doctorSemenDtoJson){

        DoctorSemenDto doctorSemenDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorSemenDtoJson, DoctorSemenDto.class);
        if(isNull(doctorSemenDto))
            throw new JsonResponseException("create.semenEvent.fail");
        return RespHelper.or500(doctorPigEventWriteService.boarSemenEvent(doctorSemenDto, buildBasicInputInfoDto(farmId, pigId, PigEvent.SEMEN)));
    }

    @RequestMapping(value = "/createEntryInfo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createEntryEvent(@RequestParam("farmId") Long farmId,
                                 @RequestParam("doctorFarmEntryJson") String doctorFarmEntryDtoJson){

        DoctorFarmEntryDto doctorFarmEntryDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorFarmEntryDtoJson, DoctorFarmEntryDto.class);

        if(isNull(doctorFarmEntryDto)){
            throw new JsonResponseException("input.pigEntryJsonConvert.error");
        }
        return RespHelper.or500(doctorPigEventWriteService.pigEntryEvent(
                        buildBasicEntryInputInfo(farmId, doctorFarmEntryDto, PigEvent.ENTRY), doctorFarmEntryDto));
    }

    @RequestMapping(value = "/createSowEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createSowEventInfo(@RequestParam("farmId") Long farmId,
                                   @RequestParam("pigId") Long pigId, @RequestParam("eventType") Integer eventType,
                                   @RequestParam("sowInfoDtoJson") String sowInfoDtoJson) {
        if (Objects.equals(eventType, PigEvent.FOSTERS.getKey())) {
            List<DoctorBasicInputInfoDto> basics = buildBasicInputPigDtoContent(farmId, pigId, sowInfoDtoJson);
            return RespHelper.or500(doctorSowEventCreateService.sowEventsCreate(basics, sowInfoDtoJson));
        } else {
            return RespHelper.or500(doctorSowEventCreateService.sowEventCreate(buildBasicInputInfoDto(farmId, pigId, PigEvent.from(eventType)), sowInfoDtoJson));

        }
    }

    /**
     *
     * @return
     */
    private List<DoctorBasicInputInfoDto> buildBasicInputPigDtoContent(Long farmId, Long pigId, String fosterJson){
        try{
            List<DoctorBasicInputInfoDto> basics = Lists.newArrayList();
            basics.add(buildBasicInputInfoDto(farmId, pigId, PigEvent.FOSTERS));
            Map<String,Object> dtoData = OBJECT_MAPPER.readValue(fosterJson, JacksonType.MAP_OF_OBJECT);
            basics.add(buildBasicInputInfoDto(farmId, Long.valueOf(dtoData.get("fosterSowId").toString()), PigEvent.FOSTERS_BY));
            return basics;
        }catch (Exception e){
            log.error("foster data build error fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("foster builder error");
        }
    }

    /**
     * 构建Basic 基础输入数据信息
     * @param farmId
     * @param entryDto
     * @param pigEvent
     * @return
     */
    private DoctorBasicInputInfoDto buildBasicEntryInputInfo(Long farmId, DoctorFarmEntryDto entryDto, PigEvent pigEvent){
        try{
            DoctorFarm doctorFarm = RespHelper.orServEx(this.doctorFarmReadService.findFarmById(farmId));
            checkState(!isNull(pigEvent), "input.eventType.error");
            Long userId = UserUtil.getUserId();
            Response<User> userResponse = userReadService.findById(userId);
            checkState(userResponse.isSuccess(), "loginUser.check.error");

            return DoctorBasicInputInfoDto.builder()
                    .pigType(entryDto.getPigType()).pigCode(entryDto.getPigCode()).barnId(entryDto.getBarnId()).barnName(entryDto.getBarnName())
                    .farmId(doctorFarm.getId()).farmName(doctorFarm.getName()).orgId(doctorFarm.getOrgId()).orgName(doctorFarm.getOrgName())
                    .staffId(userId).staffName(userResponse.getResult().getName())
                    .eventType(pigEvent.getKey()).eventName(pigEvent.getDesc()).eventDesc(pigEvent.getDesc())
                    .build();
        }catch (IllegalStateException ee){
            log.error("illegal state exception error, cause:{}", Throwables.getStackTraceAsString(ee));
            throw new JsonResponseException(ee.getMessage());
        }catch (Exception e){
            log.error("basic entry info build fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("build.basicEntry.fail");
        }
    }

    /**
     * 通过Id 获取对应的事件信息
     * @param farmId
     * @param pigId
     * @param pigEvent
     * @return
     */
    private DoctorBasicInputInfoDto buildBasicInputInfoDto(Long farmId, Long pigId, PigEvent pigEvent){
        try{
            DoctorFarm doctorFarm = RespHelper.orServEx(this.doctorFarmReadService.findFarmById(farmId));
            DoctorPigInfoDto pigDto = RespHelper.orServEx(this.doctorPigReadService.queryDoctorInfoDtoById(pigId));
            checkState(!isNull(pigEvent), "input.eventType.error");
            Long userId = UserUtil.getUserId();
            Response<User> userResponse = userReadService.findById(userId);

            return DoctorBasicInputInfoDto.builder()
                    .pigId(pigDto.getId()).pigCode(pigDto.getPigCode()).pigType(pigDto.getPigType()).barnId(pigDto.getBarnId()).barnName(pigDto.getBarnName())
                    .farmId(doctorFarm.getId()).farmName(doctorFarm.getName()).orgId(doctorFarm.getOrgId()).orgName(doctorFarm.getOrgName())
                    .staffId(userId).staffName(userResponse.getResult().getName())
                    .eventType(pigEvent.getKey()).eventName(pigEvent.getDesc()).eventDesc(pigEvent.getDesc())
                    .build();
        }catch (Exception e){
            log.error("build basic input info dto fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("build.basicInputInfo.error");
        }

    }
}
