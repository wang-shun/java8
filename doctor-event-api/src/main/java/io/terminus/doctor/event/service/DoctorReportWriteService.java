package io.terminus.doctor.event.service;

import io.terminus.doctor.event.enums.ReportTime;

import java.util.Date;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/12/21.
 */
public interface DoctorReportWriteService {


    /**
     * 生成非生产天数
     *
     * @param farmIds
     * @param countDate
     * @param reportTime
     */
    void flushNPD(List<Long> farmIds, Date countDate, ReportTime reportTime);
}
