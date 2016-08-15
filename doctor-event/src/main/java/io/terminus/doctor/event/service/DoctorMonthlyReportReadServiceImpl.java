package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorMonthlyReportDao;
import io.terminus.doctor.event.dto.report.monthly.DoctorMonthlyReportDto;
import io.terminus.doctor.event.model.DoctorMonthlyReport;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Desc: 猪场月报表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-08-11
 */
@Slf4j
@Service
@RpcProvider
public class DoctorMonthlyReportReadServiceImpl implements DoctorMonthlyReportReadService {

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private final DoctorMonthlyReportDao doctorMonthlyReportDao;

    @Autowired
    public DoctorMonthlyReportReadServiceImpl(DoctorMonthlyReportDao doctorMonthlyReportDao) {
        this.doctorMonthlyReportDao = doctorMonthlyReportDao;
    }

    @Override
    public Response<DoctorMonthlyReportDto> findMonthlyReportByFarmIdAndSumAt(Long farmId, String sumAt) {
        try {
            Date date = DateUtil.toDate(sumAt);
            DateTime datetime = new DateTime(date);

            //查未来返回没查到
            if (datetime.isAfterNow()) {
                return Response.ok(failReport());
            }

            if (datetime.getDayOfMonth() < DateTime.now().getDayOfMonth()) {
                date = new DateTime(date).plusMonths(1).withDayOfMonth(1).plusDays(-1).toDate();
            }

            //查询月报结果, 如果没查到, 返回失败的结果
            DoctorMonthlyReport report = doctorMonthlyReportDao.findByFarmIdAndSumAt(farmId, date);
            if (report == null) {
                return Response.ok(failReport());
            }
            DoctorMonthlyReportDto reportDto = JSON_MAPPER.fromJson(report.getData(), DoctorMonthlyReportDto.class);
            if (reportDto == null) {
                return Response.ok(failReport());
            }
            return Response.ok(reportDto);
        } catch (Exception e) {
            log.error("find monthly report by farmId and sumAt failed, farmId:{}, sumAt:{}, cause:{}",
                    farmId, sumAt, Throwables.getStackTraceAsString(e));
            return Response.ok(failReport());
        }
    }

    //查询失败的结果
    private DoctorMonthlyReportDto failReport() {
        DoctorMonthlyReportDto dto = new DoctorMonthlyReportDto();
        dto.setFail(true);
        return dto;
    }
}
