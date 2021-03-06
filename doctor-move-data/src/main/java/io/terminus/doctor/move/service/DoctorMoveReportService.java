package io.terminus.doctor.move.service;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.service.DoctorBoarMonthlyReportWriteService;
import io.terminus.doctor.event.service.DoctorDailyReportWriteService;
import io.terminus.doctor.event.service.DoctorParityMonthlyReportWriteService;
import io.terminus.doctor.event.service.DoctorRangeReportWriteService;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.move.model.ReportBoarLiveStock;
import io.terminus.doctor.move.model.ReportGroupLiveStock;
import io.terminus.doctor.move.model.ReportSowLiveStock;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorDepartmentReadService;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isEmpty;

/**
 * Desc: 迁移猪场统计数据
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/14
 */
@Slf4j
@Service
public class DoctorMoveReportService {

    private static final int INDEX = 365;    //总共导多少天的数据
    private static final int MONTH_INDEX = 12;    //总共导多少月的数据

    private final DoctorDailyReportDao doctorDailyReportDao;
    private final DoctorFarmDao doctorFarmDao;
    private final DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;

    private final DoctorDailyReportWriteService doctorDailyReportWriteService;
    private final DoctorRangeReportWriteService doctorRangeReportWriteService;
    private final DoctorParityMonthlyReportWriteService doctorParityMonthlyReportWriteService;
    private final DoctorBoarMonthlyReportWriteService doctorBoarMonthlyReportWriteService;
    private final DoctorFarmReadService doctorFarmReadService;
    private final DoctorDepartmentReadService doctorDepartmentReadService;

    @Autowired
    public DoctorMoveReportService(DoctorDailyReportDao doctorDailyReportDao,
                                   DoctorFarmDao doctorFarmDao,
                                   DoctorFarmReadService doctorFarmReadService,
                                   DoctorMoveDatasourceHandler doctorMoveDatasourceHandler,
                                   DoctorDailyReportWriteService doctorDailyReportWriteService,
                                   DoctorRangeReportWriteService doctorRangeReportWriteService,
                                   DoctorParityMonthlyReportWriteService doctorParityMonthlyReportWriteService,
                                   DoctorBoarMonthlyReportWriteService doctorBoarMonthlyReportWriteService,
                                   DoctorDepartmentReadService doctorDepartmentReadService) {
        this.doctorDailyReportDao = doctorDailyReportDao;
        this.doctorFarmDao = doctorFarmDao;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorMoveDatasourceHandler = doctorMoveDatasourceHandler;
        this.doctorDailyReportWriteService = doctorDailyReportWriteService;
        this.doctorRangeReportWriteService = doctorRangeReportWriteService;
        this.doctorParityMonthlyReportWriteService = doctorParityMonthlyReportWriteService;
        this.doctorBoarMonthlyReportWriteService = doctorBoarMonthlyReportWriteService;
        this.doctorDepartmentReadService = doctorDepartmentReadService;
    }


    /**
     * 迁移猪场日报(放在所有事件迁移之后进行)
     * @param moveId 数据源id
     * @param farmId 猪场id
     */
    @Transactional
    public void moveDailyReport(Long moveId, Long farmId, Integer index) {
        DoctorFarm farm = doctorFarmDao.findById(farmId);
        if (farm == null || isEmpty(farm.getOutId())) {
            return;
        }
        index = MoreObjects.firstNonNull(index, INDEX);

        //默认导365天的数据
        List<Date> dates = DateUtil.getBeforeDays(new Date(), index);

        //猪群存栏map
        List<ReportGroupLiveStock> gls = dates.stream()
                .map(date -> {
                    ReportGroupLiveStock group = RespHelper.orServEx(doctorMoveDatasourceHandler
                            .findByHbsSql(moveId, ReportGroupLiveStock.class, "DoctorDailyReport-GroupLiveStock", ImmutableMap.of("sumAt", DateUtil.toDateTimeString(Dates.endOfDay(date)), "farmOutId", farm.getOutId()))).get(0);
                    group.setSumat(Dates.startOfDay(date));
                    return group;
                })
                .collect(Collectors.toList());

        log.info("report group live stock:{}", gls);

        //母猪存栏map
        Map<Date, ReportSowLiveStock> sowMap = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, ReportSowLiveStock.class, "DoctorDailyReport-SowLiveStock", ImmutableMap.of("index", index, "farmOutId", farm.getOutId())))
                .stream().collect(Collectors.toMap(ReportSowLiveStock::getSumat, v -> v));

        log.info("report sow live stock:{}", sowMap);

        //公猪存栏
        Map<Date, ReportBoarLiveStock> boarMap = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, ReportBoarLiveStock.class, "DoctorDailyReport-BoarLiveStock", ImmutableMap.of("index", index, "farmOutId", farm.getOutId())))
                .stream().collect(Collectors.toMap(ReportBoarLiveStock::getSumat, v -> v));

        //取出所有统计数据转换一把
        List<DoctorDailyReport> reports = gls.stream()
                .map(g -> getDailyReport(farmId, g, MoreObjects.firstNonNull(sowMap.get(g.getSumat()), new ReportSowLiveStock()),
                        MoreObjects.firstNonNull(boarMap.get(g.getSumat()), new ReportBoarLiveStock())))
                .collect(Collectors.toList());

        //批量创建日报(先删除, 再创建)
        reports.forEach(report -> doctorDailyReportDao.deleteByFarmIdAndSumAt(farmId, report.getSumAt()));
        doctorDailyReportDao.creates(reports);
    }

    //日报
    private DoctorDailyReport getDailyReport(Long farmId, ReportGroupLiveStock group, ReportSowLiveStock sow, ReportBoarLiveStock boar) {
        DoctorDailyReport report = new DoctorDailyReport();
        report.setFarmId(farmId);
//        report.setSumAt(group.getSumat());
//        report.set;
//        report.setFarrowCount(group.getFarrowCount());      // 当天产房仔猪存栏
//        report.setNurseryCount(group.getNurseryCount());    // 当天保育猪存栏
//        report.setFattenCount(group.getFattenCount());      // 当天育肥猪存栏
//        report.setHoubeiCount(group.getHoubeiCount());      // 当天后备猪的存栏
//        report.setSowCount(sow.getBuruSow() + sow.getKonghuaiSow() + sow.getPeihuaiSow());     // 当天母猪存栏
//        report.setData(ToJsonMapper.JSON_NON_EMPTY_MAPPER.toJson(getDailyReportDto(farmId, group, sow, boar)));

        return report;
    }

    @Transactional
    public void moveDailyReport(Long farmId, Integer index){
        DateUtil.getBeforeDays(DateTime.now().toDate(),MoreObjects.firstNonNull(index, INDEX)).forEach(date ->{
            doctorDailyReportWriteService.createDailyReports(farmId, date);
        });
    }
    /**
     * 迁移猪场月报(放在日报迁移之后进行)
     * @param farmId 猪场id
     */
    @Transactional
    public void moveDoctorRangeReport(Long farmId, Integer index) {
        DateUtil.getBeforeMonthEnds(DateTime.now().toDate(), MoreObjects.firstNonNull(index, MONTH_INDEX))
                .forEach(date -> doctorRangeReportWriteService.flushDoctorRangeReports(farmId, date));
    }

    public void moveDoctorOrgRangeReport(List<Long> orgIds, Date since) {
        Map<Long, List<Long>> orgToFarm = Maps.newHashMap();
        orgIds.forEach(orgId -> {
            Response<List<DoctorFarm>> response = doctorDepartmentReadService.findAllFarmsByOrgId(orgId);
            if (response.isSuccess()) {
                orgToFarm.put(orgId, response.getResult().stream().map(DoctorFarm::getId).collect(Collectors.toList()));
            }
        });
        doctorRangeReportWriteService.generateOrgDoctorRangeReports(orgToFarm, since);

    }

    @Transactional
    public void moveDoctorRangeReport(Long farmId, Date date) {
        doctorRangeReportWriteService.flushDoctorRangeReports(farmId, date);
    }

    @Transactional
    public void moveParityMonthlyReport(Long farmId, Integer index){
        DateUtil.getBeforeMonthEnds(new Date(), MoreObjects.firstNonNull(index, MONTH_INDEX))
                .forEach(date -> doctorParityMonthlyReportWriteService.createMonthlyReport(farmId, date));
    }

    @Transactional
    public void moveBoarMonthlyReport(Long farmId, Integer index) {
        DateUtil.getBeforeMonthEnds(new Date(), MoreObjects.firstNonNull(index, MONTH_INDEX))
                .forEach(date -> doctorBoarMonthlyReportWriteService.createMonthlyReport(farmId, date));
    }

}
