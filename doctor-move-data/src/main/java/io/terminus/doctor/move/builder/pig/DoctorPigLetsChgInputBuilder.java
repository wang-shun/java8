package io.terminus.doctor.move.builder.pig;

import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.move.model.View_EventListSow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by xjn on 17/8/7.
 * 仔猪变动
 */
@Component
public class DoctorPigLetsChgInputBuilder implements DoctorPigEventInputBuilder{
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;

    @Override
    public BasePigEventInputDto buildFromMove(DoctorMoveBasicData moveBasicData,
                                              View_EventListPig pigRawEvent) {
        View_EventListSow event = (View_EventListSow) pigRawEvent;
        Map<Integer, Map<String, DoctorBasic>> basicMap = moveBasicData.getBasicMap();
        Map<String, DoctorChangeReason> changeReasonMap = moveBasicData.getChangeReasonMap();
        Map<String, DoctorCustomer> customerMap = moveBasicData.getCustomerMap();

        DoctorPigletsChgDto change = new DoctorPigletsChgDto();
        builderCommonOperation.fillPigEventCommonInput(change, moveBasicData, pigRawEvent);

        change.setPigletsChangeDate(event.getEventAt());               // 仔猪变动日期
        change.setPigletsCount(event.getChgCount());                   // 仔猪数量
        change.setPigletsWeight(event.getEventWeight());               // 变动重量 (非必填)
        change.setPigletsPrice(event.getPrice());                      // 变动价格 （非必填）
        change.setPigletsSum(event.getAmount());                       // 总价（非必填）
        change.setPigletsMark(event.getRemark());                      // 标识(非必填)
        change.setPigletsChangeTypeName(event.getChangeTypeName());
        change.setPigletsChangeReasonName(event.getChgReason());        // 仔猪变动原因

        DoctorBasic changeType = basicMap.get(DoctorBasic.Type.CHANGE_TYPE.getValue()).get(event.getChangeTypeName());
        change.setPigletsChangeType(changeType == null ? null : changeType.getId());    // 仔猪变动类型
        DoctorChangeReason reason = changeReasonMap.get(event.getChgReason());
        change.setPigletsChangeReason(reason == null ? null : reason.getId());          // 仔猪变动原因
        DoctorCustomer customer = customerMap.get(event.getCustomer());
        change.setPigletsCustomerId(customer == null ? null : customer.getId());         // 客户Id （非必填）
        return change;
    }
}
