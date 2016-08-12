package io.terminus.doctor.event.dao;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 12:17 16/8/11
 */
public class DoctorKpiDaoTest extends BaseDaoTest{
    @Autowired
    private DoctorKpiDao doctorKpiDao;

    @Test
    public void testGetPreDeliveryCounts(){
        int result = doctorKpiDao.getPreDelivery(12355L, DateTime.now().minusDays(100).toDate(), DateTime.now().toDate());
        Assert.assertNotNull(result);


    }

    @Test
    public void testGetDeliveryCounts(){
        int result = doctorKpiDao.getDelivery(12355L, DateTime.now().minusDays(300).toDate(), DateTime.now().toDate());
        Assert.assertNotNull(result);
    }
}
