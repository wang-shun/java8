package io.terminus.doctor.event.event;

import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.enums.DataRange;
import io.terminus.doctor.event.model.DoctorPigTypeStatistic;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.event.service.DoctorPigTypeStatisticReadService;
import io.terminus.doctor.event.service.DoctorPigTypeStatisticWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-06-06
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Slf4j
@Component
public class DoctorPigCountEventListener implements EventListener{

    private final DoctorPigReadService doctorPigReadService;

    private final DoctorPigTypeStatisticReadService doctorPigTypeStatisticReadService;

    private final DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService;

    @Autowired
    public DoctorPigCountEventListener(DoctorPigReadService doctorPigReadService,
                                       DoctorPigTypeStatisticReadService doctorPigTypeStatisticReadService,
                                       DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService){
        this.doctorPigReadService = doctorPigReadService;
        this.doctorPigTypeStatisticReadService = doctorPigTypeStatisticReadService;
        this.doctorPigTypeStatisticWriteService = doctorPigTypeStatisticWriteService;
    }

    @Subscribe
    public void countPigType(DoctorPigCountEvent doctorPigCountEvent){

        Long pigCount = RespHelper.orServEx(
                doctorPigReadService.queryPigCount(DataRange.FARM.getKey(), doctorPigCountEvent.getFarmId(), doctorPigCountEvent.getPigType()));

        Integer pigCountInt = Integer.valueOf(pigCount.toString());

        DoctorPigTypeStatistic doctorPigTypeStatistic =
                RespHelper.orServEx(doctorPigTypeStatisticReadService.findPigTypeStatisticByFarmId(doctorPigCountEvent.getFarmId()));

        if(isNull(doctorPigTypeStatistic)){
            doctorPigTypeStatistic = DoctorPigTypeStatistic.builder()
                    .farmId(doctorPigCountEvent.getFarmId()).orgId(doctorPigCountEvent.getOrgId())
                    .build();
            doctorPigTypeStatistic.putPigTypeCount(doctorPigCountEvent.getPigType(), pigCountInt);
            RespHelper.orServEx(doctorPigTypeStatisticWriteService.createPigTypeStatistic(doctorPigTypeStatistic));
        }else {
            doctorPigTypeStatistic.putPigTypeCount(doctorPigCountEvent.getPigType(), pigCountInt);
            RespHelper.orServEx(doctorPigTypeStatisticWriteService.updatePigTypeStatistic(doctorPigTypeStatistic));
        }
    }

}
