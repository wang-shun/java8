package io.terminus.doctor.event.service;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.doctor.event.dao.DoctorPigDailyDao;
import io.terminus.doctor.event.dto.DoctorPigReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by sunbo@terminus.io on 2017/12/13.
 */
@Slf4j
@Service
@RpcProvider
public class DoctorPigReportReadServiceImpl implements DoctorPigReportReadService {

    @Autowired
    private DoctorPigDailyDao doctorPigDailyDao;

    @Override
    public DoctorPigReport farmReport(Long farmId, ReportTime reportTime) {

        DoctorPigReport doctorPigReport = new DoctorPigReport();
        if (reportTime == ReportTime.DAY) {

            
        } else if (reportTime == ReportTime.WEEK) {

        } else if (reportTime == ReportTime.MONTH) {

        } else if (reportTime == ReportTime.SEASON) {

        } else if (reportTime == ReportTime.YEAR) {

        }

        return doctorPigReport;
    }

    @Override
    public DoctorPigReport companyReport(Long orgId, ReportTime reportTime) {
        return null;
    }

    @Override
    public DoctorPigReport orgReport(Long orgId, ReportTime reportTime) {
        return null;
    }
}
