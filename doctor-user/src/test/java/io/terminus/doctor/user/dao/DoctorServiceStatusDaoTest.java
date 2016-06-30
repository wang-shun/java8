package io.terminus.doctor.user.dao;

import io.terminus.doctor.user.model.DoctorServiceStatus;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DoctorServiceStatusDaoTest extends BaseDaoTest{
    @Autowired
    private DoctorServiceStatusDao doctorServiceStatusDao;

    @Test
    public void testFindById(){
        DoctorServiceStatus status = doctorServiceStatusDao.findById(1L);
        Assert.assertNotNull(status);
        Assert.assertEquals(status.getId().longValue(), 1L);
    }

    @Test
    public void testFindByUserId(){
        DoctorServiceStatus status = doctorServiceStatusDao.findByUserId(1L);
        Assert.assertNotNull(status);
        Assert.assertEquals(status.getUserId().longValue(), 1L);
    }

    @Test
    public void testCreate(){
        DoctorServiceStatus status = new DoctorServiceStatus();
        status.setPigdoctorReviewStatus(0);
        status.setPigdoctorReason("reason");
        status.setPigdoctorStatus(0);
        Boolean res = doctorServiceStatusDao.create(status);
        Assert.assertTrue(res);
    }

    @Test
    public void testDelete(){
        Boolean res = doctorServiceStatusDao.delete(1L);
        Assert.assertTrue(res);
    }

    @Test
    public void testUpdate(){
        DoctorServiceStatus status = doctorServiceStatusDao.findById(1L);
        status.setPigdoctorReason("test");
        Boolean res = doctorServiceStatusDao.update(status);
        Assert.assertTrue(res);
    }

    @Test
    public void testUpdateWithNull(){
        DoctorServiceStatus status = doctorServiceStatusDao.findById(1L);
        status.setPigdoctorReason("test");
        status.setNeverestReason(null);
        Boolean res = doctorServiceStatusDao.updateWithNull(status);
        Assert.assertTrue(res);
    }
}
