package io.terminus.doctor.event.report.count;

import io.terminus.doctor.event.constants.DoctorBasicEnums;
import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorDeadDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorSaleDailyReport;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-07-19
 * Email:yaoqj@terminus.io
 * Descirbe: 死亡淘汰数量统计
 */
@Component
public class DoctorDailyRemovalEventCount implements DoctorDailyEventCount {

    private final DoctorPigDao doctorPigDao;

    @Autowired
    public DoctorDailyRemovalEventCount(DoctorPigDao doctorPigDao){
        this.doctorPigDao = doctorPigDao;
    }

    @Override
    public boolean preDailyEventHandleValidate(DoctorPigEvent event) {
        return Objects.equals(event.getType(), PigEvent.REMOVAL.getKey());
    }

    @Override
    public void dailyEventHandle(DoctorPigEvent event, DoctorDailyReportDto doctorDailyReportDto) {

        DoctorSaleDailyReport doctorSaleDailyReport = new DoctorSaleDailyReport();
        DoctorDeadDailyReport doctorDeadDailyReport = new DoctorDeadDailyReport();

        Map<String, Object> extra = event.getExtraMap();
        DoctorPig doctorPig = doctorPigDao.findById(event.getPigId());

        if (extra == null || extra.get("chgTypeId") == null ) {
            return;
        }

        Long chgTypeId = Long.valueOf(extra.get("chgTypeId").toString());
        if(Objects.equals(chgTypeId, DoctorBasicEnums.DEAD.getId()) || Objects.equals(chgTypeId, DoctorBasicEnums.ELIMINATE.getId())){
            if(Objects.equals(doctorPig.getPigType(), DoctorPig.PIG_TYPE.BOAR.getKey())){
                doctorDeadDailyReport.setBoar(doctorDeadDailyReport.getBoar() + 1);
            }else if(Objects.equals(doctorPig.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())){
                doctorDeadDailyReport.setSow(doctorDeadDailyReport.getSow() + 1);
            }
        }else if(Objects.equals(chgTypeId, DoctorBasicEnums.SALE.getId())){
            //添加对应的销售数量
            if(Objects.equals(doctorPig.getPigType(), DoctorPig.PIG_TYPE.BOAR.getKey())){
                doctorSaleDailyReport.setBoar(doctorSaleDailyReport.getBoar() + 1);
            }else if (Objects.equals(doctorPig.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())){
                doctorSaleDailyReport.setSow(doctorSaleDailyReport.getSow() + 1);
            }
        }

        doctorDailyReportDto.getDead().addSowBoar(doctorDeadDailyReport);
        doctorDailyReportDto.getSale().addBoarSowCount(doctorSaleDailyReport);
    }
}
