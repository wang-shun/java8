package io.terminus.doctor.event.event;

import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.service.DoctorPigTypeStatisticWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/3
 */
@Slf4j
@Component
@SuppressWarnings("unused")
public class DoctorGroupCountListener implements EventListener {

    private final DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService;

    @Autowired
    public DoctorGroupCountListener(DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService) {
        this.doctorPigTypeStatisticWriteService = doctorPigTypeStatisticWriteService;
    }

    /**
     * 分类统计猪群
     * @param event 事件携带数据
     */
    @Subscribe
    public void countGroupByType(DoctorGroupCountEvent event) {
        RespHelper.or500(doctorPigTypeStatisticWriteService.statisticGroup(event.getOrgId(), event.getFarmId()));
    }
}
