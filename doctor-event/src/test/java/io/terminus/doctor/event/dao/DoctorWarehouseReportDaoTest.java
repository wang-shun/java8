package io.terminus.doctor.event.dao;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.enums.DateDimension;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Date;
import java.util.List;

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


    @Test
    public void testCountByOrg() {

        Date start = DateUtil.toDate("2017-11-01");
        Date end = DateUtil.toDate("2017-11-02");
        List<DoctorWarehouseReportDao.WarehouseReport> reports = doctorWarehouseReportDao.count(DateDimension.DAY.getValue());
        Assert.assertEquals(1L, reports.get(0).getFarrowFeedCount().longValue());
        Assert.assertEquals(12L, reports.get(0).getHoubeiFeedCount().longValue());
    }

}
