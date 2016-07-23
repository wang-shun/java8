package io.terminus.doctor.event.report.count;

import com.google.common.collect.Iterables;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.report.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.DoctorLiveStockDailyReport;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigStatusCount;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-07-19
 * Email:yaoqj@terminus.io
 * Descirbe: 进厂事件信息统计
 */
@Component
@Slf4j
public class DoctorDailyEntryEventCount implements DoctorDailyEventCount {

    private final DoctorPigTrackDao doctorPigTrackDao;

    @Autowired
    public DoctorDailyEntryEventCount(DoctorPigTrackDao doctorPigTrackDao){
        this.doctorPigTrackDao = doctorPigTrackDao;
    }

    @Override
    public List<DoctorPigEvent> preDailyEventHandleValidate(List<DoctorPigEvent> t) {
//        return t.stream().filter(e -> Objects.equals(e.getType(), PigEvent.ENTRY.getKey())).collect(Collectors.toList());
        return t;
    }

    @Override
    public void dailyEventHandle(List<DoctorPigEvent> t, DoctorDailyReportDto doctorDailyReportDto, Map<String, Object> context) {

        DoctorLiveStockDailyReport doctorLiveStockDailyReport = new DoctorLiveStockDailyReport();

        // validate same farm
        Long farmId = null;
        if(t.size() == 0){
            farmId = Long.valueOf(context.get("farmId").toString());
        }else {
            checkState(t.size() > 0, "dailyEntry.eventCount.fail");
            Long currentFarmId = t.get(0).getFarmId();
            t.forEach(e->{
                checkState(Objects.equals(e.getFarmId(), currentFarmId), "dailyEntry.notSameFarm.fail");
            });
            farmId = currentFarmId;
        }


        // check result
        List<DoctorPigStatusCount> statusCounts = doctorPigTrackDao.countPigTrackByStatus(farmId);
        if(isNull(statusCounts) || Iterables.isEmpty(statusCounts)){
            return;
        }

        //count result
        Map<Integer, Integer> statusCount = statusCounts.stream().collect(Collectors.toMap(k->k.getStatus() , v->v.getCount()));

        doctorLiveStockDailyReport.setHoubeiSow(Params.getNullDefault(statusCount, PigStatus.Entry.getKey(), 0));
        doctorLiveStockDailyReport.setPeihuaiSow(Params.getNullDefault(statusCount,PigStatus.Mate.getKey(), 0) +
                Params.getNullDefault(statusCount, PigStatus.Pregnancy.getKey(),0) +
                Params.getNullDefault(statusCount, PigStatus.Farrow.getKey(), 0));
        doctorLiveStockDailyReport.setBuruSow(Params.getNullDefault(statusCount, PigStatus.FEED.getKey(), 0));
        doctorLiveStockDailyReport.setKonghuaiSow(Params.getNullDefault(statusCount, PigStatus.Abortion.getKey(), 0) +
                Params.getNullDefault(statusCount, PigStatus.KongHuai.getKey(), 0));

        log.info("map info is map:{}, key:{}", statusCount, PigStatus.BOAR_ENTRY.getKey());
        log.info("map get value is :{}", statusCount.get(PigStatus.BOAR_ENTRY.getKey()));
        log.info("result is :{}", Params.getNullDefault(statusCount, PigStatus.BOAR_ENTRY.getKey(), 0));
        doctorLiveStockDailyReport.setBoar(Params.getNullDefault(statusCount, PigStatus.BOAR_ENTRY.getKey(), 0));

        // add to total
        doctorDailyReportDto.getLiveStock().addSowBoar(doctorLiveStockDailyReport);
    }
}
