package io.terminus.doctor.event.reportBi;

import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.event.model.DoctorPigDaily;
import io.terminus.doctor.event.reportBi.listener.DoctorReportBiEvent;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 18/1/9.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorReportBiManager {
    private final CoreEventDispatcher dispatcher;
    private final DoctorReportBiDataSynchronize synchronize;

    @Autowired
    public DoctorReportBiManager(CoreEventDispatcher dispatcher, DoctorReportBiDataSynchronize synchronize) {
        this.dispatcher = dispatcher;
        this.synchronize = synchronize;
    }

    /**
     * 同步猪报表数据到bi（t+0）
     * @param pigDaily 猪日报表
     */
    public void synchronizePigData(DoctorPigDaily pigDaily) {
        dispatcher.publish(new DoctorReportBiEvent(pigDaily.getOrgId(), pigDaily.getOrgName(),
                pigDaily.getFarmId(), pigDaily.getFarmName(), null, pigDaily.getSumAt()));
    }

    /**
     * 同步猪群报表数据到bi（t+0）
     * @param groupDaily 猪群日报表
     */
    public void synchronizeGroupData(DoctorGroupDaily groupDaily){
        dispatcher.publish(new DoctorReportBiEvent(groupDaily.getOrgId(), groupDaily.getOrgName(),
                groupDaily.getFarmId(), groupDaily.getFarmName(), groupDaily.getPigType(), groupDaily.getSumAt()));

    }

    /**
     * 同步特殊指标数据到bi（t+1）
     */
    public void synchronizeSpecificData(){}

    /**
     * 全量同步数据到bi(手动)
     */
    public void synchronizeFullData() {
        synchronize.synchronizeFullBiData();
    }


    // TODO: 18/1/9 从bi拉取数据
}
