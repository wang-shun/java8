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
     * 一个猪场一个月一条
     * 如果是季，刷出三条
     * 如果是年，刷十二条
     *
     * @param farmIds
     * @param countDate  范围之内某一个天
     * @param reportTime 月、季、年
     */
    void flushNPD(List<Long> farmIds, Date countDate, ReportTime reportTime);


    /**
     * 生成非生产天数
     *
     * @param farmIds
     * @param start
     */
    void flushNPD(List<Long> farmIds, Date start);
}
