package io.terminus.doctor.web.front.event.controller;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorPigReadService;
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

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public DoctorPigEvents(DoctorPigReadService doctorPigReadService, DoctorPigEventReadService doctorPigEventReadService){
        this.doctorPigReadService = doctorPigReadService;
        this.doctorPigEventReadService = doctorPigEventReadService;
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
                                                       @RequestParam("startDate") String startDate,
                                                       @RequestParam("endDate") String endDate){
        try{
            Date beginDateTime = Strings.isNullOrEmpty(startDate) ? null : DTF.parseDateTime(startDate).toDate();
            Date endDateTime = Strings.isNullOrEmpty(endDate) ? null : (DTF).parseDateTime(endDate).toDate();

            return RespHelper.or500(doctorPigEventReadService.queryPigDoctorEvents(farmId,pigId,pageNo, pageSize, beginDateTime, endDateTime));
        }catch (Exception e){
            log.error("pig event paging error, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, "paging.pigEvent.error");
        }
    }
}
