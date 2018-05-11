package io.terminus.doctor.event.helper;

import io.terminus.doctor.event.dao.BaseDaoTest;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xjn on 18/3/6.
 * email:xiaojiannan@terminus.io
 */
public class DoctorEventBaseHelperTest extends BaseDaoTest {
    @Autowired
    private DoctorEventBaseHelper doctorEventBaseHelper;
    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;
    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;

    @Test
    public void getCurrentStatus() {
        Integer status =  doctorEventBaseHelper.getCurrentStatus(705035L);
        Assert.assertEquals(PigStatus.Farrow.getKey(), status);
    }

    @Test
    public void getCurrentParity() {
        doctorPigEventDao.create(doctorPigEventDao.findEventById(5401872L));
        Integer status = doctorEventBaseHelper.getCurrentParity(489082L);
        Assert.assertEquals(3, status.longValue());
    }

    @Test
    public void validPigTrackTest() {
        DoctorPigTrack pigTrack = doctorPigTrackDao.findByPigId(498954L);
        doctorEventBaseHelper.validTrackAfterUpdate(pigTrack);
    }

    @Test
    public void validGroupTrackTest() {
        DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(25771L);
        doctorEventBaseHelper.validTrackAfterUpdate(groupTrack);
    }

    @Test
    public void isLastPigManualEventTest() {
        DoctorPigEvent pigEvent = doctorPigEventDao.findEventById(1203423L);
        Boolean b = doctorEventBaseHelper.isLastPigManualEvent(pigEvent);
        Assert.assertTrue(b);
    }
}
