package io.terminus.doctor.move.builder.pig;

import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.usual.DoctorRemovalDto;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListBoar;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.move.model.View_EventListSow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Created by xjn on 17/8/7.
 * 离场
 */
@Component
public class DoctorRemoveInputBuilder implements DoctorPigEventInputBuilder {
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;

    @Override
    public BasePigEventInputDto buildFromMove(DoctorMoveBasicData moveBasicData,
                                              View_EventListPig pigRawEvent) {
        if (Objects.equals(pigRawEvent.getPigSex(), DoctorPig.PigSex.SOW.getKey())) {
            return buildSowRemoveInput(moveBasicData, pigRawEvent);
        }
        return buildBoarRemoveInput(moveBasicData, pigRawEvent);
    }

    private BasePigEventInputDto buildSowRemoveInput(DoctorMoveBasicData moveBasicData,
                                                    View_EventListPig pigRawEvent) {
        View_EventListSow event = (View_EventListSow)pigRawEvent;
        Map<Integer, Map<String, DoctorBasic>> basicMap = moveBasicData.getBasicMap();
        Map<String, DoctorChangeReason> changeReasonMap = moveBasicData.getChangeReasonMap();
        Map<String, DoctorCustomer> customerMap = moveBasicData.getCustomerMap();
        Map<String, DoctorBarn> barnMap = moveBasicData.getBarnMap();

        DoctorRemovalDto remove = new DoctorRemovalDto();
        builderCommonOperation.fillPigEventCommonInput(remove, moveBasicData, pigRawEvent);

        remove.setRemovalDate(event.getEventAt());
        //变动类型, 变动原因
        DoctorBasic changeType = basicMap.get(DoctorBasic.Type.CHANGE_TYPE.getValue())
                .get(event.getChangeTypeName());
        remove.setChgTypeId(changeType == null ? null : changeType.getId());
        remove.setChgTypeName(event.getChangeTypeName());
        DoctorChangeReason reason = changeReasonMap.get(event.getChgReason());
        remove.setChgReasonId(reason == null ? null : reason.getId());
        remove.setChgReasonName(event.getChgReason());

        //重量 金额等
        remove.setWeight(event.getEventWeight());
        remove.setPrice(event.getPrice());
        remove.setSum(event.getAmount());
        remove.setRemark(event.getRemark());

        //猪舍 客户
        DoctorBarn barn = barnMap.get(event.getBarnOutId());
        remove.setToBarnId(barn == null ? null : barn.getId());
        DoctorCustomer customer = customerMap.get(event.getCustomer());
        remove.setCustomerId(customer == null ? null : customer.getId());
        return remove;
    }

    private BasePigEventInputDto buildBoarRemoveInput(DoctorMoveBasicData moveBasicData,
                                                     View_EventListPig pigRawEvent) {
        View_EventListBoar event = (View_EventListBoar) pigRawEvent;
        Map<Integer, Map<String, DoctorBasic>> basicMap = moveBasicData.getBasicMap();
        Map<String, DoctorChangeReason> changeReasonMap = moveBasicData.getChangeReasonMap();
        Map<String, DoctorCustomer> customerMap = moveBasicData.getCustomerMap();
        Map<String, DoctorBarn> barnMap = moveBasicData.getBarnMap();

        DoctorRemovalDto remove = new DoctorRemovalDto();
        builderCommonOperation.fillPigEventCommonInput(remove, moveBasicData, pigRawEvent);

        remove.setRemovalDate(event.getEventAt());
        //变动类型, 变动原因
        DoctorBasic changeType = basicMap.get(DoctorBasic.Type.CHANGE_TYPE.getValue()).get(event.getChgType());
        remove.setChgTypeId(changeType == null ? null : changeType.getId());
        remove.setChgTypeName(event.getChgType());
        DoctorChangeReason reason = changeReasonMap.get(event.getChgReason());
        remove.setChgReasonId(reason == null ? null : reason.getId());
        remove.setChgReasonName(event.getChgReason());

        //重量 金额等
        remove.setWeight(event.getEventWeight());
        remove.setPrice(event.getPrice());
        remove.setSum(event.getAmount());
        remove.setRemark(event.getRemark());

        //猪舍 客户
        DoctorBarn barn = barnMap.get(event.getBarnOutId());
        remove.setToBarnId(barn == null ? null : barn.getId());
        DoctorCustomer customer = customerMap.get(event.getCustomer());
        remove.setCustomerId(customer == null ? null : customer.getId());
        return remove;
    }
}
