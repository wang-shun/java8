package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dao.DoctorRangeReportDao;
import io.terminus.doctor.event.dto.DoctorStockStructureDto;
import io.terminus.doctor.event.enums.ReportRangeType;
import io.terminus.doctor.event.model.DoctorRangeReport;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Code generated by terminus code gen
 * Desc: 指标月报写服务实现类
 * Date: 2017-04-19
 */
@Slf4j
@Service
@RpcProvider
public class DoctorRangeReportWriteServiceImpl implements DoctorRangeReportWriteService {

    private final DoctorRangeReportDao doctorRangeReportDao;
    private final DoctorKpiDao doctorKpiDao;

    private static final Integer TIME_RANGE = 5;   //计算5个月之前的实际受胎率,分娩率
    private static final Integer WEEK_FLUSH = 5;   //重新生成5周的报表

    @Autowired
    public DoctorRangeReportWriteServiceImpl(DoctorRangeReportDao doctorRangeReportDao,
                                             DoctorKpiDao doctorKpiDao) {
        this.doctorRangeReportDao = doctorRangeReportDao;
        this.doctorKpiDao = doctorKpiDao;
    }

    @Override
    public Response<Long> createDoctorRangeReport(DoctorRangeReport doctorRangeReport) {
        try {
            doctorRangeReportDao.create(doctorRangeReport);
            return Response.ok(doctorRangeReport.getId());
        } catch (Exception e) {
            log.error("create doctorRangeReport failed, doctorRangeReport:{}, cause:{}", doctorRangeReport, Throwables.getStackTraceAsString(e));
            return Response.fail("doctorRangeReport.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateDoctorRangeReport(DoctorRangeReport doctorRangeReport) {
        try {
            return Response.ok(doctorRangeReportDao.update(doctorRangeReport));
        } catch (Exception e) {
            log.error("update doctorRangeReport failed, doctorRangeReport:{}, cause:{}", doctorRangeReport, Throwables.getStackTraceAsString(e));
            return Response.fail("doctorRangeReport.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteDoctorRangeReportById(Long doctorRangeReportId) {
        try {
            return Response.ok(doctorRangeReportDao.delete(doctorRangeReportId));
        } catch (Exception e) {
            log.error("delete doctorRangeReport failed, doctorRangeReportId:{}, cause:{}", doctorRangeReportId, Throwables.getStackTraceAsString(e));
            return Response.fail("doctorRangeReport.delete.fail");
        }
    }

    @Override
    public Response<Boolean> generateDoctorRangeReports(List<Long> farmIds, Date date) {
        try{
            farmIds.forEach(farmId -> {
                generateDoctorWeeklyReports(farmId, date);
                generateDoctorMonthlyReports(farmId, date);
                updateActualIndicator(farmId, new DateTime(date).minusMonths(TIME_RANGE).toDate());
            });
        }catch(Exception e){
            log.info("generate DoctorRangeReports failed, cause: {}", Throwables.getStackTraceAsString(e));
            return Response.ok(Boolean.FALSE);
        }

        return Response.ok(Boolean.TRUE);
    }

    @Override
    public Response<Boolean> generateDoctorRangeReports(Long farmId, Date date) {
        try{
            generateDoctorWeeklyReports(farmId, date);
            generateDoctorMonthlyReports(farmId, date);
            updateActualIndicator(farmId, new DateTime(date).minusMonths(TIME_RANGE).toDate());
        }catch(Exception e){
            log.info("generate DoctorRangeReports failed, cause: {}", Throwables.getStackTraceAsString(e));
            return Response.ok(Boolean.FALSE);
        }
        return Response.ok(Boolean.TRUE);
    }

    @Override
    public Response<Boolean> flushDoctorRangeReports(Long farmId, Date date) {
        try{
            DateUtil.getBeforeWeekEnds(date, WEEK_FLUSH).forEach(week ->{
                generateDoctorWeeklyReports(farmId, week);
            });
            generateDoctorMonthlyReports(farmId, date);
            updateActualIndicator(farmId, new DateTime(date).minusMonths(TIME_RANGE).toDate());
        }catch(Exception e){
            log.info("flush DoctorRangeReports failed, cause: {}", Throwables.getStackTraceAsString(e));
            return Response.ok(Boolean.FALSE);
        }
        return Response.ok(Boolean.TRUE);
    }

    @Override
    public Response<Boolean> updateStructureReport(Long farmId, Date startAt, Date endAt) {
        log.info("update structure report farmId: {}, startAt: {}, endAt: {}, now: {}", farmId, DateUtil.toDateString(startAt), DateUtil.toDateString(endAt), DateUtil.toDateString(new Date()));
        try{
        startAt = DateUtil.monthStart(startAt);
            do {
                String month = DateUtil.getYearMonth(startAt);
                updateMonthlyStructureReport(farmId, month);
                startAt = new DateTime(startAt).plusMonths(1).toDate();
            } while (endAt.after(startAt));
        }catch(Exception e){
            log.info("flush DoctorRangeReports failed, cause: {}", Throwables.getStackTraceAsString(e));
            return Response.ok(Boolean.FALSE);
        }

        return Response.ok(Boolean.TRUE);
    }

    private void updateMonthlyStructureReport(Long farmId, String month) {
        DoctorRangeReport doctorRangeReport = doctorRangeReportDao.findByRangeReport(farmId, ReportRangeType.MONTH.getValue(), month);
        if(!Objects.isNull(doctorRangeReport)) {
            doctorRangeReport = getStructureReport(doctorRangeReport);
            doctorRangeReportDao.update(doctorRangeReport);
        }

    }

    private void updateActualIndicator(Long farmId, Date date) {
        DateUtil.getBeforeWeekEnds(date, WEEK_FLUSH * TIME_RANGE).forEach(week ->{
            updateWeeklyActualIndicator(farmId, week);
        });
        DateUtil.getBeforeMonthEnds(date, TIME_RANGE).forEach(month ->{
            updateMonthActuralIndicator(farmId, month);
        });
    }

    private void updateMonthActuralIndicator(Long farmId, Date date) {
        String month = DateUtil.getYearMonth(date);
        DoctorRangeReport report = doctorRangeReportDao.findByRangeReport(farmId, ReportRangeType.MONTH.getValue(), month);
        if(!Arguments.isNull(report)){
            getActualIndicator(report, farmId, report.getSumFrom(), report.getSumTo());
            doctorRangeReportDao.update(report);
        }

    }

    private void updateWeeklyActualIndicator(Long farmId, Date date) {
        String week = DateUtil.getYearWeek(date);
        DoctorRangeReport report = doctorRangeReportDao.findByRangeReport(farmId, ReportRangeType.WEEK.getValue(), week);
        if(!Arguments.isNull(report)) {
            getActualIndicator(report, farmId, report.getSumFrom(), report.getSumTo());
            doctorRangeReportDao.update(report);
        }
    }


    private Boolean generateDoctorMonthlyReports(Long farmId, Date date) {
        try{
            Date startAt = DateUtil.monthStart(date);
            Date endAt = DateUtil.monthEnd(date);
            String month = DateUtil.getYearMonth(date);
            log.info("generate DoctorMonthlyReports start, farmId: {}, start: {}, end: {}, now: {}", farmId, DateUtil.toDateString(startAt), DateUtil.toDateString(endAt), DateUtil.toDateTimeString(new Date()));
            createDoctorRangeReport(farmId, ReportRangeType.MONTH.getValue(), month, startAt, endAt);
        }catch(Exception e){
            log.info("generate DoctorMonthlyReports failed, farmId: {}, date: {}, cause: {}", farmId, DateUtil.toDateString(date),  Throwables.getStackTraceAsString(e));
            return Boolean.FALSE;
        }
        return Boolean.TRUE;

    }

    private Boolean generateDoctorWeeklyReports(Long farmId, Date date) {
        Date startAt = DateUtil.weekStart(date);
        Date endAt = DateUtil.weekEnd(date);
        try{
            String week = DateUtil.getYearWeek(date);
            log.info("generate DoctorWeeklyReports start, farmId: {}, start: {}, end: {}, now: {}", farmId, DateUtil.toDateString(startAt), DateUtil.toDateString(endAt), DateUtil.toDateTimeString(new Date()));
            createDoctorRangeReport(farmId, ReportRangeType.WEEK.getValue(), week, startAt, endAt);
        }catch(Exception e){
            log.info("generate DoctorWeeklyReports failed,farmId: {}, start: {}, end: {}, cause: {}", farmId, DateUtil.toDateString(startAt), DateUtil.toDateString(endAt), Throwables.getStackTraceAsString(e));
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private void createDoctorRangeReport(Long farmId, Integer type, String sumAt, Date startAt, Date endAt){
        DoctorRangeReport doctorRangeReport = new DoctorRangeReport();
        doctorRangeReport.setFarmId(farmId);
        doctorRangeReport.setType(type);
        doctorRangeReport.setSumAt(sumAt);
        doctorRangeReport.setSumFrom(startAt);
        doctorRangeReport.setSumTo(endAt);
        getDoctorRangeIndicator(doctorRangeReport);

        doctorRangeReportDao.deleteByFarmIdAndTypeAndSumAt(farmId, type, sumAt);
        doctorRangeReportDao.create(doctorRangeReport);
    }


    /**
     * 根据时间段统计
     * @param doctorRangeReport
     * @return
     */
    private DoctorRangeReport getDoctorRangeIndicator(DoctorRangeReport doctorRangeReport) {
        Long farmId = doctorRangeReport.getFarmId();
        Date startAt = doctorRangeReport.getSumFrom();
        Date endAt = doctorRangeReport.getSumTo();

        doctorRangeReport.setMateEstimatePregRate(doctorKpiDao.assessPregnancyRate(farmId, startAt, endAt));       //估算受胎率
        doctorRangeReport.setMateEstimateFarrowingRate(doctorKpiDao.assessFarrowingRate(farmId, startAt, endAt));  //估算配种分娩率
        getActualIndicator(doctorRangeReport, farmId, startAt, endAt);

        doctorRangeReport.setNpd(doctorKpiDao.npd(farmId, startAt, endAt));                                        //非生产天数
        doctorRangeReport.setPsy(doctorKpiDao.psy(farmId, startAt, endAt));                                        //psy
        doctorRangeReport.setMateInSeven(doctorKpiDao.getMateInSeven(farmId, startAt, endAt));

        doctorRangeReport.setWeanAvgCount(doctorKpiDao.getWeanPigletCountsAvg(farmId, startAt, endAt));        //窝均断奶数
        doctorRangeReport.setWeanAvgDayAge(doctorKpiDao.getWeanDayAgeAvg(farmId, startAt, endAt));       //断奶日龄

        doctorRangeReport.setDeadFarrowRate(doctorKpiDao.getDeadFarrowRate(farmId, startAt, endAt));    //产房死淘率
        doctorRangeReport.setDeadNurseryRate(doctorKpiDao.getDeadNurseryRate(farmId, startAt, endAt));  //保育死淘率
        doctorRangeReport.setDeadFattenRate(doctorKpiDao.getDeadFattenRate(farmId, startAt, endAt));    //育肥死淘率

        return getStructureReport(doctorRangeReport);
    }

    private DoctorRangeReport getStructureReport(DoctorRangeReport doctorRangeReport) {
        Long farmId = doctorRangeReport.getFarmId();
        Date startAt = doctorRangeReport.getSumFrom();
        Date endAt = doctorRangeReport.getSumTo();

        DoctorStockStructureDto stockDistributeDto = new DoctorStockStructureDto();
        stockDistributeDto.setParityStockList(doctorKpiDao.getMonthlyParityStock(farmId, startAt, endAt));
        stockDistributeDto.setBreedStockList(doctorKpiDao.getMonthlyBreedStock(farmId, startAt, endAt));
        doctorRangeReport.setExtra(ToJsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(stockDistributeDto));
        return doctorRangeReport;
    }

    /**
     * 重新计算4个月之前数据的指标
     * @param doctorRangeReport
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    private DoctorRangeReport getActualIndicator(DoctorRangeReport doctorRangeReport, Long farmId, Date startAt, Date endAt) {
        doctorRangeReport.setMateRealPregRate(doctorKpiDao.realPregnancyRate(farmId, startAt, endAt));             //实际受胎率
        doctorRangeReport.setMateRealFarrowingRate(doctorKpiDao.realFarrowingRate(farmId, startAt, endAt));        //实际分娩率
        return doctorRangeReport;
    }


}
