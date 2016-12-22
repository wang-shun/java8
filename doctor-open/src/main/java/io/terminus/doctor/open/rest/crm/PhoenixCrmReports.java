package io.terminus.doctor.open.rest.crm;

import io.terminus.doctor.event.service.DoctorCommonReportReadService;
import io.terminus.doctor.event.service.DoctorDailyReportReadService;
import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Desc: CRM相关接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/12/22
 */
@Slf4j
@OpenBean
public class PhoenixCrmReports {

    private final DoctorDailyReportReadService doctorDailyReportReadService;
    private final DoctorCommonReportReadService doctorCommonReportReadService;

    @Autowired
    public PhoenixCrmReports(DoctorDailyReportReadService doctorDailyReportReadService,
                             DoctorCommonReportReadService doctorCommonReportReadService) {
        this.doctorDailyReportReadService = doctorDailyReportReadService;
        this.doctorCommonReportReadService = doctorCommonReportReadService;
    }



    /**
     * 根据日期查询全部猪场的日报
     *
     * @param date 日期
     * @return 日报数据json
     */
    @OpenMethod(key = "get.daily.report", paramNames = "date")
    public String getDailyReport(@NotEmpty(message = "date.not.empty") String date) {
        return null;
    }
}
