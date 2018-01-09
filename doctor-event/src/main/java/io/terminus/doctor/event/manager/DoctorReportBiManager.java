package io.terminus.doctor.event.manager;

import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import io.terminus.doctor.event.model.DoctorPigDaily;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 18/1/9.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorReportBiManager {
    private final CoreEventDispatcher dispatcher;

    @Autowired
    public DoctorReportBiManager(CoreEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * 同步猪报表数据到bi（t+0）
     * @param pigDaily 猪日报表
     */
    public void synchronizePigData(DoctorPigDaily pigDaily) {
        dispatcher.publish(pigDaily);
    }

    /**
     * 同步猪群报表数据到bi（t+0）
     * @param groupDaily 猪群日报表
     */
    public void synchronizeGroupData(DoctorGroupDaily groupDaily){
        dispatcher.publish(groupDaily);
    }

    /**
     * 同步特殊指标数据到bi（t+1）
     */
    public void synchronizeSpecificData(){}

    /**
     * 全量同步数据到bi(手动)
     */
    public void synchronizeFullData() {

    }


    // TODO: 18/1/9 从bi拉取数据
}
