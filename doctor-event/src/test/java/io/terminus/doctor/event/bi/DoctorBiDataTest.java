package io.terminus.doctor.event.bi;

import com.google.common.collect.Lists;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.BaseDaoTest;
import io.terminus.doctor.event.dao.DoctorGroupDailyDao;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.dao.DoctorPigStatisticDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportSowDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.DoctorStatisticCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorPigDailyExtend;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.model.DoctorReportSow;
import io.terminus.doctor.event.reportBi.DoctorReportBiDataSynchronize;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestExecutionListeners;

import java.util.List;

/**
 * Created by xjn on 18/1/12.
 * email:xiaojiannan@terminus.io
 */
public class DoctorBiDataTest extends BaseDaoTest {
//    @Autowired
    private DoctorReportBiDataSynchronize synchronizer;
    @Autowired
    private DoctorGroupDailyDao doctorGroupDailyDao;
    @Autowired
    private DoctorPigDailyDao pigDailyDao;
    @Autowired
    private DoctorReportSowDao doctorReportSowDao;
    @Autowired
    private DoctorPigStatisticDao doctorPigStatisticDao;
    @Autowired
    private DoctorKpiDao doctorKpiDao;

    @Test
    public void fullSynchronizeTest() {
        synchronizer.synchronizeFullBiData();
    }

    @Test
    public void synchronizeRealTimeBiDataTest() {
        synchronizer.cleanFullBiData();
//        synchronizer.synchronizeRealTimeBiData(404L);
    }

    @Test
    public void findByDateTypeTest() {
//        System.out.println(doctorGroupDailyDao.findByDateType(null, DateUtil.toDate("2017-01-01"), DateDimension.WEEK.getValue(), OrzDimension.FARM.getValue()));
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


    @Test
    public void dimensionReport() {
        DoctorDimensionCriteria doctorDimensionCriteria = new DoctorDimensionCriteria();
        doctorDimensionCriteria.setOrzId(1L);
        doctorDimensionCriteria.setOrzType(3);
        doctorDimensionCriteria.setDateType(1);

    }

    @Test
    public void startTest() {
        DoctorDimensionCriteria doctorDimensionCriteria = new DoctorDimensionCriteria();
        doctorDimensionCriteria.setOrzId(188L);
        doctorDimensionCriteria.setOrzType(2);
        doctorDimensionCriteria.setDateType(3);
        doctorDimensionCriteria.setSumAt(DateUtil.toDate("2018-01-01"));
        DoctorPigDailyExtend sow = pigDailyDao.orgStart(1L, DateUtil.toDate("2017-01-01"));
        System.out.println(sow);
    }

    @Test
    public void orgDayAvgLiveStockTest() {
        DoctorDimensionCriteria doctorDimensionCriteria = new DoctorDimensionCriteria();
        doctorDimensionCriteria.setOrzId(188L);
        doctorDimensionCriteria.setOrzType(2);
        doctorDimensionCriteria.setDateType(3);
        doctorDimensionCriteria.setPigType(4);
        doctorDimensionCriteria.setSumAt(DateUtil.toDate("2018-01-01"));
        Integer sow = doctorGroupDailyDao.orgDayAvgLiveStock(doctorDimensionCriteria);
        System.out.println(sow);
    }

    @Test
    public void orgPidDay() {
        DoctorDimensionCriteria doctorDimensionCriteria = new DoctorDimensionCriteria();
        doctorDimensionCriteria.setOrzId(188L);
        doctorDimensionCriteria.setOrzType(2);
        doctorDimensionCriteria.setDateType(3);
        doctorDimensionCriteria.setPigType(4);
        doctorDimensionCriteria.setSumAt(DateUtil.toDate("2018-01-01"));
        DoctorPigDailyExtend dayAvgLiveStock = pigDailyDao.orgSumDimension(doctorDimensionCriteria);
        int count = pigDailyDao.countDimension(doctorDimensionCriteria);
        System.out.println(dayAvgLiveStock.getBoarDailyPigCount()/count);
        System.out.println(dayAvgLiveStock.getSowDailyPigCount()/count);
    }

    @Test
    public void earlyMatingCount() {
        DoctorStatisticCriteria criteria = new DoctorStatisticCriteria(404L, null);
        criteria.setStartAt("2017-09-01");
        criteria.setEndAt("2017-09-30");
        int early = doctorPigStatisticDao.earlyMating(criteria);
        System.out.println(early);
    }

    @Test
    public void boarLiveStock() {
        DoctorStatisticCriteria criteria = new DoctorStatisticCriteria(404L, null);
        criteria.setStartAt("2017-09-01");
        criteria.setEndAt("2017-09-30");
        int early = doctorPigStatisticDao.boarLiveStock(404L, "2018-01-23");
        System.out.println(doctorKpiDao.realTimeLiveStockBoar(404L, DateUtil.toDate("2018-01-23")));
        System.out.println(early);
    }

    @Test
    public void minDate(){
        System.out.println(doctorGroupDailyDao.minDate(new DoctorDimensionCriteria(1L, 2, DateUtil.toDate("2017-01-01"), 4, 2)));
    }

    @Test
    public void maxDate(){
        System.out.println(doctorGroupDailyDao.maxDate(new DoctorDimensionCriteria(1L, 2, DateUtil.toDate("2017-01-01"), 4, 2)));
    }

    @Test
    public void pigMinDate(){
        System.out.println(pigDailyDao.minDate(new DoctorDimensionCriteria(1L, 2, DateUtil.toDate("2017-01-01"), 4, 2)));
    }

    @Test
    public void pigMaxDate(){
        System.out.println(pigDailyDao.maxDate(new DoctorDimensionCriteria(1L, 2, DateUtil.toDate("2017-01-01"), 4, 2)));
    }
}
