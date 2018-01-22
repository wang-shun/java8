package io.terminus.doctor.event.dao;

import io.terminus.doctor.event.dto.DoctorStatisticCriteria;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xjn on 17/12/19.
 * email:xiaojiannan@terminus.io
 */
public class DoctorStatisticDaoTest extends BaseDaoTest {
    @Autowired
    private DoctorGroupStatisticDao doctorGroupStatisticDao;
    @Autowired
    private DoctorPigStatisticDao doctorPigStatisticDao;

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


}
