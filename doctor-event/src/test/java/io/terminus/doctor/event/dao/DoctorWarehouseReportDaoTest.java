package io.terminus.doctor.event.dao;

import io.terminus.doctor.common.utils.DateUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2018/1/15.
 */
public class DoctorWarehouseReportDaoTest extends BaseDaoTest {


    @Autowired
    private DoctorWarehouseReportDao doctorWarehouseReportDao;


    @Test
    public void testCount() {

        Date start = DateUtil.toDate("2017-11-01");
        Date end = DateUtil.toDate("2017-11-01");

        doctorWarehouseReportDao.count(Collections.singletonList(1L), start, end);
    }


}
