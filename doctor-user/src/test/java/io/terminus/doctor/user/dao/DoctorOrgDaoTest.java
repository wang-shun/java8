package io.terminus.doctor.user.dao;

import io.terminus.doctor.user.model.DoctorOrg;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class DoctorOrgDaoTest extends BaseDaoTest {
    @Autowired
    private DoctorOrgDao doctorOrgDao;

    @Test
    public void testFindById(){
        DoctorOrg org = doctorOrgDao.findById(1L);
        Assert.assertNotNull(org);
        Assert.assertEquals(org.getId().longValue(), 1L);
    }

    @Test
    public void testCreate(){
        DoctorOrg org = new DoctorOrg();
        org.setMobile("mobile");
        org.setName("name");
        org.setLicense("http://t.t.t.com");
        Boolean res = doctorOrgDao.create(org);
        Assert.assertTrue(res);
        Assert.assertEquals(3, org.getId().longValue());
    }

    @Test
    public void testDelete(){
        Boolean res = doctorOrgDao.delete(1L);
        Assert.assertTrue(res);
    }

    @Test
    public void testUpdate(){
        DoctorOrg org = doctorOrgDao.findById(1L);
        org.setName("test");
        Boolean res = doctorOrgDao.update(org);
        Assert.assertTrue(res);
    }
}
