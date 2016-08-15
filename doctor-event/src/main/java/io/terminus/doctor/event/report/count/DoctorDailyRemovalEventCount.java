package io.terminus.doctor.event.report.count;

import com.google.common.base.MoreObjects;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public List<DoctorPigEvent> preDailyEventHandleValidate(List<DoctorPigEvent> t) {
        return t.stream().filter(e-> Objects.equals(e.getType(), PigEvent.REMOVAL.getKey())).collect(Collectors.toList());
    }

    @Override
    public void dailyEventHandle(List<DoctorPigEvent> t, DoctorDailyReportDto doctorDailyReportDto , Map<String, Object> context) {

        DoctorSaleDailyReport doctorSaleDailyReport = new DoctorSaleDailyReport();
        DoctorDeadDailyReport doctorDeadDailyReport = new DoctorDeadDailyReport();

        t.forEach(e->{
            Map<String, Object> extra = e.getExtraMap();
            DoctorPig doctorPig = doctorPigDao.findById(e.getPigId());

            Long chageReason = Long.valueOf(extra.get("chgReasonId").toString());
            if(Objects.equals(chageReason, DoctorBasicEnums.DEAD.getId()) || Objects.equals(chageReason, DoctorBasicEnums.ELIMINATE.getId())){
                if(Objects.equals(doctorPig.getPigType(), DoctorPig.PIG_TYPE.BOAR.getKey())){
                    doctorDeadDailyReport.setBoar(doctorDeadDailyReport.getBoar() + 1);
                }else if(Objects.equals(doctorPig.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())){
                    doctorDeadDailyReport.setSow(doctorDeadDailyReport.getSow() + 1);
                }
            }else if(Objects.equals(chageReason, DoctorBasicEnums.SALE.getId())){
                //添加对应的销售数量
                if(Objects.equals(doctorPig.getPigType(), DoctorPig.PIG_TYPE.BOAR.getKey())){
                    doctorSaleDailyReport.setBoar(doctorSaleDailyReport.getBoar() + 1);
                }else if (Objects.equals(doctorPig.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())){
                    doctorSaleDailyReport.setSow(doctorSaleDailyReport.getSow() + 1);
                }
            }
        });

        doctorDailyReportDto.getDead().addSowBoar(doctorDeadDailyReport);
        doctorDailyReportDto.getSale().addBoarSowCount(doctorSaleDailyReport);
    }
}
