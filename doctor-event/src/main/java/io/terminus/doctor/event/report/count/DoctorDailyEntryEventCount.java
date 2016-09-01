package io.terminus.doctor.event.report.count;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorLiveStockDailyReport;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;

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
    public List<DoctorPigEvent> preDailyEventHandleValidate(List<DoctorPigEvent> t) {
        return t.stream().filter(e -> Objects.equals(e.getType(), PigEvent.ENTRY.getKey()) || Objects.equals(e.getType(), PigEvent.REMOVAL.getKey())).collect(Collectors.toList());
    }

    @Override
    public void dailyEventHandle(List<DoctorPigEvent> t, DoctorDailyReportDto doctorDailyReportDto, Map<String, Object> context) {

        // validate same farm
        Long farmId;
        Date eventAt;
        if(t.size() == 0){
            farmId = Long.valueOf(context.get("farmId").toString());
            eventAt = DateUtil.toDate(context.get("eventAt").toString());
        }else {
            checkState(t.size() > 0, "dailyEntry.eventCount.fail");
            Long currentFarmId = t.get(0).getFarmId();
            t.forEach(e-> checkState(Objects.equals(e.getFarmId(), currentFarmId), "dailyEntry.notSameFarm.fail"));
            farmId = currentFarmId;
            eventAt = t.get(0).getEventAt();
        }

        //存栏
        DoctorLiveStockDailyReport liveStock = doctorDailyReportDto.getLiveStock();
        liveStock.setBuruSow(doctorKpiDao.realTimeLiveStockFarrowSow(farmId, eventAt));    //产房母猪
        liveStock.setPeihuaiSow(doctorKpiDao.realTimeLiveStockSow(farmId, eventAt) - liveStock.getBuruSow());    //配怀 = 总存栏 - 产房母猪
        liveStock.setKonghuaiSow(0);                                                       //空怀猪作废, 置成0
        liveStock.setBoar(doctorKpiDao.realTimeLiveStockBoar(farmId, eventAt));            //公猪
    }
}
