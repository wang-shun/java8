package io.terminus.doctor.event.manager;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dao.DoctorGroupDailyDao;
import io.terminus.doctor.event.dao.DoctorGroupStatisticDao;
import io.terminus.doctor.event.dto.DoctorStatisticCriteria;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.terminus.doctor.common.utils.Checks.expectNotNull;

/**
 * Created by xjn on 17/12/12.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorDailyReportV2Manager {
    private final DoctorGroupStatisticDao groupStatisticDao;
    private final DoctorGroupDailyDao groupDailyDao;

    @Autowired
    public DoctorDailyReportV2Manager(DoctorGroupStatisticDao groupStatisticDao, DoctorGroupDailyDao groupDailyDao) {
        this.groupStatisticDao = groupStatisticDao;
        this.groupDailyDao = groupDailyDao;
    }

    public void flushGroupDaily(DoctorStatisticCriteria criteria){
        DoctorGroupDaily doctorGroupDaily = groupDailyDao.findBy(criteria.getFarmId(), criteria.getPigType(), criteria.getSumAt());
        doctorGroupDaily.setStart(groupStatisticDao.realTimeLiveStockGroup(criteria.getFarmId(), criteria.getPigType(), criteria.getStartAt()));
        doctorGroupDaily.setTurnInto(groupStatisticDao.turnInto(criteria));
        doctorGroupDaily.setChgFarmIn(groupStatisticDao.chgFarmIn(criteria));
        doctorGroupDaily.setTurnIntoWeight(groupStatisticDao.turnIntoWeight(criteria));
        doctorGroupDaily.setChgFarm(groupStatisticDao.chgFarm(criteria));
        doctorGroupDaily.setChgFarmWeight(groupStatisticDao.chgFarmWeight(criteria));
        doctorGroupDaily.setSale(groupStatisticDao.sale(criteria));
        doctorGroupDaily.setSaleWeight(groupStatisticDao.saleWeight(criteria));
        doctorGroupDaily.setDead(groupStatisticDao.dead(criteria));
        doctorGroupDaily.setWeedOut(groupStatisticDao.weedOut(criteria));
        doctorGroupDaily.setOtherChange(groupStatisticDao.otherChange(criteria));
        doctorGroupDaily.setTurnOutWeight(groupStatisticDao.turnOutWeight(criteria));
        doctorGroupDaily.setEnd(groupStatisticDao.realTimeLiveStockGroup(criteria.getFarmId(), criteria.getPigType(), criteria.getEndAt()));

        PigType pigType = expectNotNull(PigType.from(criteria.getPigType()), "pigType.is.illegal");
        switch (pigType) {
            case DELIVER_SOW: flushDeliverDaily(doctorGroupDaily, criteria); break;
            case NURSERY_PIGLET: flushNurseryDaily(doctorGroupDaily, criteria); break;
            case FATTEN_PIG: flushFattenDaily(doctorGroupDaily, criteria); break;
            case RESERVE: flushReserveDaily(doctorGroupDaily, criteria); break;
        }
        groupDailyDao.update(doctorGroupDaily);
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

    public void flushPigDaily(DoctorStatisticCriteria criteria) {
        // TODO: 17/12/12
    }
}
