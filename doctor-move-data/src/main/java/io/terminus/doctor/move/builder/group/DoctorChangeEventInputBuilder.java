package io.terminus.doctor.move.builder.group;

import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListGain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Created by xjn on 17/8/9.
 * 变动
 */
@Slf4j
@Component
public class DoctorChangeEventInputBuilder implements DoctorGroupEventInputBuilder{
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;
    
    @Override
    public BaseGroupInput buildFromMove(DoctorMoveBasicData moveBasicData, View_EventListGain groupRawEvent) {
        Map<Integer, Map<String, DoctorBasic>> basicMap = moveBasicData.getBasicMap();
        Map<String, DoctorChangeReason> changeReasonMap = moveBasicData.getChangeReasonMap();
        Map<String, DoctorCustomer> customerMap = moveBasicData.getCustomerMap();

        DoctorChangeGroupInput change = new DoctorChangeGroupInput();
        builderCommonOperation.fillGroupEventCommonInputFromMove(change, groupRawEvent);
        //变动类型, 原因, 品种, 客户
        DoctorBasic changeType = basicMap.get(DoctorBasic.Type.CHANGE_TYPE.getValue()).get(groupRawEvent.getChangTypeName());
        change.setChangeTypeId(changeType == null ? null : changeType.getId());
        change.setChangeTypeName(groupRawEvent.getChangTypeName());

        DoctorChangeReason reason = changeReasonMap.get(groupRawEvent.getChangeReasonName());
        change.setChangeReasonId(reason == null ? null : reason.getId());
        change.setChangeReasonName(groupRawEvent.getChangeReasonName());

        DoctorBasic basic = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(groupRawEvent.getBreed());
        change.setBreedId(basic == null ? null : basic.getId());
        change.setBreedName(groupRawEvent.getBreed());

        DoctorCustomer customer = customerMap.get(groupRawEvent.getCustomer());
        change.setCustomerId(customer == null ? null : customer.getId());
        change.setCustomerName(groupRawEvent.getCustomer());

        //单价 金额 数量
        change.setPrice(groupRawEvent.getPrice());
        change.setAmount(groupRawEvent.getAmount());
        change.setBoarQty(groupRawEvent.getBoarQty());
        change.setSowQty(groupRawEvent.getSowQty());
        change.setSowEvent(Objects.equals(change.getIsAuto(), IsOrNot.YES.getValue()));
        return change;
    }
}
