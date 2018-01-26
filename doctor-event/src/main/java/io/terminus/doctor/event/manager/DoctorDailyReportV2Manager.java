package io.terminus.doctor.event.manager;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.event.cache.DoctorDepartmentCache;
import io.terminus.doctor.event.dao.DoctorEventModifyLogDao;
import io.terminus.doctor.event.dao.DoctorGroupDailyDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupStatisticDao;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigStatisticDao;
import io.terminus.doctor.event.dto.DoctorFarmEarlyEventAtDto;
import io.terminus.doctor.event.dto.DoctorStatisticCriteria;
import io.terminus.doctor.event.enums.ReportTime;
import io.terminus.doctor.event.model.DoctorEventModifyLog;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorReportWriteService;
import io.terminus.doctor.user.dto.DoctorDepartmentLinerDto;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectNotNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by xjn on 17/12/12.
 * email:xiaojiannan@terminus.io
 */
@Slf4j
@Component
public class DoctorDailyReportV2Manager {
    private final DoctorGroupStatisticDao groupStatisticDao;
    private final DoctorGroupDailyDao groupDailyDao;
    private final DoctorPigStatisticDao pigStatisticDao;
    private final DoctorPigDailyDao pigDailyDao;
    private final DoctorPigEventDao doctorPigEventDao;
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorEventModifyLogDao doctorEventModifyLogDao;
    private final DoctorDepartmentCache departmentCache;
    private final DoctorReportWriteService doctorReportWriteService;

    @Autowired
    public DoctorDailyReportV2Manager(DoctorGroupStatisticDao groupStatisticDao, DoctorGroupDailyDao groupDailyDao, DoctorPigStatisticDao pigStatisticDao, DoctorPigDailyDao pigDailyDao, DoctorPigEventDao doctorPigEventDao, DoctorGroupEventDao doctorGroupEventDao, DoctorEventModifyLogDao doctorEventModifyLogDao, DoctorDepartmentCache departmentCache, DoctorReportWriteService doctorReportWriteService) {
        this.groupStatisticDao = groupStatisticDao;
        this.groupDailyDao = groupDailyDao;
        this.pigStatisticDao = pigStatisticDao;
        this.pigDailyDao = pigDailyDao;
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorEventModifyLogDao = doctorEventModifyLogDao;
        this.departmentCache = departmentCache;
        this.doctorReportWriteService = doctorReportWriteService;
    }

    /**
     * 刷新猪群日报
     */
    public DoctorGroupDaily flushGroupDaily(DoctorStatisticCriteria criteria) {
        DoctorGroupDaily doctorGroupDaily = groupDailyDao.findBy(criteria.getFarmId(), criteria.getPigType(), criteria.getSumAt());
        if (isNull(doctorGroupDaily)) {
            DoctorDepartmentLinerDto departmentLinerDto = departmentCache.getUnchecked(criteria.getFarmId());
            doctorGroupDaily = new DoctorGroupDaily();
            doctorGroupDaily.setOrgId(departmentLinerDto.getOrgId());
            doctorGroupDaily.setOrgName(departmentLinerDto.getOrgName());
            doctorGroupDaily.setFarmId(criteria.getFarmId());
            doctorGroupDaily.setFarmName(departmentLinerDto.getFarmName());
            doctorGroupDaily.setPigType(criteria.getPigType());
            doctorGroupDaily.setSumAt(DateUtil.toDate(criteria.getSumAt()));
        }

        doctorGroupDaily.setStart(groupStatisticDao.realTimeLiveStockGroup(criteria.getFarmId(), criteria.getPigType(),
                DateUtil.toDateString(new DateTime(DateUtil.toDate(criteria.getSumAt())).minusDays(1).toDate())));
        doctorGroupDaily.setTurnInto(groupStatisticDao.turnInto(criteria));
        doctorGroupDaily.setTurnIntoWeight(groupStatisticDao.turnIntoWeight(criteria));
        doctorGroupDaily.setTurnIntoAge(groupStatisticDao.turnIntoAge(criteria));
        doctorGroupDaily.setChgFarmIn(groupStatisticDao.chgFarmIn(criteria));
        doctorGroupDaily.setChgFarm(groupStatisticDao.chgFarm(criteria));
        doctorGroupDaily.setChgFarmWeight(groupStatisticDao.chgFarmWeight(criteria));
        doctorGroupDaily.setSale(groupStatisticDao.sale(criteria));
        doctorGroupDaily.setSaleWeight(groupStatisticDao.saleWeight(criteria));
        doctorGroupDaily.setDead(groupStatisticDao.dead(criteria));
        doctorGroupDaily.setWeedOut(groupStatisticDao.weedOut(criteria));
        doctorGroupDaily.setOtherChange(groupStatisticDao.otherChange(criteria));
        doctorGroupDaily.setTurnOutWeight(groupStatisticDao.turnOutWeight(criteria));
        doctorGroupDaily.setEnd(groupStatisticDao.realTimeLiveStockGroup(criteria.getFarmId(), criteria.getPigType(), criteria.getSumAt()));

        PigType pigType = expectNotNull(PigType.from(criteria.getPigType()), "pigType.is.illegal");
        switch (pigType) {
            case DELIVER_SOW:
                flushDeliverDaily(doctorGroupDaily, criteria);
                break;
            case NURSERY_PIGLET:
                flushNurseryDaily(doctorGroupDaily, criteria);
                break;
            case FATTEN_PIG:
                flushFattenDaily(doctorGroupDaily, criteria);
                break;
            case RESERVE:
                flushReserveDaily(doctorGroupDaily, criteria);
                break;
        }
        createOrUpdateGroupDaily(doctorGroupDaily);
        return doctorGroupDaily;
    }

    public DoctorGroupDaily findDoctorGroupDaily(Long farmId, Integer pigType, Date sumAt) {
        DoctorGroupDaily doctorGroupDaily = groupDailyDao.findBy(farmId, pigType, sumAt);
//        if (isNull(doctorGroupDaily)) {
//            doctorGroupDaily = flushGroupDaily(new DoctorStatisticCriteria(farmId, pigType, DateUtil.toDateString(sumAt)));
//        }
        return doctorGroupDaily;
    }

    public void createOrUpdateGroupDaily(DoctorGroupDaily doctorGroupDaily) {
        if (isNull(doctorGroupDaily.getId())) {
            groupDailyDao.create(doctorGroupDaily);
        } else {
            expectTrue(groupDailyDao.update(doctorGroupDaily), "concurrent.error");
        }
    }

    /**
     * 刷新猪日报
     */
    public DoctorPigDaily flushPigDaily(DoctorStatisticCriteria criteria) {
        DoctorPigDaily doctorPigDaily = pigDailyDao.findBy(criteria.getFarmId(), criteria.getSumAt());
        if (isNull(doctorPigDaily)) {
            DoctorDepartmentLinerDto departmentLinerDto = departmentCache.getUnchecked(criteria.getFarmId());
            doctorPigDaily = new DoctorPigDaily();
            doctorPigDaily.setOrgId(departmentLinerDto.getOrgId());
            doctorPigDaily.setOrgName(departmentLinerDto.getOrgName());
            doctorPigDaily.setFarmId(criteria.getFarmId());
            doctorPigDaily.setFarmName(departmentLinerDto.getFarmName());
            doctorPigDaily.setSumAt(DateUtil.toDate(criteria.getSumAt()));
        }


        //统计进场未配种的母猪数。目前只在计算NPD时用到
        doctorPigDaily.setSowNotMatingCount(pigStatisticDao.sowEntryAndNotMatingNum(criteria));

        flushPhPigDaily(doctorPigDaily, criteria);
        flushCfPigDaily(doctorPigDaily, criteria);
        flushBoarPigDaily(doctorPigDaily, criteria);

        createOrUpdatePigDaily(doctorPigDaily);
        return doctorPigDaily;
    }

    public DoctorPigDaily findDoctorPigDaily(Long farmId, Date sumAt) {
        DoctorPigDaily doctorPigDaily = pigDailyDao.findBy(farmId, sumAt);
//        if (isNull(doctorPigDaily)) {
//            doctorPigDaily = flushPigDaily(new DoctorStatisticCriteria(farmId, DateUtil.toDateString(sumAt)));
//        }
        return doctorPigDaily;
    }

    public void createOrUpdatePigDaily(DoctorPigDaily doctorPigDaily) {
        if (isNull(doctorPigDaily.getId())) {
            pigDailyDao.create(doctorPigDaily);
        } else {
            expectTrue(pigDailyDao.update(doctorPigDaily), "concurrent.error");
        }
    }

    /**
     * 生成昨天和今天的猪场日报
     *
     * @param farmIds 猪场ids
     */
    public void generateYesterdayAndToday(List<Long> farmIds) {
        Date today = Dates.startOfDay(new Date());
        Date yesterday = new DateTime(today).minusDays(10).toDate();
        Map<Long, Date> farmToDate = queryFarmEarlyEventAtImpl(DateUtil.toDateString(yesterday));
        farmIds.parallelStream().forEach(farmId -> {
            try {
                DoctorStatisticCriteria criteria = new DoctorStatisticCriteria();
                log.info("generate farm daily, farmId:{}", farmId);
                criteria.setFarmId(farmId);
                Date temp = yesterday;
                if (farmToDate.containsKey(farmId)) {
                    temp = farmToDate.get(farmId);
                }
                List<Date> list = DateUtil.getDates(temp, today);
                list.parallelStream().forEach(date -> {
                    DoctorStatisticCriteria criteria1 = new DoctorStatisticCriteria();
                    BeanMapper.copy(criteria, criteria1);
                    criteria1.setSumAt(DateUtil.toDateString(date));
                    flushFarmDaily(criteria1);
                });
            } catch (Exception e) {
                log.error("generate yesterday and today, farmId:{},cause:{}", farmId, Throwables.getStackTraceAsString(e));
            }
        });

        doctorReportWriteService.flushNPD(farmIds, today, ReportTime.MONTH);

    }

    /**
     * 刷新猪场日报
     */
    public void flushFarmDaily(DoctorStatisticCriteria criteria) {
        PigType.GROUP_TYPES.forEach(pigType -> {
            criteria.setPigType(pigType);
            flushGroupDaily(criteria);
        });
        flushPigDaily(criteria);

        // TODO: 17/12/21 效率指标 
    }

    /**
     * 产房仔猪
     *
     * @param doctorGroupDaily 猪群日报
     * @param criteria         条件
     */
    private void flushDeliverDaily(DoctorGroupDaily doctorGroupDaily, DoctorStatisticCriteria criteria) {
        doctorGroupDaily.setToNursery(groupStatisticDao.toNursery(criteria));
        doctorGroupDaily.setToNurseryWeight(groupStatisticDao.toNurseryWeight(criteria));
        doctorGroupDaily.setDeliverHandTurnInto(groupStatisticDao.deliverHandTurnInto(criteria));
        doctorGroupDaily.setDeliverTurnOutAge(groupStatisticDao.deliverTurnOutAge(criteria));

    }

    /**
     * 保育
     *
     * @param doctorGroupDaily 猪群日报
     * @param criteria         条件
     */
    private void flushNurseryDaily(DoctorGroupDaily doctorGroupDaily, DoctorStatisticCriteria criteria) {
        doctorGroupDaily.setToFatten(groupStatisticDao.toFatten(criteria));
        doctorGroupDaily.setToFattenWeight(groupStatisticDao.toFattenWeight(criteria));
        doctorGroupDaily.setToHoubei(groupStatisticDao.toHoubei(criteria));
        doctorGroupDaily.setToHoubeiWeight(groupStatisticDao.toHoubeiWeight(criteria));
        doctorGroupDaily.setTurnActualCount(groupStatisticDao.turnActualCount(criteria));
        doctorGroupDaily.setTurnActualWeight(groupStatisticDao.turnActualWeight(criteria));
        doctorGroupDaily.setTurnActualAge(groupStatisticDao.turnActualAge(criteria));
        doctorGroupDaily.setNetWeightGain(groupStatisticDao.netWeightGain(criteria));
    }

    /**
     * 育肥
     *
     * @param doctorGroupDaily 猪群日报
     * @param criteria         条件
     */
    private void flushFattenDaily(DoctorGroupDaily doctorGroupDaily, DoctorStatisticCriteria criteria) {
        doctorGroupDaily.setToHoubei(groupStatisticDao.toHoubei(criteria));
        doctorGroupDaily.setToHoubeiWeight(groupStatisticDao.toHoubeiWeight(criteria));
        doctorGroupDaily.setTurnActualCount(groupStatisticDao.turnActualCount(criteria));
        doctorGroupDaily.setTurnActualWeight(groupStatisticDao.turnActualWeight(criteria));
        doctorGroupDaily.setTurnActualAge(groupStatisticDao.turnActualAge(criteria));
        doctorGroupDaily.setNetWeightGain(groupStatisticDao.netWeightGain(criteria));
    }

    /**
     * 后备
     *
     * @param doctorGroupDaily 猪群日报
     * @param criteria         条件
     */
    private void flushReserveDaily(DoctorGroupDaily doctorGroupDaily, DoctorStatisticCriteria criteria) {
        doctorGroupDaily.setToFatten(groupStatisticDao.toFatten(criteria));
        doctorGroupDaily.setTurnSeed(groupStatisticDao.turnSeed(criteria));
    }

    /**
     * 配怀
     *
     * @param doctorPigDaily 日报
     * @param criteria       条件
     */
    private void flushPhPigDaily(DoctorPigDaily doctorPigDaily, DoctorStatisticCriteria criteria) {
        doctorPigDaily.setSowPhStart(pigStatisticDao.phLiveStock(criteria.getFarmId(),
                DateUtil.toDateString(new DateTime(DateUtil.toDate(criteria.getSumAt())).minusDays(1).toDate())));
        doctorPigDaily.setSowPhReserveIn(pigStatisticDao.sowPhReserveIn(criteria));
        doctorPigDaily.setSowPhWeanIn(pigStatisticDao.sowPhWeanIn(criteria));
        doctorPigDaily.setSowPhEntryIn(pigStatisticDao.sowPhEntryIn(criteria));
        doctorPigDaily.setSowPhChgFarmIn((pigStatisticDao.sowPhChgFarmIn(criteria)));
        doctorPigDaily.setSowPhDead(pigStatisticDao.sowPhDead(criteria));
        doctorPigDaily.setSowPhWeedOut(pigStatisticDao.sowPhWeedOut(criteria));
        doctorPigDaily.setSowPhSale(pigStatisticDao.sowPhSale(criteria));
        doctorPigDaily.setSowPhChgFarm(pigStatisticDao.sowPhChgFarm(criteria));
        doctorPigDaily.setSowPhOtherOut(pigStatisticDao.sowPhOtherOut(criteria));
        doctorPigDaily.setMateHb(pigStatisticDao.mateHb(criteria));
        doctorPigDaily.setMateDn(pigStatisticDao.mateDn(criteria));
        doctorPigDaily.setMateFq(pigStatisticDao.mateFq(criteria));
        doctorPigDaily.setMateLc(pigStatisticDao.mateLc(criteria));
        doctorPigDaily.setMateYx(pigStatisticDao.mateYx(criteria));
        doctorPigDaily.setMatingCount(pigStatisticDao.matingCount(criteria));
        doctorPigDaily.setSowPhMating(pigStatisticDao.sowPhMating(criteria));
        doctorPigDaily.setSowPhKonghuai(pigStatisticDao.sowPhKonghuai(criteria));
        doctorPigDaily.setSowPhPregnant(pigStatisticDao.sowPhPregnant(criteria));
        doctorPigDaily.setPregPositive(pigStatisticDao.pregPositive(criteria));
        doctorPigDaily.setPregNegative(pigStatisticDao.pregNegative(criteria));
        doctorPigDaily.setPregFanqing(pigStatisticDao.pregFanqing(criteria));
        doctorPigDaily.setPregLiuchan(pigStatisticDao.pregLiuchan(criteria));
        doctorPigDaily.setWeanMate(pigStatisticDao.weanMate(criteria));
        doctorPigDaily.setWeanDeadWeedOut(pigStatisticDao.weanDeadWeedOut(criteria));
        doctorPigDaily.setSowPhEnd(pigStatisticDao.phLiveStock(criteria.getFarmId(), criteria.getSumAt()));
    }

    /**
     * 产房
     *
     * @param doctorPigDaily 日报
     * @param criteria       条件
     */
    private void flushCfPigDaily(DoctorPigDaily doctorPigDaily, DoctorStatisticCriteria criteria) {
        doctorPigDaily.setSowCfStart(pigStatisticDao.cfLiveStock(criteria.getFarmId(),
                DateUtil.toDateString(new DateTime(DateUtil.toDate(criteria.getSumAt())).minusDays(1).toDate())));
        doctorPigDaily.setSowCfEnd(pigStatisticDao.cfLiveStock(criteria.getFarmId(), criteria.getSumAt()));
        doctorPigDaily.setSowCfIn(pigStatisticDao.sowCfIn(criteria));
        doctorPigDaily.setSowCfInFarmIn(pigStatisticDao.sowCfInFarmIn(criteria));
        doctorPigDaily.setSowCfDead(pigStatisticDao.sowCfDead(criteria));
        doctorPigDaily.setSowCfWeedOut(pigStatisticDao.sowCfWeedOut(criteria));
        doctorPigDaily.setSowCfSale(pigStatisticDao.sowCfSale(criteria));
        doctorPigDaily.setSowCfChgFarm(pigStatisticDao.sowCfChgFarm(criteria));
        doctorPigDaily.setSowCfOtherOut(pigStatisticDao.sowCfOtherOut(criteria));
        doctorPigDaily.setEarlyMating(pigStatisticDao.earlyMating(criteria));
        doctorPigDaily.setEarlyFarrowNest(pigStatisticDao.earlyFarrowNest(criteria));
        doctorPigDaily.setLaterNest(pigStatisticDao.laterNest(criteria));
        doctorPigDaily.setFarrowNest(pigStatisticDao.farrowNest(criteria));
        doctorPigDaily.setFarrowLive(pigStatisticDao.farrowLive(criteria));
        doctorPigDaily.setFarrowHealth(pigStatisticDao.farrowHealth(criteria));
        doctorPigDaily.setFarrowWeak(pigStatisticDao.farrowWeak(criteria));
        doctorPigDaily.setFarrowDead(pigStatisticDao.farrowDead(criteria));
        doctorPigDaily.setFarrowjmh(pigStatisticDao.farrowjmh(criteria));
        doctorPigDaily.setFarrowWeight(pigStatisticDao.farrowWeight(criteria));
        doctorPigDaily.setWeanNest(pigStatisticDao.weanNest(criteria));
        doctorPigDaily.setWeanQualifiedCount(pigStatisticDao.weanQualifiedCount(criteria));
        doctorPigDaily.setWeanCount(pigStatisticDao.weanCount(criteria));
        doctorPigDaily.setWeanDayAge(pigStatisticDao.weanDayAge(criteria));
        doctorPigDaily.setWeanWeight(pigStatisticDao.weanWeight(criteria));
    }

    /**
     * 公猪
     *
     * @param doctorPigDaily 日报
     * @param criteria       条件
     */
    private void flushBoarPigDaily(DoctorPigDaily doctorPigDaily, DoctorStatisticCriteria criteria) {
        doctorPigDaily.setBoarStart(pigStatisticDao.boarLiveStock(criteria.getFarmId(),
                DateUtil.toDateString(new DateTime(DateUtil.toDate(criteria.getSumAt())).minusDays(1).toDate())));
        doctorPigDaily.setBoarIn(pigStatisticDao.boarIn(criteria));
        doctorPigDaily.setBoarChgFarmIn(pigStatisticDao.boarChgFarmIn(criteria));
        doctorPigDaily.setBoarDead(pigStatisticDao.boarDead(criteria));
        doctorPigDaily.setBoarWeedOut(pigStatisticDao.boarWeedOut(criteria));
        doctorPigDaily.setBoarSale(pigStatisticDao.boarSale(criteria));
        doctorPigDaily.setBoarOtherOut(pigStatisticDao.boarOtherOut(criteria));
        doctorPigDaily.setBoarEnd(pigStatisticDao.boarLiveStock(criteria.getFarmId(), criteria.getSumAt()));
    }

    private Map<Long, Date> queryFarmEarlyEventAtImpl(String startDate) {
        List<DoctorFarmEarlyEventAtDto> list1 = doctorPigEventDao.getFarmEarlyEventAt(startDate);
        List<DoctorFarmEarlyEventAtDto> list2 = doctorGroupEventDao.getFarmEarlyEventAt(startDate);
        list1.addAll(list2);

        List<DoctorEventModifyLog> modifyLogList = doctorEventModifyLogDao.getEventModifyLog(startDate);
        List<DoctorFarmEarlyEventAtDto> list3 = Lists.newArrayList();
        modifyLogList.forEach(modifyLog -> {
            add(list3, modifyLog.getDeleteEvent(), modifyLog.getType());
            add(list3, modifyLog.getFromEvent(), modifyLog.getType());
            add(list3, modifyLog.getToEvent(), modifyLog.getType());
        });
        list1.addAll(list3);
        Map<Long, List<DoctorFarmEarlyEventAtDto>> map = list1.stream().collect(Collectors.groupingBy(DoctorFarmEarlyEventAtDto::getFarmId));
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey
                , v -> v.getValue().stream().min(Comparator.comparing(DoctorFarmEarlyEventAtDto::getEventAt)).get().getEventAt()));

    }

    private void add(List<DoctorFarmEarlyEventAtDto> list3,
                     String json, Integer type) {
        if (notNull(json)) {
            if (Objects.equals(type, DoctorEventModifyRequest.TYPE.PIG.getValue())) {
                DoctorPigEvent event = JsonMapperUtil.nonEmptyMapper().fromJson(json, DoctorPigEvent.class);
                list3.add(new DoctorFarmEarlyEventAtDto(event.getFarmId(), event.getEventAt()));
            } else {
                DoctorGroupEvent event = JsonMapperUtil.nonEmptyMapper().fromJson(json, DoctorGroupEvent.class);
                list3.add(new DoctorFarmEarlyEventAtDto(event.getFarmId(), event.getEventAt()));
            }
        }
    }
}
