package io.terminus.doctor.event.dao;

import io.terminus.doctor.event.dao.reportBi.DoctorReportMaterialDao;
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
public class DoctorReportMaterialDaoTest extends BaseDaoTest {

    @Autowired
    private DoctorReportMaterialDao doctorReportMaterialDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testDeleteBy() {
        DoctorDimensionCriteria criteria = new DoctorDimensionCriteria();

        criteria.setOrzType(OrzDimension.FARM.getValue());
        criteria.setDateType(DateDimension.MONTH.getValue());

        doctorReportMaterialDao.delete(criteria);

        Assert.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "doctor_report_materials"));
    }

}
