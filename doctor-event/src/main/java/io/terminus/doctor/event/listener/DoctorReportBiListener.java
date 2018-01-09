package io.terminus.doctor.event.listener;

import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import io.terminus.doctor.event.model.DoctorPigDaily;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 18/1/9.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorReportBiListener implements EventListener{

    /**
     * 同步猪报表数据到bi
     * @param pigDaily 猪日报表
     */
    @Subscribe
    public void synchronizePigData(DoctorPigDaily pigDaily) {

    }

    /**
     * 同步猪群报表数据到bi
     * @param groupDaily 猪群日报表
     */
    @Subscribe
    public void synchronizeGroupData(DoctorGroupDaily groupDaily){

    }

    /**
     * 同步特殊指标数据到bi
     */
    @Subscribe
    public void synchronizeSpecificData(){}
}
