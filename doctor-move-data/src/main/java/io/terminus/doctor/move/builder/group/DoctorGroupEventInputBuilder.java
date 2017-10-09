package io.terminus.doctor.move.builder.group;

import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.move.dto.DoctorImportBasicData;
import io.terminus.doctor.move.dto.DoctorImportGroupEvent;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListGain;

/**
 * Created by xjn on 17/8/7.
 * 猪群输入构建器
 */
public interface DoctorGroupEventInputBuilder {
    BaseGroupInput buildFromMove(DoctorMoveBasicData moveBasicData,
                                 View_EventListGain groupRawEvent);

    default BaseGroupInput buildFromImport(DoctorImportBasicData importBasicData,
                                                 DoctorImportGroupEvent importGroupEvent){
        return null;
    }
}
