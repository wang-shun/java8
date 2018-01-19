package io.terminus.doctor.event.dao;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.enums.DateDimension;
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


    @Test
    public void testCountByOrgWithMonth() {

        List<DoctorWarehouseReportDao.WarehouseReport> reports = doctorWarehouseReportDao.count(DateDimension.MONTH.getValue());

        reports.forEach(r -> System.out.println(r.getOrgName()));

        Assert.assertEquals(102, reports.size());

//        Assert.assertEquals(1L, reports.get(0).getFarrowFeedCount().longValue());
//        Assert.assertEquals(12L, reports.get(0).getHoubeiFeedCount().longValue());
    }

    @Test
    public void testGetMaxAndMinDate() {
        Map<String, Date> maxAndMin = doctorWarehouseReportDao.getMaxAndMinDate();
        System.out.println(maxAndMin);
//        System.out.println(maxAndMin.get("max"));
//        System.out.println(maxAndMin.get("min"));
//        Assert.assertTrue(DateUtils.isSameDay(DateUtil.toDate("2018-01-09"), maxAndMin.get("max")));
//        Assert.assertTrue(DateUtils.isSameDay(DateUtil.toDate("2017-11-28"), maxAndMin.get("min")));
    }


}
