package io.terminus.doctor.web.front.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorNpdExportDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.DoctorPigSalesExportDto;
import io.terminus.doctor.event.dto.DoctorSowParityAvgDto;
import io.terminus.doctor.event.dto.DoctorSowParityCount;
import io.terminus.doctor.event.dto.event.DoctorEventOperator;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorEventModifyRequestWriteService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorPigEventWriteService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.doctor.web.core.export.Exporter;
import io.terminus.doctor.web.front.event.dto.DoctorGroupEventDetail;
import io.terminus.doctor.web.front.event.dto.DoctorGroupEventExportData;
import io.terminus.doctor.web.front.event.dto.DoctorPigEventExportData;
import io.terminus.doctor.web.util.TransFromUtil;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
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
import java.util.*;
import java.util.stream.Collectors;

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

    @RpcConsumer
    private DoctorEventModifyRequestWriteService doctorEventModifyRequestWriteService;
    @RpcConsumer
    private DoctorUserProfileReadService doctorUserProfileReadService;
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

            Paging<DoctorPigEvent> doctorPigEventPaging = RespHelper.or500(doctorPigEventReadService.queryPigDoctorEvents(farmId,
                    pigId, pageNo, pageSize, beginDateTime, endDateTime));
            transFromUtil.transFromExtraMap(doctorPigEventPaging.getData());
            return doctorPigEventPaging;
        } catch (Exception e) {
            log.error("pig event paging error, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, "paging.pigEvent.error");
        }
    }

    @RequestMapping(value = "/pagingRollbackPigEvent", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<DoctorPigEvent> pagingPigEventWithRollback(@RequestParam("farmId") Long farmId,
                                                              @RequestParam("pigId") Long pigId,
                                                              @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                              @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                              @RequestParam(value = "startDate", required = false) String startDate,
                                                              @RequestParam(value = "endDate", required = false) String endDate) {
       return pagingDoctorPigEvent(farmId, pigId, pageNo, pageSize, startDate, endDate);

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
    public Paging<DoctorPigEvent> queryPigEventsByCriteria(@RequestParam Map<String, Object> params, @RequestParam(required = false) Integer pageNo, @RequestParam(required = false) Integer pageSize) {
        if (params == null || params.isEmpty()) {
            return Paging.empty();
        }
        params = Params.filterNullOrEmpty(params);
        if (params.get("eventTypes") != null) {
            params.put("types", Splitters.COMMA.splitToList((String) params.get("eventTypes")));
            params.remove("eventTypes");
        }

        if (StringUtils.isNotBlank((String)params.get("barnTypes"))) {
            params.put("barnTypes", Splitters.UNDERSCORE.splitToList((String) params.get("barnTypes")));
        }

        if (StringUtils.isNotBlank((String) params.get("pigCode"))) {
            params.put("pigCodeFuzzy", params.get("pigCode"));
            params.remove("pigCode");
        }
        Response<Paging<DoctorPigEvent>> pigEventPagingResponse = doctorPigEventReadService.queryPigEventsByCriteria(params, pageNo, pageSize);
        if (!pigEventPagingResponse.isSuccess()) {
            return Paging.empty();
        }
        transFromUtil.transFromExtraMap(pigEventPagingResponse.getResult().getData());
        return pigEventPagingResponse.getResult();
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
     * 合计分娩事件
     * @param params
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/getabosum", method = RequestMethod.GET)
    @ResponseBody
    public DoctorPigEvent getabosum(@RequestParam Map<String, Object> params, @RequestParam(required = false) Integer pageNo, @RequestParam(required = false) Integer pageSize) {
        params.put("ordered",0);
        params = Params.filterNullOrEmpty(params);
        if (params.get("eventTypes") != null) {
            params.put("types", Splitters.COMMA.splitToList((String) params.get("eventTypes")));
            params.remove("eventTypes");
        }

        if (StringUtils.isNotBlank((String)params.get("barnTypes"))) {
            params.put("barnTypes", Splitters.UNDERSCORE.splitToList((String) params.get("barnTypes")));
        }

        if (StringUtils.isNotBlank((String) params.get("pigCode"))) {
            params.put("pigCodeFuzzy", params.get("pigCode"));
            params.remove("pigCode");
        }
        Response<DoctorPigEvent> pigEventPagingResponse = doctorPigEventReadService.getabosum(params, pageNo, pageSize);
        return pigEventPagingResponse.getResult();
    }

    /**
     * 合计断奶事件
     * @param params
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/getweansum", method = RequestMethod.GET)
    @ResponseBody
    public DoctorPigEvent getweansum(@RequestParam Map<String, Object> params, @RequestParam(required = false) Integer pageNo, @RequestParam(required = false) Integer pageSize) {
        params.put("ordered",0);
        params = Params.filterNullOrEmpty(params);
        if (params.get("eventTypes") != null) {
            params.put("types", Splitters.COMMA.splitToList((String) params.get("eventTypes")));
            params.remove("eventTypes");
        }

        if (StringUtils.isNotBlank((String)params.get("barnTypes"))) {
            params.put("barnTypes", Splitters.UNDERSCORE.splitToList((String) params.get("barnTypes")));
        }

        if (StringUtils.isNotBlank((String) params.get("pigCode"))) {
            params.put("pigCodeFuzzy", params.get("pigCode"));
            params.remove("pigCode");
        }
        Response<DoctorPigEvent> pigEventPagingResponse = doctorPigEventReadService.getweansum(params, pageNo, pageSize);
        return pigEventPagingResponse.getResult();
    }

    /**
     * 合计拼窝事件
     * @param params
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/getfosterssum", method = RequestMethod.GET)
    @ResponseBody
    public DoctorPigEvent getfosterssum(@RequestParam Map<String, Object> params, @RequestParam(required = false) Integer pageNo, @RequestParam(required = false) Integer pageSize) {
        params.put("ordered",0);
        params = Params.filterNullOrEmpty(params);
        if (params.get("eventTypes") != null) {
            params.put("types", Splitters.COMMA.splitToList((String) params.get("eventTypes")));
            params.remove("eventTypes");
        }

        if (StringUtils.isNotBlank((String)params.get("barnTypes"))) {
            params.put("barnTypes", Splitters.UNDERSCORE.splitToList((String) params.get("barnTypes")));
        }

        if (StringUtils.isNotBlank((String) params.get("pigCode"))) {
            params.put("pigCodeFuzzy", params.get("pigCode"));
            params.remove("pigCode");
        }
        Response<DoctorPigEvent> pigEventPagingResponse = doctorPigEventReadService.getfosterssum(params, pageNo, pageSize);
        return pigEventPagingResponse.getResult();
    }

    /**
     * 合计仔猪变动事件
     * @param params
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/getpigletssum", method = RequestMethod.GET)
    @ResponseBody
    public DoctorPigEvent getpigletssum(@RequestParam Map<String, Object> params, @RequestParam(required = false) Integer pageNo, @RequestParam(required = false) Integer pageSize) {
        params.put("ordered",0);
        params = Params.filterNullOrEmpty(params);
        if (params.get("eventTypes") != null) {
            params.put("types", Splitters.COMMA.splitToList((String) params.get("eventTypes")));
            params.remove("eventTypes");
        }

        if (StringUtils.isNotBlank((String)params.get("barnTypes"))) {
            params.put("barnTypes", Splitters.UNDERSCORE.splitToList((String) params.get("barnTypes")));
        }

        if (StringUtils.isNotBlank((String) params.get("pigCode"))) {
            params.put("pigCodeFuzzy", params.get("pigCode"));
            params.remove("pigCode");
        }
        Response<DoctorPigEvent> pigEventPagingResponse = doctorPigEventReadService.getpigletssum(params, pageNo, pageSize);
        return pigEventPagingResponse.getResult();
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

    private Paging<DoctorGroupEventDetail> queryGroupEventsByCriteria(Map<String, Object> params, Integer pageNo, Integer pageSize) {
        if (params == null || params.isEmpty()) {
            return Paging.empty();
        }
        params = Params.filterNullOrEmpty(params);
        if (params.get("eventTypes") != null) {
            params.put("types", Splitters.COMMA.splitToList((String) params.get("eventTypes")));
            params.remove("eventTypes");
        }

        if (StringUtils.isNotBlank((String) params.get("pigTypes"))) {
            params.put("pigTypes", Splitters.UNDERSCORE.splitToList((String)params.get("pigTypes")));
        }

        if (StringUtils.isNotBlank((String) params.get("changeTypeIds"))) {
            params.put("changeTypeIds", Splitters.UNDERSCORE.splitToList((String)params.get("changeTypeIds")));
        }
        if (StringUtils.isNotBlank((String) params.get("groupCode"))) {
            params.put("groupCodeFuzzy", params.get("groupCode"));
            params.remove("groupCode");
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
        // 合计：不用每页显示合计，根据筛选条件出来的所有数据最后一条显示合计 （陈娟 2018-09-06）
        Long total = pagingResponse.getResult().getTotal();
        if(pageNo==null){
            if(total<=20){
                List<DoctorGroupEvent> doctorGroupEvents = RespHelper.or500(doctorGroupReadService.getGroupEventsByCriteria(params));
                DoctorGroupEventDetail detail = this.aggregatedPigs(doctorGroupEvents);
                groupEventDetailList.add(detail);
            }
        }else{
            if(total<=pageNo*pageSize){
                List<DoctorGroupEvent> doctorGroupEvents = RespHelper.or500(doctorGroupReadService.getGroupEventsByCriteria(params));
                DoctorGroupEventDetail detail = this.aggregatedPigs(doctorGroupEvents);
                groupEventDetailList.add(detail);
            }
        }
        return new Paging<>(pagingResponse.getResult().getTotal(), groupEventDetailList);
    }

    // 猪群合计 （陈娟 2018-09-05）
    private DoctorGroupEventDetail aggregatedPigs(List<DoctorGroupEvent> doctorGroupEventList){
        Integer sumQuantity=new Integer(0);// 总猪只数
        Double sumWeight=new Double(0);// 总重，
        Long sumAmount=new Long(0);// 总价值，
        Integer sumBoarQty=new Integer(0);// 公猪数，
        Integer sumSowQty=new Integer(0);// 母猪数，
        for (DoctorGroupEvent doctorGroupEvent: doctorGroupEventList) {
            if(doctorGroupEvent.getQuantity()==null){
                sumQuantity = sumQuantity + 0;
            }else{
                sumQuantity = sumQuantity + doctorGroupEvent.getQuantity();
            }

            if(doctorGroupEvent.getWeight()==null){
                sumWeight = sumWeight + 0;
            }else{
                sumWeight = sumWeight + doctorGroupEvent.getWeight();
            }

            if(doctorGroupEvent.getAmount()==null){
                sumAmount = sumAmount + 0;
            }else{
                sumAmount = sumAmount + doctorGroupEvent.getAmount();
            }

            String extra = doctorGroupEvent.getExtra();
            //1、使用JSONObject
            JSONObject jsonObject=JSONObject.fromObject(extra);
            DoctorGroupEventDetail eventDetail=(DoctorGroupEventDetail)JSONObject.toBean(jsonObject, DoctorGroupEventDetail.class);
            if(eventDetail.getBoarQty()==null){
                sumBoarQty = sumBoarQty + 0;
            }else{
                sumBoarQty = sumBoarQty + eventDetail.getBoarQty();
            }

            if(eventDetail.getSowQty()==null){
                sumSowQty = sumSowQty + 0;
            }else{
                sumSowQty = sumSowQty + eventDetail.getSowQty();
            }
        }

        DoctorGroupEventDetail detail=new DoctorGroupEventDetail();
        detail.setSumQuantity(sumQuantity);
        detail.setSumWeight(sumWeight);
        detail.setSumAmount(sumAmount);
        detail.setSumBoarQty(sumBoarQty);
        detail.setSumSowQty(sumSowQty);
        return detail;
    }

    /**
     * 分页猪事件
     * @param pigEventCriteria 查询猪事件条件
     * @return 分页导出猪事件数据
     */
    private Paging<DoctorPigEventExportData> pagingPigEvent(Map<String, String> pigEventCriteria) {
        Map<String, Object> criteriaMap = OBJECT_MAPPER.convertValue(pigEventCriteria, Map.class);
        Paging<DoctorPigEvent> pigEventPaging = queryPigEventsByCriteria(criteriaMap, Integer.parseInt(pigEventCriteria.get("pageNo")), Integer.parseInt(pigEventCriteria.get("size")));
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
     * 非生产天数的报表导出
     */
    @RequestMapping(value = "/eventNpd/export", method = RequestMethod.GET)
    @ResponseBody
    public void pagingNpdExport(@RequestParam Map<String, String> pigEventCriteria,
                                                      HttpServletRequest request, HttpServletResponse response) {
        exporter.export("web-sow-npd",pigEventCriteria, 1, 500, this::pagingNpdPigEvent, request, response);
    }
    @RequestMapping(value = "/eventNpd", method = RequestMethod.GET)
    @ResponseBody
    public Paging<DoctorNpdExportDto> pagingNpd(@RequestParam Map<String, String> pigEventCriteria, Integer pageNo, Integer pageSize) {

        Map<String, Object> criteria = OBJECT_MAPPER.convertValue(pigEventCriteria, Map.class);
        return RespHelper.or500(doctorPigEventReadService.pagingFindNpd(criteria, pageNo, pageSize));
    }

    /**
     * ysq
     * @param pigNpd
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/getNpd", method = RequestMethod.GET)
    @ResponseBody
    public Response getNpd(@RequestParam Map<String, String> pigNpd, Integer pageNo, Integer pageSize) {

        Map<String, Object> criteria = OBJECT_MAPPER.convertValue(pigNpd, Map.class);
        return doctorPigEventReadService.findNpd(criteria, pageNo, pageSize);
    }

    public Paging<DoctorNpdExportDto> pagingNpdPigEvent(Map<String, String> pigEventCriteria) {

        Map<String, Object> criteria = OBJECT_MAPPER.convertValue(pigEventCriteria, Map.class);
        Integer pageNo = Integer.parseInt((String)criteria.get("pageNo"));
        Integer size = Integer.parseInt((String)criteria.get("size"));
        return RespHelper.or500(doctorPigEventReadService.pagingFindNpd(criteria, pageNo, size));
    }

    /**
     * 猪的销售表
     */
    @RequestMapping(value = "/sales", method = RequestMethod.GET)
    @ResponseBody
    public Paging<DoctorPigSalesExportDto> pagingPigSales(@RequestParam(required = false) Map<String, Object> pigEventCriteria,@RequestParam(required = false)Integer pageNo,@RequestParam(required = false) Integer pageSize,@RequestParam(required = false) String date) {

        pigEventCriteria = Params.filterNullOrEmpty(pigEventCriteria);
        DateTime dateTime = DateTime.parse(date);
        String startDate = dateTime.toString(DateUtil.DATE);
        String endDate = DateUtil.getMonthEnd(dateTime).toString(DateUtil.DATE);
        pigEventCriteria.put("startDate", startDate);
        pigEventCriteria.put("endDate", endDate);
        return RespHelper.or500(doctorPigEventReadService.pagingFindSales(pigEventCriteria, pageNo, pageSize));
    }

    /**
     * 猪的销售表
     */
    @RequestMapping(value = "/list/sales", method = RequestMethod.GET)
    @ResponseBody
    public List<DoctorPigSalesExportDto> listPigSales(@RequestParam(required = false) Map<String, Object> pigEventCriteria,
                                                      @RequestParam(required = false) Integer breedsId,
                                                      @RequestParam(required = false,value = "startDate") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                      @RequestParam(required = false,value = "endDate") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        pigEventCriteria = Params.filterNullOrEmpty(pigEventCriteria);
        if (null != startDate && null != endDate && startDate.after(endDate))
            throw new JsonResponseException("start.date.after.end.date");
           pigEventCriteria.put("startDate", startDate);
           pigEventCriteria.put("endDate", endDate);
           pigEventCriteria.put("breedsId", breedsId);
        return RespHelper.or500(doctorPigEventReadService.listFindSales(pigEventCriteria));
    }
    /**
     * 获取某一头某一胎次下未断奶数量
     * @param pigId 猪id
     * @param parity 胎次
     * @return 未断奶数
     */
    @RequestMapping(value = "/findUnWeanCount", method = RequestMethod.GET)
    @ResponseBody
    public Integer findUnWeanCount(@RequestParam Long pigId, @RequestParam Integer parity) {
        return RespHelper.or500(doctorPigEventReadService.findUnWeanCountByParity(pigId, parity));
    }
}