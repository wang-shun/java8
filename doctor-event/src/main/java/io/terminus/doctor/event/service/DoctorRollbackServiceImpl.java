package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.cache.DoctorDailyReportCache;
import io.terminus.doctor.event.dao.DoctorGroupBatchSummaryDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.dto.report.daily.DoctorCheckPregDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorDeadDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorDeliverDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorLiveStockDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorMatingDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorSaleDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorWeanDailyReport;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.helper.DoctorEventBaseHelper;
import io.terminus.doctor.event.manager.DoctorRollbackManager;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static io.terminus.doctor.event.enums.GroupEventType.REPORT_GROUP_EVENT;
import static io.terminus.doctor.event.handler.DoctorAbstractEventHandler.IGNORE_EVENT;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */
@Slf4j
@Service
@RpcProvider
public class DoctorRollbackServiceImpl implements DoctorRollbackService {

    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorPigEventDao doctorPigEventDao;
    private final DoctorGroupBatchSummaryDao doctorGroupBatchSummaryDao;
    private final DoctorKpiDao doctorKpiDao;
    private final DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService;
    private final DoctorRollbackManager doctorRollbackManager;
    private final DoctorDailyReportCache doctorDailyReportCache;
    private final CoreEventDispatcher coreEventDispatcher;
    private final DoctorEventBaseHelper doctorEventBaseHelper;
    private static JsonMapperUtil jsonMapper = JsonMapperUtil.nonEmptyMapper();

    @Autowired
    public DoctorRollbackServiceImpl(DoctorGroupEventDao doctorGroupEventDao,
                                     DoctorPigEventDao doctorPigEventDao,
                                     DoctorGroupBatchSummaryDao doctorGroupBatchSummaryDao,
                                     DoctorKpiDao doctorKpiDao,
                                     DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService,
                                     DoctorRollbackManager doctorRollbackManager,
                                     DoctorDailyReportCache doctorDailyReportCache, CoreEventDispatcher coreEventDispatcher, DoctorEventBaseHelper doctorEventBaseHelper) {
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorGroupBatchSummaryDao = doctorGroupBatchSummaryDao;
        this.doctorKpiDao = doctorKpiDao;
        this.doctorPigTypeStatisticWriteService = doctorPigTypeStatisticWriteService;
        this.doctorRollbackManager = doctorRollbackManager;
        this.doctorDailyReportCache = doctorDailyReportCache;
        this.coreEventDispatcher = coreEventDispatcher;
        this.doctorEventBaseHelper = doctorEventBaseHelper;
    }

    @Override
    public RespWithEx<Boolean> rollbackGroupEvent(Long eventId, Long operatorId, String operatorName) {
        try {
            DoctorGroupEvent groupEvent = doctorGroupEventDao.findById(eventId);
            if (groupEvent == null) {
                throw new InvalidException("group.event.not.found", eventId);
            }
            int a = doctorGroupEventDao.isCloseGroup(groupEvent.getGroupId());
            if(a != 1 && groupEvent.getType()!= 10){
                throw new InvalidException("猪群已关闭");
            }
            doctorRollbackManager.rollbackGroup(groupEvent, operatorId, operatorName);

            if (REPORT_GROUP_EVENT.contains(groupEvent.getType())) {
                List<Long> farmIds = Lists.newArrayList(groupEvent.getFarmId());
                if (Objects.equals(groupEvent.getType(), GroupEventType.TRANS_FARM.getValue())) {
                    DoctorTransFarmGroupInput groupInput = jsonMapper.fromJson(groupEvent.getExtra(), DoctorTransFarmGroupInput.class);
                    farmIds.add(groupInput.getToFarmId());
                }
                doctorEventBaseHelper.synchronizeReportPublish(farmIds);
            }
            return RespWithEx.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return RespWithEx.fail(e.getMessage());
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (Exception e) {
            log.error("rollack group event failed, eventId:{}, cause:{}", eventId, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("rollback.event.failed");
        }
    }

    @Override
    public RespWithEx<Boolean> rollbackPigEvent(Long eventId, Long operatorId, String operatorName) {
        try {
            DoctorPigEvent pigEvent = doctorPigEventDao.findById(eventId);
            if (pigEvent == null) {
                throw new InvalidException("pig.event.not.found", eventId);
            }
            doctorRollbackManager.rollbackPig(pigEvent, operatorId, operatorName);

            //同步数据
            if (!IGNORE_EVENT.contains(pigEvent.getType())) {
                List<Long> farmIds = Lists.newArrayList(pigEvent.getFarmId());
                if (Objects.equals(pigEvent.getType(), PigEvent.CHG_FARM.getKey())) {
                    DoctorChgFarmDto doctorChgFarmDto = jsonMapper.fromJson(pigEvent.getExtra(), DoctorChgFarmDto.class);
                    farmIds.add(doctorChgFarmDto.getToFarmId());
                }
                doctorEventBaseHelper.synchronizeReportPublish(farmIds);
            }
            return RespWithEx.ok(Boolean.TRUE);
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("rollack pig event failed, eventId:{}, cause:{}", eventId, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("rollback.event.failed");
        }
    }

    @Override
    public Response<Boolean> rollbackReportAndES(List<DoctorRollbackDto> rollbackDtos) {
        try {
            rollbackDtos.forEach(dto -> {
                if (dto.getFarmId() == null || dto.getEventAt() == null) {
                    throw new ServiceException("publish.rollback.not.null");
                }
            });
            reportWithDaily(rollbackDtos);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("rollback report and es failed, rollbackDtos:{}, cause:{}", rollbackDtos, Throwables.getStackTraceAsString(e));
            return Response.fail("rollback.report.fail");
        }
    }

    //遍历回滚类型，执行相应操作(存栏和其他日报要分开遍历)
    private synchronized void reportWithDaily(List<DoctorRollbackDto> dtos) {
        dtos.forEach(this::reportAndES);
        dtos.forEach(this::updateLiveStockUntilNow);
    }

    private void reportAndES(DoctorRollbackDto dto) {
        Date startAt = Dates.startOfDay(dto.getEventAt());
        Date endAt = DateUtil.getDateEnd(new DateTime(dto.getEventAt())).toDate();
        Long farmId = dto.getFarmId();

        DoctorDailyReportDto report = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
        if (report == null) {
            return;
        }
        for (RollbackType type : dto.getRollbackTypes()) {
//            switch (type) {
//                //日报实时更新
//                case DAILY_DEAD:
//                    report.setDead(getDeadDailyReport(farmId, startAt, endAt));
//                    break;
//                case DAILY_FARROW:
//                    report.setDeliver(getDeliverDailyReport(farmId, startAt, endAt));
//                    break;
//                case DAILY_MATE:
//                    report.setMating(getMatingDailyReport(farmId, startAt, endAt));
//                    break;
//                case DAILY_SALE:
//                    report.setSale(getSaleDailyReport(farmId, startAt, endAt));
//                    break;
//                case DAILY_WEAN:
//                    report.setWean(getWeanDailyReport(farmId, startAt, endAt));
//                    break;
//                case DAILY_PREG_CHECK:
//                    report.setCheckPreg(getCheckPregDailyReport(farmId, startAt, endAt));
//                    break;
//
//                //直接删除
//                case GROUP_BATCH:
//                    if (dto.getEsGroupId() != null) {
//                        doctorGroupBatchSummaryDao.deleteByGroupId(dto.getEsGroupId());
//                    }
//                    break;
//                default:
//                    break;
//            }
        }
        doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, report);
    }

    //更新从回滚日期到今天的存栏
    private void updateLiveStockUntilNow(DoctorRollbackDto dto) {
        if (!dto.getRollbackTypes().contains(RollbackType.DAILY_LIVESTOCK)) {
            return;
        }
        Date startAt = Dates.startOfDay(dto.getEventAt());
        Date endAt = Dates.startOfDay(new Date());
        Long farmId = dto.getFarmId();
        Long orgId = doctorKpiDao.getOrgIdByFarmId(farmId);

        //更新 PigTypeStatistic
        doctorPigTypeStatisticWriteService.statisticGroup(dto.getOrgId(), dto.getFarmId());
        doctorPigTypeStatisticWriteService.statisticPig(dto.getOrgId(), dto.getFarmId(), DoctorPig.PigSex.BOAR.getKey());
        doctorPigTypeStatisticWriteService.statisticPig(dto.getOrgId(), dto.getFarmId(), DoctorPig.PigSex.SOW.getKey());

        while (!startAt.after(endAt)) {
            //猪群存栏
            DoctorLiveStockDailyReport liveStock = new DoctorLiveStockDailyReport();
            liveStock.setHoubeiBoar(doctorKpiDao.realTimeLiveStockHoubeiBoar(farmId, startAt));
            liveStock.setHoubeiSow(doctorKpiDao.realTimeLiveStockHoubeiSow(farmId, startAt));  //后备母猪
            liveStock.setFarrow(doctorKpiDao.realTimeLiveStockFarrow(farmId, startAt));
            liveStock.setNursery(doctorKpiDao.realTimeLiveStockNursery(farmId, startAt));
            liveStock.setFatten(doctorKpiDao.realTimeLiveStockFatten(farmId, startAt));

            //猪存栏
            liveStock.setBuruSow(doctorKpiDao.realTimeLiveStockFarrowSow(orgId,farmId, startAt));    //产房母猪
            liveStock.setPeihuaiSow(doctorKpiDao.realTimeLiveStockPHSow(orgId,farmId, startAt));    //配怀 = 总存栏 - 产房母猪
            liveStock.setKonghuaiSow(0);                                                       //空怀猪作废, 置成0
            liveStock.setBoar(doctorKpiDao.realTimeLiveStockBoar(farmId, startAt));            //公猪

            DoctorDailyReportDto everyRedis = doctorDailyReportCache.getDailyReportDto(farmId, startAt);
            if (everyRedis == null) {
                continue;
            }
//            everyRedis.setLiveStock(liveStock);
            doctorDailyReportCache.putDailyReportToMySQL(farmId, startAt, everyRedis);
            startAt = new DateTime(startAt).plusDays(1).toDate();
        }
    }

    private DoctorCheckPregDailyReport getCheckPregDailyReport(Long farmId, Date startAt, Date endAt) {
        DoctorCheckPregDailyReport checkPreg = new DoctorCheckPregDailyReport();
        checkPreg.setPositive(doctorKpiDao.checkYangCounts(farmId, startAt, endAt));
        checkPreg.setNegative(doctorKpiDao.checkYingCounts(farmId, startAt, endAt));
        checkPreg.setFanqing(doctorKpiDao.checkFanQCounts(farmId, startAt, endAt));
        checkPreg.setLiuchan(doctorKpiDao.checkAbortionCounts(farmId, startAt, endAt));
        return checkPreg;
    }

    private DoctorDeadDailyReport getDeadDailyReport(Long farmId, Date startAt, Date endAt) {
        DoctorDeadDailyReport dead = new DoctorDeadDailyReport();
        dead.setBoar(doctorKpiDao.getDeadBoar(farmId, startAt, endAt));
        dead.setWeedOutBoar(doctorKpiDao.getWeedOutBoar(farmId, startAt, endAt));
        dead.setSow(doctorKpiDao.getDeadSow(farmId, startAt, endAt));
        dead.setWeedOutSow(doctorKpiDao.getWeedOutSow(farmId, startAt, endAt));
        dead.setFarrow(doctorKpiDao.getDeadFarrow(farmId, startAt, endAt));
        dead.setWeedOutFarrow(doctorKpiDao.getWeedOutFarrow(farmId, startAt, endAt));
        dead.setNursery(doctorKpiDao.getDeadNursery(farmId, startAt, endAt));
        dead.setWeedOutNursery(doctorKpiDao.getWeedOutNursery(farmId, startAt, endAt));
        dead.setFatten(doctorKpiDao.getDeadFatten(farmId, startAt, endAt));
        dead.setWeedOutFatten(doctorKpiDao.getWeedOutFatten(farmId, startAt, endAt));
        dead.setHoubei(doctorKpiDao.getDeadHoubei(farmId, startAt, endAt));
        dead.setWeedOutHoubei(doctorKpiDao.getWeedOutHoubei(farmId, startAt, endAt));
        return dead;
    }

    private DoctorDeliverDailyReport getDeliverDailyReport(Long farmId, Date startAt, Date endAt) {
        DoctorDeliverDailyReport deliver = new DoctorDeliverDailyReport();
        deliver.setNest(doctorKpiDao.getDelivery(farmId, startAt, endAt));
        deliver.setLive(doctorKpiDao.getDeliveryLive(farmId, startAt, endAt));
        deliver.setHealth(doctorKpiDao.getDeliveryHealth(farmId, startAt, endAt));
        deliver.setWeak(doctorKpiDao.getDeliveryWeak(farmId, startAt, endAt));
        deliver.setBlack(doctorKpiDao.getDeliveryDeadBlackMuJi(farmId, startAt, endAt));
        deliver.setAvgWeight(doctorKpiDao.getFarrowWeightAvg(farmId, startAt, endAt));
        return deliver;
    }

    private DoctorMatingDailyReport getMatingDailyReport(Long farmId, Date startAt, Date endAt) {
        DoctorMatingDailyReport mating = new DoctorMatingDailyReport();
        mating.setHoubei(doctorKpiDao.firstMatingCounts(farmId, startAt, endAt));
        mating.setPregCheckResultYing(doctorKpiDao.yinMatingCounts(farmId, startAt, endAt));
        mating.setDuannai(doctorKpiDao.weanMatingCounts(farmId, startAt, endAt));
        mating.setFanqing(doctorKpiDao.fanQMatingCounts(farmId, startAt, endAt));
        mating.setLiuchan(doctorKpiDao.abortionMatingCounts(farmId, startAt, endAt));
        return mating;
    }

    private DoctorSaleDailyReport getSaleDailyReport(Long farmId, Date startAt, Date endAt) {
        DoctorSaleDailyReport sale = new DoctorSaleDailyReport();
        sale.setBoar(doctorKpiDao.getSaleBoar(farmId, startAt, endAt));
        sale.setSow(doctorKpiDao.getSaleSow(farmId, startAt, endAt));
        sale.setNursery(doctorKpiDao.getSaleNursery(farmId, startAt, endAt));
        sale.setFatten(doctorKpiDao.getSaleFatten(farmId, startAt, endAt));
        sale.setHoubei(doctorKpiDao.getSaleHoubei(farmId, startAt, endAt));
        sale.setFattenPrice(doctorKpiDao.getGroupSaleFattenPrice(farmId, startAt, endAt));
        sale.setBasePrice10(doctorKpiDao.getGroupSaleBasePrice10(farmId, startAt, endAt));
        sale.setBasePrice15(doctorKpiDao.getGroupSaleBasePrice15(farmId, startAt, endAt));
        return sale;
    }

    private DoctorWeanDailyReport getWeanDailyReport(Long farmId, Date startAt, Date endAt) {
        DoctorWeanDailyReport wean = new DoctorWeanDailyReport();
        wean.setCount(doctorKpiDao.getWeanPiglet(farmId, startAt, endAt));
        wean.setWeight(doctorKpiDao.getWeanPigletWeightAvg(farmId, startAt, endAt));
        wean.setNest(doctorKpiDao.getWeanSow(farmId, startAt, endAt));
        wean.setAvgDayAge(doctorKpiDao.getWeanDayAgeAvg(farmId, startAt, endAt));
        wean.setFarrowChgFarm(doctorKpiDao.getFarrowChgFarmCount(farmId, startAt, endAt));
        wean.setFarrowToNursery(doctorKpiDao.getFarrowToNursery(farmId, startAt, endAt));
        wean.setFarrowSale(doctorKpiDao.getFarrowSaleCount(farmId, startAt, endAt));
        return wean;
    }
}
