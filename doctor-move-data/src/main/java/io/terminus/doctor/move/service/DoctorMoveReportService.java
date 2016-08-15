package io.terminus.doctor.move.service;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.service.DoctorDailyReportReadService;
import io.terminus.doctor.event.service.DoctorMonthlyReportWriteService;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.move.model.ReportBoarLiveStock;
import io.terminus.doctor.move.model.ReportGroupLiveStock;
import io.terminus.doctor.move.model.ReportSowLiveStock;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
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

    private final DoctorDailyReportDao doctorDailyReportDao;
    private final DoctorFarmDao doctorFarmDao;
    private final DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;
    private final DoctorDailyReportReadService doctorDailyReportReadService;
    private final DoctorMonthlyReportWriteService doctorMonthlyReportWriteService;

    @Autowired
    public DoctorMoveReportService(DoctorDailyReportDao doctorDailyReportDao,
                                   DoctorFarmDao doctorFarmDao,
                                   DoctorMoveDatasourceHandler doctorMoveDatasourceHandler,
                                   DoctorDailyReportReadService doctorDailyReportReadService,
                                   DoctorMonthlyReportWriteService doctorMonthlyReportWriteService) {
        this.doctorDailyReportDao = doctorDailyReportDao;
        this.doctorFarmDao = doctorFarmDao;
        this.doctorMoveDatasourceHandler = doctorMoveDatasourceHandler;
        this.doctorDailyReportReadService = doctorDailyReportReadService;
        this.doctorMonthlyReportWriteService = doctorMonthlyReportWriteService;
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

        //默认导365天的数据
        index = MoreObjects.firstNonNull(index, INDEX);
        List<ReportGroupLiveStock> gls = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, ReportGroupLiveStock.class, "DoctorDailyReport-GroupLiveStock", ImmutableMap.of("index", index, "farmOutId", farm.getOutId())));

        //母猪存栏map
        Map<Date, ReportSowLiveStock> sowMap = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, ReportSowLiveStock.class, "DoctorDailyReport-SowLiveStock", ImmutableMap.of("index", index, "farmOutId", farm.getOutId())))
                .stream().collect(Collectors.toMap(ReportSowLiveStock::getSumat, v -> v));

        //公猪存栏
        ReportBoarLiveStock boar = RespHelper.orServEx(doctorMoveDatasourceHandler.findByHbsSql(moveId, ReportBoarLiveStock.class, "DoctorDailyReport-BoarLiveStock")).get(0);

        //取出所有统计数据转换一把
        List<DoctorDailyReport> reports = gls.stream()
                .map(g -> getDailyReport(farmId, g, MoreObjects.firstNonNull(sowMap.get(g.getSumat()), new ReportSowLiveStock()), boar))
                .collect(Collectors.toList());

        //批量创建日报
        doctorDailyReportDao.creates(reports);
    }

    //日报
    private DoctorDailyReport getDailyReport(Long farmId, ReportGroupLiveStock group, ReportSowLiveStock sow, ReportBoarLiveStock boar) {
        DoctorDailyReport report = new DoctorDailyReport();
        report.setFarmId(farmId);
        report.setSumAt(group.getSumat());
        report.setFarrowCount(group.getFarrowCount());      // 当天产房仔猪存栏
        report.setNurseryCount(group.getNurseryCount());    // 当天保育猪存栏
        report.setFattenCount(group.getFattenCount());      // 当天育肥猪存栏
        report.setSowCount(sow.getBuruSow() + sow.getKonghuaiSow() + sow.getPeihuaiSow());     // 当天母猪存栏
        report.setExtra(JsonMapper.nonEmptyMapper().toJson(getDailyReportDto(farmId, group, sow, boar)));
        return report;
    }

    //日报统计json字段
    private DoctorDailyReportDto getDailyReportDto(Long farmId, ReportGroupLiveStock group, ReportSowLiveStock sow, ReportBoarLiveStock boar) {
        DoctorDailyReportDto dto = RespHelper.orServEx(doctorDailyReportReadService
                .initDailyReportByFarmIdAndDate(farmId, group.getSumat()));

        //刷新下dto里的统计
        dto.getLiveStock().setFarrow(group.getFarrowCount());
        dto.getLiveStock().setNursery(group.getNurseryCount());
        dto.getLiveStock().setFatten(group.getFattenCount());

        //注意下面的存栏都是当天的存栏
        dto.getLiveStock().setBoar(boar.getQuantity());
        dto.getLiveStock().setBuruSow(sow.getBuruSow());
        dto.getLiveStock().setHoubeiSow(group.getHoubeiCount());
        dto.getLiveStock().setKonghuaiSow(sow.getKonghuaiSow());
        dto.getLiveStock().setPeihuaiSow(sow.getPeihuaiSow());
        return dto;
    }

    /**
     * 迁移猪场日报(放在所有事件迁移之后进行)
     * @param farmId 猪场id
     */
    @Transactional
    public void moveMonthlyReport(Long farmId) {
        RespHelper.orServEx(doctorMonthlyReportWriteService.createMonthlyReports(Lists.newArrayList(farmId), new Date()));
    }
}
