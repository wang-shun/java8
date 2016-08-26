package io.terminus.doctor.web.front.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.enums.PigType;
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
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
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
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static io.terminus.doctor.common.enums.PigType.DELIVER_SOW;
import static io.terminus.doctor.common.enums.PigType.FARROW_PIGLET;
import static io.terminus.doctor.common.enums.PigType.FARROW_TYPES;
import static io.terminus.doctor.common.enums.PigType.MATE_SOW;
import static io.terminus.doctor.common.enums.PigType.MATING_TYPES;
import static io.terminus.doctor.common.enums.PigType.PREG_SOW;
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
@SuppressWarnings("all")
public class DoctorPigCreateEvents {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    //状态转舍允许类型
    private static final List<Integer> CHG_SOW_ALLOWS = Lists.newArrayList(DELIVER_SOW.getValue(), FARROW_PIGLET.getValue(), MATE_SOW.getValue());

    private final DoctorPigEventWriteService doctorPigEventWriteService;
    private final DoctorFarmReadService doctorFarmReadService;
    private final DoctorPigReadService doctorPigReadService;
    private final UserReadService userReadService;
    private final DoctorSowEventCreateService doctorSowEventCreateService;
    private final DoctorBarnReadService doctorBarnReadService;
    private final DoctorPigEventReadService doctorPigEventReadService;

    @Autowired
    public DoctorPigCreateEvents(DoctorPigEventWriteService doctorPigEventWriteService,
                                 DoctorFarmReadService doctorFarmReadService,
                                 DoctorPigReadService doctorPigReadService,
                                 UserReadService userReadService,
                                 DoctorSowEventCreateService doctorSowEventCreateService,
                                 DoctorBarnReadService doctorBarnReadService,
                                 DoctorPigEventReadService doctorPigEventReadService){
        this.doctorPigEventWriteService = doctorPigEventWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorPigReadService = doctorPigReadService;
        this.userReadService =userReadService;
        this.doctorSowEventCreateService = doctorSowEventCreateService;
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorPigEventReadService = doctorPigEventReadService;
    }

    /**
     * 创建转舍事件
     * @param pigId
     * @param farmId
     * @param doctorChgLocationDtoJson
     * @return
     */
    @RequestMapping(value = "/createChgLocation", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createChangeLocationEvent(@RequestParam("pigId") Long pigId,
                                          @RequestParam("farmId") Long farmId,
                                          @RequestParam("doctorChgLocationDtoJson") String doctorChgLocationDtoJson){
        DoctorChgLocationDto doctorChgLocationDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(
                doctorChgLocationDtoJson, DoctorChgLocationDto.class);

        if (isNull(doctorChgLocationDto)){
            throw new JsonResponseException("chgLocation.inputParam.error");
        }

        return createCasualChangeLocationInfo(doctorChgLocationDto, buildBasicInputInfoDto(farmId, pigId, PigEvent.CHG_LOCATION));
    }

    /**
     * 创建批量转舍事件
     * @param pigIds
     * @param farmId
     * @param doctorChgLocationDtoJson
     * @return
     */
    @RequestMapping(value = "/createChgLocations", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean createChangeLocationEvent(@RequestParam("pigIds") String pigIds,
                                          @RequestParam("farmId") Long farmId,
                                          @RequestParam("doctorChgLocationDtoJson") String doctorChgLocationDtoJson){
        DoctorChgLocationDto doctorChgLocationDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(
                doctorChgLocationDtoJson, DoctorChgLocationDto.class);

        if (isNull(doctorChgLocationDto)){
            throw new JsonResponseException("chgLocation.inputParam.error");
        }

        checkPigIds(pigIds);

        Splitters.COMMA.splitToList(pigIds).forEach(pigId -> {
            createCasualChangeLocationInfo(doctorChgLocationDto, buildBasicInputInfoDto(farmId, Long.valueOf(pigId), PigEvent.CHG_LOCATION));
        });

        return Boolean.TRUE;

    }

    /**
     * 创建母猪猪场变动
     * @param doctorChgFarmDtoJson
     * @param pigId
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createChgFarm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createChangeFarmEvent(@RequestParam("doctorChgFarmDtoJson") String doctorChgFarmDtoJson,
                                      @RequestParam("pigId") Long pigId, @RequestParam("farmId") Long farmId){


        DoctorChgFarmDto doctorChgFarmDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorChgFarmDtoJson, DoctorChgFarmDto.class);
        if (isNull(doctorChgFarmDto))
            throw new JsonResponseException("create.chgFarm.error");
        return RespHelper.or500(doctorPigEventWriteService.chgFarmEvent(doctorChgFarmDto, buildBasicInputInfoDto(farmId, pigId, PigEvent.CHG_FARM)));
    }

    /**
     * 批量操作转场事件
     * @param doctorChgFarmDtoJson
     * @param pigIds
     * @param farmId
     */
    @RequestMapping(value = "/createChgFarms", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean createChangeFarmEvents(@RequestParam("doctorChgFarmDtoJson") String doctorChgFarmDtoJson,
                                      @RequestParam("pigIds") String pigIds, @RequestParam("farmId") Long farmId){

        DoctorChgFarmDto doctorChgFarmDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorChgFarmDtoJson, DoctorChgFarmDto.class);
        if (isNull(doctorChgFarmDto))
            throw new JsonResponseException("create.chgFarm.error");

        //检查猪ids是否合格
        checkPigIds(pigIds);

        Splitters.COMMA.splitToList(pigIds).forEach(pigId -> {
            RespHelper.or500(doctorPigEventWriteService.chgFarmEvent(doctorChgFarmDto, buildBasicInputInfoDto(farmId, Long.valueOf(pigId), PigEvent.CHG_FARM)));
        });
        return Boolean.TRUE;
    }

    private void checkPigIds(String pigIds){
        if(Strings.isNullOrEmpty(pigIds)){
            throw new JsonResponseException("pigid.is.null");
        }
    }

    /**
     * 母猪离场事件
     * @param doctorRemovalDtoJson
     * @param pigId
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createRemovalEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createRemovalEvent(@RequestParam("doctorRemovalDtoJson") String doctorRemovalDtoJson,
                                   @RequestParam("pigId") Long pigId, @RequestParam("farmId") Long farmId){

        DoctorRemovalDto doctorRemovalDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorRemovalDtoJson, DoctorRemovalDto.class);
        if(isNull(doctorRemovalDto))
            throw new JsonResponseException("create.removalEvent.fail");

        return RespHelper.or500(doctorPigEventWriteService.removalEvent(doctorRemovalDto, buildBasicInputInfoDto(farmId,pigId, PigEvent.REMOVAL)));
    }

    /**
     * 母猪批量离场事件
     * @param doctorRemovalDtoJson
     * @param pigIds
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createRemovalEvents", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean createRemovalEvent(@RequestParam("doctorRemovalDtoJson") String doctorRemovalDtoJson,
                                   @RequestParam("pigIds") String pigIds, @RequestParam("farmId") Long farmId){

        DoctorRemovalDto doctorRemovalDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorRemovalDtoJson, DoctorRemovalDto.class);
        if(isNull(doctorRemovalDto))
            throw new JsonResponseException("create.removalEvent.fail");
        //检查猪ids是否合格
        checkPigIds(pigIds);

        RespHelper.orServEx(doctorPigEventReadService.validatePigNotInFeed(pigIds));

        Splitters.COMMA.splitToList(pigIds).forEach(pigId -> {
            RespHelper.or500(doctorPigEventWriteService.removalEvent(doctorRemovalDto, buildBasicInputInfoDto(farmId, Long.valueOf(pigId), PigEvent.REMOVAL)));
        });
        return Boolean.TRUE;
    }

    /**
     * 创建疾病事件
     * @param doctorDiseaseDtoJson
     * @param pigId
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createDiseaseEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createDiseaseEvent(@RequestParam("doctorDiseaseDtoJson") String doctorDiseaseDtoJson,
                                   @RequestParam("pigId") Long pigId, @RequestParam("farmId") Long farmId){

        DoctorDiseaseDto doctorDiseaseDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorDiseaseDtoJson, DoctorDiseaseDto.class);
        if(isNull(doctorDiseaseDto))
            throw new JsonResponseException("create.diseaseEvent.fail");
        return RespHelper.or500(doctorPigEventWriteService.diseaseEvent(doctorDiseaseDto, buildBasicInputInfoDto(farmId, pigId, PigEvent.DISEASE)));
    }


    /**
     * 创建批量疾病事件
     * @param doctorDiseaseDtoJson
     * @param pigIds
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createDiseaseEvents", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean createDiseaseEvent(@RequestParam("doctorDiseaseDtoJson") String doctorDiseaseDtoJson,
                                   @RequestParam("pigIds") String pigIds, @RequestParam("farmId") Long farmId){

        DoctorDiseaseDto doctorDiseaseDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorDiseaseDtoJson, DoctorDiseaseDto.class);
        if(isNull(doctorDiseaseDto))
            throw new JsonResponseException("create.diseaseEvent.fail");
        //检查猪ids是否合格
        checkPigIds(pigIds);

        Splitters.COMMA.splitToList(pigIds).forEach(pigId -> {
            RespHelper.or500(doctorPigEventWriteService.diseaseEvent(doctorDiseaseDto, buildBasicInputInfoDto(farmId, Long.valueOf(pigId), PigEvent.DISEASE)));
        });
        return Boolean.TRUE;
    }

    /**
     * 创建免疫事件
     * @param doctorVaccinationDtoJson
     * @param pigId
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createVaccinationEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createVaccinationEvent(@RequestParam("doctorVaccinationDtoJson") String doctorVaccinationDtoJson,
                                       @RequestParam("pigId") Long pigId, @RequestParam("farmId") Long farmId){
        DoctorVaccinationDto doctorVaccinationDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorVaccinationDtoJson, DoctorVaccinationDto.class);
        if(isNull(doctorVaccinationDto))
            throw new JsonResponseException("create.diseaseEvent.fail");
        return RespHelper.or500(doctorPigEventWriteService.vaccinationEvent(doctorVaccinationDto, buildBasicInputInfoDto(farmId, pigId, PigEvent.VACCINATION)));
    }

    /**
     * 创建批量免疫事件
     * @param doctorVaccinationDtoJson
     * @param pigIds
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createVaccinationEvents", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean createVaccinationEvent(@RequestParam("doctorVaccinationDtoJson") String doctorVaccinationDtoJson,
                                       @RequestParam("pigIds") String pigIds, @RequestParam("farmId") Long farmId){
        DoctorVaccinationDto doctorVaccinationDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorVaccinationDtoJson, DoctorVaccinationDto.class);
        if(isNull(doctorVaccinationDto))
            throw new JsonResponseException("create.diseaseEvent.fail");
        //检查猪ids是否合格
        checkPigIds(pigIds);

        Splitters.COMMA.splitToList(pigIds).forEach(pigId -> {
            RespHelper.or500(doctorPigEventWriteService.vaccinationEvent(doctorVaccinationDto, buildBasicInputInfoDto(farmId, Long.valueOf(pigId), PigEvent.VACCINATION)));        });
        return Boolean.TRUE;
    }

    /**
     * 创建体况事件
     * @param doctorConditionDtoJson
     * @param pigId
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createConditionEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createConditionEvent(@RequestParam("doctorConditionDtoJson") String doctorConditionDtoJson,
                                     @RequestParam("pigId") Long pigId, @RequestParam("farmId") Long farmId){
        DoctorConditionDto doctorConditionDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorConditionDtoJson, DoctorConditionDto.class);
        if(isNull(doctorConditionDto))
            throw new JsonResponseException("create.conditionEvent.fail");
        return RespHelper.or500(doctorPigEventWriteService.conditionEvent(doctorConditionDto, buildBasicInputInfoDto(farmId, pigId, PigEvent.CONDITION)));
    }

    /**
     * 创建批量体况事件
     * @param doctorConditionDtoJson
     * @param pigIds
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createConditionEvents", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean createConditionEvent(@RequestParam("doctorConditionDtoJson") String doctorConditionDtoJson,
                                     @RequestParam("pigIds") String pigIds, @RequestParam("farmId") Long farmId){
        DoctorConditionDto doctorConditionDto = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(doctorConditionDtoJson, DoctorConditionDto.class);
        if(isNull(doctorConditionDto))
            throw new JsonResponseException("create.conditionEvent.fail");
        //检查猪ids是否合格
        checkPigIds(pigIds);

        Splitters.COMMA.splitToList(pigIds).forEach(pigId -> {
            RespHelper.or500(doctorPigEventWriteService.conditionEvent(doctorConditionDto, buildBasicInputInfoDto(farmId, Long.valueOf(pigId), PigEvent.CONDITION)));
        });
        return Boolean.TRUE;
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
            RespHelper.or500(doctorSowEventCreateService.sowEventsCreate(basics, sowInfoDtoJson));
            // 猪批量事件操作， 返回PigId
            return pigId;
        } else {
            return RespHelper.or500(doctorSowEventCreateService.sowEventCreate(buildBasicInputInfoDto(farmId, pigId, PigEvent.from(eventType)), sowInfoDtoJson));
        }
    }


    @RequestMapping(value = "/createSowEvents", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean createSowEventInfo(@RequestParam("farmId") Long farmId,
                                   @RequestParam("pigIds") String pigIds, @RequestParam("eventType") Integer eventType,
                                   @RequestParam("sowInfoDtoJson") String sowInfoDtoJson) {
        checkPigIds(pigIds);
        Splitters.COMMA.splitToList(pigIds).forEach(pigId -> {
            RespHelper.or500(doctorSowEventCreateService.sowEventCreate(buildBasicInputInfoDto(farmId, Long.valueOf(pigId), PigEvent.from(eventType)), sowInfoDtoJson));
        });
        return Boolean.TRUE;
    }

    @RequestMapping(value = "/createCasualEvents", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean createCasualEvents(@RequestParam("farmId") Long farmId,
                                      @RequestParam("pigIds") String pigIds,
                                      @RequestParam("pigEvent") Integer pigEvent,
                                      @RequestParam("doctorRemovalDtoJson") String doctorCasualDtoJson){
        return RespHelper.or500(doctorSowEventCreateService.casualEventsCreate(
                buildBasicInputPigDtoContent(farmId, pigIds, PigEvent.from(pigEvent)),
                doctorCasualDtoJson
        ));
    }

    // 猪舍变动 信息
    private Long createCasualChangeLocationInfo(DoctorChgLocationDto chg, DoctorBasicInputInfoDto basic){
        // init pig bran from info
        DoctorPigTrack doctorPigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(basic.getPigId()));
        chg.setChgLocationFromBarnId(doctorPigTrack.getCurrentBarnId());
        chg.setChgLocationFromBarnName(doctorPigTrack.getCurrentBarnName());

        DoctorBarn fromBarn = RespHelper.or500(doctorBarnReadService.findBarnById(chg.getChgLocationFromBarnId()));
        DoctorBarn toBarn = RespHelper.or500(doctorBarnReadService.findBarnById(chg.getChgLocationToBarnId()));

        //普通转舍事件
        if (Objects.equals(fromBarn, toBarn) || (MATING_TYPES.contains(fromBarn) && MATING_TYPES.contains(toBarn))) {
            basic.setEventType(PigEvent.CHG_LOCATION.getKey());
            return RespHelper.or500(doctorPigEventWriteService.chgLocationEvent(chg, basic));
        }
        //状态转换转舍事件
        return createSowChgLocation(chg, basic, fromBarn, toBarn);
    }

    //调用状态转换事件: 母猪转舍
    private Long createSowChgLocation(DoctorChgLocationDto chg, DoctorBasicInputInfoDto basic, DoctorBarn fromBarn, DoctorBarn toBarn) {
        if (!CHG_SOW_ALLOWS.contains(toBarn.getPigType()) || !(fromBarn.getPigType() == PREG_SOW.getValue() && FARROW_TYPES.contains(toBarn.getPigType()))) {
            throw new JsonResponseException(500, "input.sowToBarnId.error");
        }

        //判断去配种还是去分娩
        basic.setEventType(Objects.equals(toBarn.getPigType(), PigType.MATE_SOW.getValue()) ?  PigEvent.TO_MATING.getKey() : PigEvent.TO_FARROWING.getKey());
        return RespHelper.or500(doctorPigEventWriteService.chgSowLocationEvent(chg, basic));
    }

    private List<DoctorBasicInputInfoDto> buildBasicInputPigDtoContent(Long farmId, String pigIds, PigEvent pigEvent){
        try{
            List<Long> pigIdsList = OBJECT_MAPPER.readValue(pigIds, JacksonType.LIST_OF_LONG);
            return pigIdsList.stream().map(id -> buildBasicInputInfoDto(farmId, id, pigEvent)).collect(Collectors.toList());
        }catch (Exception e){
            log.error("build basic input pig dto fail, farmId:{}, pigIds:{}, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("pigIds.basicBuilder.fail");
        }
    }

    /**
     * 拼窝事件， 获取被拼窝母猪信息
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
            throw new JsonResponseException("foster.builder.error");
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
