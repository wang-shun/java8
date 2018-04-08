package io.terminus.doctor.event.dao;

import io.terminus.doctor.common.utils.DateUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xjn on 18/4/8.
 * email:xiaojiannan@terminus.io
 */
public class DoctorGroupDailyDaoTest extends BaseDaoTest {
    @Autowired
    private DoctorGroupDailyDao doctorGroupDailyDao;

    @Test
    public void orgDayStockTest() {
        Integer count = doctorGroupDailyDao.orgDayStock(1L, DateUtil.toDate("2018-03-01"), 4);
        System.out.println(count);
    }
}
