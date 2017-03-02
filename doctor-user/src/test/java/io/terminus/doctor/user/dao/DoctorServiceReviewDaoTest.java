package io.terminus.doctor.user.dao;

import io.terminus.doctor.user.model.DoctorServiceReview;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class DoctorServiceReviewDaoTest extends BaseDaoTest {
    @Autowired
    private DoctorServiceReviewDao doctorServiceReviewDao;

    @Test
    public void testfindById(){
        DoctorServiceReview review = doctorServiceReviewDao.findById(1L);
        Assert.assertNotNull(review);
        Assert.assertEquals(review.getId().longValue(), 1L);
    }

    @Test
    public void testfindByUserId(){
        List<DoctorServiceReview> list = doctorServiceReviewDao.findByUserId(1L);
        Assert.assertNotNull(list);
        Assert.assertEquals(list.size(), 4L);
    }

    @Test
    public void testFindByUserIdAndType(){
        DoctorServiceReview review = doctorServiceReviewDao.findByUserIdAndType(1L, DoctorServiceReview.Type.PIG_DOCTOR);
        Assert.assertNotNull(review);
    }

    @Test
    public void testInitData(){
        boolean b = doctorServiceReviewDao.initData(11L, "11111111", "aaaa");
        Assert.assertTrue(b);
    }

    @Test
    public void testUpdateStatus1(){
        boolean b = doctorServiceReviewDao.updateStatus(1L, DoctorServiceReview.Type.PIG_DOCTOR,  DoctorServiceReview.Status.OK);
        Assert.assertTrue(b);
    }

    @Test
    public void testUpdateStatus2(){
        boolean b = doctorServiceReviewDao.updateStatus(1L, 11L, DoctorServiceReview.Type.PIG_DOCTOR,  DoctorServiceReview.Status.OK);
        Assert.assertTrue(b);
    }

    @Test
    public void testUpdate(){
        DoctorServiceReview review = doctorServiceReviewDao.findByUserIdAndType(1L, DoctorServiceReview.Type.PIG_DOCTOR);
        review.setStatus(DoctorServiceReview.Status.OK.getValue());
        boolean b = doctorServiceReviewDao.update(review);
        Assert.assertTrue(b);
    }

    @Test
    public void testCreate(){
        DoctorServiceReview review = new DoctorServiceReview();
        review.setUserId(111L);
        review.setType(DoctorServiceReview.Type.PIG_DOCTOR.getValue());
        review.setStatus(DoctorServiceReview.Status.OK.getValue());
        boolean b = doctorServiceReviewDao.create(review);
        Assert.assertTrue(b);
    }

    @Test
    public void testDelete(){
        boolean b = doctorServiceReviewDao.delete(1L);
        Assert.assertTrue(b);
    }
}
