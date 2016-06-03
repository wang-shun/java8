package io.terminus.doctor.event.event;

import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupCount;
import io.terminus.doctor.event.model.DoctorPigTypeStatistic;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorPigTypeStatisticReadService;
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
public class DoctorGroupCountListener implements EventListener {

    private final DoctorGroupReadService doctorGroupReadService;
    private final DoctorPigTypeStatisticReadService doctorPigTypeStatisticReadService;
    private final DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService;

    @Autowired
    public DoctorGroupCountListener(DoctorGroupReadService doctorGroupReadService,
                                    DoctorPigTypeStatisticReadService doctorPigTypeStatisticReadService,
                                    DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService) {
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorPigTypeStatisticReadService = doctorPigTypeStatisticReadService;
        this.doctorPigTypeStatisticWriteService = doctorPigTypeStatisticWriteService;
    }

    /**
     * 分类统计猪群
     * @param event 事件携带数据
     */
    @Subscribe
    public void countGroupByType(DoctorGroupCountEvent event) {
        DoctorGroupCount groupCount = RespHelper.or500(doctorGroupReadService.coutFarmGroups(event.getOrgId(), event.getFarmId()));
        DoctorPigTypeStatistic statistic = RespHelper.or500(doctorPigTypeStatisticReadService.findPigTypeStatisticByFarmId(event.getFarmId()));

        //如果不存在, 就新建统计数据
        if (statistic == null) {
            statistic = new DoctorPigTypeStatistic();
            statistic.setOrgId(event.getOrgId());
            statistic.setFarmId(event.getFarmId());
            setGroupStatistic(statistic, groupCount);

            RespHelper.or500(doctorPigTypeStatisticWriteService.createPigTypeStatistic(statistic));
        } else {
            setGroupStatistic(statistic, groupCount);
            RespHelper.or500(doctorPigTypeStatisticWriteService.updatePigTypeStatistic(statistic));
        }
    }

    //拼接猪群统计
    private void setGroupStatistic(DoctorPigTypeStatistic statistic, DoctorGroupCount groupCount) {
        statistic.setFarrow((int) groupCount.getFarrowCount());
        statistic.setNursery((int) groupCount.getNurseryCount());
        statistic.setFatten((int) groupCount.getFattenCount());
    }
}
