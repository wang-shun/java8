package io.terminus.doctor.event.dao;

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
 }
