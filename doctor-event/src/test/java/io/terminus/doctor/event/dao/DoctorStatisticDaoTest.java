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
 }
