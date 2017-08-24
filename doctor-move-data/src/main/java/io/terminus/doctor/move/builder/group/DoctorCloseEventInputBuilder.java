package io.terminus.doctor.move.builder.group;

import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListGain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/8/9.
 * 关闭
 */
@Slf4j
@Component
public class DoctorCloseEventInputBuilder implements DoctorGroupEventInputBuilder{
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;

    @Override
    public BaseGroupInput buildFromMove(DoctorMoveBasicData moveBasicData, View_EventListGain groupRawEvent) {
        DoctorCloseGroupInput close = new DoctorCloseGroupInput();
        builderCommonOperation.fillGroupEventCommonInputFromMove(close, groupRawEvent);
        return close;
    }
}
