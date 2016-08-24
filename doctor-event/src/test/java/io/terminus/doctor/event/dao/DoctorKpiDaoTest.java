package io.terminus.doctor.event.dao;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 12:17 16/8/11
 */
public class DoctorKpiDaoTest extends BaseDaoTest{
    @Autowired
    private DoctorKpiDao doctorKpiDao;

    @Test
    public void testGetPreDelivery(){
        int result = doctorKpiDao.getPreDelivery(12355L, DateTime.now().minusDays(100).toDate(), DateTime.now().toDate());
        Assert.assertEquals(1, result);
    }

    @Test
    public void testGetDelivery(){
        int result = doctorKpiDao.getDelivery(12355L, DateTime.now().minusDays(300).toDate(), DateTime.now().toDate());
        Assert.assertEquals(1, result);
    }

    @Test
    public void testGetDeliveryLive(){
        int result = doctorKpiDao.getDeliveryLive(12355L, DateTime.now().minusDays(300).toDate(), DateTime.now().toDate());
        Assert.assertEquals(13, result);
    }

    @Test
    public void testGetDeliveryHealth(){
        int result = doctorKpiDao.getDeliveryHealth(12355L, DateTime.now().minusDays(300).toDate(), DateTime.now().toDate());
        Assert.assertEquals(12, result);
    }

    @Test
    public void testGetDeliveryDead(){
        int result = doctorKpiDao.getDeliveryDead(12355L, DateTime.now().minusDays(300).toDate(), DateTime.now().toDate());
        Assert.assertEquals(4, result);
    }

    @Test
    public void testGetDeliveryMny(){
        int result = doctorKpiDao.getDeliveryMny(12355L, DateTime.now().minusDays(300).toDate(), DateTime.now().toDate());
        Assert.assertEquals(2, result);
    }

    @Test
    public void testGetDeliveryAll(){
        int result = doctorKpiDao.getDeliveryAll(12355L, DateTime.now().minusDays(300).toDate(), DateTime.now().toDate());
        Assert.assertEquals(27, result);
    }

    @Test
    public void testGetDeliveryHealthAvg(){
        double result = doctorKpiDao.getDeliveryHealthAvg(12355L, DateTime.now().minusDays(300).toDate(), DateTime.now().toDate());
        Assert.assertEquals(12.0, result, 0.01);
    }

    @Test
    public void testGetDeliveryAllAvg(){
        double result = doctorKpiDao.getDeliveryAllAvg(12355L, DateTime.now().minusDays(300).toDate(), DateTime.now().toDate());
        Assert.assertEquals(27.0, result, 0.01);
    }

    @Test
    public void testGetDeliveryLiveAvg(){
        double result = doctorKpiDao.getDeliveryLiveAvg(12355L, DateTime.now().minusDays(300).toDate(), DateTime.now().toDate());
        Assert.assertEquals(13.0, result, 0.01);
    }

    @Test
    public void testGetWeanSow(){
        int result = doctorKpiDao.getWeanSow(12355L, DateTime.now().minusDays(300).toDate(), DateTime.now().toDate());
        Assert.assertEquals(1, result);
    }

    @Test
    public void testGetWeanPiglet(){
        int result = doctorKpiDao.getWeanPiglet(12355L, DateTime.now().minusDays(300).toDate(), DateTime.now().toDate());
        Assert.assertEquals(13, result);
    }

    @Test
    public void testGetWeanPigletWeightAvg(){
        double result = doctorKpiDao.getWeanPigletWeightAvg(12355L, DateTime.now().minusDays(300).toDate(), DateTime.now().toDate());
        Assert.assertEquals(6.0, result, 0.01);
    }

    @Test
    public void testGetWeanPigletCountsAvg(){
        double result = doctorKpiDao.getWeanPigletCountsAvg(12355L, DateTime.now().minusDays(300).toDate(), DateTime.now().toDate());
        Assert.assertEquals(13.0, result, 0.01);
    }

    @Test
    public void testGetFarrowWeightAvg(){
        double result = doctorKpiDao.getFarrowWeightAvg(1L, DateTime.now().minusDays(300).toDate(), DateTime.now().toDate());
        Assert.assertEquals(1.519, result, 0.001);
    }
}
