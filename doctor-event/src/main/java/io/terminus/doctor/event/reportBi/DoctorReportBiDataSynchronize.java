package io.terminus.doctor.event.reportBi;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dao.DoctorGroupDailyDao;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.dao.DoctorReportNpdDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorGroupDailyExtend;
import io.terminus.doctor.event.dto.reportBi.DoctorPigDailyExtend;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.reportBi.synchronizer.DoctorBoarSynchronizer;
import io.terminus.doctor.event.reportBi.synchronizer.DoctorDeliverSynchronizer;
import io.terminus.doctor.event.reportBi.synchronizer.DoctorEfficiencySynchronizer;
import io.terminus.doctor.event.reportBi.synchronizer.DoctorFattenSynchronizer;
import io.terminus.doctor.event.reportBi.synchronizer.DoctorMatingSynchronizer;
import io.terminus.doctor.event.reportBi.synchronizer.DoctorNurserySynchronizer;
import io.terminus.doctor.event.reportBi.synchronizer.DoctorReserveSynchronizer;
import io.terminus.doctor.event.reportBi.synchronizer.DoctorSowSynchronizer;
import io.terminus.doctor.event.reportBi.synchronizer.DoctorWarehouseSynchronizer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.terminus.doctor.common.utils.Checks.expectNotNull;

/**
 * Created by xjn on 18/1/9.
 * email:xiaojiannan@terminus.io
 */
@Slf4j
@Component
public class DoctorReportBiDataSynchronize {
    private final DoctorPigDailyDao doctorPigDailyDao;
    private final DoctorGroupDailyDao doctorGroupDailyDao;
    private final DoctorReportNpdDao doctorReportNpdDao;
    private final DoctorReserveSynchronizer reserveSynchronizer;
    private final DoctorFattenSynchronizer fattenSynchronizer;
    private final DoctorNurserySynchronizer nurserySynchronizer;
    private final DoctorBoarSynchronizer boarSynchronizer;
    private final DoctorSowSynchronizer sowSynchronizer;
    private final DoctorMatingSynchronizer matingSynchronizer;
    private final DoctorDeliverSynchronizer deliverSynchronizer;
    private final DoctorWarehouseSynchronizer warehouseSynchronizer;
    private final DoctorEfficiencySynchronizer efficiencySynchronizer;

    private final Integer DELTA_DAY = 600;
    private final Integer REAL_TIME_INTERVAL = 1;

    @Autowired
    public DoctorReportBiDataSynchronize(DoctorPigDailyDao doctorPigDailyDao,
                                         DoctorGroupDailyDao doctorGroupDailyDao,
                                         DoctorReportNpdDao doctorReportNpdDao,
                                         DoctorReserveSynchronizer reserveSynchronizer,
                                         DoctorFattenSynchronizer fattenSynchronizer,
                                         DoctorNurserySynchronizer nurserySynchronizer,
                                         DoctorBoarSynchronizer boarSynchronizer,
                                         DoctorSowSynchronizer sowSynchronizer,
                                         DoctorMatingSynchronizer matingSynchronizer,
                                         DoctorDeliverSynchronizer deliverSynchronizer,
                                         DoctorWarehouseSynchronizer warehouseSynchronizer,
                                         DoctorEfficiencySynchronizer efficiencySynchronizer) {
        this.doctorPigDailyDao = doctorPigDailyDao;
        this.doctorGroupDailyDao = doctorGroupDailyDao;
        this.doctorReportNpdDao = doctorReportNpdDao;
        this.reserveSynchronizer = reserveSynchronizer;
        this.fattenSynchronizer = fattenSynchronizer;
        this.nurserySynchronizer = nurserySynchronizer;
        this.boarSynchronizer = boarSynchronizer;
        this.sowSynchronizer = sowSynchronizer;
        this.matingSynchronizer = matingSynchronizer;
        this.deliverSynchronizer = deliverSynchronizer;
        this.warehouseSynchronizer = warehouseSynchronizer;
        this.efficiencySynchronizer = efficiencySynchronizer;
    }

    /**
     * 全量同步数据
     */
    public void synchronizeFullBiData() {
        log.info("synchronize full bi data starting");
        Stopwatch stopwatch = Stopwatch.createStarted();
        cleanFullBiData();
        synchronizeBiDataImpl();
        log.info("synchronize full bi data end, minute:{}m", stopwatch.elapsed(TimeUnit.MINUTES));
    }

    /**
     * 增量同步数据
     */
    public void synchronizeDeltaDayBiData() {
        log.info("synchronize delta day bi data starting");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Date date = DateTime.now().minusMinutes(DELTA_DAY).toDate();
        synchronizeDeltaBiData(date);

        //如果当天更改了历史数据，找出历史数据，重刷历史数据所在的期间
        List<Date> dates = warehouseSynchronizer.getChangedDate(new Date());
        for (Date d : dates) {
            if (DateUtils.isSameDay(d, new Date()))
                continue;
            
            warehouseSynchronizer.sync(d);
        }

        warehouseSynchronizer.sync(new Date());
        efficiencySynchronizer.sync(new Date());

        log.info("synchronize delta day bi data end, minute:{}m", stopwatch.elapsed(TimeUnit.MINUTES));
    }

    /**
     * 增量更新
     * @param start 开始的同步日期 与日报中updateAt 比较
     */
    public void synchronizeDeltaDayBiData(Date start) {
        log.info("synchronize delta day bi data starting, start:{}", start);
        Stopwatch stopwatch = Stopwatch.createStarted();
        synchronizeDeltaBiData(start);
        log.info("synchronize delta day bi data end, minute:{}m", stopwatch.elapsed(TimeUnit.MINUTES));
    }

    /**
     * 增量同步
     * @param farmId 猪场id
     * @param start 开始的同步日期 与日报中sumAt比较
     */
    public void synchronizeDeltaDayBiData(Long farmId, Date start) {
        log.info("synchronize delta day bi data starting, farmId:{}, start:{}", farmId, start);
        Stopwatch stopwatch = Stopwatch.createStarted();
        synchronizeDeltaBiData(farmId, start);
        log.info("synchronize delta day bi data end, minute:{}m", stopwatch.elapsed(TimeUnit.MINUTES));
    }

    /**
     * 同步实时数据
     */
    public void synchronizeRealTimeBiData() {
        log.info("synchronize real time bi data starting");
        Date date = DateTime.now().minusMinutes(REAL_TIME_INTERVAL).toDate();
        synchronizeDeltaBiData(date);
        log.info("synchronize real time bi data end");
    }

    private void synchronizeDeltaBiData(Date date) {

        List<DoctorGroupDaily> groupDailyList = doctorGroupDailyDao.findByAfter(date);
        if (!Arguments.isNullOrEmpty(groupDailyList)) {
            synchronizeGroupForDay(groupDailyList);
            List<DoctorDimensionCriteria> dimensionCriteriaList = Lists.newArrayList();
            dimensionCriteriaList.addAll(doctorGroupDailyDao.findByDateType(date, DateDimension.WEEK.getValue(), OrzDimension.FARM.getValue()));
            dimensionCriteriaList.addAll(doctorGroupDailyDao.findByDateType(date, DateDimension.MONTH.getValue(), OrzDimension.FARM.getValue()));
            dimensionCriteriaList.addAll(doctorGroupDailyDao.findByDateType(date, DateDimension.QUARTER.getValue(), OrzDimension.FARM.getValue()));
            dimensionCriteriaList.addAll(doctorGroupDailyDao.findByDateType(date, DateDimension.YEAR.getValue(), OrzDimension.FARM.getValue()));
            dimensionCriteriaList.addAll(doctorGroupDailyDao.findByDateType(date, DateDimension.WEEK.getValue(), OrzDimension.ORG.getValue()));
            dimensionCriteriaList.addAll(doctorGroupDailyDao.findByDateType(date, DateDimension.MONTH.getValue(), OrzDimension.ORG.getValue()));
            dimensionCriteriaList.addAll(doctorGroupDailyDao.findByDateType(date, DateDimension.QUARTER.getValue(), OrzDimension.ORG.getValue()));
            dimensionCriteriaList.addAll(doctorGroupDailyDao.findByDateType(date, DateDimension.YEAR.getValue(), OrzDimension.ORG.getValue()));
            List<DoctorDimensionCriteria> orgList = groupDailyList.stream().map(groupDaily ->
                    new DoctorDimensionCriteria(groupDaily.getOrgId(), OrzDimension.ORG.getValue(), groupDaily.getSumAt(), DateDimension.DAY.getValue(), groupDaily.getPigType())
            ).collect(Collectors.toList());
            dimensionCriteriaList.addAll(orgList);
            dimensionCriteriaList.parallelStream().forEach(dimensionCriteria ->
                    synchronizeGroupBiData(doctorGroupDailyDao.selectOneSumForDimension(dimensionCriteria), dimensionCriteria));
        }
        List<DoctorPigDaily> pigDailyList = doctorPigDailyDao.findByAfter(date);
        if (!Arguments.isNullOrEmpty(pigDailyList)) {
            synchronizePigForDay(pigDailyList);
            List<DoctorDimensionCriteria> criteriaList = Lists.newArrayList();
            criteriaList.addAll(doctorPigDailyDao.findByDateType(date, DateDimension.WEEK.getValue(), OrzDimension.FARM.getValue()));
            criteriaList.addAll(doctorPigDailyDao.findByDateType(date, DateDimension.MONTH.getValue(), OrzDimension.FARM.getValue()));
            criteriaList.addAll(doctorPigDailyDao.findByDateType(date, DateDimension.QUARTER.getValue(), OrzDimension.FARM.getValue()));
            criteriaList.addAll(doctorPigDailyDao.findByDateType(date, DateDimension.YEAR.getValue(), OrzDimension.FARM.getValue()));
            criteriaList.addAll(doctorPigDailyDao.findByDateType(date, DateDimension.WEEK.getValue(), OrzDimension.ORG.getValue()));
            criteriaList.addAll(doctorPigDailyDao.findByDateType(date, DateDimension.MONTH.getValue(), OrzDimension.ORG.getValue()));
            criteriaList.addAll(doctorPigDailyDao.findByDateType(date, DateDimension.QUARTER.getValue(), OrzDimension.ORG.getValue()));
            criteriaList.addAll(doctorPigDailyDao.findByDateType(date, DateDimension.YEAR.getValue(), OrzDimension.ORG.getValue()));
            List<DoctorDimensionCriteria> orgs = pigDailyList.stream().map(pigDaily ->
                    new DoctorDimensionCriteria(pigDaily.getOrgId(), OrzDimension.ORG.getValue(), pigDaily.getSumAt(), DateDimension.DAY.getValue(), null)
            ).collect(Collectors.toList());
            criteriaList.addAll(orgs);
            criteriaList.parallelStream().forEach(dimensionCriteria ->
                    synchronizePigBiData(doctorPigDailyDao.selectOneSumForDimension(dimensionCriteria), dimensionCriteria));
        }

    }


    private void synchronizeDeltaBiData(Long farmId, Date date) {

        List<DoctorGroupDaily> groupDailyList = doctorGroupDailyDao.findByFarmAndAfter(farmId, date);
        if (!Arguments.isNullOrEmpty(groupDailyList)) {
            synchronizeGroupForDay(groupDailyList);
            List<DoctorDimensionCriteria> dimensionCriteriaList = Lists.newArrayList();
            dimensionCriteriaList.addAll(doctorGroupDailyDao.findBySumAt(date, DateDimension.WEEK.getValue(), OrzDimension.FARM.getValue()));
            dimensionCriteriaList.addAll(doctorGroupDailyDao.findBySumAt(date, DateDimension.MONTH.getValue(), OrzDimension.FARM.getValue()));
            dimensionCriteriaList.addAll(doctorGroupDailyDao.findBySumAt(date, DateDimension.QUARTER.getValue(), OrzDimension.FARM.getValue()));
            dimensionCriteriaList.addAll(doctorGroupDailyDao.findBySumAt(date, DateDimension.YEAR.getValue(), OrzDimension.FARM.getValue()));
            dimensionCriteriaList.addAll(doctorGroupDailyDao.findBySumAt(date, DateDimension.WEEK.getValue(), OrzDimension.ORG.getValue()));
            dimensionCriteriaList.addAll(doctorGroupDailyDao.findBySumAt(date, DateDimension.MONTH.getValue(), OrzDimension.ORG.getValue()));
            dimensionCriteriaList.addAll(doctorGroupDailyDao.findBySumAt(date, DateDimension.QUARTER.getValue(), OrzDimension.ORG.getValue()));
            dimensionCriteriaList.addAll(doctorGroupDailyDao.findBySumAt(date, DateDimension.YEAR.getValue(), OrzDimension.ORG.getValue()));
            List<DoctorDimensionCriteria> orgList = groupDailyList.stream().map(groupDaily ->
                    new DoctorDimensionCriteria(groupDaily.getOrgId(), OrzDimension.ORG.getValue(), groupDaily.getSumAt(), DateDimension.DAY.getValue(), groupDaily.getPigType())
            ).collect(Collectors.toList());
            dimensionCriteriaList.addAll(orgList);
            dimensionCriteriaList.parallelStream().forEach(dimensionCriteria ->
                    synchronizeGroupBiData(doctorGroupDailyDao.selectOneSumForDimension(dimensionCriteria), dimensionCriteria));
        }
        List<DoctorPigDaily> pigDailyList = doctorPigDailyDao.findByFarmAndAfter(farmId, date);
        if (!Arguments.isNullOrEmpty(pigDailyList)) {
            synchronizePigForDay(pigDailyList);
            List<DoctorDimensionCriteria> criteriaList = Lists.newArrayList();
            criteriaList.addAll(doctorPigDailyDao.findBySumAt(date, DateDimension.WEEK.getValue(), OrzDimension.FARM.getValue()));
            criteriaList.addAll(doctorPigDailyDao.findBySumAt(date, DateDimension.MONTH.getValue(), OrzDimension.FARM.getValue()));
            criteriaList.addAll(doctorPigDailyDao.findBySumAt(date, DateDimension.QUARTER.getValue(), OrzDimension.FARM.getValue()));
            criteriaList.addAll(doctorPigDailyDao.findBySumAt(date, DateDimension.YEAR.getValue(), OrzDimension.FARM.getValue()));
            criteriaList.addAll(doctorPigDailyDao.findBySumAt(date, DateDimension.WEEK.getValue(), OrzDimension.ORG.getValue()));
            criteriaList.addAll(doctorPigDailyDao.findBySumAt(date, DateDimension.MONTH.getValue(), OrzDimension.ORG.getValue()));
            criteriaList.addAll(doctorPigDailyDao.findBySumAt(date, DateDimension.QUARTER.getValue(), OrzDimension.ORG.getValue()));
            criteriaList.addAll(doctorPigDailyDao.findBySumAt(date, DateDimension.YEAR.getValue(), OrzDimension.ORG.getValue()));
            List<DoctorDimensionCriteria> orgs = pigDailyList.stream().map(pigDaily ->
                    new DoctorDimensionCriteria(pigDaily.getOrgId(), OrzDimension.ORG.getValue(), pigDaily.getSumAt(), DateDimension.DAY.getValue(), null)
            ).collect(Collectors.toList());
            criteriaList.addAll(orgs);
            criteriaList.parallelStream().forEach(dimensionCriteria ->
                    synchronizePigBiData(doctorPigDailyDao.selectOneSumForDimension(dimensionCriteria), dimensionCriteria));
        }

    }

    /**
     * 增量同步猪场天维度
     * @param groupDailyList
     */
    private void synchronizeGroupForDay(List<DoctorGroupDaily> groupDailyList) {
        if (Arguments.isNullOrEmpty(groupDailyList)) {
            return;
        }
        DoctorGroupDailyExtend extend = new DoctorGroupDailyExtend();
        DoctorDimensionCriteria doctorDimensionCriteria = new DoctorDimensionCriteria();
        doctorDimensionCriteria.setOrzType(OrzDimension.FARM.getValue());
        doctorDimensionCriteria.setDateType(DateDimension.DAY.getValue());
        groupDailyList.forEach(groupDaily -> {
            BeanMapper.copy(groupDaily, extend);
            extend.setDailyLivestockOnHand(groupDaily.getEnd());
            extend.setStart(groupDaily.getStart());
            extend.setEnd(groupDaily.getEnd());
            synchronizeGroupBiData(extend, doctorDimensionCriteria);
        });
    }

    /**
     * 增量同步猪场天维度
     * @param pigDailyList
     */
    public void synchronizePigForDay(List<DoctorPigDaily> pigDailyList) {
        if (Arguments.isNullOrEmpty(pigDailyList)) {
            return;
        }
        DoctorPigDailyExtend extend = new DoctorPigDailyExtend();
        DoctorDimensionCriteria doctorDimensionCriteria = new DoctorDimensionCriteria();
        doctorDimensionCriteria.setOrzType(OrzDimension.FARM.getValue());
        doctorDimensionCriteria.setDateType(DateDimension.DAY.getValue());
        pigDailyList.forEach(pigDaily -> {
            BeanMapper.copy(pigDaily, extend);
            extend.setSowStart(pigDaily.getSowPhStart() + pigDaily.getSowCfStart());
            extend.setSowEnd(pigDaily.getSowPhEnd() + pigDaily.getSowCfEnd());
            extend.setSowDailyPigCount(extend.getSowEnd());
            extend.setBoarDailyPigCount(pigDaily.getBoarEnd());
            synchronizePigBiData(extend, doctorDimensionCriteria);
        });
    }

    /**
     * 清空bi相关表的所有数据
     */
    public void cleanFullBiData() {
        deliverSynchronizer.deleteAll();
        fattenSynchronizer.deleteAll();
        nurserySynchronizer.deleteAll();
        reserveSynchronizer.deleteAll();
        boarSynchronizer.deleteAll();
        sowSynchronizer.deleteAll();
//        matingSynchronizer.deleteAll();
//        efficiencySynchronizer.deleteAll();
    }

    /**
     * 全量同步实现
     */
    private void synchronizeBiDataImpl() {
        //同步猪场日
        synchronizeFullBiDataForDay();
        List<DoctorDimensionCriteria> dimensionCriteriaList = Lists.newArrayList();
        DoctorDimensionCriteria dimensionCriteria;
        //同步猪场周
        dimensionCriteria = new DoctorDimensionCriteria();
        dimensionCriteria.setOrzType(OrzDimension.FARM.getValue());
        dimensionCriteria.setDateType(DateDimension.WEEK.getValue());
        dimensionCriteriaList.add(dimensionCriteria);
        //同步猪场月
        dimensionCriteria = new DoctorDimensionCriteria();
        dimensionCriteria.setOrzType(OrzDimension.FARM.getValue());
        dimensionCriteria.setDateType(DateDimension.MONTH.getValue());
        dimensionCriteriaList.add(dimensionCriteria);
        //同步猪场季
        dimensionCriteria = new DoctorDimensionCriteria();
        dimensionCriteria.setOrzType(OrzDimension.FARM.getValue());
        dimensionCriteria.setDateType(DateDimension.QUARTER.getValue());
        dimensionCriteriaList.add(dimensionCriteria);

        //同步猪场年
        dimensionCriteria = new DoctorDimensionCriteria();
        dimensionCriteria.setOrzType(OrzDimension.FARM.getValue());
        dimensionCriteria.setDateType(DateDimension.YEAR.getValue());
        dimensionCriteriaList.add(dimensionCriteria);

        //同步公司日
        dimensionCriteria = new DoctorDimensionCriteria();
        dimensionCriteria.setOrzType(OrzDimension.ORG.getValue());
        dimensionCriteria.setDateType(DateDimension.DAY.getValue());
        dimensionCriteriaList.add(dimensionCriteria);

        //同步公司周
        dimensionCriteria = new DoctorDimensionCriteria();
        dimensionCriteria.setOrzType(OrzDimension.ORG.getValue());
        dimensionCriteria.setDateType(DateDimension.WEEK.getValue());
        dimensionCriteriaList.add(dimensionCriteria);

        //同步公司月
        dimensionCriteria = new DoctorDimensionCriteria();
        dimensionCriteria.setOrzType(OrzDimension.ORG.getValue());
        dimensionCriteria.setDateType(DateDimension.MONTH.getValue());
        dimensionCriteriaList.add(dimensionCriteria);

        //同步公司季
        dimensionCriteria = new DoctorDimensionCriteria();
        dimensionCriteria.setOrzType(OrzDimension.ORG.getValue());
        dimensionCriteria.setDateType(DateDimension.QUARTER.getValue());
        dimensionCriteriaList.add(dimensionCriteria);

        //同步公司年
        dimensionCriteria = new DoctorDimensionCriteria();
        dimensionCriteria.setOrzType(OrzDimension.ORG.getValue());
        dimensionCriteria.setDateType(DateDimension.YEAR.getValue());
        dimensionCriteriaList.add(dimensionCriteria);

        dimensionCriteriaList.parallelStream().forEach(this::synchronizeFullBiDataForDimension);
    }

    /**
     * 全量同步不同维度的数据
     *
     * @param dimensionCriteria
     */
    private void synchronizeFullBiDataForDimension(DoctorDimensionCriteria dimensionCriteria) {
        List<DoctorGroupDailyExtend> groupDailyList = doctorGroupDailyDao.sumForDimension(dimensionCriteria);
        groupDailyList.forEach(groupDaily -> synchronizeGroupBiData(groupDaily, dimensionCriteria));
        List<DoctorPigDailyExtend> pigDailyList = doctorPigDailyDao.sumForDimension(dimensionCriteria);
        pigDailyList.forEach(pigDaily -> synchronizePigBiData(pigDaily, dimensionCriteria));

//        efficiencySynchronizer.sync(dimensionCriteria);
//        warehouseSynchronizer.sync(dimensionCriteria);
    }

    /**
     * 全量同步猪场日维度
     */
    private void synchronizeFullBiDataForDay() {
        List<DoctorGroupDaily> groupDailyList;
        Integer pageSize = 5000;
        Integer pageNo = 0;
        while (true) {
            groupDailyList = doctorGroupDailyDao.paging(pageNo, pageSize).getData();
            synchronizeGroupForDay(groupDailyList);
            if (groupDailyList.size() < 5000) break;
            pageNo += 5000;
        }

        List<DoctorPigDaily> pigDailyList;
        pageNo = 0;
        while (true) {
            pigDailyList = doctorPigDailyDao.paging(pageNo, pageSize).getData();
            synchronizePigForDay(pigDailyList);
            if (pigDailyList.size() < 5000) break;
            pageNo += 5000;
        }
    }

    /**
     * 同步某一维度猪群数据最终地方
     * @param groupDaily
     * @param dimensionCriteria
     */
    private void synchronizeGroupBiData(DoctorGroupDailyExtend groupDaily, DoctorDimensionCriteria dimensionCriteria) {
        PigType pigType = expectNotNull(PigType.from(groupDaily.getPigType()), "pigType.is.illegal");
        if (Objects.equals(dimensionCriteria.getOrzType(), OrzDimension.ORG.getValue())) {
            dimensionCriteria.setOrzId(groupDaily.getOrgId());
        } else {
            dimensionCriteria.setOrzId(groupDaily.getFarmId());
        }
        dimensionCriteria.setSumAt(groupDaily.getSumAt());
        dimensionCriteria.setPigType(groupDaily.getPigType());
        if (!Objects.equals(dimensionCriteria.getDateType(), DateDimension.DAY.getValue())
                || !Objects.equals(dimensionCriteria.getOrzType(), OrzDimension.FARM.getValue())) {
            Integer start;
            Integer end;
            if (Objects.equals(dimensionCriteria.getOrzType(), OrzDimension.FARM.getValue())) {
                start = doctorGroupDailyDao.farmStart(dimensionCriteria);
                end = doctorGroupDailyDao.farmEnd(dimensionCriteria);
            } else {
                start = doctorGroupDailyDao.orgStart(dimensionCriteria);
                end = doctorGroupDailyDao.orgEnd(dimensionCriteria);
                groupDaily.setDailyLivestockOnHand(doctorGroupDailyDao.orgDayAvgLiveStock(dimensionCriteria));
            }
            groupDaily.setStart(start);
            groupDaily.setEnd(end);
        }
        switch (pigType) {
            case DELIVER_SOW:
                deliverSynchronizer.synchronize(groupDaily, dimensionCriteria);
                break;
            case NURSERY_PIGLET:
                nurserySynchronizer.synchronize(groupDaily, dimensionCriteria);
                break;
            case FATTEN_PIG:
                fattenSynchronizer.synchronize(groupDaily, dimensionCriteria);
                break;
            case RESERVE:

                reserveSynchronizer.synchronize(groupDaily, dimensionCriteria);
                break;
        }
    }

    /**
     * 同步某一维度猪数据最终地方
     * @param dailyExtend
     * @param dimensionCriteria
     */
    private void synchronizePigBiData(DoctorPigDailyExtend dailyExtend, DoctorDimensionCriteria dimensionCriteria) {
        if (Objects.equals(dimensionCriteria.getOrzType(), OrzDimension.ORG.getValue())) {
            dimensionCriteria.setOrzId(dailyExtend.getOrgId());
        } else {
            dimensionCriteria.setOrzId(dailyExtend.getFarmId());
        }
        dimensionCriteria.setSumAt(dailyExtend.getSumAt());
        if (!Objects.equals(dimensionCriteria.getDateType(), DateDimension.DAY.getValue())
                || !Objects.equals(dimensionCriteria.getOrzType(), OrzDimension.FARM.getValue())) {
            DoctorPigDailyExtend start;
            DoctorPigDailyExtend end;
            if (Objects.equals(dimensionCriteria.getOrzType(), OrzDimension.FARM.getValue())) {
                start = doctorPigDailyDao.farmStart(dimensionCriteria);
                end = doctorPigDailyDao.farmEnd(dimensionCriteria);
            } else {
                start = doctorPigDailyDao.orgStart(dimensionCriteria);
                end = doctorPigDailyDao.orgEnd(dimensionCriteria);
                DoctorPigDailyExtend dayAvgLiveStock = doctorPigDailyDao.orgSumDimension(dimensionCriteria);
                int count = doctorPigDailyDao.countDimension(dimensionCriteria);
                dailyExtend.setBoarDailyPigCount(dayAvgLiveStock.getBoarDailyPigCount()/count);
                dailyExtend.setSowDailyPigCount(dayAvgLiveStock.getSowDailyPigCount()/count);
            }
            dailyExtend.setSowCfStart(start.getSowCfStart());
            dailyExtend.setSowPhStart(start.getSowPhStart());
            dailyExtend.setSowStart(start.getSowStart());
            dailyExtend.setBoarStart(start.getBoarStart());
            dailyExtend.setSowCfEnd(end.getSowCfEnd());
            dailyExtend.setSowPhEnd(end.getSowPhEnd());
            dailyExtend.setSowEnd(end.getSowEnd());
            dailyExtend.setBoarEnd(end.getBoarEnd());
            dailyExtend.setSowPhMating(end.getSowPhMating());
            dailyExtend.setSowPhKonghuai(end.getSowPhKonghuai());
            dailyExtend.setSowPhPregnant(end.getSowPhPregnant());
        }
        boarSynchronizer.synchronize(dailyExtend, dimensionCriteria);
        sowSynchronizer.synchronize(dailyExtend, dimensionCriteria);
        matingSynchronizer.synchronize(dailyExtend, dimensionCriteria);
        deliverSynchronizer.synchronize(dailyExtend, dimensionCriteria);
    }
}
