package io.terminus.doctor.move.service;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.service.DoctorDailyReportReadService;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.move.model.ReportGroupLiveStock;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc: 迁移猪场统计数据
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/14
 */
@Slf4j
@Service
public class DoctorMoveReportService {

    private static final int INDEX = 50;    //总共导多少天的数据

    private final DoctorDailyReportDao doctorDailyReportDao;
    private final DoctorFarmDao doctorFarmDao;
    private final DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;
    private final DoctorDailyReportReadService doctorDailyReportReadService;

    @Autowired
    public DoctorMoveReportService(DoctorDailyReportDao doctorDailyReportDao,
                                   DoctorFarmDao doctorFarmDao,
                                   DoctorMoveDatasourceHandler doctorMoveDatasourceHandler,
                                   DoctorDailyReportReadService doctorDailyReportReadService) {
        this.doctorDailyReportDao = doctorDailyReportDao;
        this.doctorFarmDao = doctorFarmDao;
        this.doctorMoveDatasourceHandler = doctorMoveDatasourceHandler;
        this.doctorDailyReportReadService = doctorDailyReportReadService;
    }

    /**
     * 迁移猪场日报(放在所有事件迁移之后进行)
     * @param moveId 数据源id
     * @param farmId 猪场id
     */
    @Transactional
    public void moveDailyReport(Long moveId, Long farmId) {
        DoctorFarm farm = doctorFarmDao.findById(farmId);
        if (farm == null || notEmpty(farm.getOutId())) {
            return;
        }

        List<ReportGroupLiveStock> gls = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, ReportGroupLiveStock.class, "DoctorDailyReport-GroupLiveStock", ImmutableMap.of("index", INDEX, "farmOutId", farm.getOutId())));

        //取出所有统计数据转换一把
        List<DoctorDailyReport> reports = gls.stream()
                .map(g -> getDailyReport(farmId, g))
                .collect(Collectors.toList());

        //批量创建日报
        doctorDailyReportDao.creates(reports);
    }

    //日报
    private DoctorDailyReport getDailyReport(Long farmId, ReportGroupLiveStock group) {
        DoctorDailyReport report = new DoctorDailyReport();
        report.setFarmId(farmId);
        report.setSumAt(group.getSumat());
        report.setFarrowCount(group.getFarrowCount());      // 当天产房仔猪存栏
        report.setNurseryCount(group.getNurseryCount());    // 当天保育猪存栏
        report.setFattenCount(group.getFattenCount());      // 当天育肥猪存栏
        report.setSowCount(0);     // 当天母猪存栏
        report.setExtra(JsonMapper.nonEmptyMapper().toJson(getDailyReportDto(farmId, group)));
        return report;
    }

    //日报统计json字段
    private DoctorDailyReportDto getDailyReportDto(Long farmId, ReportGroupLiveStock group) {
        DoctorDailyReportDto dto = RespHelper.orServEx(doctorDailyReportReadService
                .initDailyReportByFarmIdAndDate(farmId, group.getSumat()));

        //刷新下dto里的统计 // TODO: 16/8/14  
        dto.getLiveStock().setFarrow(group.getFarrowCount());
        dto.getLiveStock().setNursery(group.getNurseryCount());
        dto.getLiveStock().setFatten(group.getFattenCount());

        //注意下面的存栏都是当天的存栏
        dto.getLiveStock().setBoar(0);
        dto.getLiveStock().setBuruSow(0);
        dto.getLiveStock().setHoubeiSow(0);
        dto.getLiveStock().setKonghuaiSow(0);
        dto.getLiveStock().setPeihuaiSow(0);
        return dto;
    }
}
