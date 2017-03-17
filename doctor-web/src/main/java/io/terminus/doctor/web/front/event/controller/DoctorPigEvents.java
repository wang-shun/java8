package io.terminus.doctor.web.front.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.DoctorSowParityAvgDto;
import io.terminus.doctor.event.dto.DoctorSowParityCount;
import io.terminus.doctor.event.dto.event.DoctorEventOperator;
import io.terminus.doctor.event.dto.event.boar.DoctorBoarConditionDto;
import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
import io.terminus.doctor.event.dto.event.group.DoctorAntiepidemicGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorChangeGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorDiseaseGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorNewGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorTransFarmGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorTransGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorTurnSeedGroupEvent;
import io.terminus.doctor.event.dto.event.sow.*;
import io.terminus.doctor.event.dto.event.usual.*;
import io.terminus.doctor.event.enums.*;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorPigEventWriteService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.web.core.export.Exporter;
import io.terminus.doctor.web.front.event.dto.*;
import io.terminus.doctor.web.util.TransFromUtil;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notNull;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

/**
 * Created by yaoqijun.
 * Date:2016-05-26
 * Email:yaoqj@terminus.io
 * Descirbe: 公猪， 母猪事件信息列表
 */
@Slf4j
@Controller
@RequestMapping("/api/doctor/events/pig")
public class DoctorPigEvents {

    private final DoctorPigReadService doctorPigReadService;

    private final DoctorPigEventReadService doctorPigEventReadService;

    private final DoctorPigEventWriteService doctorPigEventWriteService;

    private final UserReadService userReadService;

    private final DoctorGroupReadService doctorGroupReadService;

    private final TransFromUtil transFromUtil;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();
    private static final JsonMapperUtil JSON_MAPPER  = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER;

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Autowired
    private Exporter exporter;

    @Autowired
    public DoctorPigEvents(DoctorPigReadService doctorPigReadService,
                           DoctorPigEventReadService doctorPigEventReadService,
                           DoctorPigEventWriteService doctorPigEventWriteService,
                           UserReadService userReadService,
                           DoctorGroupReadService doctorGroupReadService, TransFromUtil transFromUtil) {
        this.doctorPigReadService = doctorPigReadService;
        this.doctorPigEventReadService = doctorPigEventReadService;
        this.doctorPigEventWriteService = doctorPigEventWriteService;
        this.userReadService = userReadService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.transFromUtil = transFromUtil;
    }

    @RequestMapping(value = "/pagingDoctorInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<DoctorPigInfoDto> pagingPigDoctorInfoByBarn(@RequestParam("farmId") Long farmId,
                                                              @RequestParam("barnId") Long branId,
                                                              @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                              @RequestParam(value = "pageSize", required = false) Integer pageSize) {

        return RespHelper.or500(doctorPigReadService.pagingDoctorInfoDtoByPigTrack(DoctorPigTrack.builder()
                .farmId(farmId).currentBarnId(branId).build(), pageNo, pageSize));
    }

    @RequestMapping(value = "/pagingPigEvent", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<DoctorPigEvent> pagingDoctorPigEvent(@RequestParam("farmId") Long farmId,
                                                       @RequestParam("pigId") Long pigId,
                                                       @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                       @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                       @RequestParam(value = "startDate", required = false) String startDate,
                                                       @RequestParam(value = "endDate", required = false) String endDate) {
        try {
            Date beginDateTime = Strings.isNullOrEmpty(startDate) ? null : DTF.parseDateTime(startDate).withTimeAtStartOfDay().toDate();
            Date endDateTime = Strings.isNullOrEmpty(endDate) ? null : (DTF).parseDateTime(endDate).plusDays(1).withTimeAtStartOfDay().toDate(); // 添加一天

            Paging<DoctorPigEvent> doctorPigEventPaging = RespHelper.or500(doctorPigEventReadService.queryPigDoctorEvents(farmId, pigId, pageNo, pageSize, beginDateTime, endDateTime));
            transFromUtil.transFromExtraMap(doctorPigEventPaging.getData());
            return doctorPigEventPaging;
        } catch (Exception e) {
            log.error("pig event paging error, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, "paging.pigEvent.error");
        }
    }

    @RequestMapping(value = "/pagingRollbackPigEvent", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorPigEventPagingDto pagingPigEventWithRollback(@RequestParam("farmId") Long farmId,
                                                              @RequestParam("pigId") Long pigId,
                                                              @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                              @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                              @RequestParam(value = "startDate", required = false) String startDate,
                                                              @RequestParam(value = "endDate", required = false) String endDate) {
        Paging<DoctorPigEvent> doctorPigEventPaging = pagingDoctorPigEvent(farmId, pigId, pageNo, pageSize, startDate, endDate);
        Response<DoctorPigEvent> pigEventResponse = doctorPigEventReadService.canRollbackEvent(pigId);
        Long canRollback = null;
        if (pigEventResponse.isSuccess() && notNull(pigEventResponse.getResult())) {
            canRollback = pigEventResponse.getResult().getId();
        }
        return DoctorPigEventPagingDto.builder().paging(doctorPigEventPaging).canRollback(canRollback).build();
    }

    /**
     * 查找一只猪(指定时间点之后)的第一个事件
     *
     * @param pigId
     * @param startDate
     * @return
     */
    @RequestMapping(value = "/findFirstPigEvent", method = RequestMethod.GET)
    @ResponseBody
    public DoctorPigEvent findFirstPigEvent(@RequestParam("pigId") Long pigId,
                                            @RequestParam(value = "startDate", required = false) String startDate) {
        Date beginDateTime = Strings.isNullOrEmpty(startDate) ? null : DTF.parseDateTime(startDate).withTimeAtStartOfDay().toDate();
        return RespHelper.or500(doctorPigEventReadService.findFirstPigEvent(pigId, beginDateTime));
    }

    @RequestMapping(value = "/queryPigEventById", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorPigEvent queryPigEventById(@RequestParam("eventId") Long eventId) {
        return RespHelper.or500(doctorPigEventReadService.queryPigEventById(eventId));
    }


    @RequestMapping(value = "/queryPigEvents", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Integer> queryPigExecuteEvent(@RequestParam("ids") String ids) {
        List<Long> pigIds = null;
        try {
            pigIds = OBJECT_MAPPER.readValue(ids, JacksonType.LIST_OF_LONG);
        } catch (Exception e) {
            log.error("query pig execute event error, ids:{} cause:{}", ids, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("query.executeEvent.fail");
        }
        return RespHelper.or500(doctorPigEventReadService.queryPigEvents(pigIds));
    }

    /**
     * 母猪胎次查询
     * @param pigId 母猪id
     * @return 胎次信息列表
     */
    @RequestMapping(value = "/queryDoctorSowParityCount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DoctorSowParityCount> queryDoctorSowParityCount(@RequestParam("pigId") Long pigId) {
        return RespHelper.or500(doctorPigEventReadService.querySowParityCount(pigId));
    }

    /**
     * 查询母猪胎次中数据平均值
     * @param pigId
     * @return
     */
    @RequestMapping(value = "/querySowParityAvg", method = RequestMethod.GET)
    @ResponseBody
    public DoctorSowParityAvgDto querySowParityAvg(Long pigId) {
        return RespHelper.or500(doctorPigEventReadService.querySowParityAvg(pigId));
    }

    /**
     * 分页查询某一类型的猪事件
     *
     * @param params
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/pigPaging", method = RequestMethod.GET)
    @ResponseBody
    public Paging<DoctorPigEventDetail> queryPigEventsByCriteria(@RequestParam Map<String, Object> params, @RequestParam(required = false) Integer pageNo, @RequestParam(required = false) Integer pageSize) {
        if (params == null || params.isEmpty()) {
            return Paging.empty();
        }
        params = Params.filterNullOrEmpty(params);
        if (params.get("eventTypes") != null) {
            params.put("types", Splitters.COMMA.splitToList((String) params.get("eventTypes")));
            params.remove("eventTypes");
        }
        if (StringUtils.isNotBlank((String) params.get("endDate"))) {
            params.put("endDate", new DateTime(params.get("endDate")).plusDays(1).minusMillis(1).toDate());
        }
        Response<Paging<DoctorPigEvent>> pigEventPagingResponse = doctorPigEventReadService.queryPigEventsByCriteria(params, pageNo, pageSize);
        if (!pigEventPagingResponse.isSuccess()) {
            return Paging.empty();
        }
        List<DoctorPigEventDetail> pigEventDetailList = pigEventPagingResponse.getResult().getData().stream()
                .map(doctorPigEvent -> {
                        Map<String, Object> extraMap = MoreObjects.firstNonNull(doctorPigEvent.getExtraMap(), Maps.newHashMap());
                        if (Objects.equals(doctorPigEvent.getType(), PigEvent.MATING.getKey()) && doctorPigEvent.getExtraMap().containsKey("matingType")) {
                            extraMap.put("matingType", MatingType.from((Integer) extraMap.get("matingType")).getDesc());
                        }
                        if (Objects.equals(doctorPigEvent.getType(), PigEvent.PREG_CHECK.getKey()) && doctorPigEvent.getPregCheckResult() != null) {
                            extraMap.put("checkResult", PregCheckResult.from(doctorPigEvent.getPregCheckResult()).getDesc());
                        }
                        doctorPigEvent.setExtraMap(extraMap);
                        DoctorPigEventDetail detail = OBJECT_MAPPER.convertValue(doctorPigEvent, DoctorPigEventDetail.class);

                        Boolean isRollback = false;
                        Response<Boolean> booleanResponse = doctorPigEventReadService.eventCanRollback(doctorPigEvent.getId());
                        if (booleanResponse.isSuccess()) {
                            isRollback = booleanResponse.getResult();
                        }
                        detail.setIsRollback(isRollback);
                        return detail;
                }).collect(toList());
        return new Paging<>(pigEventPagingResponse.getResult().getTotal(), pigEventDetailList);
    }


    //针对公猪、母猪、猪群pagingEvent事件集成
    @RequestMapping(value = "/queryEvents", method = RequestMethod.GET)
    @ResponseBody
    public Object queryEventsByCriteria(@RequestParam Map<String, Object> params, @RequestParam(required = false) Integer pageNo, @RequestParam(required = false) Integer pageSize) {
        if (params == null || params.isEmpty() ) {
            return Paging.empty();
        }
        if (params.get("kind")==null||"".equals(params.get("kind"))){
            params.put("kind",1);
        }
        //针对  kind进行识别
        String kind = String.valueOf(params.get("kind"));
        Object result = null;
        switch (kind) {
            case "4":
                //猪群查询事件
                result = this.queryGroupEventsByCriteria(params, pageNo, pageSize);
                break;
            case "1":
                //母猪
                params.put("ordered",0);
                result = this.queryPigEventsByCriteria(params, pageNo, pageSize);
                break;
            //公猪
            case "2":
                params.put("ordered",0);
                result = this.queryPigEventsByCriteria(params, pageNo, pageSize);
                break;
            case "3":
                params.put("ordered",0);
                result = this.queryPigEventsByCriteria(params, pageNo, pageSize);
                break;
            default:
                result = Paging.empty();
                break;
        }
        return result;
    }


    /**
     * 获取相应的猪类型事件列表
     *
     * @param types
     * @return
     * @see PigEvent
     */
    @RequestMapping(value = "/pigEvents", method = RequestMethod.GET)
    @ResponseBody
    public List<String> queryPigEvents(@RequestParam String types) {
        List<PigEvent> events = PigEvent.from(Splitters.UNDERSCORE.splitToList(types).stream().filter(type -> StringUtils.isNotBlank(type)).map(type -> Integer.parseInt(type)).collect(Collectors.toList()));
        return events.stream().map(pigEvent -> pigEvent.getDesc()).collect(Collectors.toList());
    }

    /**
     * 获取相应的猪类型事件列表<K,V>形式
     *
     * @param types
     * @return
     * @see PigEvent
     */
    @RequestMapping(value = "/getPigEvents", method = RequestMethod.GET)
    @ResponseBody
    public List<ImmutableMap<String, Object>> getPigEvents(@RequestParam String types) {
        List<PigEvent> events = PigEvent.from(Splitters.UNDERSCORE.splitToList(types).stream().filter(type -> StringUtils.isNotBlank(type)).map(type -> Integer.parseInt(type)).collect(toList()));
        List<ImmutableMap<String, Object>> list = Lists.newArrayList();
        for (PigEvent p : events) {
            list.add(ImmutableMap.of("id", p.getKey(), "name", p.getDesc()));
        }
        return list;
    }


    /**
     * 获取拥有事件的操作人列表
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/event/operators", method = RequestMethod.GET)
    @ResponseBody
    public List<DoctorEventOperator> queryOperatorForEvent(@RequestParam Map<String, Object> params) {
        params = Params.filterNullOrEmpty(params);
        if (params.get("eventTypes") != null) {
            params.put("types", Splitters.COMMA.splitToList((String) params.get("eventTypes")));
            params.remove("eventTypes");
        }
        if (Objects.equals(params.get("kind"), "4")) {
            return RespHelper.or500(doctorGroupReadService.queryOperators(params));
        } else {
            return RespHelper.or500(doctorPigEventReadService.queryOperators(params));
        }
    }

    private Paging<DoctorGroupEventDetail> queryGroupEventsByCriteria(Map<String, Object> params, Integer pageNo, Integer pageSize) {
        if (params == null || params.isEmpty()) {
            return Paging.empty();
        }
        params = Params.filterNullOrEmpty(params);
        if (params.get("eventTypes") != null) {
            params.put("types", Splitters.COMMA.splitToList((String) params.get("eventTypes")));
            params.remove("eventTypes");
        }
        if (StringUtils.isNotBlank((String) params.get("endDate"))) {
            params.put("endDate", new DateTime(params.get("endDate")).plusDays(1).minusMillis(1).toDate());
        }
        Response<Paging<DoctorGroupEvent>> pagingResponse = doctorGroupReadService.queryGroupEventsByCriteria(params, pageNo, pageSize);
        if (!pagingResponse.isSuccess()) {
            return Paging.empty();
        }
        List<DoctorGroupEventDetail> groupEventDetailList = pagingResponse.getResult().getData().stream()
                .map(doctorGroupEvent -> {
                    DoctorGroupEventDetail detail = OBJECT_MAPPER.convertValue(doctorGroupEvent, DoctorGroupEventDetail.class);
                    Boolean isRollback = false;
                    Response<Boolean> booleanResponse = doctorGroupReadService.eventCanRollback(doctorGroupEvent.getId());
                    if (booleanResponse.isSuccess()) {
                        isRollback = booleanResponse.getResult();
                    }
                    detail.setIsRollback(isRollback);
                    return detail;
                }).collect(toList());
        return new Paging<>(pagingResponse.getResult().getTotal(), groupEventDetailList);
    }

    /**
     * 事件导出
     * @param eventCriteria 查询条件
     * @param request HttpRequest
     * @param response HttpResponse
     */
    @RequestMapping(value = "/eventExport", method = RequestMethod.GET)
    public void pigEventExport(@RequestParam Map<String, String> eventCriteria, HttpServletRequest request, HttpServletResponse response){
        try {
            log.info("event.export.starting");
            if (Strings.isNullOrEmpty(eventCriteria.get("kind"))) {
                return;
            }
            if (Objects.equals(eventCriteria.get("kind"), "4")) {
                exporter.export("web-group-event", eventCriteria, 1, 500, this::pagingGroupEvent, request, response);
            } else {
                eventCriteria.put("ordered","0");
                exporter.export("web-pig-event", eventCriteria, 1, 500, this::pagingPigEvent, request, response);
            }
            log.info("event.export.ending");
        } catch (Exception e) {
            log.error("event.export.failed");
        }
    }

    /**
     * 分页猪事件
     * @param pigEventCriteria 查询猪事件条件
     * @return 分页导出猪事件数据
     */
    private Paging<DoctorPigEventExportData> pagingPigEvent(Map<String, String> pigEventCriteria) {
        Map<String, Object> criteriaMap = OBJECT_MAPPER.convertValue(pigEventCriteria, Map.class);
        Paging<DoctorPigEventDetail> pigEventPaging = queryPigEventsByCriteria(criteriaMap, Integer.parseInt(pigEventCriteria.get("pageNo")), Integer.parseInt(pigEventCriteria.get("size")));
        List<DoctorPigEventExportData> list = pigEventPaging.getData()
                .stream().map(doctorPigEventDetail -> OBJECT_MAPPER.convertValue(doctorPigEventDetail, DoctorPigEventExportData.class)).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }

    /**
     * 分页猪群事件
     * @param groupEventCriteria 查询猪群事件条件
     * @return 分页导出猪群事件数据
     */
    private Paging<DoctorGroupEventExportData> pagingGroupEvent(Map<String, String> groupEventCriteria) {
        Map<String, Object> criteriaMap = OBJECT_MAPPER.convertValue(groupEventCriteria, Map.class);
        Paging<DoctorGroupEventDetail> groupEventPaging = queryGroupEventsByCriteria(criteriaMap, Integer.parseInt(groupEventCriteria.get("pageNo")), Integer.parseInt(groupEventCriteria.get("size")));
        List<DoctorGroupEventExportData> list = groupEventPaging.getData()
                .stream().map(doctorGroupEventDetail -> OBJECT_MAPPER.convertValue(doctorGroupEventDetail, DoctorGroupEventExportData.class)).collect(toList());
        return new Paging<>(groupEventPaging.getTotal(), list);
    }

    /**
     * 猪入场事件的导出报表的构建
     */
    public Paging<DoctorPigBoarInFarmExportDto> pagingInFarmExport(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPigBoarInFarmExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorFarmEntryDto farmEntryDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorFarmEntryDto.class);
                DoctorPigBoarInFarmExportDto dto = OBJECT_MAPPER.convertValue(farmEntryDto, DoctorPigBoarInFarmExportDto.class);
                dto.setSourceName(notNull(dto.getSource()) ? null : PigSource.from(dto.getSource()).getDesc());
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setParity(doctorPigEventDetail.getParity());
                dto.setInitBarnName(doctorPigEventDetail.getBarnName());
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                return dto;
            }catch (Exception e){
                log.error("pagingInFarmExport error: {}", Throwables.getStackTraceAsString(e));
            }
            return new DoctorPigBoarInFarmExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 公猪采精事件事件的导出报表构建
     */
    public Paging<DoctorPigSemenExportDto> pagingSemenExport(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPigSemenExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorSemenDto semenDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorSemenDto.class);
                DoctorPigSemenExportDto dto = OBJECT_MAPPER.convertValue(semenDto, DoctorPigSemenExportDto.class);
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                return dto;
            }catch (Exception e){
                log.error("pagingSemenExport error");
            }
            return new DoctorPigSemenExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 转舍事件
     */
    public Paging<DoctorPigChangeBarnExportDto> pagingChangeBarn(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPigChangeBarnExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorChgLocationDto conditionDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorChgLocationDto.class);
                DoctorPigChangeBarnExportDto dto = OBJECT_MAPPER.convertValue(conditionDto, DoctorPigChangeBarnExportDto.class);
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                dto.setParity(doctorPigEventDetail.getParity());
                dto.setRemark(doctorPigEventDetail.getRemark());
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                if (doctorPigEventDetail.getPigStatusAfter() != null) {
                    dto.setPigStatusAfterName(PigStatus.from(doctorPigEventDetail.getPigStatusAfter()).getDesc());
                }
                return dto;
            }catch (Exception e){
                log.error("pagingChangeBarn error");
            }
            return new DoctorPigChangeBarnExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 公猪的疾病事件
     */
    public Paging<DoctorPigDiseaseExportDto> pagingDisease(Map<String, String> pigEventCriteria){
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPigDiseaseExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorDiseaseDto diseaseDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorDiseaseDto.class);
                DoctorPigDiseaseExportDto dto = OBJECT_MAPPER.convertValue(diseaseDto, DoctorPigDiseaseExportDto.class);
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                dto.setParity(doctorPigEventDetail.getParity());
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                return dto;
            }catch (Exception e){
                log.error("pagingDisease error");
            }
            return new DoctorPigDiseaseExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }

    /**
     * 猪的防疫事件报表模板
     */
    public Paging<DoctorPigVaccinationExportDto> pagingVaccination(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPigVaccinationExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorVaccinationDto vaccinationDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorVaccinationDto.class);
                DoctorPigVaccinationExportDto dto = OBJECT_MAPPER.convertValue(vaccinationDto, DoctorPigVaccinationExportDto.class);
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                return dto;
            }catch (Exception e){
                log.error("pagingVaccination error");
            }
            return new DoctorPigVaccinationExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 猪的离场事件
     */
    public Paging<DoctorPigRemoveExportDto> pagingRemove(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPigRemoveExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorRemovalDto removalDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorRemovalDto.class);
                DoctorPigRemoveExportDto dto = OBJECT_MAPPER.convertValue(removalDto, DoctorPigRemoveExportDto.class);
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setAmount(doctorPigEventDetail.getAmount());
                if (dto.getPrice() != null) {
                    dto.setPrice(dto.getPrice() / 100);
                }
                dto.setParity(doctorPigEventDetail.getParity());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                dto.setOperatorName(doctorPigEventDetail.getOperatorName());
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                return dto;
            }catch(Exception e){
                log.error("pagingRemove error");
            }
            return new DoctorPigRemoveExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 猪的配种事件
     */
    public Paging<DoctorPigMatingExportDto> pagingMating(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPigMatingExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorMatingDto matingDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorMatingDto.class);
                DoctorPigMatingExportDto dto = OBJECT_MAPPER.convertValue(matingDto, DoctorPigMatingExportDto.class);
                dto.setParity(doctorPigEventDetail.getParity());
                if (dto.getMatingType() != null) {
                    dto.setMatingTypeName(MatingType.from(dto.getMatingType()).getDesc());
                }
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                if (doctorPigEventDetail.getPigStatusAfter() != null) {
                    dto.setPigStatusAfterName(PigStatus.from(doctorPigEventDetail.getPigStatusAfter()).getDesc());
                }
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setOperatorName(doctorPigEventDetail.getOperatorName());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                return dto;
            }catch (Exception e){
                log.error("pagingMating error");
            }
            return new DoctorPigMatingExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }

    /**
     * 仔猪变动事件
     */
    public Paging<DoctorPigletsChgExportDto> pagingLetsChg(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPigletsChgExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorPigletsChgDto matingDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorPigletsChgDto.class);
                DoctorPigletsChgExportDto dto = OBJECT_MAPPER.convertValue(matingDto, DoctorPigletsChgExportDto.class);
                dto.setParity(doctorPigEventDetail.getParity());
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                return dto;
            }catch (Exception e){
                log.error("pagingLetsChg error");
            }
            return new DoctorPigletsChgExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 断奶事件
     */
    public Paging<DoctorWeanExportDto> pagingWean(Map<String, String> pigEventCriteria) {

        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorWeanExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorWeanDto weanDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorWeanDto.class);
                DoctorWeanExportDto dto = OBJECT_MAPPER.convertValue(weanDto, DoctorWeanExportDto.class);
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setParity(doctorPigEventDetail.getParity());
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                dto.setOperatorName(doctorPigEventDetail.getOperatorName());
                return dto;
            }catch (Exception e){
                log.error("pagingWean error :{} faile "+ Throwables.getStackTraceAsString(e));
            }
            return new DoctorWeanExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     *检查
     */
    public Paging<DoctorPregChkResultExportDto> pagingPregChkResult(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPregChkResultExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorPregChkResultDto pregChkResultDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorPregChkResultDto.class);
                DoctorPregChkResultExportDto dto = OBJECT_MAPPER.convertValue(pregChkResultDto, DoctorPregChkResultExportDto.class);

                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setParity(doctorPigEventDetail.getParity());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                dto.setOperatorName(doctorPigEventDetail.getOperatorName());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                if (doctorPigEventDetail.getPregCheckResult() != null) {
                    dto.setCheckResultName(PregCheckResult.from(doctorPigEventDetail.getPregCheckResult()).getDesc());
                }
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                return dto;
            }catch (Exception e){
                log.error("pagingPregChkResult error");
            }
            return new DoctorPregChkResultExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);

    }
    /**
     *分娩
     */
    public Paging<DoctorFarrowingExportDto> pagingFarrowing( Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorFarrowingExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorFarrowingDto farrowingDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorFarrowingDto.class);
                DoctorFarrowingExportDto dto = OBJECT_MAPPER.convertValue(farrowingDto, DoctorFarrowingExportDto.class);
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setParity(doctorPigEventDetail.getParity());
                if (dto.getFarrowingType() != null) {
                    dto.setFarrowingTypeName(FarrowingType.from(dto.getFarrowingType()).getDesc());
                }
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                return dto;
            }catch (Exception e){
                log.error("pagingFarrowing error, cause:{}", Throwables.getStackTraceAsString(e));
            }
            return new DoctorFarrowingExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 拼窝
     */
    public Paging<DoctorFostersExportDto> pagingFosters(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorFostersExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorFostersDto fostersDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorFostersDto.class);
                DoctorFostersExportDto dto = OBJECT_MAPPER.convertValue(fostersDto, DoctorFostersExportDto.class);
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setParity(doctorPigEventDetail.getParity());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                dto.setOperatorName(doctorPigEventDetail.getOperatorName());
                return dto;
            }catch (Exception e){
                log.error("pagingFosters error :{} fail",Throwables.getStackTraceAsString(e));
            }
            return new DoctorFostersExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 公猪体况
     */
    public Paging<DoctorBoarConditionExportDto> pagingBoarCondition(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorBoarConditionExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorBoarConditionDto boarConditionDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorBoarConditionDto.class);
                DoctorBoarConditionExportDto dto = OBJECT_MAPPER.convertValue(boarConditionDto, DoctorBoarConditionExportDto.class);
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                dto.setOperatorName(doctorPigEventDetail.getOperatorName());
                return dto;
            }catch (Exception e){
                log.error("pagingBoarCondition error");
            }
            return new DoctorBoarConditionExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 母猪体况
     */
    public Paging<DoctorSowConditionExportDto> pagingsowCondition(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorSowConditionExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorConditionDto boarConditionDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorConditionDto.class);
                DoctorSowConditionExportDto dto = OBJECT_MAPPER.convertValue(boarConditionDto, DoctorSowConditionExportDto.class);
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                dto.setOperatorName(doctorPigEventDetail.getOperatorName());
                return dto;
            }catch (Exception e){
                log.error("pagingBoarCondition error");
            }
            return new DoctorSowConditionExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 猪的转场事件
     */
    public Paging<DoctorChgFarmExportDto> pagingChgFarm(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorChgFarmExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorChgFarmDto chgFarmDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorChgFarmDto.class);
                DoctorChgFarmExportDto dto = OBJECT_MAPPER.convertValue(chgFarmDto, DoctorChgFarmExportDto.class);
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setOperatorName(doctorPigEventDetail.getOperatorName());
                return dto;
            }catch (Exception e){
                log.error("pagingChgFarm error");
            }
            return new DoctorChgFarmExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }

    /**
     * 新建猪
     */
    public Paging<DoctorNewExportGroup> pagingNewGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorNewExportGroup> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            try {
                DoctorNewGroupEvent newGroupEvent = JSON_MAPPER.fromJson(doctorGroupEventDetail.getExtra(), DoctorNewGroupEvent.class);
                DoctorNewExportGroup exportData = BeanMapper.map(newGroupEvent, DoctorNewExportGroup.class);
                DoctorGroup group = RespHelper.or500(doctorGroupReadService.findGroupById(doctorGroupEventDetail.getGroupId()));
                exportData.setPigTypeName(PigType.from(doctorGroupEventDetail.getPigType()).getDesc());
                exportData.setBreedName(group.getBreedName());
                exportData.setStatus(DoctorGroup.Status.from(group.getStatus()).getDesc());
                exportData.setGroupCode(doctorGroupEventDetail.getGroupCode());
                exportData.setBarnName(doctorGroupEventDetail.getBarnName());
                exportData.setEventAt(doctorGroupEventDetail.getEventAt());
                exportData.setRemark(doctorGroupEventDetail.getRemark());
                exportData.setCreatorName(doctorGroupEventDetail.getCreatorName());
                return exportData;
            } catch (Exception e) {
                log.info("get.group.new.failed, eventId:{}", doctorGroupEventDetail.getId());
            }
            return new DoctorNewExportGroup();
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }

    public Paging<DoctorMoveInGroupExportDto> pagingMoveInGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorMoveInGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            try {
                DoctorMoveInGroupEvent moveInGroupEvent = JSON_MAPPER.fromJson(doctorGroupEventDetail.getExtra(), DoctorMoveInGroupEvent.class);
                DoctorMoveInGroupExportDto exportData = BeanMapper.map(moveInGroupEvent, DoctorMoveInGroupExportDto.class);
                exportData.setQuantity(doctorGroupEventDetail.getQuantity());
                exportData.setAvgDayAge(doctorGroupEventDetail.getAvgDayAge());
                exportData.setAvgWeight(doctorGroupEventDetail.getAvgWeight());
                exportData.setAmount(doctorGroupEventDetail.getAmount());
                exportData.setWeight(doctorGroupEventDetail.getWeight());
                exportData.setEventAt(doctorGroupEventDetail.getEventAt());
                exportData.setGroupCode(doctorGroupEventDetail.getGroupCode());
                exportData.setBarnName(doctorGroupEventDetail.getBarnName());
                exportData.setRemark(doctorGroupEventDetail.getRemark());
                exportData.setCreatorName(doctorGroupEventDetail.getCreatorName());
                exportData.setSource(isNull(moveInGroupEvent.getSource()) ? null : PigSource.from(moveInGroupEvent.getSource()).getDesc());
                exportData.setSex(isNull(moveInGroupEvent.getSex()) ? null : DoctorGroupTrack.Sex.from(moveInGroupEvent.getSex()).getDesc());
                return exportData;
            } catch (Exception e) {
                log.info("get.group.MoveIn.failed, eventId:{}", doctorGroupEventDetail.getId());
            }
            return new DoctorMoveInGroupExportDto();
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }

    public Paging<DoctorChangeGroupExportDto> pagingChangeGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorChangeGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            try {
                DoctorChangeGroupEvent changeGroupEvent = JSON_MAPPER.fromJson(doctorGroupEventDetail.getExtra(), DoctorChangeGroupEvent.class);
                DoctorChangeGroupExportDto exportData = BeanMapper.map(changeGroupEvent, DoctorChangeGroupExportDto.class);
                exportData.setGroupCode(doctorGroupEventDetail.getGroupCode());
                exportData.setBarnName(doctorGroupEventDetail.getBarnName());
                exportData.setQuantity(doctorGroupEventDetail.getQuantity());
                exportData.setEventAt(doctorGroupEventDetail.getEventAt());
                exportData.setRemark(doctorGroupEventDetail.getRemark());
                exportData.setCreatorName(doctorGroupEventDetail.getCreatorName());
                return exportData;
            } catch (Exception e) {
                log.info("get.group.change.failed, eventId:{}", doctorGroupEventDetail.getId());
            }
            return new DoctorChangeGroupExportDto();
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }

    public Paging<DoctorChgFarmGroupExportDto> pagingChgFramGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorChgFarmGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            try {
                DoctorTransFarmGroupEvent transFarmGroupEvent = JSON_MAPPER.fromJson(doctorGroupEventDetail.getExtra(), DoctorTransFarmGroupEvent.class);
                DoctorChgFarmGroupExportDto exportData = BeanMapper.map(transFarmGroupEvent, DoctorChgFarmGroupExportDto.class);
                exportData.setGroupCode(doctorGroupEventDetail.getGroupCode());
                exportData.setBarnName(doctorGroupEventDetail.getBarnName());
                exportData.setFarmName(doctorGroupEventDetail.getFarmName());
                exportData.setEventAt(doctorGroupEventDetail.getEventAt());
                exportData.setQuantity(doctorGroupEventDetail.getQuantity());
                exportData.setWeight(doctorGroupEventDetail.getWeight());
                exportData.setRemark(doctorGroupEventDetail.getRemark());
                exportData.setCreatorName(doctorGroupEventDetail.getCreatorName());
                return exportData;
            } catch (Exception e) {
                log.info("get.group.chgFarm.failed, eventId:{}", doctorGroupEventDetail.getId());
            }
            return new DoctorChgFarmGroupExportDto();
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }

    public Paging<DoctorDiseaseGroupExportDto> pagingDiseaseGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorDiseaseGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            try {
                DoctorDiseaseGroupEvent diseaseGroupEvent = JSON_MAPPER.fromJson(doctorGroupEventDetail.getExtra(), DoctorDiseaseGroupEvent.class);
                DoctorDiseaseGroupExportDto exportData = BeanMapper.map(diseaseGroupEvent, DoctorDiseaseGroupExportDto.class);
                exportData.setGroupCode(doctorGroupEventDetail.getGroupCode());
                exportData.setBarnName(doctorGroupEventDetail.getBarnName());
                exportData.setEventAt(doctorGroupEventDetail.getEventAt());
                exportData.setRemark(doctorGroupEventDetail.getRemark());
                exportData.setCreatorName(doctorGroupEventDetail.getCreatorName());
                return exportData;
            } catch (Exception e) {
                log.info("get.group.disease.failed, eventId:{}", doctorGroupEventDetail.getId());
            }
            return new DoctorDiseaseGroupExportDto();
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }

    public Paging<DoctorVaccinationGroupExportDto> pagingVaccinationGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorVaccinationGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            try {
                DoctorAntiepidemicGroupEvent antiepidemicGroupEvent = JSON_MAPPER.fromJson(doctorGroupEventDetail.getExtra(), DoctorAntiepidemicGroupEvent.class);
                DoctorVaccinationGroupExportDto exportData = BeanMapper.map(antiepidemicGroupEvent, DoctorVaccinationGroupExportDto.class);
                exportData.setGroupCode(doctorGroupEventDetail.getGroupCode());
                exportData.setBarnName(doctorGroupEventDetail.getBarnName());
                exportData.setEventAt(doctorGroupEventDetail.getEventAt());
                exportData.setRemark(doctorGroupEventDetail.getRemark());
                exportData.setCreatorName(doctorGroupEventDetail.getCreatorName());
                return exportData;
            } catch (Exception e) {
                log.info("get.group.vaccination.failed, eventId:{}", doctorGroupEventDetail.getId());
            }
            return new DoctorVaccinationGroupExportDto();
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }

    public Paging<DoctorTransGroupExportDto> pagingTransGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorTransGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            try {
                DoctorTransGroupExportDto exportData = BeanMapper.map(doctorGroupEventDetail, DoctorTransGroupExportDto.class);
                DoctorTransGroupEvent transGroupEvent = JSON_MAPPER.fromJson(exportData.getExtra(), DoctorTransGroupEvent.class);
                exportData.setToBarnName(transGroupEvent.getToBarnName());
                exportData.setToGroupCode(transGroupEvent.getToGroupCode());
                exportData.setGroupCode(doctorGroupEventDetail.getGroupCode());
                exportData.setBarnName(doctorGroupEventDetail.getBarnName());
                exportData.setEventAt(doctorGroupEventDetail.getEventAt());
                exportData.setRemark(doctorGroupEventDetail.getRemark());
                exportData.setCreatorName(doctorGroupEventDetail.getCreatorName());
                return exportData;
            } catch (Exception e) {
                log.info("get.group.transGroup.failed, eventId:{}", doctorGroupEventDetail.getId());
            }
            return new DoctorTransGroupExportDto();
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }

    public Paging<DoctorTurnSeedGroupExportDto> pagingTurnSeedGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorTurnSeedGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            try {
                DoctorTurnSeedGroupEvent seedGroupEvent = JSON_MAPPER.fromJson(doctorGroupEventDetail.getExtra(), DoctorTurnSeedGroupEvent.class);
                DoctorTurnSeedGroupExportDto exportData = BeanMapper.map(seedGroupEvent, DoctorTurnSeedGroupExportDto.class);
                exportData.setGroupCode(doctorGroupEventDetail.getGroupCode());
                exportData.setBarnName(doctorGroupEventDetail.getBarnName());
                exportData.setEventAt(doctorGroupEventDetail.getEventAt());
                exportData.setRemark(doctorGroupEventDetail.getRemark());
                exportData.setCreatorName(doctorGroupEventDetail.getCreatorName());
                return exportData;
            } catch (Exception e) {
                log.info("get.group.turnSeed.failed, eventId:{}", doctorGroupEventDetail.getId());
            }
            return new DoctorTurnSeedGroupExportDto();
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }

    private Paging<DoctorGroupEvent> groupEventPaging(Map<String, String> groupEventCriteriaMap) {
        Map<String, Object> params = OBJECT_MAPPER.convertValue(groupEventCriteriaMap, JacksonType.MAP_OF_OBJECT);
        if (params == null || params.isEmpty()) {
            return Paging.empty();
        }
        params = Params.filterNullOrEmpty(params);
        if (params.get("eventTypes") != null) {
            params.put("types", Splitters.COMMA.splitToList((String) params.get("eventTypes")));
            params.remove("eventTypes");
        }
        if (StringUtils.isNotBlank((String) params.get("endDate"))) {
            params.put("endDate", new DateTime(params.get("endDate")).plusDays(1).minusMillis(1).toDate());
        }
        Response<Paging<DoctorGroupEvent>> pagingResponse = doctorGroupReadService.queryGroupEventsByCriteria(params, Integer.parseInt(groupEventCriteriaMap.get("pageNo")), Integer.parseInt(groupEventCriteriaMap.get("size")));
        if (!pagingResponse.isSuccess()) {
            return Paging.empty();
        }

        return pagingResponse.getResult();
    }

    private Paging<DoctorPigEvent> pigEventPaging(Map<String, String> groupEventCriteriaMap) {
        Map<String, Object> params = OBJECT_MAPPER.convertValue(groupEventCriteriaMap, JacksonType.MAP_OF_OBJECT);

        if (params == null || params.isEmpty()) {
            return Paging.empty();
        }
        params = Params.filterNullOrEmpty(params);
        if (params.get("eventTypes") != null) {
            params.put("types", Splitters.COMMA.splitToList((String) params.get("eventTypes")));
            params.remove("eventTypes");
        }
        if (StringUtils.isNotBlank((String) params.get("endDate"))) {
            params.put("endDate", new DateTime(params.get("endDate")).plusDays(1).minusMillis(1).toDate());
        }
        Response<Paging<DoctorPigEvent>> pigEventPagingResponse = doctorPigEventReadService.queryPigEventsByCriteria(params, Integer.parseInt(groupEventCriteriaMap.get("pageNo")), Integer.parseInt(groupEventCriteriaMap.get("size")));
        if (!pigEventPagingResponse.isSuccess()) {
            return Paging.empty();
        }
        return pigEventPagingResponse.getResult();
    }

    @RequestMapping(value = "/pigEventExport", method = RequestMethod.GET)
    public void eventExport(@RequestParam Map<String, String> eventCriteria, HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("event.export.starting");
            if (Strings.isNullOrEmpty(eventCriteria.get("kind"))) {
                return;
            }
            if (Objects.equals(eventCriteria.get("kind"), "1")) {
                exportSowEvents(eventCriteria, request, response);
            }
            if (Objects.equals(eventCriteria.get("kind"), "2")) {
                exportBoarEvents(eventCriteria, request, response);
            }
            if (Objects.equals(eventCriteria.get("kind"), "4")) {
                exportGroupEvents(eventCriteria, request, response);
            }

            log.info("event.export.ending");
        } catch (Exception e) {
            log.error("event.export.failed,cause:{}", Throwables.getStackTraceAsString(e));
        }
    }

    private void exportSowEvents(Map<String, String> eventCriteria, HttpServletRequest request, HttpServletResponse response) {
        switch (eventCriteria.get("eventTypes")) {
            case "7":
                //进场
                exporter.export("web-pig-sowInputFactory", eventCriteria, 1, 500, this::pagingInFarmExport, request, response);
                break;
            case "9":
                //配种
                exporter.export("web-pig-sowMating", eventCriteria, 1, 500, this::pagingMating, request, response);
                break;
            case "11":
                //妊娠检查
                exporter.export("web-pig-sowPregChkResult", eventCriteria, 1, 500, this::pagingPregChkResult, request, response);
                break;
            case "15":
                //分娩
                exporter.export("web-pig-sowFarrowing", eventCriteria, 1, 500, this::pagingFarrowing, request, response);
                break;
            case "16":
                //断奶
                exporter.export("web-pig-sowWean", eventCriteria, 1, 500, this::pagingWean, request, response);
                break;
            case "17":
                //拼窝
                exporter.export("web-pig-sowFosters", eventCriteria, 1, 500, this::pagingFosters, request, response);
                break;
            case "18":
                //仔猪变动
                exporter.export("web-pig-PigletsChg", eventCriteria, 1, 500, this::pagingLetsChg, request, response);
                break;
            case "1,12,14":
                //转舍
                exporter.export("web-pig-sowChangeBarn", eventCriteria, 1, 500, this::pagingChangeBarn, request, response);
                break;
            case "2":
                //转场
                exporter.export("web-pig-sowTransFarm", eventCriteria, 1, 500, this::pagingChgFarm, request, response);
                break;
            case "3":
                //体况
                exporter.export("web-pig-sowCondition", eventCriteria, 1, 500, this::pagingsowCondition, request, response);
                break;
            case "4":
                //疾病
                exporter.export("web-pig-sowDisease", eventCriteria, 1, 500, this::pagingDisease, request, response);
                break;
            case "5":
                //防疫
                exporter.export("web-pig-sowVaccination", eventCriteria, 1, 500, this::pagingVaccination, request, response);
                break;
            case "6":
                //离场
                exporter.export("web-pig-sowRemove", eventCriteria, 1, 500, this::pagingRemove, request, response);
                break;

        }
    }


    private void exportBoarEvents(Map<String, String> eventCriteria, HttpServletRequest request, HttpServletResponse response) {
        switch(PigEvent.from(Integer.parseInt(eventCriteria.get("eventTypes")))){
            case ENTRY:
                //进场
                exporter.export("web-pig-boarInputFactory", eventCriteria, 1, 500, this::pagingInFarmExport, request, response);
                break;
            case SEMEN:
                //采精
                exporter.export("web-pig-boarCollect", eventCriteria, 1, 500, this::pagingSemenExport, request, response);
                break;
            case CHG_LOCATION:
                exporter.export("web-pig-boarChangeBarn", eventCriteria, 1, 500, this::pagingChangeBarn, request, response);
                //转舍
                break;
            case CHG_FARM:
                exporter.export("web-pig-boarTransFarm", eventCriteria, 1, 500, this::pagingChgFarm, request, response);
                //转场
                break;
            case CONDITION:
                exporter.export("web-pig-boarCondition", eventCriteria, 1, 500, this::pagingBoarCondition, request, response);
                //体况
                break;
            case DISEASE:
                exporter.export("web-pig-boarDisease", eventCriteria, 1, 500, this::pagingDisease, request, response);
                //疾病
                break;
            case VACCINATION:
                exporter.export("web-pig-boarVaccination", eventCriteria, 1, 500, this::pagingVaccination, request, response);
                //防疫
                break;
            case REMOVAL:
                exporter.export("web-pig-boarRemove", eventCriteria, 1, 500, this::pagingRemove, request, response);
                //离场
                break;
        }
    }

    /**
     * 猪群导出
     * @param eventCriteria
     * @param request
     * @param response
     */
    private void exportGroupEvents(Map<String, String> eventCriteria, HttpServletRequest request, HttpServletResponse response) {
        switch(GroupEventType.from(Integer.parseInt(eventCriteria.get("eventTypes")))){
            case NEW:
                //新建
                exporter.export("web-group-new", eventCriteria, 1, 500, this::pagingNewGroup, request, response);
                break;
            case MOVE_IN:
                exporter.export("web-group-MoveIn", eventCriteria, 1, 500, this::pagingMoveInGroup, request, response);
                //转入
                break;
            case CHANGE:
                //猪群变动
                exporter.export("web-group-change", eventCriteria, 1, 500, this::pagingChangeGroup, request, response);
                break;
            case TRANS_GROUP:
                exporter.export("web-group-transGroup", eventCriteria, 1, 500, this::pagingTransGroup, request, response);
                //转群
                break;
            case TURN_SEED:
                //商品猪转种猪
                exporter.export("web-group-turnSeed", eventCriteria, 1, 500, this::pagingTurnSeedGroup, request, response);
                break;
            case LIVE_STOCK:
                //猪只存栏
//                exporter.export("web-group-event", eventCriteria, 1, 500, this::pagingTurnSeedGroup, request, response);
                break;
            case DISEASE:
                //疾病
                exporter.export("web-group-Disease", eventCriteria, 1, 500, this::pagingDiseaseGroup, request, response);
                break;
            case ANTIEPIDEMIC:
                //防疫
                exporter.export("web-group-accination", eventCriteria, 1, 500, this::pagingVaccinationGroup, request, response);
                break;
            case TRANS_FARM:
                //转场
                exporter.export("web-group-transFarm", eventCriteria, 1, 500, this::pagingChgFramGroup, request, response);
                break;
            case CLOSE:
                break;
        }
    }



}