package io.terminus.doctor.event.dao;

import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.DoctorStatisticCriteria;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by xjn on 17/12/19.
 * email:xiaojiannan@terminus.io
 */
public class DoctorStatisticDaoTest extends BaseDaoTest {
    @Autowired
    private DoctorGroupStatisticDao doctorGroupStatisticDao;
    @Autowired
    private DoctorPigStatisticDao doctorPigStatisticDao;
    @Autowired
    private DoctorWarehouseReportDao doctorWarehouseReportDao;
    @Autowired
    private DoctorKpiDao doctorKpiDao;

    @Test
    public void groupLiveStockTest() {
        Integer groupLiveStock = doctorGroupStatisticDao.groupLiveStock(3142L, "2017-01-01");
        Assert.assertNotEquals(groupLiveStock.longValue(), 0);
    }

    @Test
    public void turnActualCountTest() {
        DoctorStatisticCriteria criteria = new DoctorStatisticCriteria(404L, 2, "2018-01-22");

        Integer turnActualCount = doctorGroupStatisticDao.turnActualCount(criteria);
        System.out.println(turnActualCount);
    }


    @Test
    public void testSowEntryAndNoMating() {

        DoctorStatisticCriteria criteria = new DoctorStatisticCriteria();
        criteria.setFarmId(1L);
        criteria.setSumAt("2012-02-22");

        Assert.assertEquals(1, doctorPigStatisticDao.sowEntryAndNotMatingNum(criteria).intValue());
    }

    @Test
    public void testSowEntryAndMating() {
        DoctorStatisticCriteria criteria = new DoctorStatisticCriteria();
        criteria.setFarmId(2L);
        criteria.setSumAt("2012-02-22");

        Assert.assertEquals(0, doctorPigStatisticDao.sowEntryAndNotMatingNum(criteria).intValue());
    }

    @Test
    public void boarLiveStock() {
        DoctorStatisticCriteria criteria = new DoctorStatisticCriteria(404L, "2018-01-23");
        criteria.setStartAt("2017-09-01");
        criteria.setEndAt("2017-09-30");
        int early = doctorPigStatisticDao.boarOtherOut(criteria);
//        System.out.println(doctorKpiDao.realTimeLiveStockBoar(404L, DateUtil.toDate("2018-01-23")));
        System.out.println(early);
    }

    @Test
    public void materialApplyTest() {
        DoctorDimensionCriteria criteria = new DoctorDimensionCriteria();
        criteria.setOrzId(404L);
        criteria.setOrzType(3);
        criteria.setSumAt(DateUtil.toDate("2018-01-23"));
        criteria.setDateType(3);
        criteria.setPigType(3);
        System.out.println(doctorWarehouseReportDao.materialApply(criteria));
    }


    @Test
    public void netWeightGainTest() {
        DoctorStatisticCriteria criteria = new DoctorStatisticCriteria(404L, 3, "2018-01-23");
        System.out.println(doctorGroupStatisticDao.netWeightGain(criteria));
    }

    @Test
    public void phStockLive() {
        DoctorStatisticCriteria criteria = new DoctorStatisticCriteria(404L, "2018-01-23");
        String date = "2017-08-31";
        System.out.println(doctorPigStatisticDao.phLiveStock(404L, date));
        System.out.println(doctorPigStatisticDao.cfLiveStock(404L, date));
        System.out.println(doctorKpiDao.realTimeLiveStockSow(404L, DateUtil.toDate(date)));
        System.out.println(doctorKpiDao.realTimeLiveStockFarrowSow(404L, DateUtil.toDate(date)));
    }

    @Test
    public void weanMate() {
        DoctorStatisticCriteria criteria = new DoctorStatisticCriteria(404L, "2017-09-06");
        System.out.println(doctorPigStatisticDao.weanNest(criteria));
        System.out.println(doctorPigStatisticDao.weanMate(criteria));
        System.out.println(doctorPigStatisticDao.weanDeadWeedOut(criteria));
    }


    public void dateTest() {
        System.out.println(JsonMapper.nonEmptyMapper().fromJson(JsonMapper.nonEmptyMapper().toJson(DateUtil.toDate("2017-01-01")), Date.class));

    }

    @Test
    public void mateLeadToFarrowTest() {
        Integer count = doctorPigStatisticDao.mateLeadToFarrow(404L, "2018-04-19", "2018-04-20");
        Assert.assertEquals(count.longValue(), 1);
    }
}
