package io.terminus.doctor.event.manager;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorGroupDailyDao;
import io.terminus.doctor.event.dao.DoctorGroupStatisticDao;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.dao.DoctorPigStatisticDao;
import io.terminus.doctor.event.dto.DoctorStatisticCriteria;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import io.terminus.doctor.event.model.DoctorPigDaily;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.doctor.common.utils.Checks.expectNotNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by xjn on 17/12/12.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorDailyReportV2Manager {
    private final DoctorGroupStatisticDao groupStatisticDao;
    private final DoctorGroupDailyDao groupDailyDao;
    private final DoctorPigStatisticDao pigStatisticDao;
    private final DoctorPigDailyDao pigDailyDao;

    @Autowired
    public DoctorDailyReportV2Manager(DoctorGroupStatisticDao groupStatisticDao, DoctorGroupDailyDao groupDailyDao, DoctorPigStatisticDao pigStatisticDao, DoctorPigDailyDao pigDailyDao) {
        this.groupStatisticDao = groupStatisticDao;
        this.groupDailyDao = groupDailyDao;
        this.pigStatisticDao = pigStatisticDao;
        this.pigDailyDao = pigDailyDao;
    }

    public void flushGroupDaily(DoctorStatisticCriteria criteria){
        DoctorGroupDaily doctorGroupDaily = groupDailyDao.findBy(criteria.getFarmId(), criteria.getPigType(), criteria.getSumAt());
        if (isNull(doctorGroupDaily)) {
            doctorGroupDaily = new DoctorGroupDaily();
            doctorGroupDaily.setFarmId(criteria.getFarmId());
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
            case DELIVER_SOW: flushDeliverDaily(doctorGroupDaily, criteria); break;
            case NURSERY_PIGLET: flushNurseryDaily(doctorGroupDaily, criteria); break;
            case FATTEN_PIG: flushFattenDaily(doctorGroupDaily, criteria); break;
            case RESERVE: flushReserveDaily(doctorGroupDaily, criteria); break;
        }
        createOrUpdateGroupDaily(doctorGroupDaily);
    }


    private void flushDeliverDaily(DoctorGroupDaily doctorGroupDaily, DoctorStatisticCriteria criteria){
        doctorGroupDaily.setToNursery(groupStatisticDao.toNursery(criteria));
    }

    private void flushNurseryDaily(DoctorGroupDaily doctorGroupDaily, DoctorStatisticCriteria criteria){
        doctorGroupDaily.setToFatten(groupStatisticDao.toFatten(criteria));
        doctorGroupDaily.setToFattenWeight(groupStatisticDao.toFattenWeight(criteria));
        doctorGroupDaily.setToHoubei(groupStatisticDao.toHoubei(criteria));
        doctorGroupDaily.setToHoubeiWeight(groupStatisticDao.toHoubeiWeight(criteria));
    }

    private void flushFattenDaily(DoctorGroupDaily doctorGroupDaily, DoctorStatisticCriteria criteria){
        doctorGroupDaily.setToHoubei(groupStatisticDao.toHoubei(criteria));
        doctorGroupDaily.setToHoubeiWeight(groupStatisticDao.toHoubeiWeight(criteria));
    }

    private void flushReserveDaily(DoctorGroupDaily doctorGroupDaily, DoctorStatisticCriteria criteria){
        doctorGroupDaily.setToFatten(groupStatisticDao.toFatten(criteria));
        doctorGroupDaily.setTurnSeed(groupStatisticDao.turnSeed(criteria));
    }

    /**
     * 有则更新,无则创建
     */
    public void createOrUpdateGroupDaily(DoctorGroupDaily doctorGroupDaily) {
        if (isNull(doctorGroupDaily.getId())) {
            groupDailyDao.create(doctorGroupDaily);
        } else {
            expectTrue(groupDailyDao.update(doctorGroupDaily), "concurrent.error");
        }
    }

    public void flushPigDaily(DoctorStatisticCriteria criteria) {
        DoctorPigDaily doctorPigDaily = pigDailyDao.findBy(criteria.getFarmId(), criteria.getSumAt());
        if (isNull(doctorPigDaily)) {
            doctorPigDaily = new DoctorPigDaily();
            doctorPigDaily.setFarmId(criteria.getFarmId());
            doctorPigDaily.setSumAt(DateUtil.toDate(criteria.getSumAt()));
        }

        flushPhPigDaily(doctorPigDaily, criteria);
        flushCfPigDaily(doctorPigDaily, criteria);
        flushBoarPigDaily(doctorPigDaily, criteria);

        createOrUpdatePigDaily(doctorPigDaily);
    }

    /**
     * 配怀
     * @param doctorPigDaily 日报
     * @param criteria 条件
     */
    private void flushPhPigDaily(DoctorPigDaily doctorPigDaily, DoctorStatisticCriteria criteria){
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
        doctorPigDaily.setMatingCount(pigStatisticDao.matingCount(criteria));
        doctorPigDaily.setPregPositive(pigStatisticDao.pregPositive(criteria));
        doctorPigDaily.setPregNegative(pigStatisticDao.pregNegative(criteria));
        doctorPigDaily.setPregFanqing(pigStatisticDao.pregFanqing(criteria));
        doctorPigDaily.setPregLiuchan(pigStatisticDao.pregLiuchan(criteria));
        doctorPigDaily.setSowPhEnd(pigStatisticDao.phLiveStock(criteria.getFarmId(), criteria.getSumAt()));
    }

    /**
     * 产房
     * @param doctorPigDaily 日报
     * @param criteria 条件
     */
    private void flushCfPigDaily(DoctorPigDaily doctorPigDaily, DoctorStatisticCriteria criteria){
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
        doctorPigDaily.setFarrowNest(pigStatisticDao.farrowNest(criteria));
        doctorPigDaily.setFarrowHealth(pigStatisticDao.farrowHealth(criteria));
        doctorPigDaily.setFarrowWeak(pigStatisticDao.farrowWeak(criteria));
        doctorPigDaily.setFarrowDead(pigStatisticDao.farrowDead(criteria));
        doctorPigDaily.setFarrowjmh(pigStatisticDao.farrowjmh(criteria));
        doctorPigDaily.setFarrowWeight(pigStatisticDao.farrowWeight(criteria));
        doctorPigDaily.setWeanNest(pigStatisticDao.weanNest(criteria));
        doctorPigDaily.setWeanQualifiedCount(pigStatisticDao.weanQualifiedCount(criteria));
        doctorPigDaily.setWeanCount(pigStatisticDao.weanCount(criteria));
//        doctorPigDaily.setWeanCount(pigStatisticDao.weanDayAge(criteria));
        doctorPigDaily.setWeanWeight(pigStatisticDao.weanWeight(criteria));
    }

    /**
     * 公猪
     * @param doctorPigDaily 日报
     * @param criteria 条件
     */
    private void flushBoarPigDaily(DoctorPigDaily doctorPigDaily, DoctorStatisticCriteria criteria){
        doctorPigDaily.setBoarStart(pigStatisticDao.boarLiveStock(criteria.getFarmId(),
                DateUtil.toDateString(new DateTime(DateUtil.toDate(criteria.getSumAt())).minusDays(1).toDate())));
        doctorPigDaily.setBoarIn(pigStatisticDao.boarIn(criteria));
        doctorPigDaily.setBoarDead(pigStatisticDao.boarDead(criteria));
        doctorPigDaily.setBoarWeedOut(pigStatisticDao.boarWeedOut(criteria));
        doctorPigDaily.setBoarSale(pigStatisticDao.boarSale(criteria));
        doctorPigDaily.setBoarOtherOut(pigStatisticDao.boarOtherOut(criteria));
        doctorPigDaily.setBoarEnd(pigStatisticDao.boarLiveStock(criteria.getFarmId(), criteria.getSumAt()));
    }

    /**
     * 有则更新,无则创建
     */
    public void createOrUpdatePigDaily(DoctorPigDaily doctorPigDaily) {
        if (isNull(doctorPigDaily.getId())) {
            pigDailyDao.create(doctorPigDaily);
        } else {
            expectTrue(pigDailyDao.update(doctorPigDaily), "concurrent.error");
        }
    }
}
