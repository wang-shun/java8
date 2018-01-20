package io.terminus.doctor.event.dao;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
