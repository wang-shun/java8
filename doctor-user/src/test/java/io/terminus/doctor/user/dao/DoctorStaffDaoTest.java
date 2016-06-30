package io.terminus.doctor.user.dao;

import io.terminus.doctor.user.model.DoctorStaff;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class DoctorStaffDaoTest extends BaseDaoTest{
    @Autowired
    private DoctorStaffDao doctorStaffDao;

    @Test
    public void testFindById(){
        DoctorStaff staff = doctorStaffDao.findById(1L);
        Assert.assertNotNull(staff);
        Assert.assertEquals(staff.getId().longValue(), 1L);
    }

    @Test
    public void testFindByUserId(){
        DoctorStaff staff = doctorStaffDao.findByUserId(1L);
        Assert.assertNotNull(staff);
        Assert.assertEquals(staff.getUserId().longValue(), 1L);
    }

    @Test
    public void testFindByOrgId(){
        List<DoctorStaff> staff = doctorStaffDao.findByOrgId(1L);
        Assert.assertNotNull(staff);
        Assert.assertTrue(staff.size() > 0);
    }

    @Test
    public void testCreate(){
        DoctorStaff staff = new DoctorStaff();
        staff.setUserId(111L);
        staff.setOrgId(1L);
        staff.setOrgName("orgName");
        Boolean res = doctorStaffDao.create(staff);
        Assert.assertTrue(res);
    }

    @Test
    public void testDelete(){
        Boolean res = doctorStaffDao.delete(1L);
        Assert.assertTrue(res);
    }

    @Test
    public void testUpdate(){
        DoctorStaff staff = doctorStaffDao.findById(1L);
        staff.setAvatar("http://e.e.e");
        Boolean res = doctorStaffDao.update(staff);
        Assert.assertTrue(res);
    }
}
