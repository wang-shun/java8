package io.terminus.doctor.event.dao;

import io.terminus.doctor.event.dao.reportBi.DoctorReportEfficiencyDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

/**
 * Created by sunbo@terminus.io on 2018/1/22.
 */
public class DoctorReportEfficiencyDaoTest extends BaseDaoTest {
    @Autowired
    private DoctorReportEfficiencyDao doctorReportEfficiencyDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testDeleteBy() {
        DoctorDimensionCriteria criteria = new DoctorDimensionCriteria();

        criteria.setDateType(DateDimension.DAY.getValue());
        criteria.setOrzType(OrzDimension.ORG.getValue());

        doctorReportEfficiencyDao.delete(criteria);

        Assert.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "doctor_report_efficiencies"));
    }
}
