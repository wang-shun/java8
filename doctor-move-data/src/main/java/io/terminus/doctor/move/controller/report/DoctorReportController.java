package io.terminus.doctor.move.controller.report;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.service.DoctorDailyReportV2Service;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

import static java.util.Objects.isNull;

/**
 * Created by xjn on 17/12/12.
 * email:xiaojiannan@terminus.io
 * 手动刷新报表入口
 */

@Slf4j
@RestController
@RequestMapping("/api/doctor/report")
public class DoctorReportController {

    private final DoctorDailyReportV2Service doctorDailyReportV2Service;
    private final DoctorFarmReadService doctorFarmReadService;

    @Autowired
    public DoctorReportController(DoctorDailyReportV2Service doctorDailyReportV2Service, DoctorFarmReadService doctorFarmReadService) {
        this.doctorDailyReportV2Service = doctorDailyReportV2Service;
        this.doctorFarmReadService = doctorFarmReadService;
    }

    /**
     *
     * @param farmId 猪场id 可选，默认全部猪场
     * @param from 开始时间 yyyy-MM-dd
     * @param to 结束时间，可选，默认当前 yyyy-MM-dd
     * @return 是否成功
     */
    @RequestMapping(value = "/flush/farm/daily", method = RequestMethod.GET)
    public Boolean flushFarmDaily(@RequestParam(required = false) Long farmId,
                                  @RequestParam String from,
                                  @RequestParam(required = false) String to) {
        if (isNull(to)) {
            to = DateUtil.toDateString(new Date());
        }
        if (!isNull(farmId)) {
            return RespHelper.or500(doctorDailyReportV2Service.flushFarmDaily(farmId, from, to));
        }

        String temp = to;
        List<DoctorFarm> farmList = RespHelper.or500(doctorFarmReadService.findAllFarms());
        farmList.stream().parallel().forEach(doctorFarm ->
        RespHelper.or500(doctorDailyReportV2Service.flushFarmDaily(doctorFarm.getId(), from, temp)));
        return Boolean.TRUE;
    }

    /**
     *
     * @param farmId 猪场id 可选，默认全部猪场
     * @param from 开始时间 yyyy-MM-dd
     * @param to 结束时间，可选，默认当前 yyyy-MM-dd
     * @return 是否成功
     */
    @RequestMapping(value = "/flush/group/daily", method = RequestMethod.GET)
    public Boolean flushGroupDaily(@RequestParam(required = false) Long farmId,
                                   @RequestParam String from,
                                   @RequestParam(required = false) String to) {
        if (isNull(to)) {
            to = DateUtil.toDateString(new Date());
        }

        if (!isNull(farmId)) {
            return RespHelper.or500(doctorDailyReportV2Service.flushGroupDaily(farmId, from, to));
        }

        String temp = to;
        List<DoctorFarm> farmList = RespHelper.or500(doctorFarmReadService.findAllFarms());
        farmList.stream().parallel().forEach(doctorFarm ->
                RespHelper.or500(doctorDailyReportV2Service.flushGroupDaily(doctorFarm.getId(), from, temp)));
        return Boolean.TRUE;
    }

    /**
     *
     * @param farmId 猪场id 可选，默认全部猪场
     * @param from 开始时间 yyyy-MM-dd
     * @param to 结束时间，可选，默认当前 yyyy-MM-dd
     * @return 是否成功
     */
    @RequestMapping(value = "/flush/pig/daily", method = RequestMethod.GET)
    public Boolean flushPigDaily(@RequestParam(required = false) Long farmId,
                                 @RequestParam String from,
                                 @RequestParam(required = false) String to) {
        if (isNull(to)) {
            to = DateUtil.toDateString(new Date());
        }

        if (!isNull(farmId)) {
            return RespHelper.or500(doctorDailyReportV2Service.flushPigDaily(farmId, from, to));
        }

        String temp = to;
        List<DoctorFarm> farmList = RespHelper.or500(doctorFarmReadService.findAllFarms());
        farmList.stream().parallel().forEach(doctorFarm ->
                RespHelper.or500(doctorDailyReportV2Service.flushPigDaily(doctorFarm.getId(), from, temp)));
        return Boolean.TRUE;
    }
}
