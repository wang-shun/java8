package io.terminus.doctor.event.bi;

import com.google.common.collect.Lists;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorGroupDailyDao;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportSowDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.model.DoctorReportSow;
import io.terminus.doctor.event.reportBi.DoctorReportBiDataSynchronize;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by xjn on 18/1/12.
 * email:xiaojiannan@terminus.io
 */
public class DoctorBiDataTest extends BaseServiceTest {
    @Autowired
    private DoctorReportBiDataSynchronize synchronizer;
    @Autowired
    private DoctorGroupDailyDao doctorGroupDailyDao;
    @Autowired
    private DoctorPigDailyDao pigDailyDao;
    @Autowired
    private DoctorReportSowDao doctorReportSowDao;

    @Test
    public void fullSynchronizeTest() {
        synchronizer.synchronizeFullBiData();
    }

    @Test
    public void synchronizeRealTimeBiDataTest() {
        synchronizer.cleanFullBiData();
        synchronizer.synchronizeRealTimeBiData();
    }

    @Test
    public void findByDateTypeTest() {
        System.out.println(doctorGroupDailyDao.findByDateType(DateUtil.toDate("2017-01-01"), DateDimension.WEEK.getValue(), OrzDimension.FARM.getValue()));
    }

    @Test
    public void sumForDimension() {
        DoctorDimensionCriteria doctorDimensionCriteria = new DoctorDimensionCriteria();
        doctorDimensionCriteria.setOrzId(1L);
        doctorDimensionCriteria.setOrzType(3);
        doctorDimensionCriteria.setDateType(2);
        pigDailyDao.sumForDimension(doctorDimensionCriteria);
    }

    @Test
    public void synchronizePigForDayTest() {
        List<DoctorPigDaily> doctorPigDailies = Lists.newArrayList(pigDailyDao.findBy(1L, "2017-02-01"));
        synchronizer.synchronizePigForDay(doctorPigDailies);
    }

    @Test
    public void findByDimensionTest() {
        DoctorDimensionCriteria doctorDimensionCriteria = new DoctorDimensionCriteria();
        doctorDimensionCriteria.setOrzId(1L);
        doctorDimensionCriteria.setOrzType(3);
        doctorDimensionCriteria.setDateType(1);
        doctorDimensionCriteria.setDateDimensionName("日");
        doctorDimensionCriteria.setOrzDimensionName("猪场");
        doctorDimensionCriteria.setSumAt(DateUtil.toDate("2018-01-15"));
        DoctorReportSow sow = doctorReportSowDao.findByDimension(doctorDimensionCriteria);
        Assert.assertNotNull(sow);
    }
}
