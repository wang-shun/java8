package io.terminus.doctor.web.front.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.DoctorSowParityCount;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorPigEventWriteService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.web.util.TransFromUtil;
import io.terminus.doctor.workflow.core.WorkFlowService;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final WorkFlowService workFlowService;

    private final TransFromUtil transFromUtil;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Autowired
    public DoctorPigEvents(DoctorPigReadService doctorPigReadService,
                           DoctorPigEventReadService doctorPigEventReadService,
                           DoctorPigEventWriteService doctorPigEventWriteService,
                           UserReadService userReadService,
                           WorkFlowService workFlowService, TransFromUtil transFromUtil) {
        this.doctorPigReadService = doctorPigReadService;
        this.doctorPigEventReadService = doctorPigEventReadService;
        this.doctorPigEventWriteService = doctorPigEventWriteService;
        this.userReadService = userReadService;
        this.workFlowService = workFlowService;
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
            DoctorPigEvent doctorPigEvent = RespHelper.or500(doctorPigEventReadService.canRollbackEvent(pigId));
            return doctorPigEventPaging;
        } catch (Exception e) {
            log.error("pig event paging error, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, "paging.pigEvent.error");
        }
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

    @RequestMapping(value = "/queryDoctorSowParityCount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DoctorSowParityCount> queryDoctorSowParityCount(@RequestParam("pigId") Long pigId) {
        return RespHelper.or500(doctorPigEventReadService.querySowParityCount(pigId));
    }

    /**
     * 同步流程数据
     * @param key
     * @param businessId
     * @return
     */
    @RequestMapping(value = "/updateData", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean updateData(@RequestParam("key") String key, @RequestParam(value = "businessId", required = false) Long businessId) {
        return RespHelper.or500(workFlowService.updateData(key, businessId));
    }

    @RequestMapping(value = "/pigPaging", method = RequestMethod.GET)
    @ResponseBody
    public Paging<DoctorPigEvent> queryPigEventsByCriteria(@RequestParam Map<String, Object> params, @RequestParam(required = false) Integer pageNo, @RequestParam(required = false) Integer pageSize) {
        if (params == null || params.isEmpty()) {
            return Paging.empty();
        }
        params = Params.filterNullOrEmpty(params);
        if (params.containsKey("eventName")) {
            params.put("type", PigEvent.fromDesc((String) params.get("eventName")).getKey().toString());
            params.remove("eventName");
        }
        return RespHelper.or500(doctorPigEventReadService.queryPigEventsByCriteria(params, pageNo, pageSize));
    }

    @RequestMapping(value = "/pigEvents")
    @ResponseBody
    public List<String> queryPigEvents(@RequestParam String types) {
        List<PigEvent> events = PigEvent.from(Splitters.UNDERSCORE.splitToList(types).stream().filter(type -> StringUtils.isNotBlank(type)).map(type -> Integer.parseInt(type)).collect(Collectors.toList()));
        return events.stream().map(pigEvent -> pigEvent.getDesc()).collect(Collectors.toList());
    }

}
