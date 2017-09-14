package io.terminus.doctor.move.builder.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.move.dto.DoctorImportBasicData;
import io.terminus.doctor.move.dto.DoctorImportPigEvent;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListPig;

/**
 * Created by xjn on 17/8/4.
 * 事件输入构建器
 */
public interface DoctorPigEventInputBuilder {

    BasePigEventInputDto buildFromMove(DoctorMoveBasicData moveBasicData,
                                       View_EventListPig pigRawEvent);

    default BasePigEventInputDto buildFromImport(DoctorImportBasicData importBasicData,
                                         DoctorImportPigEvent importPigEvent){
        return null;
    }
}
