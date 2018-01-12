package io.terminus.doctor.event.dao;

import com.google.common.collect.Lists;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.model.DoctorPigDaily;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2017/12/15.
 */
public class DoctorPigDailyDaoTest extends BaseDaoTest {

    @Autowired
    private DoctorPigDailyDao doctorPigDailyDao;

    @Test
    public void testReportDailyByFarm() throws Exception {
        Date end;
        Date start = end = DateUtils.parseDate("2017-12-01", "yyyy-MM-dd");
        DoctorPigDaily pigDaily = doctorPigDailyDao.countByFarm(1L, start, end);

        Assert.assertEquals(2, pigDaily.getSowPhStart().intValue());
        Assert.assertEquals(4, pigDaily.getSowPhReserveIn().intValue());

    }

    @Test
    public void testReportDailyByOrg() {

        Date end;
        Date start = end = DateUtil.toDate("2017-12-01");

        DoctorPigDaily pigDaily = doctorPigDailyDao.countByOrg(Lists.newArrayList(1L, 2L, 3L),
                start,
                end);

        Assert.assertEquals(2, pigDaily.getSowPhStart().intValue());
        Assert.assertEquals(12, pigDaily.getSowPhReserveIn().intValue());
        Assert.assertEquals(1, pigDaily.getBoarStart().intValue());
        Assert.assertEquals(4, pigDaily.getSowPhEnd().intValue());
    }


    @Test
    public void testReportMonthlyByFarm() {
        Date start = DateUtil.toYYYYMM("2017-11");
        Date end = DateUtil.monthEnd(start);

        DoctorPigDaily pigDaily = doctorPigDailyDao.countByFarm(1L, start, end);
        Assert.assertEquals(12, pigDaily.getSowPhReserveIn().intValue());
    }
}
