package io.terminus.doctor.event.service;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.cache.DoctorDailyReportCache;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.dto.report.DoctorDailyReportDto;
import io.terminus.doctor.event.model.DoctorDailyReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Desc: 猪场日报表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-19
 */
@Slf4j
@Service
@RpcProvider
public class DoctorDailyReportReadServiceImpl implements DoctorDailyReportReadService {

    private final DoctorDailyReportDao doctorDailyReportDao;
    private final DoctorDailyReportCache doctorDailyReportCache;
    private final DoctorDailyPigReportReadService doctorDailyPigReportReadService;
    private final DoctorDailyGroupReportReadService doctorDailyGroupReportReadService;

    @Autowired
    public DoctorDailyReportReadServiceImpl(DoctorDailyReportDao doctorDailyReportDao,
                                            DoctorDailyReportCache doctorDailyReportCache,
                                            DoctorDailyPigReportReadService doctorDailyPigReportReadService,
                                            DoctorDailyGroupReportReadService doctorDailyGroupReportReadService) {
        this.doctorDailyReportDao = doctorDailyReportDao;
        this.doctorDailyReportCache = doctorDailyReportCache;
        this.doctorDailyPigReportReadService = doctorDailyPigReportReadService;
        this.doctorDailyGroupReportReadService = doctorDailyGroupReportReadService;
    }

    @Override
    public Response<DoctorDailyReport> findDailyReportById(Long dailyReportId) {
        try {
            return Response.ok(doctorDailyReportDao.findById(dailyReportId));
        } catch (Exception e) {
            log.error("find dailyReport by id failed, dailyReportId:{}, cause:{}", dailyReportId, Throwables.getStackTraceAsString(e));
            return Response.fail("dailyReport.find.fail");
        }
    }

    @Override
    public Response<DoctorDailyReportDto> findDailyReportByFarmIdAndSumAtWithCache(Long farmId, String sumAt) {
        try {
            Date date = DateUtil.toDate(sumAt);
            DoctorDailyReportDto report = doctorDailyReportCache.getDailyReport(farmId, date);

            //如果缓存里不存在, 直接查数据库
            if (report == null) {
                report = getDailyReport(farmId, date);
            }

            //如果数据库里不存在, 重新计算
            if (report == null) {
                report = RespHelper.orServEx(initDailyReportByFarmIdAndDate(farmId, date));
            }

            //如果计算结果为空, 返回初始化的日报
            if (report == null) {
                return Response.ok(new DoctorDailyReportDto());
            }
            doctorDailyReportCache.putDailyReport(farmId, date, report);
            return Response.ok(report);
        } catch (Exception e) {
            log.error("find dailyReport by farm id and sumat fail, farmId:{}, sumat:{}, cause:{}",
                    farmId, sumAt, Throwables.getStackTraceAsString(e));
            return Response.fail("dailyReport.find.fail");
        }
    }

    @Override
    public Response<List<DoctorDailyReportDto>> initDailyReportByDate(Date date) {
        try {
            return Response.ok(setReportFromPigAndGroupByDate(date));
        } catch (Exception e) {
            log.error("init daily report failed, date:{}, cause:{}", date, Throwables.getStackTraceAsString(e));
            return Response.fail("init.daily.report.fail");
        }
    }

    @Override
    public Response<DoctorDailyReportDto> initDailyReportByFarmIdAndDate(Long farmId, Date date) {
        try {
            //设置统计结果, 不修改report的引用
            DoctorDailyReportDto report = doctorDailyReportCache.getDailyReport(farmId, date);
            report.setPig(RespHelper.orServEx(doctorDailyPigReportReadService.countByFarmIdDate(farmId, date)));
            report.setGroup(RespHelper.orServEx(doctorDailyGroupReportReadService.getGroupDailyReportByFarmIdAndDate(farmId, date)));
            return Response.ok(report);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("init daily report failed, farmId:{}, date:{}, cause:{}", farmId, date, Throwables.getStackTraceAsString(e));
            return Response.fail("init.daily.report.fail");
        }
    }

    //根据farmId和sumAt从数据库查询, 并转换成日报统计
    private DoctorDailyReportDto getDailyReport(Long farmId, Date sumAt) {
        DoctorDailyReport report = doctorDailyReportDao.findByFarmIdAndSumAt(farmId, sumAt);

        //如果没有查到, 要返回null, 交给上层判断
        if (report == null || Strings.isNullOrEmpty(report.getData())) {
            return null;
        }
        return report.getReportData();
    }

    //拼接猪和猪群的日报统计
    private List<DoctorDailyReportDto> setReportFromPigAndGroupByDate(Date date) {
        Map<Long, DoctorDailyReportDto> pigReportMap = RespHelper.orServEx(doctorDailyPigReportReadService.countByDate(date))
                .stream().collect(Collectors.toMap(DoctorDailyReportDto::getFarmId, v -> v));
        Map<Long, DoctorDailyReportDto> groupReportMap = RespHelper.orServEx(doctorDailyGroupReportReadService.getGroupDailyReportsByDate(date))
                .stream().collect(Collectors.toMap(DoctorDailyReportDto::getFarmId, v -> v));

        log.info("daily report info: date:{}, pigReport:{}, groupReport:{}", date, pigReportMap, groupReportMap);

        //求下 farmIds 的并集
        Set<Long> farmIds = pigReportMap.keySet();
        farmIds.addAll(groupReportMap.keySet());

        //拼接数据
        return farmIds.stream()
                .map(farmId -> {
                    DoctorDailyReportDto report = new DoctorDailyReportDto();
                    report.setPig(pigReportMap.get(farmId));
                    report.setGroup(groupReportMap.get(farmId));
                    return report;
                })
                .collect(Collectors.toList());
    }
}
