package io.terminus.doctor.event.service;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.DoctorStatisticCriteria;
import io.terminus.doctor.event.dto.report.daily.DoctorFarmLiveStockDto;
import io.terminus.doctor.event.dto.reportBi.DoctorDimensionReport;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.manager.DoctorDailyReportV2Manager;
import io.terminus.doctor.event.reportBi.DoctorReportBiManager;
import io.terminus.doctor.event.reportBi.synchronizer.DoctorEfficiencySynchronizer;
import io.terminus.doctor.event.reportBi.synchronizer.DoctorWarehouseSynchronizer;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Created by xjn on 17/12/12.
 * email:xiaojiannan@terminus.io
 */
@Slf4j
@Service
@RpcProvider
public class DoctorDailyReportV2ServiceImpl implements DoctorDailyReportV2Service {

    private final DoctorDailyReportV2Manager doctorDailyReportV2Manager;
    private final DoctorReportBiManager doctorReportBiManager;

    @Autowired
    private DoctorWarehouseSynchronizer warehouseSynchronizer;
    @Autowired
    private DoctorEfficiencySynchronizer efficiencySynchronizer;

    @Autowired
    public DoctorDailyReportV2ServiceImpl(DoctorDailyReportV2Manager doctorDailyReportV2Manager, DoctorReportBiManager doctorReportBiManager) {
        this.doctorDailyReportV2Manager = doctorDailyReportV2Manager;
        this.doctorReportBiManager = doctorReportBiManager;
    }

    @Override
    public Response<Boolean> flushFarmDaily(Long farmId, String startAt, String endAt) {
        log.info("flush farm daily starting, farmId:{}, startAt:{}, endAt:{}", farmId, startAt, endAt);
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            DoctorStatisticCriteria criteria = new DoctorStatisticCriteria();
            criteria.setFarmId(farmId);
            List<Date> list = DateUtil.getDates(DateUtil.toDate(startAt), DateUtil.toDate(endAt));
            if (list.isEmpty()) {
                log.error("flush farm daily startAt or endAt is illegal, startAt:{}, endAt:{}", startAt, endAt);
                return Response.fail("startAt.or.endAt.is.error");
            }

            list.parallelStream().forEach(date -> {
                DoctorStatisticCriteria criteria1 = new DoctorStatisticCriteria();
                BeanMapper.copy(criteria, criteria1);
                criteria1.setSumAt(DateUtil.toDateString(date));
                log.info("flush farm daily farmId:{}, sumAt:{}", criteria1.getFarmId(), criteria1.getSumAt());
                doctorDailyReportV2Manager.flushFarmDaily(criteria1);
            });
            log.info("flush farm daily end, consume:{}m", stopwatch.elapsed(TimeUnit.MINUTES));
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("flush farm daily failed, farmId:{}, startAt:{}, endAt:{}, cause:{}",
                    farmId, startAt, endAt, Throwables.getStackTraceAsString(e));
            return Response.fail("flush.farm.daily.failed");
        }
    }

    @Override
    public Response<Boolean> flushGroupDaily(Long farmId, String startAt, String endAt) {
        log.info("flush group daily starting, farmId:{}, startAt:{}, endAt:{}", farmId, startAt, endAt);
        try {
            PigType.GROUP_TYPES.forEach(pigType -> flushGroupDaily(farmId, pigType, startAt, endAt));
            log.info("flush group daily end");
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("flush group daily failed, farmId:{}, startAt:{}, endAt:{}, cause:{}",
                    farmId, startAt, endAt, Throwables.getStackTraceAsString(e));
            return Response.fail("flush.group.daily.failed");
        }
    }

    @Override
    public Response<Boolean> flushGroupDaily(Long farmId, Integer pigType, String startAt, String endAt) {
        log.info("flush group daily for pigType starting, farmId:{}, pigType:{}, startAt:{}, endAt:{}", farmId, pigType, startAt, endAt);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            if (!PigType.GROUP_TYPES.contains(pigType)) {
                log.error("flush group daily pig type is illegal,pigType:{}", pigType);
                return Response.fail("pig.type.is.illegal");
            }
            DoctorStatisticCriteria criteria = new DoctorStatisticCriteria();
            criteria.setFarmId(farmId);
            criteria.setPigType(pigType);
            List<Date> list = DateUtil.getDates(DateUtil.toDate(startAt), DateUtil.toDate(endAt));
            if (list.isEmpty()) {
                log.error("flush group daily startAt or endAt is illegal, startAt:{}, endAt:{}", startAt, endAt);
                return Response.fail("startAt.or.endAt.is.error");
            }

            list.forEach(date -> {
                criteria.setSumAt(DateUtil.toDateString(date));
                log.info("flush group daily farmId:{}, pigType:{}, sumAt:{}", criteria.getFarmId(), criteria.getPigType(), criteria.getSumAt());
                doctorDailyReportV2Manager.flushGroupDaily(criteria);
            });
            log.info("flush group daily for pigType end, consume:{}minute", stopwatch.elapsed(TimeUnit.MINUTES));
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("flush group daily failed, farmId:{}, pigType:{}, startAt:{}, endAt:{}, cause:{}",
                    farmId, pigType, startAt, endAt, Throwables.getStackTraceAsString(e));
            return Response.fail("flush.group.daily.failed");
        }
    }

    @Override
    public Response<Boolean> flushPigDaily(Long farmId, String startAt, String endAt) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            log.info("flush pig daily starting, farmId:{}, startAt:{}, endAt:{}", farmId, startAt, endAt);
            DoctorStatisticCriteria criteria = new DoctorStatisticCriteria();
            criteria.setFarmId(farmId);
            List<Date> list = DateUtil.getDates(DateUtil.toDate(startAt), DateUtil.toDate(endAt));
            if (list.isEmpty()) {
                log.error("flush pig daily startAt or endAt is illegal, startAt:{}, endAt:{}", startAt, endAt);
                return Response.fail("startAt.or.endAt.is.error");
            }

            list.forEach(date -> {
                criteria.setSumAt(DateUtil.toDateString(date));
                log.info("flush pig daily farmId:{}, sumAt:{}", criteria.getFarmId(), criteria.getSumAt());
                doctorDailyReportV2Manager.flushPigDaily(criteria);
            });

            log.info("flush pig daily end, consume:{}minute", stopwatch.elapsed(TimeUnit.MINUTES));
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("flush pig daily failed, farmId:{}, startAt:{}, endAt:{}, cause:{}",
                    farmId, startAt, endAt, Throwables.getStackTraceAsString(e));
            return Response.fail("flush.pig.daily.failed");
        }
    }

    @Override
    public Response<Boolean> generateYesterdayAndToday(List<Long> farmIds) {
        try {
            log.info("generate yesterday and today starting");
            Stopwatch stopWatch = Stopwatch.createStarted();
            doctorDailyReportV2Manager.generateYesterdayAndToday(farmIds);
            doctorReportBiManager.synchronizeDeltaDayBiData();
            log.info("generate yesterday and today end, consume:{}minute", stopWatch.elapsed(TimeUnit.MINUTES));
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("generate yesterday and today failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("generate.yesterday.and.today.failed");
        }
    }

    @Override
    public Response<Boolean> synchronizeFullBiData() {
        try {
            doctorReportBiManager.synchronizeFullData();
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("synchronize full bi data failed,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("synchronize.full.bi.data.failed");
        }
    }

    @Override
    public Response<Boolean> synchronizeDeltaDayBiData(Long farmI, Date start) {
        try {
            doctorReportBiManager.synchronizeDeltaDayBiData(farmI, start);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("synchronize delta day bi data failed,farmId:{}, start:{}, cause:{}",
                    farmI, start, Throwables.getStackTraceAsString(e));
            return Response.fail("synchronize.delta.day.bi.data.failed");
        }
    }

    @Override
    public Response<DoctorDimensionReport> dimensionReport(DoctorDimensionCriteria dimensionCriteria) {
        try {
            return Response.ok(doctorReportBiManager.dimensionReport(dimensionCriteria));
        } catch (Exception e) {
            log.error("dimension report failed,dimension:{}, cause:{}", dimensionCriteria, Throwables.getStackTraceAsString(e));
            return Response.fail("dimension.report.failed");
        }

    }

    @Override
    public Response<Boolean> syncWarehouse(Date date) {
        warehouseSynchronizer.sync(date);
        return Response.ok(true);
    }

    @Override
    public Response<Boolean> syncWarehouse(Integer dateType, Integer orgType) {
        DoctorDimensionCriteria criteria = new DoctorDimensionCriteria();
        criteria.setDateType(dateType);
        criteria.setOrzType(orgType);

        warehouseSynchronizer.delete(criteria);
        warehouseSynchronizer.sync(criteria);
        return Response.ok(true);
    }

    @Override
    public Response<Boolean> syncEfficiency(Date date) {
        efficiencySynchronizer.sync(date);

        return Response.ok(true);
    }


    @Override
    public Response<Boolean> syncEfficiency(Integer dateType, Integer orgType) {
        DoctorDimensionCriteria criteria = new DoctorDimensionCriteria();
        criteria.setDateType(dateType);
        criteria.setOrzType(orgType);

        efficiencySynchronizer.delete(criteria);
        efficiencySynchronizer.sync(criteria);
        return Response.ok(true);
    }

    @Override
    public Response<Boolean> syncEfficiency(Long farmId) {

        DoctorDimensionCriteria criteria = new DoctorDimensionCriteria();
        criteria.setDateType(DateDimension.MONTH.getValue());
        criteria.setOrzType(OrzDimension.FARM.getValue());
        criteria.setOrzId(farmId);

        efficiencySynchronizer.delete(criteria);
        efficiencySynchronizer.sync(criteria);


        criteria.setDateType(DateDimension.QUARTER.getValue());
        efficiencySynchronizer.delete(criteria);
        efficiencySynchronizer.sync(criteria);

        criteria.setDateType(DateDimension.YEAR.getValue());
        efficiencySynchronizer.delete(criteria);
        efficiencySynchronizer.sync(criteria);


        criteria.setDateType(DateDimension.MONTH.getValue());
        criteria.setOrzDimensionName(OrzDimension.ORG.getName());
        efficiencySynchronizer.delete(criteria);
        efficiencySynchronizer.sync(criteria);

        criteria.setDateType(DateDimension.QUARTER.getValue());
        efficiencySynchronizer.delete(criteria);
        efficiencySynchronizer.sync(criteria);

        criteria.setDateType(DateDimension.YEAR.getValue());
        efficiencySynchronizer.delete(criteria);
        efficiencySynchronizer.sync(criteria);

        return Response.ok(true);
    }

    @Override
    public Response<List<DoctorFarmLiveStockDto>> findFarmsLiveStock(List<Long> farmIdList) {
        try {
            Date now = new Date();
            List<DoctorFarmLiveStockDto> dtos = farmIdList.parallelStream().map(farmId -> {
                DoctorDimensionCriteria dimensionCriteria =
                        new DoctorDimensionCriteria(farmId, OrzDimension.FARM.getValue(), now, DateDimension.DAY.getValue());
                DoctorDimensionReport report = doctorReportBiManager.dimensionReport(dimensionCriteria);
                return DoctorFarmLiveStockDto.builder()
                        .farmId(farmId)
                        .boar(report.getReportBoar().getEnd())
                        .farrow(report.getReportDeliver().getPigletEnd())
                        .deliverSow(report.getReportDeliver().getEnd())
                        .sow(report.getReportSow().getEnd())
                        .houbei(report.getReportReserve().getEnd())
                        .peihuai(report.getReportMating().getEnd())
                        .nursery(report.getReportNursery().getEnd())
                        .fatten(report.getReportFatten().getEnd())
                        .build();

            }).collect(Collectors.toList());
            return Response.ok(dtos);
        } catch (Exception e) {
            log.error("find farms live stock failed,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("find.farms.live.stock.failed");
        }

    }
}


