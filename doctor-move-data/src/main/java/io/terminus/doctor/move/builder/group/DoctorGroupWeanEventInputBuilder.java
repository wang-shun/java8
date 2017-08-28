package io.terminus.doctor.move.builder.group;

import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorWeanGroupInput;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListGain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/8/24.
 * 猪群断奶
 */
@Component
public class DoctorGroupWeanEventInputBuilder implements DoctorGroupEventInputBuilder {
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;

    @Override
    public BaseGroupInput buildFromMove(DoctorMoveBasicData moveBasicData, View_EventListGain groupRawEvent) {

        DoctorWeanGroupInput input = new DoctorWeanGroupInput();
        builderCommonOperation.fillGroupEventCommonInput(input, groupRawEvent);

        input.setPartWeanPigletsCount(groupRawEvent.getQuantity());
        input.setPartWeanAvgWeight(groupRawEvent.getAvgWeight());
        return input;
    }
}
