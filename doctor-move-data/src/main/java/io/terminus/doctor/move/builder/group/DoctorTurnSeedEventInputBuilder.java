package io.terminus.doctor.move.builder.group;

import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListGain;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/9/3.
 * 转种猪
 */
@Component
public class DoctorTurnSeedEventInputBuilder implements DoctorGroupEventInputBuilder{

    @Override
    public BaseGroupInput buildFromMove(DoctorMoveBasicData moveBasicData,
                                        View_EventListGain groupRawEvent) {
        return null;
    }
}
