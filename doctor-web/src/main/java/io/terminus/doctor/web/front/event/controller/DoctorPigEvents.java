package io.terminus.doctor.web.front.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorPigEventWriteService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
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

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Autowired
    public DoctorPigEvents(DoctorPigReadService doctorPigReadService,
                           DoctorPigEventReadService doctorPigEventReadService,
                           DoctorPigEventWriteService doctorPigEventWriteService,
                           UserReadService userReadService){
        this.doctorPigReadService = doctorPigReadService;
        this.doctorPigEventReadService = doctorPigEventReadService;
        this.doctorPigEventWriteService = doctorPigEventWriteService;
        this.userReadService = userReadService;
    }

    /**
     * 回滚事件信息
     * @param pigEventId
     * @param revertPigType
     * @return
     */
    @RequestMapping(value = "/rollBackPigEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long rollBackPigEvent(@RequestParam("pigEventId") Long pigEventId,
                                 @RequestParam("revertPigType") Integer revertPigType){
        Long userId = null;
        String userName = null;
        try{
            userId = UserUtil.getUserId();
            Response<User> userResponse = userReadService.findById(userId);
            userName = userResponse.getResult().getName();
        }catch (Exception e){
            log.error("get user info error, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("query.userInfo.error");
        }
        return RespHelper.or500(doctorPigEventWriteService.rollBackPigEvent(pigEventId, revertPigType, userId, userName));
    }

    @RequestMapping(value = "/pagingDoctorInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<DoctorPigInfoDto> pagingPigDoctorInfoByBarn(@RequestParam("farmId") Long farmId,
                                                              @RequestParam("barnId") Long branId,
                                                              @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                              @RequestParam(value = "pageSize", required = false)Integer pageSize){

        return RespHelper.or500(doctorPigReadService.pagingDoctorInfoDtoByPigTrack(DoctorPigTrack.builder()
                .farmId(farmId).currentBarnId(branId).build(), pageNo, pageSize));
    }

    @RequestMapping(value = "/pagingPigEvent", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<DoctorPigEvent> pagingDoctorPigEvent(@RequestParam("farmId") Long farmId,
                                                       @RequestParam("pigId") Long pigId,
                                                       @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                       @RequestParam(value = "pageSize", required = false)Integer pageSize,
                                                       @RequestParam(value = "startDate",required = false) String startDate,
                                                       @RequestParam(value = "endDate", required = false) String endDate){
        try{
            Date beginDateTime = Strings.isNullOrEmpty(startDate) ? null : DTF.parseDateTime(startDate).withTimeAtStartOfDay().toDate();
            Date endDateTime = Strings.isNullOrEmpty(endDate) ? null : (DTF).parseDateTime(endDate).plusDays(1).withTimeAtStartOfDay().toDate(); // 添加一天

            return RespHelper.or500(doctorPigEventReadService.queryPigDoctorEvents(farmId,pigId,pageNo, pageSize, beginDateTime, endDateTime));
        }catch (Exception e){
            log.error("pig event paging error, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, "paging.pigEvent.error");
        }
    }

    @RequestMapping(value = "/queryPigEvents", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Integer> queryPigExecuteEvent(@RequestParam("ids") String ids){
        List<Long> pigIds = null;
        try{
            pigIds = OBJECT_MAPPER.readValue(ids, JacksonType.LIST_OF_LONG);
        }catch (Exception e){
            log.error("query pig execute event error, ids:{} cause:{}", ids, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("query.executeEvent.fail");
        }
        return RespHelper.or500(doctorPigEventReadService.queryPigEvents(pigIds));
    }
}
