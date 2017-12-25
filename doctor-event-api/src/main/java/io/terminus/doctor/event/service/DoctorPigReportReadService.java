package io.terminus.doctor.event.service;

import io.terminus.doctor.event.dto.DoctorPigReport;
import io.terminus.doctor.event.enums.ReportTime;
import lombok.Getter;

import java.util.Date;
import java.util.List;

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
    DoctorPigReport farmReport(Long farmId, Date start, ReportTime reportTime);

    /**
     * 公司维度猪报表
     *
     * @param farmIds    公司下的猪场ID列表
     * @param reportTime 时间维度
     */
    DoctorPigReport companyReport(List<Long> farmIds, Date start, ReportTime reportTime);


    /**
     * 集团维度猪表报
     *
     * @param farmIds    集团下每个公司下的所有猪场ID列表
     * @param reportTime 时间维度
     */
    DoctorPigReport orgReport(List<Long> farmIds, Date start, ReportTime reportTime);


    /**
     * 根据类型获取时间间隔
     *
     * @param countDate
     * @param reportTime
     * @return
     */
    DateDuration getDuration(Date countDate, ReportTime reportTime);


    @Getter
    public class DateDuration {
        public DateDuration(Date start, Date end) {
            this.start = start;
            this.end = end;
        }

        private Date start;
        private Date end;
    }

}
