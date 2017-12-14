package io.terminus.doctor.event.service;

import io.terminus.doctor.event.dto.DoctorPigReport;

import java.util.Date;

/**
 * 猪报表
 * Created by sunbo@terminus.io on 2017/12/13.
 */
public interface DoctorPigReportReadService {


    /**
     * 猪场维度猪报表
     *
     * @param farmId     猪场
     * @param reportTime 时间维度
     */
    DoctorPigReport farmReport(Long farmId, Date start, Date end, ReportTime reportTime);

    /**
     * 公司维度猪报表
     *
     * @param orgId
     * @param reportTime
     */
    DoctorPigReport companyReport(Long orgId, Date start, Date end, ReportTime reportTime);


    /**
     * 集团维度猪表报
     *
     * @param orgId
     * @param reportTime
     */
    DoctorPigReport orgReport(Long orgId, Date start, Date end, ReportTime reportTime);


    public enum ReportTime {
        DAY, WEEK, MONTH, SEASON, YEAR
    }

}
