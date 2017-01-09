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
import io.terminus.doctor.basic.service.DoctorBasicWriteService;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFostersDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorWeanDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.dto.event.usual.DoctorConditionDto;
import io.terminus.doctor.event.dto.event.usual.DoctorDiseaseDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.dto.event.usual.DoctorRemovalDto;
import io.terminus.doctor.event.dto.event.usual.DoctorVaccinationDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
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
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
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
    //private final DoctorSowEventCreateService doctorSowEventCreateService;
    private final DoctorBarnReadService doctorBarnReadService;
    private final DoctorPigEventReadService doctorPigEventReadService;
    private final DoctorGroupWebService doctorGroupWebService;
    @RpcConsumer
    private DoctorBasicWriteService doctorBasicWriteService;

    private static JsonMapper jsonMapper = JSON_NON_DEFAULT_MAPPER;

    @Autowired
    public DoctorPigCreateEvents(DoctorPigEventWriteService doctorPigEventWriteService,
                                 DoctorFarmReadService doctorFarmReadService,
                                 DoctorPigReadService doctorPigReadService,
                                 UserReadService userReadService,
                                // DoctorSowEventCreateService doctorSowEventCreateService,
                                 DoctorBarnReadService doctorBarnReadService,
                                 DoctorPigEventReadService doctorPigEventReadService,
                                 DoctorGroupWebService doctorGroupWebService) {
        this.doctorPigEventWriteService = doctorPigEventWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorPigReadService = doctorPigReadService;
        this.userReadService = userReadService;
     //   this.doctorSowEventCreateService = doctorSowEventCreateService;
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
        checkEventAt(pigId, PigEvent.CHG_LOCATION, doctorChgLocationDtoJson);
        DoctorChgLocationDto doctorChgLocationDto = jsonMapper.fromJson(
                doctorChgLocationDtoJson, DoctorChgLocationDto.class);

        if (isNull(doctorChgLocationDto)) {
            throw new JsonResponseException("chgLocation.inputParam.error");
        }
        DoctorPigTrack doctorPigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(pigId));
        doctorChgLocationDto.setChgLocationFromBarnId(doctorPigTrack.getCurrentBarnId());
        doctorChgLocationDto.setChgLocationFromBarnName(doctorPigTrack.getCurrentBarnName());
        return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorChgLocationDto, pigId), buildBasicInputInfoDto(farmId, PigEvent.CHG_LOCATION)));
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
        checkEventAt(pigIds, PigEvent.CHG_LOCATION, doctorChgLocationDtoJson);
        DoctorChgLocationDto doctorChgLocationDto = jsonMapper.fromJson(
                doctorChgLocationDtoJson, DoctorChgLocationDto.class);
        if (isNull(doctorChgLocationDto)) {
            throw new JsonResponseException("chgLocation.inputParam.error");
        }

        checkPigIds(pigIds);
        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().map(idStr -> {
            DoctorPigTrack doctorPigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(Long.parseLong(idStr)));
            doctorChgLocationDto.setChgLocationFromBarnId(doctorPigTrack.getCurrentBarnId());
            doctorChgLocationDto.setChgLocationFromBarnName(doctorPigTrack.getCurrentBarnName());
            return buildEventInput(doctorChgLocationDto, Long.parseLong(idStr));
        }).collect(Collectors.toList());
        return RespHelper.or500(doctorPigEventWriteService.batchPigEventHandle(inputDtos,

    buildBasicInputInfoDto(farmId, PigEvent.CHG_LOCATION)));
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

        checkEventAt(pigId, PigEvent.CHG_FARM, doctorChgFarmDtoJson);
        DoctorChgFarmDto doctorChgFarmDto = jsonMapper.fromJson(doctorChgFarmDtoJson, DoctorChgFarmDto.class);
        if (isNull(doctorChgFarmDto))
            throw new JsonResponseException("create.chgFarm.error");
        return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorChgFarmDto, pigId), buildBasicInputInfoDto(farmId, PigEvent.CHG_FARM)));
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
        checkEventAt(pigIds, PigEvent.CHG_FARM, doctorChgFarmDtoJson);
        DoctorChgFarmDto doctorChgFarmDto = jsonMapper.fromJson(doctorChgFarmDtoJson, DoctorChgFarmDto.class);
        if (isNull(doctorChgFarmDto))
            throw new JsonResponseException("create.chgFarm.error");

        //检查猪ids是否合格
        checkPigIds(pigIds);

        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().map(idStr -> buildEventInput(doctorChgFarmDto, Long.parseLong(idStr))).collect(Collectors.toList());
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
        checkEventAt(pigId, PigEvent.REMOVAL, doctorRemovalDtoJson);
        DoctorRemovalDto doctorRemovalDto = jsonMapper.fromJson(doctorRemovalDtoJson, DoctorRemovalDto.class);
        if (isNull(doctorRemovalDto))
            throw new JsonResponseException("create.removalEvent.fail");

        return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorRemovalDto, pigId), buildBasicInputInfoDto(farmId, PigEvent.REMOVAL)));
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
        checkEventAt(pigIds, PigEvent.REMOVAL, doctorRemovalDtoJson);
        DoctorRemovalDto doctorRemovalDto = jsonMapper.fromJson(doctorRemovalDtoJson, DoctorRemovalDto.class);
        if (isNull(doctorRemovalDto))
            throw new JsonResponseException("create.removalEvent.fail");
        //检查猪ids是否合格
        checkPigIds(pigIds);

        RespHelper.orServEx(doctorPigEventReadService.validatePigNotInFeed(pigIds));
        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().map(idStr -> buildEventInput(doctorRemovalDto, Long.parseLong(idStr))).collect(Collectors.toList());
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
        checkEventAt(pigId, PigEvent.DISEASE, doctorDiseaseDtoJson);

        DoctorDiseaseDto doctorDiseaseDto = jsonMapper.fromJson(doctorDiseaseDtoJson, DoctorDiseaseDto.class);
        if (isNull(doctorDiseaseDto))
            throw new JsonResponseException("create.diseaseEvent.fail");
        return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorDiseaseDto, pigId), buildBasicInputInfoDto(farmId, PigEvent.DISEASE)));
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
        checkEventAt(pigIds, PigEvent.DISEASE, doctorDiseaseDtoJson);
        DoctorDiseaseDto doctorDiseaseDto = jsonMapper.fromJson(doctorDiseaseDtoJson, DoctorDiseaseDto.class);
        if (isNull(doctorDiseaseDto))
            throw new JsonResponseException("create.diseaseEvent.fail");
        //检查猪ids是否合格
        checkPigIds(pigIds);
        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().map(idStr -> buildEventInput(doctorDiseaseDto, Long.parseLong(idStr))).collect(Collectors.toList());
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
        checkEventAt(pigId, PigEvent.VACCINATION, doctorVaccinationDtoJson);
        DoctorVaccinationDto doctorVaccinationDto = jsonMapper.fromJson(doctorVaccinationDtoJson, DoctorVaccinationDto.class);
        if (isNull(doctorVaccinationDto))
            throw new JsonResponseException("create.diseaseEvent.fail");
        return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorVaccinationDto, pigId), buildBasicInputInfoDto(farmId, PigEvent.VACCINATION)));
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
        checkEventAt(pigIds, PigEvent.VACCINATION, doctorVaccinationDtoJson);
        DoctorVaccinationDto doctorVaccinationDto = jsonMapper.fromJson(doctorVaccinationDtoJson, DoctorVaccinationDto.class);
        if (isNull(doctorVaccinationDto))
            throw new JsonResponseException("create.diseaseEvent.fail");
        //检查猪ids是否合格
        checkPigIds(pigIds);

        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().map(idStr -> buildEventInput(doctorVaccinationDto, Long.parseLong(idStr))).collect(Collectors.toList());
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
        checkEventAt(pigId, PigEvent.CONDITION, doctorConditionDtoJson);
        DoctorConditionDto doctorConditionDto = jsonMapper.fromJson(doctorConditionDtoJson, DoctorConditionDto.class);
        if (isNull(doctorConditionDto))
            throw new JsonResponseException("create.conditionEvent.fail");
        return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorConditionDto, pigId), buildBasicInputInfoDto(farmId, PigEvent.CONDITION)));
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
        checkEventAt(pigIds, PigEvent.CONDITION, doctorConditionDtoJson);
        DoctorConditionDto doctorConditionDto = jsonMapper.fromJson(doctorConditionDtoJson, DoctorConditionDto.class);
        if (isNull(doctorConditionDto))
            throw new JsonResponseException("create.conditionEvent.fail");
        //检查猪ids是否合格
        checkPigIds(pigIds);

        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().map(idStr -> buildEventInput(doctorConditionDto, Long.parseLong(idStr))).collect(Collectors.toList());
        return RespHelper.or500(doctorPigEventWriteService.batchPigEventHandle(inputDtos, buildBasicInputInfoDto(farmId, PigEvent.CONDITION)));
    }

    @RequestMapping(value = "/createSemen", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createSemenEvent(@RequestParam("farmId") Long farmId,
                                 @RequestParam("pigId") Long pigId,
                                 @RequestParam("doctorSemenDtoJson") String doctorSemenDtoJson) {
        checkEventAt(pigId, PigEvent.SEMEN, doctorSemenDtoJson);

        DoctorSemenDto doctorSemenDto = jsonMapper.fromJson(doctorSemenDtoJson, DoctorSemenDto.class);
        if (isNull(doctorSemenDto))
            throw new JsonResponseException("create.semenEvent.fail");
        return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(buildEventInput(doctorSemenDto, pigId), buildBasicInputInfoDto(farmId, PigEvent.SEMEN)));
    }

    @RequestMapping(value = "/createEntryInfo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean createEntryEvent(@RequestParam("farmId") Long farmId,
                                 @RequestParam("doctorFarmEntryJson") String doctorFarmEntryDtoJson) {
        DoctorFarmEntryDto doctorFarmEntryDto = jsonMapper.fromJson(doctorFarmEntryDtoJson, DoctorFarmEntryDto.class);

        if (isNull(doctorFarmEntryDto)) {
            throw new JsonResponseException("input.pigEntryJsonConvert.error");
        }
        return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(doctorFarmEntryDto,
                buildBasicInputInfoDto(farmId, PigEvent.ENTRY)));
    }

//    @RequestMapping(value = "/batchCreateEntryInfo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    public List<Long> batchCreateEntryInfo(@RequestBody DoctorFarmEntryDtoList doctorFarmEntryDtoList) {
//        if (doctorFarmEntryDtoList == null || Arguments.isNullOrEmpty(doctorFarmEntryDtoList.getDoctorFarmEntryDtos())) {
//            Lists.newArrayList();
//        }
//        List<DoctorPigEntryEventDto> result=Lists.newArrayList();
//        for (DoctorFarmEntryDto doctorFarmEntryDto : doctorFarmEntryDtoList.getDoctorFarmEntryDtos()) {
//            DoctorPigEntryEventDto doctorPigEntryEventDto=new DoctorPigEntryEventDto();
////            DoctorFarmEntryDto doctorFarmEntryDto = jsonMapper.fromJson(str, DoctorFarmEntryDto.class);
//            if (isNull(doctorFarmEntryDto)) {
//                throw new JsonResponseException("input.pigEntryJsonConvert.error");
//            }
//            doctorPigEntryEventDto.setDoctorFarmEntryDto(doctorFarmEntryDto);
//            doctorPigEntryEventDto.setDoctorBasicInputInfoDto(buildBasicEntryInputInfo(doctorFarmEntryDtoList.getFarmId(), doctorFarmEntryDto, PigEvent.ENTRY));
//            result.add(doctorPigEntryEventDto);
//        }
//        return RespHelper.or500(doctorPigEventWriteService.batchPigEntryEvent(result));
//    }

    @RequestMapping(value = "/createSowEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createSowEventInfo(@RequestParam("farmId") Long farmId,
                                   @RequestParam("pigId") Long pigId, @RequestParam("eventType") Integer eventType,
                                   @RequestParam("sowInfoDtoJson") String sowInfoDtoJson) {
        try {
        checkEventAt(pigId, PigEvent.from(eventType), sowInfoDtoJson);
       // Long tempPigId = pigId;
//        if (Objects.equals(eventType, PigEvent.FOSTERS.getKey())) {
//            List<DoctorBasicInputInfoDto> basics = fosterInfo(farmId, pigId, sowInfoDtoJson);
//            RespHelper.or500(doctorSowEventCreateService.sowEventsCreate(basics, sowInfoDtoJson));
//            // 猪批量事件操作， 返回PigId
//        } else {
            return RespHelper.or500(doctorPigEventWriteService.pigEventHandle(buildEventInput(eventInput(PigEvent.from(eventType), sowInfoDtoJson, farmId), pigId), buildBasicInputInfoDto(farmId, PigEvent.from(eventType))));
//            //1.断奶后触发转舍事件
//            if (Objects.equals(eventType, PigEvent.WEAN.getKey())) {
//                DoctorPartWeanDto doctorPartWeanDto = jsonMapper.fromJson(sowInfoDtoJson, DoctorPartWeanDto.class);
//                Map<String, Object> temp = jsonMapper.getMapper().readValue(sowInfoDtoJson, JacksonType.MAP_OF_OBJECT);
//                temp.put("changeLocationDate", temp.get("partWeanDate"));
//                String sowInfoDto = jsonMapper.getMapper().writeValueAsString(temp);
//                DoctorChgLocationDto doctorChgLocationDto = jsonMapper.fromJson(sowInfoDto, DoctorChgLocationDto.class);
//                DoctorBarn doctorBarn = RespHelper.or500(doctorBarnReadService.findBarnById(doctorChgLocationDto.getChgLocationToBarnId()));
//                doctorChgLocationDto.setChgLocationToBarnName(doctorBarn.getName());
//                DoctorPigTrack doctorPigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(tempPigId));
//                doctorChgLocationDto.setChgLocationFromBarnId(doctorPigTrack.getCurrentBarnId());
//                doctorChgLocationDto.setChgLocationFromBarnName(doctorPigTrack.getCurrentBarnName());
//                if (Objects.equals(doctorPartWeanDto.getPartWeanPigletsCount(), doctorPartWeanDto.getFarrowingLiveCount()) && doctorPartWeanDto.getChgLocationToBarnId() != null) {
//                    if (MATING_TYPES.contains(doctorBarn.getPigType())) {
//                        doctorSowEventCreateService.sowEventCreate(buildBasicInputInfoDto(farmId, tempPigId, PigEvent.TO_MATING, IsOrNot.YES.getValue()), jsonMapper.toJson(doctorChgLocationDto));
//                    } else {
//                        createCasualChangeLocationInfo(doctorChgLocationDto, buildBasicInputInfoDto(farmId, tempPigId, PigEvent.CHG_LOCATION, IsOrNot.YES.getValue()));
//                    }
//                }
//                //2.分娩后全部死亡触发断奶事件
//            } else if (Objects.equals(eventType, PigEvent.FARROWING.getKey())) {
//                DoctorFarrowingDto farrowingDto = jsonMapper.fromJson(sowInfoDtoJson, DoctorFarrowingDto.class);
//                if (Objects.equals(farrowingDto.getFarrowingLiveCount(), 0)) {
//                    autoCreateWeanEvent(farrowingDto.getFarrowingDate(), farmId, tempPigId);
//                }
//                //3.全部仔猪变动触发断奶事件
//            } else if (Objects.equals(eventType, PigEvent.PIGLETS_CHG.getKey())) {
//                DoctorPigTrack doctorPigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(tempPigId));
//                DoctorPigletsChgDto pigletsChgDto = jsonMapper.fromJson(sowInfoDtoJson, DoctorPigletsChgDto.class);
//                if (Objects.equals(doctorPigTrack.getUnweanQty(), 0)) {
//                    autoCreateWeanEvent(pigletsChgDto.getPigletsChangeDate(), farmId, tempPigId);
//                }
//            }
//          }
        } catch (Exception e) {
            log.error("pig.event.create.failed, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("pig.event.create.fail");
        }
        //return pigId;
    }
//
//    /**
//     * 由其他事件触发的断奶事件
//     * @param weanDate
//     * @param farmId
//     * @param pigId
//     */
//    private void autoCreateWeanEvent(Date weanDate, Long farmId, Long pigId){
//        DoctorPartWeanDto doctorPartWeanDto = DoctorPartWeanDto.builder()
//                .partWeanDate(weanDate)
//                .partWeanPigletsCount(0)
//                .partWeanAvgWeight(0d)
//                .build();
//        String partWeanJson = jsonMapper.toJson(doctorPartWeanDto);
//        doctorSowEventCreateService.sowEventCreate(buildBasicInputInfoDto(farmId, pigId, PigEvent.WEAN, IsOrNot.YES.getValue()), partWeanJson);
//
//    }

    @RequestMapping(value = "/createSowEvents", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createSowEventInfo(@RequestParam("farmId") Long farmId,
                                      @RequestParam("pigIds") String pigIds, @RequestParam("eventType") Integer eventType,
                                      @RequestParam("sowInfoDtoJson") String sowInfoDtoJson) {
        checkEventAt(pigIds, PigEvent.from(eventType), sowInfoDtoJson);
        checkPigIds(pigIds);
        List<BasePigEventInputDto> inputDtos = Splitters.COMMA.splitToList(pigIds).stream().filter(idStr -> !Strings.isNullOrEmpty(idStr))
                .map(idStr -> buildEventInput(eventInput(PigEvent.from(eventType), sowInfoDtoJson, farmId), Long.parseLong(idStr)))
                .collect(Collectors.toList());
        return RespHelper.or500(doctorPigEventWriteService.batchPigEventHandle(inputDtos, buildBasicInputInfoDto(farmId, PigEvent.from(eventType))));
    }



//    @RequestMapping(value = "/createCasualEvents", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    public Boolean createCasualEvents(@RequestParam("farmId") Long farmId,
//                                      @RequestParam("pigIds") String pigIds,
//                                      @RequestParam("pigEvent") Integer pigEvent,
//                                      @RequestParam("doctorRemovalDtoJson") String doctorCasualDtoJson) {
//        checkEventAt(pigIds, PigEvent.from(pigEvent), doctorCasualDtoJson);
//        return RespHelper.or500(doctorSowEventCreateService.casualEventsCreate(
//                buildBasicInputPigDtoContent(farmId, pigIds, PigEvent.from(pigEvent)),
//                doctorCasualDtoJson
//        ));
//    }
//
//    // 猪舍变动 信息
//    private Long createCasualChangeLocationInfo(DoctorChgLocationDto chg, DoctorBasicInputInfoDto basic) {
//        // init pig bran from info
//        DoctorPigTrack doctorPigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(basic.getPigId()));
//        chg.setChgLocationFromBarnId(doctorPigTrack.getCurrentBarnId());
//        chg.setChgLocationFromBarnName(doctorPigTrack.getCurrentBarnName());
//
//        DoctorBarn fromBarn = RespHelper.or500(doctorBarnReadService.findBarnById(chg.getChgLocationFromBarnId()));
//        DoctorBarn toBarn = RespHelper.or500(doctorBarnReadService.findBarnById(chg.getChgLocationToBarnId()));
//
//        //普通转舍事件(同类型, 配种妊娠互转, 产房分娩互转)
//        if (Objects.equals(fromBarn.getPigType(), toBarn.getPigType()) ||
//                (MATING_TYPES.contains(fromBarn.getPigType()) && MATING_TYPES.contains(toBarn.getPigType())) ||
//                (FARROW_TYPES.contains(fromBarn.getPigType()) && FARROW_TYPES.contains(toBarn.getPigType()))) {
//            basic.setEventType(PigEvent.CHG_LOCATION.getKey());
//            return RespHelper.or500(doctorPigEventWriteService.chgLocationEvent(chg, basic));
//        }
//        //状态转换转舍事件
//        return createSowChgLocation(chg, basic, fromBarn, toBarn);
//    }
//
//    //调用状态转换事件: 母猪转舍
//    private Long createSowChgLocation(DoctorChgLocationDto chg, DoctorBasicInputInfoDto basic, DoctorBarn fromBarn, DoctorBarn toBarn) {
////        if (!CHG_SOW_ALLOWS.contains(toBarn.getPigType()) ||
////                !(fromBarn.getPigType() == PREG_SOW.getValue() && FARROW_TYPES.contains(toBarn.getPigType())) || !(Objects.equals(fromBarn.getPigType(), DELIVER_SOW.getValue()) && MATING_FARROW_TYPES.contains(toBarn.getPigType()))) {
////            throw new JsonResponseException(500, "input.sowToBarnId.error");
////        }
//
//        if(!(MATING_FARROW_TYPES.contains(fromBarn.getPigType()) && MATING_FARROW_TYPES.contains(toBarn.getPigType()))) {
//            throw new JsonResponseException(500, "input.sowToBarnId.error");
//        }
//
//        //判断去配种还是去分娩
//        basic.setEventType(Objects.equals(fromBarn.getPigType(), PigType.DELIVER_SOW.getValue()) ? PigEvent.TO_MATING.getKey() : PigEvent.TO_FARROWING.getKey());
//        return RespHelper.or500(doctorPigEventWriteService.chgSowLocationEvent(chg, basic));
//    }
//
//    private List<DoctorBasicInputInfoDto> buildBasicInputPigDtoContent(Long farmId, String pigIds, PigEvent pigEvent) {
//        try {
//            List<Long> pigIdsList = OBJECT_MAPPER.readValue(pigIds, JacksonType.LIST_OF_LONG);
//            return pigIdsList.stream().map(id -> buildBasicInputInfoDto(farmId, id, pigEvent, null)).collect(Collectors.toList());
//        } catch (Exception e) {
//            log.error("build basic input pig dto fail, farmId:{}, pigIds:{}, cause:{}", Throwables.getStackTraceAsString(e));
//            throw new JsonResponseException("pigIds.basicBuilder.fail");
//        }
//    }
//
//    /**
//     * 拼窝事件， 获取被拼窝母猪信息
//     *
//     * @return
//     */
//    private List<DoctorBasicInputInfoDto> fosterInfo(Long farmId, Long pigId, String fosterJson) {
//        try {
//            DoctorBasicInputInfoDto foster = buildBasicInputInfoDto(farmId, pigId, PigEvent.FOSTERS, null);
//            Map<String, Object> dtoData = OBJECT_MAPPER.readValue(fosterJson, JacksonType.MAP_OF_OBJECT);
//            DoctorBasicInputInfoDto fosterBy = buildBasicInputInfoDto(farmId, Long.valueOf(dtoData.get("fosterSowId").toString()), PigEvent.FOSTERS_BY, null);
//            fosterBy.setNeed(!Objects.equals(foster.getBarnId(), fosterBy.getBarnId()));
//
//            return Lists.newArrayList(foster, fosterBy);
//        } catch (Exception e) {
//            log.error("foster data build error fail, cause:{}", Throwables.getStackTraceAsString(e));
//            throw new JsonResponseException("foster.builder.error");
//        }
//    }
//
//    /**
//     * 构建Basic 基础输入数据信息
//     *
//     * @param farmId
//     * @param entryDto
//     * @param pigEvent
//     * @return
//     */
//    private DoctorBasicInputInfoDto buildBasicEntryInputInfo(Long farmId, DoctorFarmEntryDto entryDto, PigEvent pigEvent) {
//        try {
//            DoctorFarm doctorFarm = RespHelper.orServEx(this.doctorFarmReadService.findFarmById(farmId));
//            checkState(!isNull(pigEvent), "input.eventType.error");
//            Long userId = UserUtil.getUserId();
//
//            return DoctorBasicInputInfoDto.builder()
//                    .pigType(entryDto.getPigType()).pigCode(entryDto.getPigCode()).barnId(entryDto.getBarnId()).barnName(entryDto.getBarnName())
//                    .farmId(doctorFarm.getId()).farmName(doctorFarm.getName()).orgId(doctorFarm.getOrgId()).orgName(doctorFarm.getOrgName())
//                    .staffId(userId).staffName(RespHelper.orServEx(doctorGroupWebService.findRealName(userId)))
//                    .eventType(pigEvent.getKey()).eventName(pigEvent.getName()).eventDesc(pigEvent.getDesc())
//                    .build();
//        } catch (IllegalStateException ee) {
//            log.error("illegal state exception error, cause:{}", Throwables.getStackTraceAsString(ee));
//            throw new JsonResponseException(ee.getMessage());
//        } catch (Exception e) {
//            log.error("basic entry info build fail, cause:{}", Throwables.getStackTraceAsString(e));
//            throw new JsonResponseException("build.basicEntry.fail");
//        }
//    }

    @RequestMapping(value = "/batchCreateEvnet", method = RequestMethod.POST)
    public Boolean batchCreatePigEvent(@RequestParam DoctorBatchPigEventDto batchPigEventDto){
        if (Arguments.isNullOrEmpty(batchPigEventDto.getInputJsonList())) {
            return true;
        }
        PigEvent pigEvent = PigEvent.from(batchPigEventDto.getEventType());
        List<BasePigEventInputDto> inputDtoList = batchPigEventDto.getInputJsonList()
                .stream().map(inputJson -> {
                    BasePigEventInputDto inputDto = eventInput(pigEvent, inputJson, batchPigEventDto.getFarmId());
                    return buildEventInput(inputDto, inputDto.getPigId());
                }).collect(Collectors.toList());
        return RespHelper.or500(doctorPigEventWriteService.batchPigEventHandle(inputDtoList, buildBasicInputInfoDto(batchPigEventDto.getFarmId(), pigEvent)));
    }
    /**
     * 通过Id 获取对应的事件信息
     *
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
//            Long relPigEventId = null;
//            if (Objects.equals(isAuto, IsOrNot.YES.getValue())) {
//                DoctorPigEvent doctorPigEvent = RespHelper.or500(doctorPigEventReadService.lastEvent(pigId));
//                if (!isNull(doctorPigEvent)) {
//                    relPigEventId = doctorPigEvent.getId();
//                }
//            }
            return DoctorBasicInputInfoDto.builder()
                    //.pigId(pigDto.getId()).pigCode(pigDto.getPigCode()).pigType(pigDto.getPigType()).barnId(pigDto.getBarnId()).barnName(pigDto.getBarnName())
                    .farmId(doctorFarm.getId()).farmName(doctorFarm.getName()).orgId(doctorFarm.getOrgId()).orgName(doctorFarm.getOrgName())
                    .staffId(userId).staffName(RespHelper.orServEx(doctorGroupWebService.findRealName(userId)))
                    .eventType(pigEvent.getKey()).eventName(pigEvent.getName()).eventDesc(pigEvent.getDesc())
                    //.isAuto(isAuto)
                    //.relPigEventId(relPigEventId)
                    .build();
        } catch (Exception e) {
            log.error("build basic input info dto fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("build.basicInputInfo.error");
        }
    }


    private BasePigEventInputDto buildEventInput(BasePigEventInputDto inputDto, Long pigId){
        DoctorPigInfoDto pigDto = RespHelper.orServEx(this.doctorPigReadService.queryDoctorInfoDtoById(pigId));
        inputDto.setIsAuto(IsOrNot.NO.getValue());
        inputDto.setPigId(pigId);
        inputDto.setPigCode(pigDto.getPigCode());
        inputDto.setPigType(pigDto.getPigType());
        inputDto.setBarnId(pigDto.getBarnId());
        inputDto.setBarnName(pigDto.getBarnName());
        return inputDto;
    }
    /**
     * 猪事件时间限制
     *
     * @param pigid
     * @param eventType
     * @param json
     */
    private void checkEventAt(Long pigid, PigEvent eventType, String json) {
        checkEventAt(pigid.toString(), eventType, json);
    }
    /**
     * 批量猪事件时间限制
     *
     * @param pigIds
     * @param eventType
     * @return
     */
    private void checkEventAt(String ids, PigEvent eventType, String json) {
        if (Objects.equals(eventType, PigEvent.ENTRY.getKey())) {
            return;
        }
        List<Long> pigIds = Splitters.COMMA.splitToList(ids).stream().map(id -> Long.parseLong(id)).collect(Collectors.toList());
        String key = null;
        switch (eventType) {
            case PIGLETS_CHG:
                key = "pigletsChangeDate";
                break;
            case PREG_CHECK:
                key = "checkDate";
                break;
            case CHG_FARM:
                key = "chgFarmDate";
                break;
            case CHG_LOCATION:
            case TO_MATING:
            case TO_FARROWING:
                key = "changeLocationDate";
                break;
            case VACCINATION:
                key = "vaccinationDate";
                break;
            case WEAN:
                key = "partWeanDate";
                break;
            case SEMEN:
                key = "semenDate";
                break;
            case MATING:
                key = "matingDate";
                break;
            case CONDITION:
                key = "conditionDate";
                break;
            case DISEASE:
                key = "diseaseDate";
                break;
            case REMOVAL:
                key = "removalDate";
                break;
            case FOSTERS:
            case FOSTERS_BY:
                key = "fostersDate";
                break;
            case FARROWING:
                key = "farrowingDate";
                break;
            default:
                break;
        }
        try {
            Map<String, Object> map = OBJECT_MAPPER.readValue(json, JacksonType.MAP_OF_OBJECT);
            String eventAtInMap = (String) map.get(key);
            if(eventAtInMap == null && PigEvent.CONDITION == eventType){
                eventAtInMap = (String) map.get("checkAt");
            }
            Date eventAt = DateUtil.toDate(eventAtInMap);
            if(eventAt == null){
                throw new JsonResponseException("event.at.illegal");
            }
            DoctorPigEvent lastEvent = RespHelper.or500(doctorPigEventReadService.lastEvent(pigIds));
            if (lastEvent != null) {
                if (new DateTime(eventAt).plusDays(1).isAfter(lastEvent.getEventAt().getTime()) && eventAt.before(DateUtil.toDate(DateTime.now().plusDays(1).toString(DateTimeFormat.forPattern("yyyy-MM-dd"))))) {
                    return;
                } else {
                    throw new JsonResponseException("event.at.illegal");
                }
            }
        } catch (Exception e) {
            throw new JsonResponseException("event.at.illegal");
        }
    }

    /**
     * 事件列表(在能够导致断奶的事件之后添加断奶事件, 暂时)
     *
     * @return
     */
    @RequestMapping(value = "/addWeanEvent", method = RequestMethod.GET)
    @ResponseBody
    public Boolean addWeanEventAfterFosAndPigLets() {
        try {
            Map<String, String> map = Maps.newHashMap();
            map.put("#", "#");
            String extra = OBJECT_MAPPER.writeValueAsString(map);
            List<DoctorPigEvent> events = RespHelper.or500(doctorPigEventReadService.addWeanEventAfterFosAndPigLets());
            events.forEach(doctorPigEvent -> {
                DoctorPigEvent weanEvent = new DoctorPigEvent();
                BeanMapper.copy(doctorPigEvent, weanEvent);
                weanEvent.setType(PigEvent.WEAN.getKey());
                weanEvent.setName(PigEvent.WEAN.getName());
                weanEvent.setRelPigEventId(doctorPigEvent.getId());
                weanEvent.setPartweanDate(doctorPigEvent.getEventAt());
                weanEvent.setExtra(extra);
                weanEvent.setIsAuto(IsOrNot.YES.getValue());
                doctorPigEventWriteService.createPigEvent(weanEvent);
            });
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("add.failed, cause{}", Throwables.getStackTraceAsString(e));
            return Boolean.FALSE;
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

    private BasePigEventInputDto eventInput(PigEvent pigEvent, String eventInfoDtoJson, Long farmId) {
            switch (pigEvent) {
                case ENTRY:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorFarmEntryDto.class);
                case CHG_FARM:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorChgFarmDto.class);
                case CHG_LOCATION:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorChgLocationDto.class);
                case CONDITION:
                    return jsonMapper.fromJson(eventInfoDtoJson, DoctorConditionDto.class);
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
