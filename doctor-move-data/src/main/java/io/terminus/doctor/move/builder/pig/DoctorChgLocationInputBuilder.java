package io.terminus.doctor.move.builder.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListPig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by xjn on 17/8/4.
 * 转舍
 */
@Component
public class DoctorChgLocationInputBuilder implements DoctorPigEventInputBuilder{
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;

    @Override
    public BasePigEventInputDto buildPigEventInputFromMove(DoctorMoveBasicData moveBasicData,
                                                           View_EventListPig pigRawEvent) {

        Map<String, DoctorBarn> barnMap = moveBasicData.getBarnMap();

        DoctorChgLocationDto transBarn = new DoctorChgLocationDto();
        builderCommonOperation.fillPigEventCommonInputFromMove(transBarn, moveBasicData, pigRawEvent);
        transBarn.setChangeLocationDate(pigRawEvent.getEventAt());
        DoctorBarn fromBarn = barnMap.get(pigRawEvent.getBarnOutId());    //来源猪舍
        if (fromBarn != null) {
            transBarn.setChgLocationFromBarnId(fromBarn.getId());
            transBarn.setChgLocationFromBarnName(fromBarn.getName());
        }
        DoctorBarn toBarn = barnMap.get(pigRawEvent.getToBarnOutId());    //去往猪舍
        if (toBarn != null) {
            transBarn.setChgLocationToBarnId(toBarn.getId());
            transBarn.setChgLocationToBarnName(toBarn.getName());
        }
        return transBarn;
    }
}
