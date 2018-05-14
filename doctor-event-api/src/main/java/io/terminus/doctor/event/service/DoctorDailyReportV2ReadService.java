package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.model.DoctorReportSow;

import java.util.List;

/**
 * @author xjn
 * email xiaojiannan@terminus.io
 * @date 18/5/14
 */
public interface DoctorDailyReportV2ReadService {

    /**
     * 母猪区报表
     * @param dimensionCriteria 查询条件
     * @return 报表数据
     */
    Response<List<DoctorReportSow>> sowReport(DoctorDimensionCriteria dimensionCriteria);
}
