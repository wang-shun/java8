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
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.DoctorSowParityAvgDto;
import io.terminus.doctor.event.dto.DoctorSowParityCount;
import io.terminus.doctor.event.dto.event.DoctorEventOperator;
import io.terminus.doctor.event.dto.event.boar.DoctorBoarConditionDto;
import io.terminus.doctor.event.dto.event.group.DoctorChangeGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorNewGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorTransGroupEvent;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.dto.event.sow.DoctorWeanDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.enums.MatingType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorPigEventWriteService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.web.core.export.Exporter;
import io.terminus.doctor.web.front.event.dto.DoctorBoarConditionExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorChangeGroupExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorChgFarmExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorChgFarmGroupExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorDiseaseGroupExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorFarrowingExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorFostersExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorGroupEventDetail;
import io.terminus.doctor.web.front.event.dto.DoctorGroupEventExportData;
import io.terminus.doctor.web.front.event.dto.DoctorMoveInGroupExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorNewExportGroup;
import io.terminus.doctor.web.front.event.dto.DoctorPigBoarInFarmExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorPigChangeBarnExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorPigDiseaseExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorPigEventDetail;
import io.terminus.doctor.web.front.event.dto.DoctorPigEventExportData;
import io.terminus.doctor.web.front.event.dto.DoctorPigEventPagingDto;
import io.terminus.doctor.web.front.event.dto.DoctorPigMatingExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorPigRemoveExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorPigSemenExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorPigVaccinationExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorPigletsChgExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorPregChkResultExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorTransGroupExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorTurnSeedGroupExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorVaccinationGroupExportDto;
import io.terminus.doctor.web.front.event.dto.DoctorWeanExportDto;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notNull;
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
     * 猪事件的报表生成
     */
    @RequestMapping(value = "/pigEventExport", method = RequestMethod.GET)
    public void pigEventExports(@RequestParam Map<String, String> pigEventCriteria, HttpServletRequest request, HttpServletResponse response) {

        try {
            pigEventCriteria.put("size",pigEventCriteria.get("pageSize"));
            pigEventCriteria.put("size","500");
            pigEventCriteria.put("pageNo","1");
            log.info("event.export.starting");
            if (Strings.isNullOrEmpty(pigEventCriteria.get("kind")) && Strings.isNullOrEmpty(pigEventCriteria.get("eventTypes"))) {
                return;
            }
            String eventType = pigEventCriteria.get("eventTypes");
            if (Objects.equals(pigEventCriteria.get("kind"),"2")) {

                switch (eventType) {
                    case "7": exporter.export(pagingInFarmExport(pigEventCriteria), "web-pig-boarInputFactory", request, response);
                        break;
                    case "8": exporter.export(pagingSemenExport(pigEventCriteria), "web-pig-boarCollect" , request, response);
                        break;
                    case "1": exporter.export(pagingChangeBarn(pigEventCriteria), "web-pig-boarChangeBarn" , request, response);
                        break;
//                    case "2": exporter.export();
//                        break;
//                    case "3": exporter.export();
//                        break;
                    case "4": exporter.export(pagingDisease(pigEventCriteria), "web-pig-boarDisease", request, response);
                        break;
                    case "5": exporter.export(pagingVaccination(pigEventCriteria), "web-pig-boarVaccination", request, response);
                        break;
                    case "6": exporter.export(pagingRemove(pigEventCriteria), "web-pig-boarRemove", request, response);
                        break;
                    default:
                        log.error("eventType error");
                        break;
                }
            }
            if (Objects.equals(pigEventCriteria.get("kind"),"1")) {

                switch (eventType) {
                    case "7":
                        exporter.export(pagingInFarmExport(pigEventCriteria), "web-pig-sowInputFactory", request, response);
                        break;
//                    case "9":
//                        exporter.export(, , request, response);
//                        break;
//                    case "11":
//                        exporter.export(, , request, response);
//                        break;
//                    case "15":
//                        exporter.export(, , request, response);
//                        break;
//                    case "16":
//                        exporter.export(, , request, response);
//                        break;
//                    case "17":
//                        exporter.export(, , request, response);
//                        break;
//                    case "18":
//                        exporter.export(, , request, response);
//                        break;
                    case "1":
                    case "12":
                    case "14": exporter.export(pagingChangeBarn(pigEventCriteria), "web-pig-sowChangeBarn", request, response);
                        break;
//                    case "2":
//                        exporter.export(, , request, response);
//                        break;
//                    case "3":
//                        exporter.export(, , request, response);
//                        break;
                    case "4":
                        exporter.export(pagingDisease(pigEventCriteria), "web-pig-sowDisease", request, response);
                        break;
                    case "5":
                        exporter.export(pagingVaccination(pigEventCriteria), "web-pig-sowVaccination", request, response);
                        break;
                    case "6": exporter.export(pagingRemove(pigEventCriteria), "web-pig-sowRemove", request, response);
                        break;

                    default:
                        log.error("eventType error");
                        break;
                }

            }
            if (Objects.equals(pigEventCriteria.get("kind"),"4")) {
                switch (eventType) {
//                    case "1":
//                        exporter.export(pagingChangeBarn(pigEventCriteria), "web-pig-sowChangeBarn", request, response);
//                        break;
//                    case "2":
//                        exporter.export(, , request, response);
//                        break;
//                    case "3":
//                        exporter.export(, , request, response);
//                        break;
//                    case "6":
//                        exporter.export(, , request, response);
//                        break;
//                    case "4":
//                        exporter.export(pagingDisease(pigEventCriteria), "web-pig-sowDisease", request, response);
//                        break;
//                    case "5":
//                        exporter.export(pagingVaccination(pigEventCriteria), "web-pig-sowVaccination", request, response);
//                        break;
//                    case "9":
//                        exporter.export(, , request, response);
//                        break;
//                    case "7":
//                        exporter.export(, , request, response);
//                        break;
//                    case "8":
//                        exporter.export(, , request, response);
//                        break;
                    default:
                        log.error("ventType error");
                        break;
                }
            }
            log.info("event.export.ending");
        } catch (Exception e) {
            log.error("event.export.failed");
        }
    }

    /**
     * 猪入场事件的导出报表的构建
     */
    public List<DoctorPigBoarInFarmExportDto> pagingInFarmExport(Map<String, String> pigEventCriteria) {

        List<DoctorPigBoarInFarmExportDto> pigBoarInFarmExportLists = Lists.newArrayList();

        Map<String, Object> criteriaMap = OBJECT_MAPPER.convertValue(pigEventCriteria, Map.class);

        Paging<DoctorPigEventDetail> pigEventPaging = queryPigEventsByCriteria(criteriaMap, Integer.parseInt(pigEventCriteria.get("pageNo")), Integer.parseInt(pigEventCriteria.get("size")));


        List<DoctorPigEventDetail> list = pigEventPaging.getData();
        for(DoctorPigEventDetail doctorPigEventDetail : list) {

            DoctorPigEventDetail detail = OBJECT_MAPPER.convertValue(doctorPigEventDetail, DoctorPigEventDetail.class);
            DoctorPigBoarInFarmExportDto doctorPigBoarInFarmExportDto = new DoctorPigBoarInFarmExportDto();
            doctorPigBoarInFarmExportDto.setPigCode(detail.getPigCode());
            doctorPigBoarInFarmExportDto.setPigStatusAfter(detail.getPigStatusAfter());
            if (detail.getExtraMap().containsKey("parity")) {
                doctorPigBoarInFarmExportDto.setParity((int)detail.getExtraMap().get("parity"));
            }else {
                doctorPigBoarInFarmExportDto.setParity(doctorPigEventDetail.getParity());
            }
            if (detail.getExtraMap().containsKey("breedName")) {
                doctorPigBoarInFarmExportDto.setBreedName((String) detail.getExtraMap().get("breedName"));
            }
            doctorPigBoarInFarmExportDto.setGeneticName("");

            if (detail.getExtraMap().containsKey("inFarmDate")) {
                doctorPigBoarInFarmExportDto.setInFarmDate(new Date((Long) detail.getExtraMap().get("inFarmDate")));
            }else {
                doctorPigBoarInFarmExportDto.setInFarmDate(null);
            }
            if (detail.getExtraMap().containsKey("birthday")) {
                doctorPigBoarInFarmExportDto.setBirthDate(new Date((Long) detail.getExtraMap().get("birthday")));
            }else {
                doctorPigBoarInFarmExportDto.setBirthDate(null);
            }
            if (detail.getExtraMap().containsKey("breedTypeName")) {
                doctorPigBoarInFarmExportDto.setGeneticName((String) detail.getExtraMap().get("breedTypeName"));
            }else {
                doctorPigBoarInFarmExportDto.setGeneticName(null);
            }
            if (detail.getExtraMap().containsKey("source")) {
                doctorPigBoarInFarmExportDto.setSource((int)detail.getExtraMap().get("source"));
            }else {
                doctorPigBoarInFarmExportDto.setSource(0);
            }
            if (detail.getExtraMap().containsKey("boarTypeName")) {
                doctorPigBoarInFarmExportDto.setBoarType((String)detail.getExtraMap().get("boarTypeName"));
            }else {
                doctorPigBoarInFarmExportDto.setBoarType(null);
            }
            if (detail.getExtraMap().containsKey("remark")) {
                doctorPigBoarInFarmExportDto.setRemark((String) detail.getExtraMap().get("remark"));
            } else {
                doctorPigBoarInFarmExportDto.setRemark(detail.getRemark());
            }
            if (detail.getExtraMap().containsKey("updatorName")) {
                doctorPigBoarInFarmExportDto.setRemark((String)detail.getExtraMap().get("updatorName"));
            }else {
                doctorPigBoarInFarmExportDto.setUpdatorName(detail.getUpdatorName());
            }
            if (detail.getExtraMap().containsKey("fatherCode")) {
                doctorPigBoarInFarmExportDto.setPigFatherCode((String) detail.getExtraMap().get("atherCode"));
            }else {
                doctorPigBoarInFarmExportDto.setPigFatherCode(null);
            }
            if (detail.getExtraMap().containsKey("motherCode")) {
                doctorPigBoarInFarmExportDto.setPigMotherCode((String) detail.getExtraMap().get("motherCode"));
            }else {
                doctorPigBoarInFarmExportDto.setPigMotherCode(null);
            }
            if (detail.getExtraMap().containsKey("earCode")) {
                doctorPigBoarInFarmExportDto.setEarCode((String) detail.getExtraMap().get("earCode"));
            }else {
                doctorPigBoarInFarmExportDto.setEarCode(null);
            }
            if (detail.getExtraMap().containsKey("left")) {
                doctorPigBoarInFarmExportDto.setLeft((int) detail.getExtraMap().get("left"));
            }else {
                doctorPigBoarInFarmExportDto.setLeft(0);
            }
            if (detail.getExtraMap().containsKey("right")) {
                doctorPigBoarInFarmExportDto.setRight((int) detail.getExtraMap().get("right"));
            }else {
                doctorPigBoarInFarmExportDto.setRight(0);
            }
            pigBoarInFarmExportLists.add(doctorPigBoarInFarmExportDto);

        }
        return pigBoarInFarmExportLists;
    }
    /**
     * 公猪采精事件事件的导出报表构建
     */
    public List<DoctorPigSemenExportDto> pagingSemenExport(Map<String, String> pigEventCriteria) {

        List<DoctorPigSemenExportDto> doctorEventSemenLists = Lists.newArrayList();

        Map<String, Object> criteriaMap = OBJECT_MAPPER.convertValue(pigEventCriteria, Map.class);
        Paging<DoctorPigEventDetail> pigEventPaging = queryPigEventsByCriteria(criteriaMap, Integer.parseInt(pigEventCriteria.get("pageNo")), Integer.parseInt(pigEventCriteria.get("size")));


        List<DoctorPigEventDetail> list = pigEventPaging.getData();
        for(DoctorPigEventDetail detail : list) {

//            DoctorPigEventDetail detail = OBJECT_MAPPER.convertValue(doctorPigEventDetail, DoctorPigEventDetail.class);
            DoctorPigSemenExportDto doctorEventSemenExport = new DoctorPigSemenExportDto();
            doctorEventSemenExport.setPigCode(detail.getPigCode());

            if(detail.getExtraMap().containsKey("barnName")) {
                doctorEventSemenExport.setBarnName((String)detail.getExtraMap().get("barnName"));
            }else {
                doctorEventSemenExport.setBarnName(detail.getBarnName());
            }
            if (detail.getExtraMap().containsKey("semenDate")) {
                doctorEventSemenExport.setSemenDate(new Date((Long) detail.getExtraMap().get("semenDate")));
            }else {
                doctorEventSemenExport.setSemenDate(null);
            }
            if (detail.getExtraMap().containsKey("semenWeight")) {
                doctorEventSemenExport.setSemenWeight((double) detail.getExtraMap().get("semenWeight"));
            }else {
                doctorEventSemenExport.setSemenWeight(0.0);
            }
            if (detail.getExtraMap().containsKey("dilutionRatio")) {
                doctorEventSemenExport.setDilutionRatio((double)detail.getExtraMap().get("dilutionRatio"));
            }else {
                doctorEventSemenExport.setDilutionRatio(0.0);
            }
            if (detail.getExtraMap().containsKey("dilutionWeight")) {
                doctorEventSemenExport.setDilutionWeight((double)detail.getExtraMap().get("dilutionWeight"));
            }else {
                doctorEventSemenExport.setDilutionWeight(0.0);
            }
            if (detail.getExtraMap().containsKey("semenDensity")) {
                doctorEventSemenExport.setSemenDensity((double)detail.getExtraMap().get("semenDensity"));
            }else {
                doctorEventSemenExport.setSemenDensity(0.0);
            }
            if (detail.getExtraMap().containsKey("semenActive")) {
                doctorEventSemenExport.setSemenActive((double)detail.getExtraMap().get("semenActive"));
            }else {
                doctorEventSemenExport.setSemenActive(0.0);
            }
            if (detail.getExtraMap().containsKey("semenPh")) {
                doctorEventSemenExport.setSemenPh((double)detail.getExtraMap().get("semenPh"));
            }else {
                doctorEventSemenExport.setSemenPh(null);
            }
            if (detail.getExtraMap().containsKey("semenTotal")) {
                doctorEventSemenExport.setSemenTotal((double)detail.getExtraMap().get("semenTotal"));
            }else {
                doctorEventSemenExport.setSemenTotal(null);
            }
            if (detail.getExtraMap().containsKey("semenJxRatio")) {
                doctorEventSemenExport.setSemenJxRatio((double)detail.getExtraMap().get("semenJxRatio"));
            }else {
                doctorEventSemenExport.setSemenJxRatio(null);
            }
            if (detail.getExtraMap().containsKey("semenRemark")) {
                doctorEventSemenExport.setSemenRemark((String) detail.getExtraMap().get("semenRemark"));
            }else {
                doctorEventSemenExport.setSemenRemark(detail.getRemark());
            }
            if (detail.getExtraMap().containsKey("updatorName")) {
                doctorEventSemenExport.setUpdatorName((String) detail.getExtraMap().get("updatorName"));
            }else {
                doctorEventSemenExport.setUpdatorName(detail.getUpdatorName());
            }
            doctorEventSemenLists.add(doctorEventSemenExport);
        }

        return doctorEventSemenLists;
    }
    /**
     * 转舍事件
     */
    public List<DoctorPigChangeBarnExportDto> pagingChangeBarn(Map<String, String> pigEventCriteria) {

        List<DoctorPigChangeBarnExportDto> doctorPigChangeBarnLists = Lists.newArrayList();
        Map<String, Object> criteriaMap = OBJECT_MAPPER.convertValue(pigEventCriteria, Map.class);
        Paging<DoctorPigEventDetail> pigEventPaging = queryPigEventsByCriteria(criteriaMap, Integer.parseInt(pigEventCriteria.get("pageNo")), Integer.parseInt(pigEventCriteria.get("size")));
        List<DoctorPigEventDetail> list = pigEventPaging.getData();
        for(DoctorPigEventDetail detail : list) {
            DoctorPigChangeBarnExportDto doctorPigChangeBarnExportDto = new DoctorPigChangeBarnExportDto();
            doctorPigChangeBarnExportDto.setPigCode(detail.getPigCode());
            doctorPigChangeBarnExportDto.setPigStatusAfter(detail.getPigStatusAfter());
            if (detail.getExtraMap().containsKey("changeLocationDate")) {
                doctorPigChangeBarnExportDto.setChangeLocationDate(new Date((long)detail.getExtraMap().get("changeLocationDate")));
            } else{
                doctorPigChangeBarnExportDto.setChangeLocationDate(null);
            }
            if (detail.getExtraMap().containsKey("parity")) {
                doctorPigChangeBarnExportDto.setParity((int)detail.getExtraMap().get("parity"));
            }
            if (detail.getExtraMap().containsKey("chgLocationFromBarnName")){
                doctorPigChangeBarnExportDto.setChgLocationFromBarnName((String)detail.getExtraMap().get("chgLocationFromBarnName"));
            }else {
                doctorPigChangeBarnExportDto.setChgLocationFromBarnName(null);
            }
            if (detail.getExtraMap().containsKey("chgLocationToBarnName")) {
                doctorPigChangeBarnExportDto.setChgLocationToBarnName((String)detail.getExtraMap().get("chgLocationToBarnName"));
            }else {
                doctorPigChangeBarnExportDto.setChgLocationToBarnName(null);
            }

            if (detail.getExtraMap().containsKey("remark")) {
                doctorPigChangeBarnExportDto.setRemark((String) detail.getExtraMap().get("remark"));
            }else {
                doctorPigChangeBarnExportDto.setRemark(detail.getRemark());
            }
            if (detail.getExtraMap().containsKey("updatorName")) {
                doctorPigChangeBarnExportDto.setUpdatorName((String) detail.getExtraMap().get("updatorName"));
            }else {
                doctorPigChangeBarnExportDto.setUpdatorName(detail.getUpdatorName());
            }
            doctorPigChangeBarnLists.add(doctorPigChangeBarnExportDto);
        }
        return doctorPigChangeBarnLists;
    }
    /**
     * 公猪的疾病事件
     */
    public List<DoctorPigDiseaseExportDto> pagingDisease(Map<String, String> pigEventCriteria){
        List<DoctorPigDiseaseExportDto> doctorEventDiseaseLists = Lists.newArrayList();

        Map<String, Object> criteriaMap = OBJECT_MAPPER.convertValue(pigEventCriteria, Map.class);
        Paging<DoctorPigEventDetail> pigEventPaging = queryPigEventsByCriteria(criteriaMap, Integer.parseInt(pigEventCriteria.get("pageNo")), Integer.parseInt(pigEventCriteria.get("size")));


        List<DoctorPigEventDetail> list = pigEventPaging.getData();
        for(DoctorPigEventDetail detail : list) {

            DoctorPigDiseaseExportDto doctorEventDiseaseExport = new DoctorPigDiseaseExportDto();
            doctorEventDiseaseExport.setPigCode(detail.getPigCode());
            if (detail.getExtraMap().containsKey("barnName")) {
                doctorEventDiseaseExport.setBarnName((String)detail.getExtraMap().get("barnName"));
            }else {
                doctorEventDiseaseExport.setBarnName(null);
            }
            if (detail.getExtraMap().containsKey("parity")) {
                doctorEventDiseaseExport.setParity((int)detail.getExtraMap().get("parity"));
            }else {
                doctorEventDiseaseExport.setParity(detail.getParity());
            }
            if (detail.getExtraMap().containsKey("diseaseDate")) {
                doctorEventDiseaseExport.setDiseaseDate(new Date((long) detail.getExtraMap().get("diseaseDate")));
            }else {
                doctorEventDiseaseExport.setDiseaseDate(null);
            }
            if (detail.getExtraMap().containsKey("diseaseName")) {
                doctorEventDiseaseExport.setDiseaseName((String) detail.getExtraMap().get("diseaseName"));
            }else {
                doctorEventDiseaseExport.setDiseaseName(null);
            }
            if (detail.getExtraMap().containsKey("diseaseStaff")) {
                doctorEventDiseaseExport.setDiseaseStaff((String) detail.getExtraMap().get("diseaseStaff"));
            }else {
                doctorEventDiseaseExport.setDiseaseStaff(null);
            }
            if (detail.getExtraMap().containsKey("diseaseRemark")) {
                doctorEventDiseaseExport.setDiseaseRemark((String) detail.getExtraMap().get("diseaseRemark"));
            }else {
                doctorEventDiseaseExport.setDiseaseRemark(null);
            }
            if (detail.getExtraMap().containsKey("updatorName")) {
                doctorEventDiseaseExport.setUpdatorName((String) detail.getExtraMap().get("updatorName"));
            }else {
                doctorEventDiseaseExport.setUpdatorName(detail.getUpdatorName());
            }
            doctorEventDiseaseLists.add(doctorEventDiseaseExport);
        }
        return doctorEventDiseaseLists;
    }

    /**
     * 猪的防疫事件报表模板
     */
    public List<DoctorPigVaccinationExportDto> pagingVaccination(Map<String, String> pigEventCriteria) {
        List<DoctorPigVaccinationExportDto> doctorPigVaccinalionLists = Lists.newArrayList();
        Map<String, Object> criteriaMap = OBJECT_MAPPER.convertValue(pigEventCriteria, Map.class);
        Paging<DoctorPigEventDetail> pigEventPaging = queryPigEventsByCriteria(criteriaMap, Integer.parseInt(pigEventCriteria.get("pageNo")), Integer.parseInt(pigEventCriteria.get("size")));
        List<DoctorPigEventDetail> list = pigEventPaging.getData();
        for(DoctorPigEventDetail detail : list) {
            DoctorPigVaccinationExportDto doctorPigVaccinalion = new DoctorPigVaccinationExportDto();
            doctorPigVaccinalion.setPigCode(detail.getPigCode());
            if (detail.getExtraMap().containsKey("barnName")) {
                doctorPigVaccinalion.setBarnName((String) detail.getExtraMap().get("barnName"));
            }else {
                doctorPigVaccinalion.setBarnName(detail.getBarnName());
            }
            if (detail.getExtraMap().containsKey("vaccinationItemName")) {
                doctorPigVaccinalion.setVaccinationItemName((String) detail.getExtraMap().get("vaccinationItemName"));
            } else {
                doctorPigVaccinalion.setVaccinationItemName(null);
            }
            if (detail.getExtraMap().containsKey("vaccinationName")) {
                doctorPigVaccinalion.setVaccinationName((String) detail.getExtraMap().get("vaccinationName"));
            }else {
                doctorPigVaccinalion.setVaccinationName(null);
            }
            if (detail.getExtraMap().containsKey("vaccinationStaffName")) {
                doctorPigVaccinalion.setVaccinationStaffName((String) detail.getExtraMap().get("vaccinationStaffName"));
            }else {
                doctorPigVaccinalion.setVaccinationStaffName(null);
            }
            if (detail.getExtraMap().containsKey("vaccinationDate")) {
                doctorPigVaccinalion.setVaccinationDate(new Date((long) detail.getExtraMap().get("vaccinationDate")));
            }else {
                doctorPigVaccinalion.setVaccinationDate(null);
            }
            if (detail.getExtraMap().containsKey("remark")) {
                doctorPigVaccinalion.setRemark((String) detail.getExtraMap().get("remark"));
            }else {
                doctorPigVaccinalion.setRemark(detail.getRemark());
            }
            if (detail.getExtraMap().containsKey("updatorName")) {
                doctorPigVaccinalion.setUpdatorName((String) detail.getExtraMap().get("updatorName"));
            }else {
                doctorPigVaccinalion.setUpdatorName(detail.getUpdatorName());
            }
            doctorPigVaccinalionLists.add(doctorPigVaccinalion);
        }
        return doctorPigVaccinalionLists;
    }
    /**
     * 猪的离场事件
     */
    public List<DoctorPigRemoveExportDto> pagingRemove(Map<String, String> pigEventCriteria) {
        List<DoctorPigRemoveExportDto> doctorPigRemoveLists = Lists.newArrayList();
        Map<String, Object> criteriaMap = OBJECT_MAPPER.convertValue(pigEventCriteria, Map.class);
        Paging<DoctorPigEventDetail> pigEventPaging = queryPigEventsByCriteria(criteriaMap, Integer.parseInt(pigEventCriteria.get("pageNo")), Integer.parseInt(pigEventCriteria.get("size")));
        List<DoctorPigEventDetail> list = pigEventPaging.getData();
        for(DoctorPigEventDetail detail : list) {
            DoctorPigRemoveExportDto doctorPigRemove = new DoctorPigRemoveExportDto();
            doctorPigRemove.setPigCode(detail.getPigCode());
            if (detail.getExtraMap().containsKey("parity")) {
                doctorPigRemove.setParity((int) detail.getExtraMap().get("parity"));
            }else {
                doctorPigRemove.setParity(detail.getParity());
            }
            if (detail.getExtraMap().containsKey("barnName")){
                doctorPigRemove.setBarnName((String) detail.getExtraMap().get("barnName"));
            }else {
                doctorPigRemove.setBarnName(detail.getBarnName());
            }
            if (detail.getExtraMap().containsKey("removalDate")) {
                doctorPigRemove.setRemovalDate(new Date((long) detail.getExtraMap().get("removalDate")));
            }else {
                doctorPigRemove.setRemovalDate(null);
            }
            if (detail.getExtraMap().containsKey("chgTypeName")) {
                doctorPigRemove.setChgTypeName((String) detail.getExtraMap().get("chgTypeName"));
            }else {
                doctorPigRemove.setChgTypeName(null);
            }
            if (detail.getExtraMap().containsKey("chgReasonName")) {
                doctorPigRemove.setChgReasonName((String) detail.getExtraMap().get("chgReasonName"));
            }else {
                doctorPigRemove.setChgReasonName(null);
            }
            if (detail.getExtraMap().containsKey("weight")) {
                doctorPigRemove.setWeight((double) detail.getExtraMap().get("weight"));
            }else {
                doctorPigRemove.setWeight(0.0);
            }
            if (detail.getExtraMap().containsKey("price")) {
                doctorPigRemove.setPrice((int) detail.getExtraMap().get("price") / 1000);
            }else {
                doctorPigRemove.setPrice(0);
            }
            if (detail.getExtraMap().containsKey("amount")) {
                doctorPigRemove.setAmount((int) detail.getExtraMap().get("amount") / 1000);
            }else {
                doctorPigRemove.setAmount(0);
            }
            //客户未知
            doctorPigRemove.setXxx(null);
            if (detail.getExtraMap().containsKey("operatorName")) {
                doctorPigRemove.setOperatorName((String) detail.getExtraMap().get("operatorName"));
            }else {
                doctorPigRemove.setOperatorName(detail.getOperatorName());
            }
            if (detail.getExtraMap().containsKey("remark")) {
                doctorPigRemove.setRemark((String) detail.getExtraMap().get("remark"));
            }else {
                doctorPigRemove.setRemark(detail.getRemark());
            }
            if (detail.getExtraMap().containsKey("updatorName")) {
                doctorPigRemove.setUpdatorName((String) detail.getExtraMap().get("updatorName"));
            }else {
                doctorPigRemove.setUpdatorName(detail.getUpdatorName());
            }
            doctorPigRemoveLists.add(doctorPigRemove);
        }
        return doctorPigRemoveLists;
    }
    /**
     * 猪的配种事件
     */
    public List<DoctorPigMatingExportDto> pagingMating(Map<String, String> pigEventCriteria) {
        List<DoctorPigMatingExportDto> doctorPigMatingLists = Lists.newArrayList();
        Map<String, Object> criteriaMap = OBJECT_MAPPER.convertValue(pigEventCriteria, Map.class);
        Paging<DoctorPigEventDetail> pigEventPaging = queryPigEventsByCriteria(criteriaMap, Integer.parseInt(pigEventCriteria.get("pageNo")), Integer.parseInt(pigEventCriteria.get("size")));
        List<DoctorPigEventDetail> list = pigEventPaging.getData();
        for(DoctorPigEventDetail detail : list) {
            DoctorMatingDto matingDto = JSON_MAPPER.fromJson(detail.getExtra(), DoctorMatingDto.class);
            DoctorPigMatingExportDto dto = BeanMapper.map(matingDto, DoctorPigMatingExportDto.class);
            dto.setPigStatusAfter((int) new DoctorPigEvent().getPigStatusAfter());
            doctorPigMatingLists.add(dto);
        }
        return doctorPigMatingLists;
    }
    /**
     * 仔猪变动事件
     */
    public List<DoctorPigletsChgExportDto> pagingLetsChg(Map<String, String> pigEventCriteria) {
        List<DoctorPigletsChgExportDto> doctorPigLetsChgLists = Lists.newArrayList();
        Map<String, Object> criteriaMap = OBJECT_MAPPER.convertValue(pigEventCriteria, Map.class);
        Paging<DoctorPigEventDetail> pigEventPaging = queryPigEventsByCriteria(criteriaMap, Integer.parseInt(pigEventCriteria.get("pageNo")), Integer.parseInt(pigEventCriteria.get("size")));
        List<DoctorPigEventDetail> list = pigEventPaging.getData();
        for(DoctorPigEventDetail detail : list) {
            DoctorPigletsChgDto letsChgDto = JSON_MAPPER.fromJson(detail.getExtra(), DoctorPigletsChgDto.class);
            DoctorPigletsChgExportDto dto = BeanMapper.map(letsChgDto, DoctorPigletsChgExportDto.class);
            doctorPigLetsChgLists.add(dto);
        }
        return doctorPigLetsChgLists;
    }
    /**
     * 断奶事件
     */
    public List<DoctorWeanExportDto> pagingWean(Map<String, String> pigEventCriteria) {
        List<DoctorWeanExportDto> doctorWeanLists = Lists.newArrayList();
        Map<String, Object> criteriaMap = OBJECT_MAPPER.convertValue(pigEventCriteria, Map.class);
        Paging<DoctorPigEventDetail> pigEventPaging = queryPigEventsByCriteria(criteriaMap, Integer.parseInt(pigEventCriteria.get("pageNo")), Integer.parseInt(pigEventCriteria.get("size")));
        List<DoctorPigEventDetail> list = pigEventPaging.getData();
        for(DoctorPigEventDetail detail : list) {
            DoctorWeanDto weanDto = JSON_MAPPER.fromJson(detail.getExtra(), DoctorWeanDto.class);
            DoctorWeanExportDto dto = BeanMapper.map(weanDto, DoctorWeanExportDto.class);
            doctorWeanLists.add(dto);
        }
        return doctorWeanLists;
    }
    /**
     *检查
     */
    public List<DoctorPregChkResultExportDto> pagingPregChkResult(Map<String, String> pigEventCriteria) {
        List<DoctorPregChkResultExportDto> pregChkResultLists = Lists.newArrayList();
        Map<String, Object> criteriaMap = OBJECT_MAPPER.convertValue(pigEventCriteria, Map.class);
        Paging<DoctorPigEventDetail> pigEventPaging = queryPigEventsByCriteria(criteriaMap, Integer.parseInt(pigEventCriteria.get("pageNo")), Integer.parseInt(pigEventCriteria.get("size")));
        List<DoctorPigEventDetail> list = pigEventPaging.getData();
        for(DoctorPigEventDetail detail : list) {
            DoctorPregChkResultDto pregChkResultDto = JSON_MAPPER.fromJson(detail.getExtra(), DoctorPregChkResultDto.class);
            DoctorPregChkResultExportDto dto = BeanMapper.map(pregChkResultDto, DoctorPregChkResultExportDto.class);
            pregChkResultLists.add(dto);
        }
        return pregChkResultLists;
    }
    /**
     *分娩
     */
    public List<DoctorFarrowingExportDto> pagingFarrowing(Map<String, String> pigEventCriteria) {
        List<DoctorFarrowingExportDto> farrowingLists = Lists.newArrayList();
        Map<String, Object> criteriaMap = OBJECT_MAPPER.convertValue(pigEventCriteria, JacksonType.MAP_OF_OBJECT);
        Paging<DoctorPigEventDetail> pigEventPaging = queryPigEventsByCriteria(criteriaMap, Integer.parseInt(pigEventCriteria.get("pageNo")), Integer.parseInt(pigEventCriteria.get("size")));
        List<DoctorPigEventDetail> list = pigEventPaging.getData();
        for(DoctorPigEventDetail detail : list) {
            DoctorFarrowingDto farrowing = JSON_MAPPER.fromJson(detail.getExtra(), DoctorFarrowingDto.class);
            DoctorFarrowingExportDto dto = BeanMapper.map(farrowing, DoctorFarrowingExportDto.class);
            farrowingLists.add(dto);
        }
        return farrowingLists;
    }
    /**
     * 拼窝
     */
    public List<DoctorFostersExportDto> pagingFosters(Map<String, String> pigEventCriteria) {
        List<DoctorFostersExportDto> fostersLists = Lists.newArrayList();
        Map<String, Object> criteriaMap = OBJECT_MAPPER.convertValue(pigEventCriteria, JacksonType.MAP_OF_OBJECT);
        Paging<DoctorPigEventDetail> pigEventPaging = queryPigEventsByCriteria(criteriaMap, Integer.parseInt(pigEventCriteria.get("pageNo")), Integer.parseInt(pigEventCriteria.get("size")));
        List<DoctorPigEventDetail> list = pigEventPaging.getData();
        for(DoctorPigEventDetail detail : list) {
            DoctorFarrowingDto fosters = JSON_MAPPER.fromJson(detail.getExtra(), DoctorFarrowingDto.class);
            DoctorFostersExportDto dto = BeanMapper.map(fosters, DoctorFostersExportDto.class);
            fostersLists.add(dto);
        }
        return fostersLists;
    }
    /**
     * 公猪体况
     */
    public List<DoctorBoarConditionExportDto> pagingBoarCondition(Map<String, String> pigEventCriteria) {
        List<DoctorBoarConditionExportDto> boarConditionLists = Lists.newArrayList();
        Map<String, Object> criteriaMap = OBJECT_MAPPER.convertValue(pigEventCriteria, JacksonType.MAP_OF_OBJECT);
        Paging<DoctorPigEventDetail> pigEventPaging = queryPigEventsByCriteria(criteriaMap, Integer.parseInt(pigEventCriteria.get("pageNo")), Integer.parseInt(pigEventCriteria.get("size")));
        List<DoctorPigEventDetail> list = pigEventPaging.getData();
        for(DoctorPigEventDetail detail : list) {
            DoctorBoarConditionDto boarCondition = JSON_MAPPER.fromJson(detail.getExtra(), DoctorBoarConditionDto.class);
            DoctorBoarConditionExportDto dto = BeanMapper.map(boarCondition, DoctorBoarConditionExportDto.class);
            boarConditionLists.add(dto);
        }
        return boarConditionLists;
    }
    /**
     * 猪的转场事件
     */
    public Paging<DoctorChgFarmExportDto> pagingChgFarm(Map<String, String> pigEventCriteria) {
        List<DoctorChgFarmExportDto> chgFarmLists = Lists.newArrayList();
        Map<String, Object> criteriaMap = OBJECT_MAPPER.convertValue(pigEventCriteria, JacksonType.MAP_OF_OBJECT);
        Paging<DoctorPigEventDetail> pigEventPaging = queryPigEventsByCriteria(criteriaMap, Integer.parseInt(pigEventCriteria.get("pageNo")), Integer.parseInt(pigEventCriteria.get("size")));
        List<DoctorPigEventDetail> list = pigEventPaging.getData();
        for(DoctorPigEventDetail detail : list) {
            DoctorChgFarmDto chgFarm = JSON_MAPPER.fromJson(detail.getExtra(), DoctorChgFarmDto.class);
            DoctorChgFarmExportDto dto = BeanMapper.map(chgFarm, DoctorChgFarmExportDto.class);
            chgFarmLists.add(dto);
        }
        return new Paging<>(pigEventPaging.getTotal(), chgFarmLists);
    }

    /**
     * 新建猪
     */
    public Paging<DoctorNewExportGroup> pagingNewGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorNewExportGroup> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            DoctorNewExportGroup exportData = BeanMapper.map(doctorGroupEventDetail, DoctorNewExportGroup.class);
            DoctorNewGroupEvent newGroupEvent = JSON_MAPPER.fromJson(exportData.getExtra(), DoctorNewGroupEvent.class);
            return exportData;
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }

    public Paging<DoctorMoveInGroupExportDto> pagingMoveInGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorMoveInGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            DoctorMoveInGroupExportDto exportData = BeanMapper.map(doctorGroupEventDetail, DoctorMoveInGroupExportDto.class);
            DoctorMoveInGroupEvent moveInGroupEvent = JSON_MAPPER.fromJson(exportData.getExtra(), DoctorMoveInGroupEvent.class);
            exportData.setSource(PigSource.from(moveInGroupEvent.getSource()).getDesc());
            exportData.setBreedName(moveInGroupEvent.getBreedName());
            exportData.setSex(DoctorGroupTrack.Sex.from(moveInGroupEvent.getSex()).getDesc());
            exportData.setSowQty(moveInGroupEvent.getSowQty());
            exportData.setBoarQty(moveInGroupEvent.getBoarQty());
            exportData.setInTypeName(moveInGroupEvent.getInTypeName());
            return exportData;
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }
    public Paging<DoctorChangeGroupExportDto> pagingChangeGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorChangeGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            DoctorChangeGroupExportDto exportData = BeanMapper.map(doctorGroupEventDetail, DoctorChangeGroupExportDto.class);
            DoctorChangeGroupEvent changeGroupEvent = JSON_MAPPER.fromJson(exportData.getExtra(), DoctorChangeGroupEvent.class);
            exportData.setChangeTypeName(changeGroupEvent.getChangeTypeName());
            exportData.setChangeReasonName(changeGroupEvent.getChangeReasonName());
            return exportData;
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }
    public Paging<DoctorChgFarmGroupExportDto> pagingChgFramGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorChgFarmGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            DoctorChgFarmGroupExportDto exportData = BeanMapper.map(doctorGroupEventDetail, DoctorChgFarmGroupExportDto.class);
            DoctorTransGroupEvent transGroupEvent = JSON_MAPPER.fromJson(exportData.getExtra(), DoctorTransGroupEvent.class);
            exportData.
            return exportData;
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }
    public Paging<DoctorDiseaseGroupExportDto> pagingDiseaseGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorDiseaseGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            DoctorDiseaseGroupExportDto exportData = BeanMapper.map(doctorGroupEventDetail, DoctorDiseaseGroupExportDto.class);
            return exportData;
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }
    public Paging<DoctorVaccinationGroupExportDto> pagingVaccinationGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorVaccinationGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            DoctorVaccinationGroupExportDto exportData = BeanMapper.map(doctorGroupEventDetail, DoctorVaccinationGroupExportDto.class);
            return exportData;
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }
    public Paging<DoctorTransGroupExportDto> pagingTransGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorTransGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            DoctorTransGroupExportDto exportData = BeanMapper.map(doctorGroupEventDetail, DoctorTransGroupExportDto.class);
            DoctorTransGroupEvent transGroupEvent = JSON_MAPPER.fromJson(exportData.getExtra(), DoctorTransGroupEvent.class);
            exportData.setToBarnName(transGroupEvent.getToBarnName());
            exportData.setToGroupCode(transGroupEvent.getToGroupCode());

            return exportData;
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }
    public Paging<DoctorTurnSeedGroupExportDto> pagingTurnSeedGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorTurnSeedGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            DoctorTurnSeedGroupExportDto exportData = BeanMapper.map(doctorGroupEventDetail, DoctorTurnSeedGroupExportDto.class);
            return exportData;
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

    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void eventExport(@RequestParam Map<String, String> eventCriteria, HttpServletRequest request, HttpServletResponse response){
        try {
            log.info("event.export.starting");
            if (Strings.isNullOrEmpty(eventCriteria.get("kind"))) {
                return;
            }
            if (Objects.equals(eventCriteria.get("kind"), "4")) {
                exporter.export("web-group-event", eventCriteria, 1, 500, this::pagingNewGroup, request, response);
            } else {
                eventCriteria.put("ordered","0");
                exporter.export("web-pig-event", eventCriteria, 1, 500, this::pagingPigEvent, request, response);
            }
            log.info("event.export.ending");
        } catch (Exception e) {
            log.error("event.export.failed");
        }
    }

}