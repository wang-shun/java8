package io.terminus.doctor.event.reportBi.listener;

import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Created by xjn on 18/1/9.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorReportBiListener implements EventListener{

    /**
     * 同步猪报表数据到bi
     * @param reportBiEvent 猪日报表
     */
    @Subscribe
    public void synchronizePigData(DoctorReportBiEvent reportBiEvent) {
        
    }

    /**
     * 同步猪群报表数据到bi
     * @param reportBiEvent 猪群日报表
     */
    @Subscribe
    public void synchronizeGroupData(DoctorReportBiEvent reportBiEvent){
        if (Objects.equals(reportBiEvent.getPigType(), PigType.NURSERY_PIGLET.getValue())) {
            // TODO: 18/1/9  
        } else if (Objects.equals(reportBiEvent.getPigType(), PigType.FATTEN_PIG.getValue())) {
            // TODO: 18/1/9  
        } else {
            // TODO: 18/1/9  
        }
    }

    /**
     * 同步特殊指标数据到bi
     */
    @Subscribe
    public void synchronizeSpecificData(){}

    @Subscribe
    public void synchronizeFullData(DoctorReportBiFullEvent doctorReportBiFullEvent){

    }
}
