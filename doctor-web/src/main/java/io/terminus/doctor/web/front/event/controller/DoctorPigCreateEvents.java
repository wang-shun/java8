package io.terminus.doctor.web.front.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Maps;
import com.google.api.client.util.Strings;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorBasicWriteService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.boar.DoctorBoarConditionDto;
import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFostersDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
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
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorPigEventWriteService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.web.front.event.dto.DoctorBatchPigEventDto;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static io.terminus.common.utils.JsonMapper.JSON_NON_DEFAULT_MAPPER;
import static io.terminus.doctor.common.enums.PigType.*;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-26
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/events/create")
@SuppressWarnings("all")
public class DoctorPigCreateEvents {

    private static final ObjectMapper OBJECT_MAPPER = JSON_NON_DEFAULT_MAPPER.getMapper();

    //状态转舍允许类型
    private static final List<Integer> CHG_SOW_ALLOWS = Lists.newArrayList(DELIVER_SOW.getValue(), MATE_SOW.getValue(), PREG_SOW.getValue());

    private final DoctorPigEventWriteService doctorPigEventWriteService;
    private final DoctorFarmReadService doctorFarmReadService;
    private final DoctorPigReadService doctorPigReadService;
    private final UserReadService userReadService;
    private final DoctorBarnReadService doctorBarnReadService;
    private final DoctorPigEventReadService doctorPigEventReadService;
    private final DoctorGroupWebService doctorGroupWebService;
    @RpcConsumer
    private DoctorBasicWriteService doctorBasicWriteService;
    @RpcConsumer
    private DoctorBasicReadService doctorBasicReadService;

    private static JsonMapper jsonMapper = JSON_NON_DEFAULT_MAPPER;

    @Autowired
    public DoctorPigCreateEvents(DoctorPigEventWriteService doctorPigEventWriteService,
                                 DoctorFarmReadService doctorFarmReadService,
                                 DoctorPigReadService doctorPigReadService,
                                 UserReadService userReadService,
                                 DoctorBarnReadService doctorBarnReadService,
                                 DoctorPigEventReadService doctorPigEventReadService,
                                 DoctorGroupWebService doctorGroupWebService) {
        this.doctorPigEventWriteService = doctorPigEventWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorPigReadService = doctorPigReadService;
        this.userReadService = userReadService;
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorPigEventReadService = doctorPigEventReadService;
        this.doctorGroupWebService = doctorGroupWebService;
    }

    /**
     * 创建转舍事件
     *
     * @param pigId
     * @param farmId
     * @param doctorChgLocationDtoJson
     * @return
     */
    @RequestMapping(value = "/createChgLocation", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createChangeLocationEvent(@RequestParam("pigId") Long pigId,
                                          @RequestParam("farmId") Long farmId,
                                          @RequestParam("doctorChgLocationDtoJson") String doctorChgLocationDtoJson) {
        DoctorChgLocationDto doctorChgLocationDto = jsonMapper.fromJson(
                doctorChgLocationDtoJson, DoctorChgLocationDto.class);

        if (isNull(doctorChgLocationDto)) {
            throw new JsonResponseException("chgLocation.inputParam.error");
        }

        DoctorPigTrack doctorPigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(pigId));
        doctorChgLocationDto.setChgLocationFromBarnId(doctorPigTrack.getCurrentBarnId());
        doctorChgLocationDto.setChgLocationFromBarnName(doctorPigTrack.getCurrentBarnName());
        return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorChgLocationDto, pigId, PigEvent.CHG_LOCATION), buildBasicInputInfoDto(farmId, PigEvent.CHG_LOCATION)));
    }

    /**
     * 创建批量转舍事件
     *
     * @param pigIds
     * @param farmId
     * @param doctorChgLocationDtoJson
     * @return
     */
    @RequestMapping(value = "/createChgLocations", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createChangeLocationEvent(@RequestParam("pigIds") String pigIds,
                                             @RequestParam("farmId") Long farmId,
                                             @RequestParam("doctorChgLocationDtoJson") String doctorChgLocationDtoJson) {
        DoctorChgLocationDto doctorChgLocationDto = jsonMapper.fromJson(
                doctorChgLocationDtoJson, DoctorChgLocationDto.class);
        if (isNull(doctorChgLocationDto)) {
            throw new JsonResponseException("chgLocation.inputParam.error");
        }

        checkPigIds(pigIds);
        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().map(idStr -> {
            DoctorChgLocationDto chgLocationDto = BeanMapper.map(doctorChgLocationDto, DoctorChgLocationDto.class);
            DoctorPigTrack doctorPigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(Long.parseLong(idStr)));
            chgLocationDto.setChgLocationFromBarnId(doctorPigTrack.getCurrentBarnId());
            chgLocationDto.setChgLocationFromBarnName(doctorPigTrack.getCurrentBarnName());
            return buildEventInput(chgLocationDto, Long.parseLong(idStr), PigEvent.CHG_LOCATION);
        }).collect(Collectors.toList());
        return RespHelper.or500(doctorPigEventWriteService.batchPigEventHandle(inputDtos, buildBasicInputInfoDto(farmId, PigEvent.CHG_LOCATION)));
}

    /**
     * 创建母猪猪场变动
     *
     * @param doctorChgFarmDtoJson
     * @param pigId
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createChgFarm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createChangeFarmEvent(@RequestParam("doctorChgFarmDtoJson") String doctorChgFarmDtoJson,
                                      @RequestParam("pigId") Long pigId, @RequestParam("farmId") Long farmId) {

        DoctorChgFarmDto doctorChgFarmDto = jsonMapper.fromJson(doctorChgFarmDtoJson, DoctorChgFarmDto.class);
        if (isNull(doctorChgFarmDto))
            throw new JsonResponseException("create.chgFarm.error");

        return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorChgFarmDto, pigId, PigEvent.CHG_FARM), buildBasicInputInfoDto(farmId, PigEvent.CHG_FARM)));
    }

    /**
     * 批量操作转场事件
     *
     * @param doctorChgFarmDtoJson
     * @param pigIds
     * @param farmId
     */
    @RequestMapping(value = "/createChgFarms", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createChangeFarmEvents(@RequestParam("doctorChgFarmDtoJson") String doctorChgFarmDtoJson,
                                          @RequestParam("pigIds") String pigIds, @RequestParam("farmId") Long farmId) {
        DoctorChgFarmDto doctorChgFarmDto = jsonMapper.fromJson(doctorChgFarmDtoJson, DoctorChgFarmDto.class);
        if (isNull(doctorChgFarmDto))
            throw new JsonResponseException("create.chgFarm.error");

        //检查猪ids是否合格
        checkPigIds(pigIds);

        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().map(idStr -> {
            DoctorChgFarmDto chgFarmDto = BeanMapper.map(doctorChgFarmDto, DoctorChgFarmDto.class);
            buildEventInput(chgFarmDto, Long.parseLong(idStr), PigEvent.CHG_FARM);
            return chgFarmDto;
        }).collect(Collectors.toList());
        return RespHelper.or500(doctorPigEventWriteService.batchPigEventHandle(inputDtos, buildBasicInputInfoDto(farmId, PigEvent.CHG_FARM)));
    }

    private void checkPigIds(String pigIds) {
        if (Strings.isNullOrEmpty(pigIds)) {
            throw new JsonResponseException("pigid.is.null");
        }
    }

    /**
     * 母猪离场事件
     *
     * @param doctorRemovalDtoJson
     * @param pigId
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createRemovalEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createRemovalEvent(@RequestParam("doctorRemovalDtoJson") String doctorRemovalDtoJson,
                                   @RequestParam("pigId") Long pigId, @RequestParam("farmId") Long farmId) {
        DoctorRemovalDto doctorRemovalDto = jsonMapper.fromJson(doctorRemovalDtoJson, DoctorRemovalDto.class);
        if (isNull(doctorRemovalDto))
            throw new JsonResponseException("create.removalEvent.fail");

        return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorRemovalDto, pigId, PigEvent.REMOVAL), buildBasicInputInfoDto(farmId, PigEvent.REMOVAL)));
    }

    /**
     * 母猪批量离场事件
     *
     * @param doctorRemovalDtoJson
     * @param pigIds
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createRemovalEvents", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createRemovalEvent(@RequestParam("doctorRemovalDtoJson") String doctorRemovalDtoJson,
                                      @RequestParam("pigIds") String pigIds, @RequestParam("farmId") Long farmId) {
        DoctorRemovalDto doctorRemovalDto = jsonMapper.fromJson(doctorRemovalDtoJson, DoctorRemovalDto.class);
        if (isNull(doctorRemovalDto))
            throw new JsonResponseException("create.removalEvent.fail");

        //检查猪ids是否合格
        checkPigIds(pigIds);

        RespHelper.orServEx(doctorPigEventReadService.validatePigNotInFeed(pigIds));
        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().map(idStr -> {
            DoctorRemovalDto removalDto = BeanMapper.map(doctorRemovalDto, DoctorRemovalDto.class);
            buildEventInput(removalDto, Long.parseLong(idStr), PigEvent.REMOVAL);
            return removalDto;
        }).collect(Collectors.toList());
        return RespHelper.or500(doctorPigEventWriteService.batchPigEventHandle(inputDtos, buildBasicInputInfoDto(farmId, PigEvent.REMOVAL)));
    }

    /**
     * 创建疾病事件
     *
     * @param doctorDiseaseDtoJson
     * @param pigId
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createDiseaseEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createDiseaseEvent(@RequestParam("doctorDiseaseDtoJson") String doctorDiseaseDtoJson,
                                   @RequestParam("pigId") Long pigId, @RequestParam("farmId") Long farmId) {

        DoctorDiseaseDto doctorDiseaseDto = jsonMapper.fromJson(doctorDiseaseDtoJson, DoctorDiseaseDto.class);
        if (isNull(doctorDiseaseDto))
            throw new JsonResponseException("create.diseaseEvent.fail");

        return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorDiseaseDto, pigId, PigEvent.DISEASE), buildBasicInputInfoDto(farmId, PigEvent.DISEASE)));
    }


    /**
     * 创建批量疾病事件
     *
     * @param doctorDiseaseDtoJson
     * @param pigIds
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createDiseaseEvents", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createDiseaseEvent(@RequestParam("doctorDiseaseDtoJson") String doctorDiseaseDtoJson,
                                      @RequestParam("pigIds") String pigIds, @RequestParam("farmId") Long farmId) {
        DoctorDiseaseDto doctorDiseaseDto = jsonMapper.fromJson(doctorDiseaseDtoJson, DoctorDiseaseDto.class);
        if (isNull(doctorDiseaseDto))
            throw new JsonResponseException("create.diseaseEvent.fail");

        //检查猪ids是否合格
        checkPigIds(pigIds);
        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().map(idStr -> {
            DoctorDiseaseDto inputDto = BeanMapper.map(doctorDiseaseDto, DoctorDiseaseDto.class);
            buildEventInput(inputDto, Long.parseLong(idStr), PigEvent.DISEASE);
            return inputDto;
        }).collect(Collectors.toList());
        return RespHelper.or500(doctorPigEventWriteService.batchPigEventHandle(inputDtos, buildBasicInputInfoDto(farmId, PigEvent.DISEASE)));
    }

    /**
     * 创建免疫事件
     *
     * @param doctorVaccinationDtoJson
     * @param pigId
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createVaccinationEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createVaccinationEvent(@RequestParam("doctorVaccinationDtoJson") String doctorVaccinationDtoJson,
                                       @RequestParam("pigId") Long pigId, @RequestParam("farmId") Long farmId) {
        DoctorVaccinationDto doctorVaccinationDto = jsonMapper.fromJson(doctorVaccinationDtoJson, DoctorVaccinationDto.class);
        if (isNull(doctorVaccinationDto))
            throw new JsonResponseException("create.diseaseEvent.fail");

        return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorVaccinationDto, pigId, PigEvent.VACCINATION), buildBasicInputInfoDto(farmId, PigEvent.VACCINATION)));
    }

    /**
     * 创建批量免疫事件
     *
     * @param doctorVaccinationDtoJson
     * @param pigIds
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createVaccinationEvents", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createVaccinationEvent(@RequestParam("doctorVaccinationDtoJson") String doctorVaccinationDtoJson,
                                          @RequestParam("pigIds") String pigIds, @RequestParam("farmId") Long farmId) {
        DoctorVaccinationDto doctorVaccinationDto = jsonMapper.fromJson(doctorVaccinationDtoJson, DoctorVaccinationDto.class);


        //检查猪ids是否合格
        checkPigIds(pigIds);

        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().map(idStr -> {
            DoctorVaccinationDto vaccinationDto = BeanMapper.map(doctorVaccinationDto, DoctorVaccinationDto.class);
            buildEventInput(vaccinationDto, Long.parseLong(idStr), PigEvent.VACCINATION);
            return vaccinationDto;
        }).collect(Collectors.toList());
        return RespHelper.or500(doctorPigEventWriteService.batchPigEventHandle(inputDtos, buildBasicInputInfoDto(farmId, PigEvent.VACCINATION)));
    }

    /**
     * 创建体况事件
     *
     * @param doctorConditionDtoJson
     * @param pigId
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createConditionEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createConditionEvent(@RequestParam("doctorConditionDtoJson") String doctorConditionDtoJson,
                                     @RequestParam("pigId") Long pigId, @RequestParam("farmId") Long farmId) {
        DoctorPig doctorPig = RespHelper.or500(doctorPigReadService.findPigById(pigId));
        BasePigEventInputDto inputDto;
        if (Objects.equals(doctorPig.getPigType(), DoctorPig.PigSex.SOW.getKey())) {
            inputDto = jsonMapper.fromJson(doctorConditionDtoJson, DoctorConditionDto.class);
        } else {
            inputDto = jsonMapper.fromJson(doctorConditionDtoJson, DoctorBoarConditionDto.class);
        }
        if (isNull(inputDto))
            throw new JsonResponseException("create.conditionEvent.fail");


        return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(buildEventInput(inputDto, pigId, PigEvent.CONDITION), buildBasicInputInfoDto(farmId, PigEvent.CONDITION)));
    }

    /**
     * 创建批量体况事件
     *
     * @param doctorConditionDtoJson
     * @param pigIds
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/createConditionEvents", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createConditionEvent(@RequestParam("doctorConditionDtoJson") String doctorConditionDtoJson,
                                        @RequestParam("pigIds") String pigIds, @RequestParam("farmId") Long farmId) {
        //检查猪ids是否合格
        checkPigIds(pigIds);
        List<Long> pigIdList = Splitters.COMMA.splitToList(pigIds).stream().map(Long::parseLong).collect(Collectors.toList());


        List<BasePigEventInputDto> inputDtos = pigIdList.stream().map(id -> {
            DoctorPig doctorPig = RespHelper.or500(doctorPigReadService.findPigById(pigIdList.get(0)));
            BasePigEventInputDto doctorConditionDto;
            if (Objects.equals(doctorPig.getPigType(), DoctorPig.PigSex.SOW.getKey())) {
                doctorConditionDto = jsonMapper.fromJson(doctorConditionDtoJson, DoctorConditionDto.class);
            } else {
                doctorConditionDto = jsonMapper.fromJson(doctorConditionDtoJson, DoctorBoarConditionDto.class);
            }
            buildEventInput(doctorConditionDto, id, PigEvent.CONDITION);
            return doctorConditionDto;
        }).collect(Collectors.toList());
        return RespHelper.or500(doctorPigEventWriteService.batchPigEventHandle(inputDtos, buildBasicInputInfoDto(farmId, PigEvent.CONDITION)));
    }

    @RequestMapping(value = "/createSemen", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createSemenEvent(@RequestParam("farmId") Long farmId,
                                 @RequestParam("pigId") Long pigId,
                                 @RequestParam("doctorSemenDtoJson") String doctorSemenDtoJson) {

        DoctorSemenDto doctorSemenDto = jsonMapper.fromJson(doctorSemenDtoJson, DoctorSemenDto.class);

        if (isNull(doctorSemenDto))
            throw new JsonResponseException("create.semenEvent.fail");
        return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorSemenDto, pigId, PigEvent.SEMEN), buildBasicInputInfoDto(farmId, PigEvent.SEMEN)));
    }

    @RequestMapping(value = "/createEntryInfo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean createEntryEvent(@RequestParam("farmId") Long farmId,
                                 @RequestParam("doctorFarmEntryJson") String doctorFarmEntryDtoJson) {
        DoctorFarmEntryDto doctorFarmEntryDto = jsonMapper.fromJson(doctorFarmEntryDtoJson, DoctorFarmEntryDto.class);

        if (isNull(doctorFarmEntryDto)) {
            throw new JsonResponseException("input.pigEntryJsonConvert.error");
        }
        return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(buildEntryEventInput(doctorFarmEntryDto, PigEvent.ENTRY),
                buildBasicInputInfoDto(farmId, PigEvent.ENTRY)));
    }

    @RequestMapping(value = "/createSowEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createSowEventInfo(@RequestParam("farmId") Long farmId,
                                   @RequestParam("pigId") Long pigId, @RequestParam("eventType") Integer eventType,
                                   @RequestParam("sowInfoDtoJson") String sowInfoDtoJson) {
        try {
            BasePigEventInputDto inputDto = eventInput(PigEvent.from(eventType), sowInfoDtoJson, farmId, DoctorPig.PigSex.SOW.getKey());
            return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(buildEventInput(inputDto, pigId, PigEvent.from(eventType)), buildBasicInputInfoDto(farmId, PigEvent.from(eventType))));
        } catch (Exception e) {
            log.error("pig.event.create.failed, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("pig.event.create.fail");
        }
    }

    @RequestMapping(value = "/createSowEvents", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createSowEventInfo(@RequestParam("farmId") Long farmId,
                                      @RequestParam("pigIds") String pigIds, @RequestParam("eventType") Integer eventType,
                                      @RequestParam("sowInfoDtoJson") String sowInfoDtoJson) {
        checkPigIds(pigIds);
        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().filter(idStr -> !Strings.isNullOrEmpty(idStr))
                .map(idStr -> {
                    BasePigEventInputDto inputDto = eventInput(PigEvent.from(eventType), sowInfoDtoJson, farmId, DoctorPig.PigSex.SOW.getKey());
                    buildEventInput(inputDto, Long.parseLong(idStr), PigEvent.from(eventType));
                    return inputDto;
                })
                .collect(Collectors.toList());
        return RespHelper.or500(doctorPigEventWriteService.batchPigEventHandle(inputDtos, buildBasicInputInfoDto(farmId, PigEvent.from(eventType))));
    }

    /**
     * 批量事件
     * @param batchPigEventDto
     * @return
     */
    @RequestMapping(value = "/batchCreateEvnet", method = RequestMethod.POST)
    public Boolean batchCreatePigEvent(@RequestBody DoctorBatchPigEventDto batchPigEventDto){
        if (Arguments.isNullOrEmpty(batchPigEventDto.getInputJsonList())) {
            return false;
        }

        PigEvent pigEvent = PigEvent.from(batchPigEventDto.getEventType());
        List<BasePigEventInputDto> inputDtoList = batchPigEventDto.getInputJsonList()
                .stream().map(inputJson -> {
                    BasePigEventInputDto inputDto = eventInput(pigEvent, inputJson, batchPigEventDto.getFarmId(), batchPigEventDto.getPigType());
                    if (Objects.equals(pigEvent.getKey(), PigEvent.ENTRY.getKey())) {
                        return buildEntryEventInput(inputDto, pigEvent);
                    } else {
                        return buildEventInput(inputDto, inputDto.getPigId(), pigEvent);
                    }
                }).collect(Collectors.toList());
        return RespHelper.or500(doctorPigEventWriteService.batchPigEventHandle(inputDtoList, buildBasicInputInfoDto(batchPigEventDto.getFarmId(), pigEvent)));
    }

    /**
     *
     * 事件基础信息
     * @param farmId
     * @param pigId
     * @param pigEvent
     * @return
     */
    private DoctorBasicInputInfoDto buildBasicInputInfoDto(Long farmId, PigEvent pigEvent) {
        try {
            DoctorFarm doctorFarm = RespHelper.orServEx(this.doctorFarmReadService.findFarmById(farmId));
            checkState(!isNull(pigEvent), "input.eventType.error");
            Long userId = UserUtil.getUserId();

            return DoctorBasicInputInfoDto.builder()
                    .farmId(doctorFarm.getId()).farmName(doctorFarm.getName()).orgId(doctorFarm.getOrgId()).orgName(doctorFarm.getOrgName())
                    .staffId(userId).staffName(RespHelper.orServEx(doctorGroupWebService.findRealName(userId)))
                    .build();
        } catch (Exception e) {
            log.error("build basic input info dto fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("build.basicInputInfo.error");
        }
    }

    /**
     * 构建猪事件共有信息
     * @param inputDto
     * @param pigId
     * @return
     */
    private BasePigEventInputDto buildEventInput(BasePigEventInputDto inputDto, Long pigId, PigEvent pigEvent){
        DoctorPigInfoDto pigDto = RespHelper.orServEx(this.doctorPigReadService.queryDoctorInfoDtoById(pigId));
        inputDto.setIsAuto(IsOrNot.NO.getValue());
        inputDto.setPigId(pigId);
        inputDto.setPigCode(pigDto.getPigCode());
        inputDto.setPigType(pigDto.getPigType());
        inputDto.setBarnId(pigDto.getBarnId());
        inputDto.setBarnName(pigDto.getBarnName());
        inputDto.setEventType(pigEvent.getKey());
        inputDto.setEventName(pigEvent.getName());
        inputDto.setEventDesc(pigEvent.getDesc());
        return inputDto;
    }

    /**
     * 构建进场事件信息
     * @param inputDto
     * @param pigEvent
     * @return
     */
    private BasePigEventInputDto buildEntryEventInput(BasePigEventInputDto inputDto, PigEvent pigEvent) {
        inputDto.setEventType(pigEvent.getKey());
        inputDto.setEventName(pigEvent.getName());
        inputDto.setEventDesc(pigEvent.getDesc());
        inputDto.setIsAuto(IsOrNot.NO.getValue());
        return inputDto;
    }
    /**
     * 修复事件名称(临时)
     *
     * @return
     */
    @RequestMapping(value = "/fix/eventName", method = RequestMethod.GET)
    @ResponseBody
    public Boolean fixEventName() {
        for (PigEvent pigEvent : PigEvent.values()) {
            DoctorPigEvent doctorPigEvent = DoctorPigEvent.builder()
                    .name(pigEvent.getName())
                    .type(pigEvent.getKey())
                    .build();
            doctorPigEventWriteService.updatePigEvents(doctorPigEvent);
        }
        return Boolean.TRUE;
    }

    /**
     * 修复事件描述(临时)
     *
     * @return
     */
    @RequestMapping(value = "/fix/desc", method = RequestMethod.GET)
    @ResponseBody
    public Boolean fixEventDesc() {
        try {
            int pageNo = 1;
            int pageSize = 5000;
            while (true) {
                Map<String, Object> map = Maps.newHashMap();
                map.put("types", Lists.newArrayList(PigEvent.ENTRY.getKey(), PigEvent.WEAN.getKey(), PigEvent.FARROWING.getKey()));
                List<DoctorPigEvent> events = RespHelper.or500(doctorPigEventReadService.queryPigEventsByCriteria(map, pageNo, pageSize)).getData();
                events.parallelStream()
                        .forEach(doctorPigEvent -> {
                            try {
                                Map<String, String> descMap = null;
                                if (Objects.equals(doctorPigEvent.getType(), PigEvent.ENTRY.getKey())) {
                                    DoctorFarmEntryDto doctorFarmEntryDto = jsonMapper.fromJson(doctorPigEvent.getExtra(), DoctorFarmEntryDto.class);
                                    descMap = doctorFarmEntryDto.descMap();
                                }
                                if (Objects.equals(doctorPigEvent.getType(), PigEvent.WEAN.getKey())) {
                                    DoctorWeanDto doctorWeanDto = jsonMapper.fromJson(doctorPigEvent.getExtra(), DoctorWeanDto.class);
                                    descMap = doctorWeanDto.descMap();
                                }
                                if (Objects.equals(doctorPigEvent.getType(), PigEvent.FARROWING.getKey())) {
                                    DoctorFarrowingDto doctorFarrowingDto = jsonMapper.fromJson(doctorPigEvent.getExtra(), DoctorFarrowingDto.class);
                                    descMap = doctorFarrowingDto.descMap();
                                }
                                DoctorPigEvent event = DoctorPigEvent.builder().name(doctorPigEvent.getName())
                                        .id(doctorPigEvent.getId()).desc(Joiner.on("#").withKeyValueSeparator("：").join(descMap)).build();
                                RespHelper.or500(doctorPigEventWriteService.updatePigEvents(event));

                            } catch (Exception e) {
                                log.error("fix desc error, event {}", doctorPigEvent);
                            }
                        });
                if (events.size() < pageSize) {
                    break;
                }
                pageNo++;
            }
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("fix desc cause by {}", Throwables.getStackTraceAsString(e));
            return Boolean.FALSE;
        }

    }

    /**
     * 事件信息json转相应dto
     * @param pigEvent
     * @param eventInfoDtoJson
     * @param farmId
     * @param pigType
     * @return
     */
    private BasePigEventInputDto eventInput(PigEvent pigEvent, String eventInfoDtoJson, Long farmId, Integer pigType) {
            switch (pigEvent) {
                case ENTRY:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorFarmEntryDto.class);
                case CHG_FARM:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorChgFarmDto.class);
                case CHG_LOCATION:
                    DoctorChgLocationDto doctorChgLocationDto = jsonMapper.fromJson(eventInfoDtoJson, DoctorChgLocationDto.class);
                    DoctorPigTrack doctorPigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(doctorChgLocationDto.getPigId()));
                    doctorChgLocationDto.setChgLocationFromBarnId(doctorPigTrack.getCurrentBarnId());
                    doctorChgLocationDto.setChgLocationFromBarnName(doctorPigTrack.getCurrentBarnName());
                    return doctorChgLocationDto;
                case CONDITION:
                    if (Objects.equals(pigType, DoctorPig.PigSex.SOW.getKey())){
                        return jsonMapper.fromJson(eventInfoDtoJson, DoctorConditionDto.class);
                    } else {
                        return jsonMapper.fromJson(eventInfoDtoJson, DoctorBoarConditionDto.class);
                    }
                case DISEASE:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorDiseaseDto.class);
                case VACCINATION:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorVaccinationDto.class);
                case REMOVAL:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorRemovalDto.class);
                case SEMEN:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorSemenDto.class);
                case MATING:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorMatingDto.class);
                case TO_PREG:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorChgLocationDto.class);
                case PREG_CHECK:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorPregChkResultDto.class);
                case TO_MATING:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorChgLocationDto.class);
                case TO_FARROWING:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorChgLocationDto.class);
                case FARROWING:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorFarrowingDto.class);
                case WEAN:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorWeanDto.class);
                case FOSTERS:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorFostersDto.class);
                case PIGLETS_CHG:
                    DoctorPigletsChgDto pigletsChg = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(eventInfoDtoJson, DoctorPigletsChgDto.class);
                    DoctorBasic doctorBasic = RespHelper.or500(doctorBasicReadService.findBasicById(pigletsChg.getPigletsChangeType()));
                    pigletsChg.setPigletsChangeTypeName(doctorBasic.getName());
                    //新录入的客户要创建一把
                    DoctorFarm doctorFarm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
                    Long customerId = RespHelper.orServEx(doctorBasicWriteService.addCustomerWhenInput(doctorFarm.getId(),
                            doctorFarm.getName(), pigletsChg.getPigletsCustomerId(), pigletsChg.getPigletsCustomerName(),
                            UserUtil.getUserId(), UserUtil.getCurrentUser().getName()));
                    pigletsChg.setPigletsCustomerId(customerId);
                    return pigletsChg;
                default:
                    throw new JsonResponseException("eventType.error");
            }
    }
}
