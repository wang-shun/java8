package io.terminus.doctor.move.builder.pig;

import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFostersDto;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.move.model.View_EventListSow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by xjn on 17/9/5.
 * 拼窝
 */
@Component
public class DoctorFosterInputBuilder implements DoctorPigEventInputBuilder{
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;

    @Override
    public BasePigEventInputDto buildFromMove(DoctorMoveBasicData moveBasicData, View_EventListPig pigRawEvent) {

        DoctorFostersDto foster = new DoctorFostersDto();
        View_EventListSow event = (View_EventListSow) pigRawEvent;
        Map<Integer, Map<String, DoctorBasic>> basicMap = moveBasicData.getBasicMap();
        builderCommonOperation.fillPigEventCommonInput(foster, moveBasicData, pigRawEvent);
        foster.setFostersDate(DateUtil.toDateString(event.getEventAt()));   // 拼窝日期
        foster.setFostersCount(event.getNetOutCount());   //  拼窝数量
        foster.setFosterTotalWeight(event.getWeanWeight());   //拼窝总重量
        foster.setFosterSowCode(event.getDisease());      //拼窝母猪号
        foster.setFosterSowOutId(event.getNurseSow());      //拼窝母猪号

        //寄养原因
        DoctorBasic reason = basicMap.get(DoctorBasic.Type.FOSTER_REASON.getValue()).get(event.getFosterReasonName());
        foster.setFosterReason(reason == null ? null : reason.getId());
        foster.setFosterReasonName(event.getFosterReasonName());
        return foster;
    }
}
