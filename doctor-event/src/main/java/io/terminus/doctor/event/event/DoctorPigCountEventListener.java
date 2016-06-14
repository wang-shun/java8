package io.terminus.doctor.event.event;

import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.service.DoctorPigTypeStatisticWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by yaoqijun.
 * Date:2016-06-06
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Slf4j
@Component
public class DoctorPigCountEventListener implements EventListener{

    private final DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService;

    @Autowired
    public DoctorPigCountEventListener(DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService){
        this.doctorPigTypeStatisticWriteService = doctorPigTypeStatisticWriteService;
    }

    @Subscribe
    public void countPigType(DoctorPigCountEvent event){
        RespHelper.or500(doctorPigTypeStatisticWriteService.statisticPig(event.getOrgId(), event.getFarmId(), event.getPigType()));
    }
}
