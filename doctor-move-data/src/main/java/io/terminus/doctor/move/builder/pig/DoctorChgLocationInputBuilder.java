package io.terminus.doctor.move.builder.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorImportBasicData;
import io.terminus.doctor.move.dto.DoctorImportPigEvent;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListPig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by xjn on 17/8/4.
 * 转舍
 */
@Component
public class DoctorChgLocationInputBuilder implements DoctorPigEventInputBuilder{
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;

    @Override
    public BasePigEventInputDto buildFromMove(DoctorMoveBasicData moveBasicData,
                                              View_EventListPig pigRawEvent) {

        Map<String, DoctorBarn> barnMap = moveBasicData.getBarnMap();

        DoctorChgLocationDto transBarn = new DoctorChgLocationDto();
        builderCommonOperation.fillPigEventCommonInput(transBarn, moveBasicData, pigRawEvent);
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

    @Override
    public BasePigEventInputDto buildFromImport(DoctorImportBasicData importBasicData, DoctorImportPigEvent importPigEvent) {
        Map<String, DoctorBarn> barnMap = importBasicData.getBarnMap();

        DoctorChgLocationDto transBarn = new DoctorChgLocationDto();
        builderCommonOperation.fillPigEventCommonInput(transBarn, importBasicData, importPigEvent);
        transBarn.setChangeLocationDate(importPigEvent.getEventAt());
        DoctorBarn fromBarn = barnMap.get(importPigEvent.getBarnName());    //来源猪舍
        expectTrue(notNull(fromBarn), "farmBarn");
            transBarn.setChgLocationFromBarnId(fromBarn.getId());
            transBarn.setChgLocationFromBarnName(fromBarn.getName());
        DoctorBarn toBarn = barnMap.get(importPigEvent.getToBarnName());    //去往猪舍
        expectTrue(notNull(toBarn), "toBarn");
            transBarn.setChgLocationToBarnId(toBarn.getId());
            transBarn.setChgLocationToBarnName(toBarn.getName());
        return transBarn;
    }
}
