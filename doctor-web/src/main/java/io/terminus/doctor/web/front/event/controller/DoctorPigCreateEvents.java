package io.terminus.doctor.web.front.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.serializer.SerializerException;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.service.DoctorBasicMaterialReadService;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorBasicWriteService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSkuReadService;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.RespWithExHelper;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorEventModifyRequestDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.boar.DoctorBoarConditionDto;
import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
import io.terminus.doctor.event.dto.event.sow.*;
import io.terminus.doctor.event.dto.event.usual.*;
import io.terminus.doctor.event.enums.DoctorBasicEnums;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.*;
import io.terminus.doctor.event.service.*;
import io.terminus.doctor.event.util.EventUtil;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.doctor.web.core.aspects.DoctorValidService;
import io.terminus.doctor.web.front.event.dto.DoctorBatchPigEventDto;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.*;
import static io.terminus.common.utils.JsonMapper.JSON_NON_DEFAULT_MAPPER;
import static io.terminus.doctor.common.enums.PigType.*;
import static io.terminus.doctor.common.utils.Checks.expectNotNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;
import static io.terminus.doctor.event.enums.PigEvent.*;

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
    private final DoctorValidService doctorValidService;

    @RpcConsumer
    private DoctorBasicWriteService doctorBasicWriteService;
    @RpcConsumer
    private DoctorBasicReadService doctorBasicReadService;
    @RpcConsumer
    private DoctorBasicMaterialReadService doctorBasicMaterialReadService;
    @RpcConsumer
    private DoctorEventModifyRequestWriteService doctorEventModifyRequestWriteService;
    @RpcConsumer
    private DoctorEventModifyRequestReadService doctorEventModifyRequestReadService;
    @RpcConsumer
    private DoctorUserProfileReadService doctorUserProfileReadService;
    @RpcConsumer
    private DoctorModifyEventService doctorModifyEventService;

    private static JsonMapper jsonMapper = JSON_NON_DEFAULT_MAPPER;
    @RpcConsumer
    private DoctorEventModifyLogReadService doctorEventModifyLogReadService;

    @RpcConsumer
    private DoctorWarehouseSkuReadService doctorWarehouseSkuReadService;

    @Autowired
    public DoctorPigCreateEvents(DoctorPigEventWriteService doctorPigEventWriteService,
                                 DoctorFarmReadService doctorFarmReadService,
                                 DoctorPigReadService doctorPigReadService,
                                 UserReadService userReadService,
                                 DoctorBarnReadService doctorBarnReadService,
                                 DoctorPigEventReadService doctorPigEventReadService,
                                 DoctorGroupWebService doctorGroupWebService,
                                 DoctorValidService doctorValidService) {
        this.doctorPigEventWriteService = doctorPigEventWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorPigReadService = doctorPigReadService;
        this.userReadService = userReadService;
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorPigEventReadService = doctorPigEventReadService;
        this.doctorGroupWebService = doctorGroupWebService;
        this.doctorValidService = doctorValidService;
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
        log.info("createChangeLocationEvent, pigId:{}, farmId:{}, data:{}", pigId, farmId, doctorChgLocationDtoJson);
        BasePigEventInputDto doctorChgLocationDto = eventInput(PigEvent.CHG_LOCATION, doctorChgLocationDtoJson, farmId, null, pigId);
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorChgLocationDto, pigId, PigEvent.CHG_LOCATION), buildBasicInputInfoDto(farmId, PigEvent.CHG_LOCATION)));
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
        log.info("createChangeLocationEvent, pigIds:{}, farmId:{}, data:{}", pigIds, farmId, doctorChgLocationDtoJson);
        checkPigIds(pigIds);
        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().map(idStr -> {
            BasePigEventInputDto doctorChgLocationDto = eventInput(PigEvent.CHG_LOCATION, doctorChgLocationDtoJson, farmId, null, Long.parseLong(idStr));
            return buildEventInput(doctorChgLocationDto, Long.parseLong(idStr), PigEvent.CHG_LOCATION);
        }).collect(Collectors.toList());
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.batchPigEventHandle(inputDtos, buildBasicInputInfoDto(farmId, PigEvent.CHG_LOCATION)));
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
        log.info("createChangeFarmEvent, pigId:{}, farmId:{}, data:{}", pigId, farmId, doctorChgFarmDtoJson);
        BasePigEventInputDto doctorChgFarmDto = eventInput(CHG_FARM, doctorChgFarmDtoJson, farmId, null, pigId);
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorChgFarmDto, pigId, CHG_FARM), buildBasicInputInfoDto(farmId, CHG_FARM)));
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
        log.info("createChangeFarmEvents, pigIds:{}, farmId:{}, data:{}", pigIds, farmId, doctorChgFarmDtoJson);

        //检查猪ids是否合格
        checkPigIds(pigIds);

        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().map(idStr -> {
            BasePigEventInputDto doctorChgFarmDto = eventInput(CHG_FARM, doctorChgFarmDtoJson, farmId, null, Long.parseLong(idStr));
            buildEventInput(doctorChgFarmDto, Long.parseLong(idStr), CHG_FARM);
            return doctorChgFarmDto;
        }).collect(Collectors.toList());
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.batchPigEventHandle(inputDtos, buildBasicInputInfoDto(farmId, CHG_FARM)));
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
                                      @RequestParam("pigId") Long pigId,
                                      @RequestParam("farmId") Long farmId) {
        log.info("createRemovalEvent, pigId:{}, farmId:{}, data:{}", pigId, farmId, doctorRemovalDtoJson);
        BasePigEventInputDto doctorRemovalDto = eventInput(REMOVAL, doctorRemovalDtoJson, farmId, null, pigId);
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorRemovalDto, pigId, PigEvent.REMOVAL), buildBasicInputInfoDto(farmId, PigEvent.REMOVAL)));
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
        log.info("createRemovalEvent, pigIds:{}, farmId:{}, data:{}", pigIds, farmId, doctorRemovalDtoJson);

        //检查猪ids是否合格
        checkPigIds(pigIds);

        RespHelper.orServEx(doctorPigEventReadService.validatePigNotInFeed(pigIds));
        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().map(idStr -> {
            BasePigEventInputDto doctorRemovalDto = eventInput(REMOVAL, doctorRemovalDtoJson, farmId, null, Long.parseLong(idStr));
            return buildEventInput(doctorRemovalDto, Long.parseLong(idStr), PigEvent.REMOVAL);
        }).collect(Collectors.toList());
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.batchPigEventHandle(inputDtos, buildBasicInputInfoDto(farmId, PigEvent.REMOVAL)));
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
                                      @RequestParam("pigId") Long pigId,
                                      @RequestParam("farmId") Long farmId) {

        log.info("createDiseaseEvent, pigId:{}, farmId:{}, data:{}", pigId, farmId, doctorDiseaseDtoJson);

        BasePigEventInputDto doctorDiseaseDto = eventInput(PigEvent.DISEASE, doctorDiseaseDtoJson, farmId, null, pigId);
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorDiseaseDto, pigId, PigEvent.DISEASE), buildBasicInputInfoDto(farmId, PigEvent.DISEASE)));
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

        log.info("createDiseaseEvent, pigIds:{}, farmId:{}, data:{}", pigIds, farmId, doctorDiseaseDtoJson);

        //检查猪ids是否合格
        checkPigIds(pigIds);
        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().map(idStr -> {
            BasePigEventInputDto doctorDiseaseDto = eventInput(PigEvent.DISEASE, doctorDiseaseDtoJson, farmId, null, Long.parseLong(idStr));
            return buildEventInput(doctorDiseaseDto, Long.parseLong(idStr), PigEvent.DISEASE);
        }).collect(Collectors.toList());
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.batchPigEventHandle(inputDtos, buildBasicInputInfoDto(farmId, PigEvent.DISEASE)));
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
        log.info("createVaccinationEvent, pigId:{}, farmId:{}, data:{}", pigId, farmId, doctorVaccinationDtoJson);

        BasePigEventInputDto doctorVaccinationDto = eventInput(PigEvent.VACCINATION, doctorVaccinationDtoJson, farmId, null, pigId);
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorVaccinationDto, pigId, PigEvent.VACCINATION), buildBasicInputInfoDto(farmId, PigEvent.VACCINATION)));
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
        log.info("createVaccinationEvent, pigIds:{}, farmId:{}, data:{}", pigIds, farmId, doctorVaccinationDtoJson);

        //检查猪ids是否合格
        checkPigIds(pigIds);

        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().map(idStr -> {
            BasePigEventInputDto vaccinationDto = eventInput(PigEvent.VACCINATION, doctorVaccinationDtoJson, farmId, null, Long.parseLong(idStr));
            return buildEventInput(vaccinationDto, Long.parseLong(idStr), PigEvent.VACCINATION);
        }).collect(Collectors.toList());
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.batchPigEventHandle(inputDtos, buildBasicInputInfoDto(farmId, PigEvent.VACCINATION)));
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

        log.info("createConditionEvent, pigId:{}, farmId:{}, data:{}", pigId, farmId, doctorConditionDtoJson);

        DoctorPig doctorPig = RespHelper.or500(doctorPigReadService.findPigById(pigId));
        BasePigEventInputDto inputDto = eventInput(PigEvent.CONDITION, doctorConditionDtoJson, farmId, doctorPig.getPigType(), pigId);
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.pigEventHandle(buildEventInput(inputDto, pigId, PigEvent.CONDITION), buildBasicInputInfoDto(farmId, PigEvent.CONDITION)));
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

        log.info("createConditionEvent, pigIds:{}, farmId:{}, data:{}", pigIds, farmId, doctorConditionDtoJson);

        //检查猪ids是否合格
        checkPigIds(pigIds);
        List<Long> pigIdList = Splitters.COMMA.splitToList(pigIds).stream().map(Long::parseLong).collect(Collectors.toList());


        List<BasePigEventInputDto> inputDtos = pigIdList.stream().map(id -> {
            DoctorPig doctorPig = RespHelper.or500(doctorPigReadService.findPigById(id));
            BasePigEventInputDto doctorConditionDto = eventInput(PigEvent.CONDITION, doctorConditionDtoJson, farmId, doctorPig.getPigType(), id);
            return buildEventInput(doctorConditionDto, id, PigEvent.CONDITION);
        }).collect(Collectors.toList());
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.batchPigEventHandle(inputDtos, buildBasicInputInfoDto(farmId, PigEvent.CONDITION)));
    }

    @RequestMapping(value = "/createSemen", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createSemenEvent(@RequestParam("farmId") Long farmId,
                                 @RequestParam("pigId") Long pigId,
                                 @RequestParam("doctorSemenDtoJson") String doctorSemenDtoJson) {
        log.info("createSemenEvent, pigId:{}, farmId:{}, data:{}", pigId, farmId, doctorSemenDtoJson);

        BasePigEventInputDto doctorSemenDto = eventInput(PigEvent.SEMEN, doctorSemenDtoJson, farmId, DoctorPig.PigSex.BOAR.getKey(), pigId);
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorSemenDto, pigId, PigEvent.SEMEN), buildBasicInputInfoDto(farmId, PigEvent.SEMEN)));
    }

    @RequestMapping(value = "/createEntryInfo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean createEntryEvent(@RequestParam("farmId") Long farmId,
                                 @RequestParam("doctorFarmEntryJson") String doctorFarmEntryDtoJson) {
        log.info("createEntryEvent, farmId:{}, data:{}", farmId, doctorFarmEntryDtoJson);
        DoctorFarmEntryDto farmEntryDto = jsonMapper.fromJson(doctorFarmEntryDtoJson, DoctorFarmEntryDto.class);
        expectTrue(notEmpty(farmEntryDto.getPigCode()), "pig.code.not.empty");
        BasePigEventInputDto doctorFarmEntryDto = doctorValidService.valid(farmEntryDto, farmEntryDto.getPigCode());
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.pigEventHandle(buildEntryEventInput(doctorFarmEntryDto, ENTRY), buildBasicInputInfoDto(farmId, PigEvent.ENTRY)));
    }

    @RequestMapping(value = "/createSowEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createSowEventInfo(@RequestParam("farmId") Long farmId,
                                   @RequestParam("pigId") Long pigId, @RequestParam("eventType") Integer eventType,
                                   @RequestParam("sowInfoDtoJson") String sowInfoDtoJson) {

        log.info("createSowEventInfo, pigId:{}, farmId:{}, eventType:{}, data:{}", pigId, farmId, eventType, sowInfoDtoJson);

       /* JSONObject  jasonObject = JSONObject.fromObject(sowInfoDtoJson);
        Map maps = (Map)JSONObject.fromObject(sowInfoDtoJson);
            for (Object map : maps.entrySet()){
                System.out.println(((Map.Entry)map).getKey()+"     " + ((Map.Entry)map).getValue());
            }

        sowInfoDtoJson = jasonObject.fromObject(sowInfoDtoJson).toString();*/

        BasePigEventInputDto inputDto = eventInput(PigEvent.from(eventType), sowInfoDtoJson, farmId, DoctorPig.PigSex.SOW.getKey(), pigId);
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.pigEventHandle(buildEventInput(inputDto, pigId, PigEvent.from(eventType)), buildBasicInputInfoDto(farmId, PigEvent.from(eventType))));
    }

    @RequestMapping(value = "/createSowEvents", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createSowEventInfo(@RequestParam("farmId") Long farmId,
                                      @RequestParam("pigIds") String pigIds, @RequestParam("eventType") Integer eventType,
                                      @RequestParam("sowInfoDtoJson") String sowInfoDtoJson) {

        log.info("createSowEventInfo, pigIds:{}, farmId:{}, eventType:{}, data:{}", pigIds, farmId, eventType, sowInfoDtoJson);

        checkPigIds(pigIds);
        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().filter(idStr -> !Strings.isNullOrEmpty(idStr))
                .map(idStr -> {
                    BasePigEventInputDto inputDto = eventInput(PigEvent.from(eventType), sowInfoDtoJson, farmId, DoctorPig.PigSex.SOW.getKey(), Long.parseLong(idStr));
                    return buildEventInput(inputDto, Long.parseLong(idStr), PigEvent.from(eventType));
                })
                .collect(Collectors.toList());
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.batchPigEventHandle(inputDtos, buildBasicInputInfoDto(farmId, PigEvent.from(eventType))));
    }

    /**
     * 批量事件
     * @param batchPigEventDto
     * @return
     */
    @RequestMapping(value = "/batchCreateEvnet", method = RequestMethod.POST)
    public Boolean batchCreatePigEvent(@RequestBody DoctorBatchPigEventDto batchPigEventDto){
        log.info("web batch create pig starting");
        if (Arguments.isNullOrEmpty(batchPigEventDto.getInputJsonList())) {
            throw new JsonResponseException("batch.event.input.empty");
        }
        expectTrue(notNull(batchPigEventDto.getFarmId()), "farm.id.not.null");
        expectTrue(notNull(batchPigEventDto.getEventType()), "event.type.not.null");
        expectTrue(notNull(batchPigEventDto.getPigType()), "pig.type.not.null");
        PigEvent pigEvent = PigEvent.from(batchPigEventDto.getEventType());
        List<BasePigEventInputDto> inputDtoList = batchPigEventDto.getInputJsonList()
                .stream().map(inputJson -> {
                    if (Objects.equals(pigEvent.getKey(), ENTRY.getKey())) {
                        DoctorFarmEntryDto farmEntryDto = jsonMapper.fromJson(inputJson, DoctorFarmEntryDto.class);
                        expectTrue(notEmpty(farmEntryDto.getPigCode()), "pig.code.not.empty");
                        try {
                            BasePigEventInputDto inputDto = doctorValidService.valid(farmEntryDto, farmEntryDto.getPigCode());
                            if (Objects.equals(batchPigEventDto.getPigType(), DoctorPig.PigSex.SOW.getKey())) {
                                expectTrue(notNull(farmEntryDto.getParity()) && farmEntryDto.getParity() > 0, "sow.entry.farm.parity.input.fail");
                            }
                            return buildEntryEventInput(inputDto, pigEvent);
                        } catch (InvalidException e) {
                            log.error("batch entry event fail inputJson:{}, cause:{}", inputJson, Throwables.getStackTraceAsString(e));
                            throw new InvalidException(true, e.getError(), farmEntryDto.getPigCode(), e.getParams());
                        } catch (SerializerException e) {
                            log.error("batch entry event fail inputJson:{}, cause:{}", inputJson, Throwables.getStackTraceAsString(e));
                            throw new InvalidException(true, e.getMessage(), farmEntryDto.getPigCode());
                        }
                    } else {
                        try {
                            BasePigEventInputDto inputDto = eventInput(pigEvent, inputJson, batchPigEventDto.getFarmId(), batchPigEventDto.getPigType(), null);
                            return buildEventInput(inputDto, inputDto.getPigId(), pigEvent);
                        } catch (InvalidException e) {
                            log.error("batch create event fail inputJson:{}, cause:{}", inputJson, Throwables.getStackTraceAsString(e));
                            throw new InvalidException(true, e.getError(), getPigCode(inputJson), e.getParams());
                        } catch (ServiceException e) {
                            log.error("batch create event fail inputJson:{}, cause:{}", inputJson, Throwables.getStackTraceAsString(e));
                            throw new InvalidException(true, e.getMessage(), getPigCode(inputJson));
                        }
                    }
                }).collect(Collectors.toList());
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.batchPigEventHandle(inputDtoList, buildBasicInputInfoDto(batchPigEventDto.getFarmId(), pigEvent)));
    }

    /**
     * 创建编辑事件请求
     * @param farmId 猪场id
     * @param eventId 事件id
     * @param eventTye 事件类型
     * @param pigSex 猪性别
     * @param input 事件编辑内容
     */
    @RequestMapping(value = "/createPigModifyRequest", method = RequestMethod.POST)
    public void createPigModifyRequest(@RequestParam Long farmId,
                                       @RequestParam Long eventId,
                                       @RequestParam Integer eventType,
                                       @RequestParam Integer pigSex,
                                       @RequestParam String input) {
        //构建事件所需信息
        PigEvent pigEvent = PigEvent.from(eventType);
        DoctorBasicInputInfoDto basic = buildBasicInputInfoDto(farmId, pigEvent);
        BasePigEventInputDto inputDto;
        if (Objects.equals(eventType, PigEvent.ENTRY.getKey())) {
            inputDto = getEntryDto(input);
            buildEntryEventInput(inputDto, pigEvent);
        } else {
            inputDto = eventInput(pigEvent, input, farmId, pigSex, null);
            buildEventInput(inputDto, inputDto.getPigId(), pigEvent);
        }
        RespWithExHelper.orInvalid(doctorModifyEventService.modifyPigEvent(inputDto, eventId ,eventType));
    }

    /**
     * 分页查询事件编辑请求
     * @param modifyRequest 查询条件
     * @return 分页结果
     */
    @RequestMapping(value = "/pagingRequest", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Paging<DoctorEventModifyRequestDto> pagingRequest(@RequestParam Long farmId,
                                                             @RequestParam(required = false) Integer status,
                                                             @RequestParam(required = false) String code,
                                                             @RequestParam Integer pageNo,
                                                             @RequestParam Integer pageSize) {
        return RespHelper.or500(doctorEventModifyLogReadService
                .pageModifyLog(DoctorEventModifyLog.builder().farmId(farmId).build(), pageNo, pageSize));
    }

    /**
     * 根据id查询事件编辑请求
     * @param id 请求id
     * @return 编辑请求
     */
    @RequestMapping(value = "/findModifyReuqest/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorEventModifyRequestDto findModifyRequest(@PathVariable Long id) {
        return RespHelper.or500(doctorEventModifyLogReadService.findRequestDto(id));
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
            Long userId = UserUtil.getUserId();
            String name = RespHelper.orServEx(doctorGroupWebService.findRealName(userId));
            if (Strings.isNullOrEmpty(name)) {
                name = UserUtil.getCurrentUser().getName();
            }
            return DoctorBasicInputInfoDto.builder()
                    .farmId(doctorFarm.getId()).farmName(doctorFarm.getName()).orgId(doctorFarm.getOrgId()).orgName(doctorFarm.getOrgName())
                    .staffId(userId).staffName(name)
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
        inputDto.setBarnType(pigDto.getBarnType());
        inputDto.setEventType(pigEvent.getKey());
        inputDto.setEventName(pigEvent.getName());
        inputDto.setEventDesc(pigEvent.getDesc());
        inputDto.setEventSource(SourceType.INPUT.getValue());
        return inputDto;
    }

    /**
     * 构建进场事件信息
     * @param inputDto
     * @param pigEvent
     * @return
     */
    private BasePigEventInputDto buildEntryEventInput(BasePigEventInputDto inputDto, PigEvent pigEvent) {
        DoctorBarn doctorBarn = RespHelper.orServEx(doctorBarnReadService.findBarnById(inputDto.getBarnId()));
        inputDto.setBarnType(doctorBarn.getPigType());
        inputDto.setEventType(pigEvent.getKey());
        inputDto.setEventName(pigEvent.getName());
        inputDto.setEventDesc(pigEvent.getDesc());
        inputDto.setIsAuto(IsOrNot.NO.getValue());
        inputDto.setEventSource(SourceType.INPUT.getValue());
        return inputDto;
    }

    /**
     * 事件信息json转相应dto
     * @param pigEvent
     * @param eventInfoDtoJson
     * @param farmId
     * @param pigType
     * @return
     */
    private BasePigEventInputDto eventInput(PigEvent pigEvent, String eventInfoDtoJson, Long farmId, Integer pigType, Long pigId) {
        Map<String, Object> map = Maps.newHashMap();
        try {
            map = OBJECT_MAPPER.readValue(eventInfoDtoJson, Map.class);
        } catch (Exception e) {
            throw new InvalidException("json.to.map.error");
        }
        if (!notNull(pigId)) {
            expectTrue(notEmpty((String) map.get("pigId")), "pig.id.not.null");
        }
        Long realPigId = pigId !=null?pigId:Long.parseLong((String) map.get("pigId"));
        DoctorPig doctorPig = RespHelper.or500(doctorPigReadService.findPigById(realPigId));
        expectTrue(notNull(doctorPig), "pig.not.null", realPigId);

        switch (pigEvent) {
            case CHG_FARM:
                DoctorChgFarmDto chgFarmDto = jsonMapper.fromJson(eventInfoDtoJson, DoctorChgFarmDto.class);
                DoctorFarm fromFarm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
                expectTrue(notNull(fromFarm), "farm.not.null", farmId);
                //构建来源场信息
                chgFarmDto.setFromFarmId(fromFarm.getId());
                chgFarmDto.setFromFarmName(fromFarm.getName());
                DoctorPigTrack doctorPigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(realPigId));
                expectTrue(notNull(doctorPigTrack), "pig.track.not.null", realPigId);
                chgFarmDto.setFromBarnId(doctorPigTrack.getCurrentBarnId());
                chgFarmDto.setFromBarnName(doctorPigTrack.getCurrentBarnName());
                //构建转入场信息
                DoctorFarm toFarm = RespHelper.or500(doctorFarmReadService.findFarmById(chgFarmDto.getToFarmId()));
                expectTrue(notNull(fromFarm), "farm.not.null", farmId);
                DoctorBarn toBarn = RespHelper.or500(doctorBarnReadService.findBarnById(chgFarmDto.getToBarnId()));
                expectTrue(notNull(toBarn), "barn.not.null", chgFarmDto.getToBarnId());
                chgFarmDto.setToFarmName(toFarm.getName());
                chgFarmDto.setToBarnName(toBarn.getName());
                chgFarmDto = doctorValidService.valid(chgFarmDto, doctorPig.getPigCode());

                return chgFarmDto;
            case CHG_LOCATION:
                DoctorChgLocationDto doctorChgLocationDto = jsonMapper.fromJson(eventInfoDtoJson, DoctorChgLocationDto.class);
                String fromBarnName;
                if (isNull(doctorChgLocationDto.getChgLocationFromBarnId())) {
                    DoctorPigTrack doctorPigTrack1 = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(realPigId));
                    fromBarnName = doctorPigTrack1.getCurrentBarnName();
                    doctorChgLocationDto.setChgLocationFromBarnId(doctorPigTrack1.getCurrentBarnId());
                } else {
                    DoctorBarn fromBarn = RespHelper.or500(doctorBarnReadService.findBarnById(doctorChgLocationDto.getChgLocationFromBarnId()));
                    fromBarnName = fromBarn.getName();
                }
                doctorChgLocationDto.setChgLocationFromBarnName(fromBarnName);
                doctorChgLocationDto.setChgLocationToBarnName(RespHelper.or500(doctorBarnReadService.findBarnById(doctorChgLocationDto.getChgLocationToBarnId())).getName());
                doctorChgLocationDto = doctorValidService.valid(doctorChgLocationDto, doctorPig.getPigCode());
                return doctorChgLocationDto;
            case CONDITION:
                if (Objects.equals(pigType, DoctorPig.PigSex.SOW.getKey())) {
                    DoctorConditionDto conditionDto = jsonMapper.fromJson(eventInfoDtoJson, DoctorConditionDto.class);
                    Date conditionDate = conditionDto.getConditionDate();
                    Long pigId1 = conditionDto.getPigId();
                    Map<String,Object> maps = doctorPigEventReadService.getBranName(pigId1, conditionDate);
                    if(maps.size() != 0){
                        Long barnId = Long.valueOf(maps.get("barnId").toString());
                        String barnName = maps.get("barnName").toString();
                        conditionDto.setBarnId(barnId);
                        conditionDto.setBarnName(barnName);
                    }
                    return doctorValidService.valid(conditionDto, doctorPig.getPigCode());
                } else {
                    DoctorBoarConditionDto boarConditionDto = jsonMapper.fromJson(eventInfoDtoJson, DoctorBoarConditionDto.class);
                    return doctorValidService.valid(boarConditionDto, doctorPig.getPigCode());
                }
            case DISEASE:
                DoctorDiseaseDto diseaseDto = jsonMapper.fromJson(eventInfoDtoJson, DoctorDiseaseDto.class);
                diseaseDto = doctorValidService.valid(diseaseDto, doctorPig.getPigCode());
                DoctorBasic disease = RespHelper.or500(doctorBasicReadService.findBasicById(diseaseDto.getDiseaseId()));
                expectTrue(notNull(disease), "basic.not.null", diseaseDto.getDiseaseId());
                diseaseDto.setDiseaseName(disease.getName());
                return diseaseDto;
            case VACCINATION:
                DoctorVaccinationDto vaccinationDto = jsonMapper.fromJson(eventInfoDtoJson, DoctorVaccinationDto.class);
                vaccinationDto = doctorValidService.valid(vaccinationDto, vaccinationDto.getPigCode());
                //DoctorBasicMaterial vaccinationItem = RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialById(vaccinationDto.getVaccinationItemId()));
                //基础数据查询从DoctorBasicMaterial换至DoctorBasic ----QT 2018/5/15
                DoctorBasic cType = RespHelper.or500(doctorBasicReadService.findBasicById(vaccinationDto.getVaccinationItemId()));
                expectTrue(notNull(cType), "basic.not.null", vaccinationDto.getVaccinationItemId());
                vaccinationDto.setVaccinationItemName(cType.getName());
                DoctorWarehouseSku vaccination = RespHelper.or500(doctorWarehouseSkuReadService.findById(vaccinationDto.getVaccinationId()));
                expectTrue(notNull(vaccination), "basic.material.not.null", vaccinationDto.getVaccinationId());
                vaccinationDto.setVaccinationName(vaccination.getName());
                return vaccinationDto;
            case REMOVAL:
                DoctorRemovalDto removalDto = jsonMapper.fromJson(eventInfoDtoJson, DoctorRemovalDto.class);
                removalDto = doctorValidService.valid(removalDto, doctorPig.getPigCode());
                DoctorBasic chgType = RespHelper.or500(doctorBasicReadService.findBasicById(removalDto.getChgTypeId()));
                expectTrue(notNull(chgType), "basic.not.null", removalDto.getChgTypeId());
                removalDto.setChgTypeName(chgType.getName());
                //变动原因
                if (removalDto.getChgReasonId() != null) {
                    DoctorChangeReason changeReason = RespHelper.or500(doctorBasicReadService.findChangeReasonById(removalDto.getChgReasonId()));
                    expectTrue(notNull(changeReason), "change.reason.not.null", removalDto.getChgReasonId());
                    removalDto.setChgReasonName(changeReason.getReason());
                }
                //销售时 价格不可为空
                if (Objects.equals(removalDto.getChgTypeId(), DoctorBasicEnums.SALE.getId())) {
                    expectTrue(notNull(removalDto.getPrice()), "sale.price.not.null");
                }
                DoctorFarm doctorFarm2 = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
                expectTrue(notNull(doctorFarm2), "farm.not.null", farmId);
                Long customerId1 = RespHelper.orServEx(doctorBasicWriteService.addCustomerWhenInput(doctorFarm2.getId(),
                        doctorFarm2.getName(), removalDto.getCustomerId(), removalDto.getCustomerName(),
                        UserUtil.getUserId(), UserUtil.getCurrentUser().getName()));
                removalDto.setCustomerId(customerId1);
                return removalDto;
            case SEMEN:
                DoctorSemenDto semenDto = jsonMapper.fromJson(eventInfoDtoJson, DoctorSemenDto.class);
                return doctorValidService.valid(semenDto, doctorPig.getPigCode());
            case MATING:
                DoctorMatingDto matingDto = jsonMapper.fromJson(eventInfoDtoJson, DoctorMatingDto.class);
                DoctorPig matingBoar = expectNotNull(RespHelper.or500(doctorPigReadService.findPigById(matingDto.getMatingBoarPigId())), "mating.boar.not.null", matingDto.getMatingBoarPigId());
                matingDto.setMatingBoarPigCode(matingBoar.getPigCode());
                matingDto.setOperatorName(RespHelper.orServEx(doctorGroupWebService.findRealName(matingDto.getOperatorId())));
                return doctorValidService.valid(matingDto, doctorPig.getPigCode());
            case TO_PREG:
                DoctorChgLocationDto chgLocationDto = jsonMapper.fromJson(eventInfoDtoJson, DoctorChgLocationDto.class);
                return doctorValidService.valid(chgLocationDto, doctorPig.getPigCode());
            case PREG_CHECK:
                DoctorPregChkResultDto pregChkResultDto = jsonMapper.fromJson(eventInfoDtoJson, DoctorPregChkResultDto.class);
                return doctorValidService.valid(pregChkResultDto, doctorPig.getPigCode());

            case TO_MATING:
            case TO_FARROWING:
                DoctorChgLocationDto dto = jsonMapper.fromJson(eventInfoDtoJson, DoctorChgLocationDto.class);
                return doctorValidService.valid(dto, doctorPig.getPigCode());
            case FARROWING:
                DoctorFarrowingDto farrowingDto = jsonMapper.fromJson(eventInfoDtoJson, DoctorFarrowingDto.class);
                if(realPigId != null) {
                    DoctorPigEvent doctorPigEvent = RespHelper.orServEx(doctorPigEventReadService.findLastFirstMateEvent(realPigId));
                    Long farrowingDate = farrowingDto.getFarrowingDate().getTime();
                    Long lastMateDate = doctorPigEvent.getEventAt().getTime();
                    Long time = (farrowingDate - lastMateDate) / (3600 * 24 * 1000);
                    if (time < 100) {
                        throw new InvalidException("分娩时间必须大于配种时间100天");
                    }
                }
                DoctorFarm doctorFarm3 = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
                if (Objects.equals(doctorFarm3.getIsWeak(), io.terminus.doctor.common.enums.IsOrNot.NO.getKey())) {
                    farrowingDto.setFarrowingLiveCount(farrowingDto.getHealthCount());
                } else {
                    farrowingDto.setFarrowingLiveCount(EventUtil.plusInt(farrowingDto.getHealthCount(), farrowingDto.getWeakCount()));
                }
                return doctorValidService.valid(farrowingDto, doctorPig.getPigCode());
            case WEAN:
                DoctorWeanDto weanDto = jsonMapper.fromJson(eventInfoDtoJson, DoctorWeanDto.class);
                return doctorValidService.valid(weanDto, doctorPig.getPigCode());
            case FOSTERS:
                DoctorFostersDto fostersDto = jsonMapper.fromJson(eventInfoDtoJson, DoctorFostersDto.class);
                return doctorValidService.valid(fostersDto, doctorPig.getPigCode());
            case PIGLETS_CHG:
                DoctorPigletsChgDto pigletsChg = JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(eventInfoDtoJson, DoctorPigletsChgDto.class);
                if (Objects.equals(pigletsChg.getPigletsChangeType(), DoctorBasicEnums.SALE.getId())) {
                    expectTrue(notNull(pigletsChg.getPigletsPrice()), "sale.price.not.null");
                    expectTrue(notNull(pigletsChg.getPigletsCustomerName()), "sale.customer.not.null");
                }
                if (notNull(pigletsChg.getPigletsChangeReason())) {
                    pigletsChg.setPigletsChangeReasonName(RespHelper.orServEx(doctorBasicReadService.findChangeReasonById(pigletsChg.getPigletsChangeReason())).getReason());
                }
                pigletsChg = doctorValidService.valid(pigletsChg, doctorPig.getPigCode());

                DoctorBasic doctorBasic = RespHelper.or500(doctorBasicReadService.findBasicById(pigletsChg.getPigletsChangeType()));
                expectTrue(notNull(doctorBasic), "basic.not.null", pigletsChg.getPigletsChangeType());
                pigletsChg.setPigletsChangeTypeName(doctorBasic.getName());
                //新录入的客户要创建一把
                DoctorFarm doctorFarm1 = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
                expectTrue(notNull(doctorFarm1), "farm.not.null", farmId);
                Long customerId = RespHelper.orServEx(doctorBasicWriteService.addCustomerWhenInput(doctorFarm1.getId(),
                        doctorFarm1.getName(), pigletsChg.getPigletsCustomerId(), pigletsChg.getPigletsCustomerName(),
                        UserUtil.getUserId(), UserUtil.getCurrentUser().getName()));
                pigletsChg.setPigletsCustomerId(customerId);
                return pigletsChg;
            default:
                throw new JsonResponseException("eventType.error");
        }
    }

    private BasePigEventInputDto getEntryDto(String input){
        DoctorFarmEntryDto farmEntryDto = JSON_NON_DEFAULT_MAPPER.fromJson(input, DoctorFarmEntryDto.class);
        farmEntryDto = doctorValidService.valid(farmEntryDto, farmEntryDto.getPigCode());
        farmEntryDto.setBreedName(RespHelper.orServEx(doctorBasicReadService.findBasicById(farmEntryDto.getBreed())).getName());
        if (notNull(farmEntryDto.getBreedType())) {
            farmEntryDto.setBreedTypeName(RespHelper.orServEx(doctorBasicReadService.findBasicById(farmEntryDto.getBreedType())).getName());
        }
        return farmEntryDto;
    }
    /**
     * 从json获取pidCode
     * @param inputJson
     * @return
     */
    private String getPigCode(String inputJson) {
        Map<String, Object> map = Maps.newHashMap();
        try {
            map = OBJECT_MAPPER.readValue(inputJson, Map.class);
            return expectNotNull(map.get("pigCode"), "pig.code.not.empty").toString();
        } catch (Exception e) {
            throw new InvalidException("json.to.map.error");
        }
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
                map.put("types", Lists.newArrayList(ENTRY.getKey(), PigEvent.WEAN.getKey(), PigEvent.FARROWING.getKey()));
                List<DoctorPigEvent> events = RespHelper.or500(doctorPigEventReadService.queryPigEventsByCriteria(map, pageNo, pageSize)).getData();
                events.parallelStream()
                        .forEach(doctorPigEvent -> {
                            try {
                                Map<String, String> descMap = null;
                                if (Objects.equals(doctorPigEvent.getType(), ENTRY.getKey())) {
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

    @RequestMapping(value = "/fixTimeFormate", method = RequestMethod.GET)
    @ResponseBody
    public Boolean fixEventExtraTimeFormate() {
        try {
            JsonMapperUtil jsonMapperUtil = JsonMapperUtil.nonDefaultMapperWithFormat(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));
            JsonMapperUtil jsonMapperUtil1 = JsonMapperUtil.nonDefaultMapper();
            Integer pageNo = 1;
            Integer pageSize = 5000;
            while (true) {
                List<DoctorPigEvent> events = RespHelper.or500(doctorPigEventReadService.queryPigEventsByCriteria(Maps.newHashMap(), pageNo, pageSize)).getData();
                int size = events.size();
                events = events.stream().filter(pigEvent -> !Strings.isNullOrEmpty(pigEvent.getExtra())).collect(Collectors.toList());
                List<DoctorPigEvent> updateEvents = Lists.newArrayList();
                events.parallelStream().forEach(pigEvent -> {
                    Class clazz = null;
                    switch (PigEvent.from(pigEvent.getType())) {
                        case ENTRY:
                            clazz = DoctorFarmEntryDto.class;
                            break;
                        case CHG_FARM:
                            clazz = DoctorChgFarmDto.class;
                            break;
                        case CHG_LOCATION:
                            clazz = DoctorChgLocationDto.class;
                            break;
                        case CONDITION:
                            if (Objects.equals(pigEvent.getKind(), DoctorPig.PigSex.SOW.getKey())){
                                clazz = DoctorConditionDto.class;
                            } else {
                                clazz = DoctorBoarConditionDto.class;
                            }
                            break;
                        case DISEASE:
                            clazz = DoctorDiseaseDto.class;
                            break;
                        case VACCINATION:
                            clazz = DoctorVaccinationDto.class;
                            break;
                        case REMOVAL:
                            clazz = DoctorRemovalDto.class;
                            break;
                        case SEMEN:
                            clazz = DoctorSemenDto.class;
                            break;
                        case MATING:
                            clazz = DoctorMatingDto.class;
                            break;
                        case PREG_CHECK:
                            clazz = DoctorPregChkResultDto.class;
                            break;
                        case TO_PREG:
                        case TO_MATING:
                        case TO_FARROWING:
                            clazz = DoctorChgLocationDto.class;
                            break;
                        case FARROWING:
                            clazz = DoctorFarrowingDto.class;
                            break;
                        case WEAN:
                            clazz = DoctorWeanDto.class;
                            break;
                        case FOSTERS:
                            clazz = DoctorFostersDto.class;
                            break;
                        case FOSTERS_BY:
                            clazz = DoctorFosterByDto.class;
                            break;
                        case PIGLETS_CHG:
                            clazz = DoctorPigletsChgDto.class;
                            break;
                    }
                    try {
                        jsonMapperUtil1.getMapper().readValue(pigEvent.getExtra(), clazz);
                    } catch (IOException e) {
                        DoctorPigEvent updateEvent = new DoctorPigEvent();
                        updateEvent.setId(pigEvent.getId());
                        updateEvent.setName(pigEvent.getName());
                        updateEvent.setExtra(ToJsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(jsonMapperUtil.fromJson(pigEvent.getExtra(), clazz)));
                        RespHelper.or500(doctorPigEventWriteService.updatePigEvents(updateEvent));
                    } catch (Exception e) {
                        log.error("id:{}, cause:{}", pigEvent.getId(), Throwables.getStackTraceAsString(e));
                    }
                });
                if (size < pageSize) {
                    break;
                }
                pageNo++;
            }
        } catch (Exception e) {
            log.error("fix extra time formate error, cause:{}", Throwables.getStackTraceAsString(e));
            return false;
        }
        return true;
    }
}
