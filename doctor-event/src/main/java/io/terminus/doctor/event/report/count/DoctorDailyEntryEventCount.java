package io.terminus.doctor.event.report.count;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.event.daily.DoctorDailyEventCount;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorLiveStockDailyReport;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigStatusCount;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
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

    private final DoctorBarnDao doctorBarnDao;

    @Autowired
    public DoctorDailyEntryEventCount(DoctorPigTrackDao doctorPigTrackDao, DoctorBarnDao doctorBarnDao){
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorBarnDao = doctorBarnDao;
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
        Long farmId;
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
        List<DoctorPigTrack> listTrack = doctorPigTrackDao.list(ImmutableMap.of("farmId", farmId));
        if(isNull(listTrack) || Iterables.isEmpty(listTrack)){
            return;
        }
        listTrack.forEach(doctorPigTrack->{
            DoctorBarn doctorBarn = doctorBarnDao.findById(doctorPigTrack.getCurrentBarnId());
            doctorPigTrack.setPigType(doctorBarn.getPigType());
            switch (doctorBarn.getPigType()){
                case 4:
                    doctorLiveStockDailyReport.setHoubeiSow(isNull(doctorLiveStockDailyReport.getHoubeiSow()) ? 1 : (doctorLiveStockDailyReport.getHoubeiSow() + 1));
                    break;
                case 5:
                    doctorLiveStockDailyReport.setPeihuaiSow(isNull(doctorLiveStockDailyReport.getPeihuaiSow()) ? 1 : (doctorLiveStockDailyReport.getPeihuaiSow() + 1));
                    break;
                case 6:
                    doctorLiveStockDailyReport.setPeihuaiSow(isNull(doctorLiveStockDailyReport.getPeihuaiSow()) ? 1 : (doctorLiveStockDailyReport.getPeihuaiSow() + 1));
                    break;
                case 7:
                    doctorLiveStockDailyReport.setBuruSow(isNull(doctorLiveStockDailyReport.getBuruSow()) ? 1 : (doctorLiveStockDailyReport.getPeihuaiSow() + 1));
                    break;
                default:
                    break;

            }
        });
        List<DoctorPigStatusCount> statusCounts = doctorPigTrackDao.countPigTrackByStatus(farmId);
        if(isNull(statusCounts) || Iterables.isEmpty(statusCounts)){
            return;
        }

        //count result
        Map<Integer, Integer> statusCount = statusCounts.stream().collect(Collectors.toMap(DoctorPigStatusCount::getStatus, DoctorPigStatusCount::getCount));

        doctorLiveStockDailyReport.setKonghuaiSow(0);

        doctorLiveStockDailyReport.setBoar(Params.getNullDefault(statusCount, PigStatus.BOAR_ENTRY.getKey(), 0));

        // add to total
        doctorDailyReportDto.getLiveStock().addSowBoar(doctorLiveStockDailyReport);
    }
}
