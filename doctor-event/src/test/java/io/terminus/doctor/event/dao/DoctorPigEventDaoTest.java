package io.terminus.doctor.event.dao;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2018/2/13.
 */
public class DoctorPigEventDaoTest extends BaseDaoTest {


    @Autowired
    private DoctorPigEventDao doctorPigEventDao;

    @Test
    public void countFarrowingEventTest() {

        List<DoctorPigEvent> events = doctorPigEventDao.queryAllEventsByPigId(395635L);

        Long count = events.parallelStream().
                filter(e -> e.getType().equals(PigEvent.FARROWING.getKey()))
                .filter(e -> e.getEventAt().compareTo(DateUtil.toDate("2017-09-16")) <= 0)
                .count();

        Assert.assertEquals(1L, count.longValue());
    }


    @Test
    public void countNoFarrowingEventTest() {
        List<DoctorPigEvent> events = doctorPigEventDao.queryAllEventsByPigId(395636L);

        Long count = events.parallelStream().
                filter(e -> e.getType().equals(PigEvent.FARROWING.getKey()) && e.getEventAt().compareTo(DateUtil.toDate("2017-09-16")) <= 0)
                .count();

        Assert.assertEquals(0L, count.longValue());
    }


    @Test
    public void testGetFarrowEventByParity() {
        DoctorPigEvent event = doctorPigEventDao.getFarrowEventByParity(395635L, 1);

        Assert.assertEquals(4973190L, event.getId().longValue());
    }

    @Test
    public void testGetLastStatusEventBeforeEventAt() {
        DoctorPigEvent event = doctorPigEventDao.getLastStatusEventBeforeEventAt(453480L, new Date());
        Assert.assertEquals(event.getId().longValue(), 5402988L);
    }

}
