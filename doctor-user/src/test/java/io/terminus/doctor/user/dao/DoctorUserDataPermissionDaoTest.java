package io.terminus.doctor.user.dao;

import io.terminus.doctor.user.model.DoctorUserDataPermission;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DoctorUserDataPermissionDaoTest extends BaseDaoTest{
    @Autowired
    private DoctorUserDataPermissionDao doctorUserDataPermissionDao;

    @Test
    public void testFindById(){
        DoctorUserDataPermission permission = doctorUserDataPermissionDao.findById(1L);
        Assert.assertNotNull(permission);
        Assert.assertEquals(permission.getId().longValue(), 1L);
    }

    @Test
    public void testFindByUserId(){
        DoctorUserDataPermission permission = doctorUserDataPermissionDao.findByUserId(1L);
        Assert.assertNotNull(permission);
        Assert.assertEquals(permission.getUserId().longValue(), 1L);
    }

    @Test
    public void testCreate(){
        DoctorUserDataPermission permission = new DoctorUserDataPermission();
        permission.setUserId(111L);
        permission.setFarmIds("2,3,4");
        permission.setBarnIds("1,23");
        Boolean res = doctorUserDataPermissionDao.create(permission);
        Assert.assertTrue(res);
    }

    @Test
    public void testDelete(){
        Boolean res = doctorUserDataPermissionDao.delete(1L);
        Assert.assertTrue(res);
    }

    @Test
    public void testUpdate(){
        DoctorUserDataPermission permission = doctorUserDataPermissionDao.findById(1L);
        permission.setBarnIds("2,3");
        Boolean res = doctorUserDataPermissionDao.update(permission);
        Assert.assertTrue(res);
    }
}
