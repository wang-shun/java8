package io.terminus.doctor.move.builder.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFosterByDto;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.move.model.View_EventListSow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/9/5.
 * 被拼窝事件
 */
@Component
public class DoctorFosterByInputBuilder implements DoctorPigEventInputBuilder{
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;

    @Override
    public BasePigEventInputDto buildFromMove(DoctorMoveBasicData moveBasicData,
                                              View_EventListPig pigRawEvent) {
        DoctorFosterByDto foster = new DoctorFosterByDto();
        View_EventListSow event = (View_EventListSow) pigRawEvent;
        builderCommonOperation.fillPigEventCommonInput(foster, moveBasicData, pigRawEvent);
        foster.setFosterByDate(event.getEventAt());   // 拼窝日期
        foster.setFosterByCount(event.getNetOutCount());   //  拼窝数量
        foster.setFosterByTotalWeight(event.getWeanWeight());   //拼窝总重量
        foster.setFromSowCode(event.getDisease());      //拼窝母猪号
        return foster;
    }
}
