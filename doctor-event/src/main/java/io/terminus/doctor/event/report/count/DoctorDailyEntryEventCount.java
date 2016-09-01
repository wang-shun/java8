package io.terminus.doctor.event.report.count;

import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorLiveStockDailyReport;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-07-19
 * Email:yaoqj@terminus.io
 * Descirbe: 进厂事件信息统计
 */
@Component
@Slf4j
public class DoctorDailyEntryEventCount implements DoctorDailyEventCount {

    private final DoctorKpiDao doctorKpiDao;

    @Autowired
    public DoctorDailyEntryEventCount(DoctorKpiDao doctorKpiDao){
        this.doctorKpiDao = doctorKpiDao;
    }

    @Override
    public boolean preDailyEventHandleValidate(DoctorPigEvent event) {
        return Objects.equals(event.getType(), PigEvent.ENTRY.getKey()) || Objects.equals(event.getType(), PigEvent.REMOVAL.getKey());
    }

    @Override
    public void dailyEventHandle(DoctorPigEvent event, DoctorDailyReportDto dailyReportDto) {
        //存栏
        DoctorLiveStockDailyReport liveStock = dailyReportDto.getLiveStock();
        liveStock.setBuruSow(doctorKpiDao.realTimeLiveStockFarrowSow(event.getFarmId(), event.getEventAt()));    //产房母猪
        liveStock.setPeihuaiSow(doctorKpiDao.realTimeLiveStockSow(event.getFarmId(), event.getEventAt()) - liveStock.getBuruSow());    //配怀 = 总存栏 - 产房母猪
        liveStock.setKonghuaiSow(0);                                                       //空怀猪作废, 置成0
        liveStock.setBoar(doctorKpiDao.realTimeLiveStockBoar(event.getFarmId(), event.getEventAt()));            //公猪
    }
}
