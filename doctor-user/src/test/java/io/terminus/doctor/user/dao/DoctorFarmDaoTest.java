package io.terminus.doctor.user.dao;

import io.terminus.doctor.user.model.DoctorFarm;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class DoctorFarmDaoTest extends BaseDaoTest {
    @Autowired
    private DoctorFarmDao doctorFarmDao;

    @Test
    public void testFindByOrgId(){
        List<DoctorFarm> farm = doctorFarmDao.findByOrgId(1L);
        Assert.assertNotNull(farm);
        Assert.assertEquals(farm.size(), 4);
    }

    @Test
    public void testFindById(){
        DoctorFarm farm = doctorFarmDao.findById(1L);
        Assert.assertNotNull(farm);
        Assert.assertEquals(farm.getId().longValue(), 1L);
    }

    @Test
    public void testFindAll(){
        List<DoctorFarm> farm = doctorFarmDao.findAll();
        Assert.assertNotNull(farm);
        Assert.assertEquals(farm.size(), 4);
    }

    @Test
    public void testCreate(){
        DoctorFarm farm = new DoctorFarm();
        farm.setOrgId(111L);
        farm.setOrgName("111");
        Boolean res = doctorFarmDao.create(farm);
        Assert.assertTrue(res);
        Assert.assertEquals(5, farm.getId().longValue());
    }

    @Test
    public void testDelete(){
        Boolean res = doctorFarmDao.delete(1L);
        Assert.assertTrue(res);
        List<DoctorFarm> farm = doctorFarmDao.findAll();
        Assert.assertNotNull(farm);
        Assert.assertEquals(farm.size(), 3);
    }

    @Test
    public void testUpdate(){
        DoctorFarm farm = doctorFarmDao.findById(1L);
        farm.setName("test");
        Boolean res = doctorFarmDao.update(farm);
        Assert.assertTrue(res);
    }
}
