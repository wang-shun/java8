package io.terminus.doctor.user.dao;

import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.model.ServiceReviewTrack;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ServiceReviewTrackDaoTest extends BaseDaoTest{
    @Autowired
    private ServiceReviewTrackDao serviceReviewTrackDao;

    @Test
    public void testFindById(){
        ServiceReviewTrack track = serviceReviewTrackDao.findById(1L);
        Assert.assertNotNull(track);
        Assert.assertEquals(track.getId().longValue(), 1L);
    }

    @Test
    public void testFindByUserId(){
        List<ServiceReviewTrack> track = serviceReviewTrackDao.findByUserId(1L);
        Assert.assertNotNull(track);
        Assert.assertEquals(track.size(), 3L);
    }

    @Test
    public void testCreate(){
        ServiceReviewTrack track = new ServiceReviewTrack();
        track.setUserId(111L);
        track.setReason("reason");
        track.setType(1);
        track.setNewStatus(1);
        track.setOldStatus(0);
        Boolean res = serviceReviewTrackDao.create(track);
        Assert.assertTrue(res);
    }

    @Test
    public void testDelete(){
        Boolean res = serviceReviewTrackDao.delete(1L);
        Assert.assertTrue(res);
    }

    @Test
    public void testUpdate(){
        ServiceReviewTrack track = serviceReviewTrackDao.findById(1L);
        track.setReason("reason");
        Boolean res = serviceReviewTrackDao.update(track);
        Assert.assertTrue(res);
    }

    @Test
    public void findByUserIdAndType(){
        List<ServiceReviewTrack> list = serviceReviewTrackDao.findByUserIdAndType(1L, DoctorServiceReview.Type.PIG_DOCTOR);
        Assert.assertNotNull(list);
    }

    @Test
    public void findLastByUserIdAndType(){
        ServiceReviewTrack track = serviceReviewTrackDao.findLastByUserIdAndType(1L, DoctorServiceReview.Type.PIG_DOCTOR);
        Assert.assertNotNull(track);
    }
}
