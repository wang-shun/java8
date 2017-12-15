package io.terminus.doctor.event.dao;

import io.terminus.doctor.event.model.DoctorPigDaily;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.text.EditorKit;
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
        Date start = end = DateUtils.parseDate("2017-11-12", "yyyy-MM-dd");
        DoctorPigDaily pigDaily = doctorPigDailyDao.countByFarm(1L, start, end);


    }
}
