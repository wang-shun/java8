package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.report.common.DoctorCommonReportTrendDto;
import io.terminus.doctor.event.test.BaseServiceTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertTrue;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/11/21
 */
@Slf4j
public class DoctorWeeklyReportServiceTest extends BaseServiceTest {

    @Autowired
    private DoctorCommonReportReadService doctorCommonReportReadService;

    @Test
    public void testFindWeeklyReportTrendByFarmIdAndSumAt() {
        Response<DoctorCommonReportTrendDto> response = doctorCommonReportReadService
                .findWeeklyReportTrendByFarmIdAndSumAt(1L, null, 10, null);
        assertTrue(response.isSuccess());
        log.info("weekly report:{}", response.getResult());
    }
}
