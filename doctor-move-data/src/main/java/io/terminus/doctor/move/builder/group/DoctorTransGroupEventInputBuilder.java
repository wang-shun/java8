package io.terminus.doctor.move.builder.group;

import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
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
 * 转群
 */
@Slf4j
@Component
public class DoctorTransGroupEventInputBuilder implements DoctorGroupEventInputBuilder{
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;

    @Override
    public BaseGroupInput buildFromMove(DoctorMoveBasicData moveBasicData, View_EventListGain groupRawEvent) {
        Map<String, DoctorBarn> barnMap = moveBasicData.getBarnMap();
        Map<Integer, Map<String, DoctorBasic>> basicMap = moveBasicData.getBasicMap();
        Map<String, DoctorGroup> groupMap = moveBasicData.getGroupMap();

        DoctorTransGroupInput transGroup = new DoctorTransGroupInput();
        builderCommonOperation.fillGroupEventCommonInput(transGroup, groupRawEvent);

        //来源猪舍 猪群
        DoctorGroup fromGroup = groupMap.get(groupRawEvent.getGroupOutId());
        transGroup.setFromBarnId(fromGroup.getCurrentBarnId());
        transGroup.setFromBarnName(fromGroup.getCurrentBarnName());
        transGroup.setFromGroupId(fromGroup.getId());
        transGroup.setFromGroupCode(fromGroup.getGroupCode());

        //ChgReason=群间转移， 说明这是转群事件！ Treament: 转入猪舍， OutDest: 转入猪群
        if ("群间转移".equals(groupRawEvent.getChangeReasonName())) {
            DoctorBarn barn = barnMap.get(groupRawEvent.getContext()); //就是TreatMent
            if (barn != null) {
                transGroup.setToBarnId(barn.getId());
                transGroup.setToBarnName(barn.getName());
                transGroup.setToBarnType(barn.getPigType());
            }
            DoctorGroup toGroup = groupMap.get(groupRawEvent.getToBarnOutId()); //就是 OutDestination
            if (toGroup != null) {
                transGroup.setToGroupId(toGroup.getId());
                transGroup.setToGroupCode(toGroup.getGroupCode());
            }
        } else {
            DoctorBarn barn = barnMap.get(groupRawEvent.getToBarnOutId()); // 注意和 "群间转移" 区别
            if (barn != null) {
                transGroup.setToBarnId(barn.getId());
                transGroup.setToBarnName(barn.getName());
                transGroup.setToBarnType(barn.getPigType());
            }
            DoctorGroup toGroup = groupMap.get(groupRawEvent.getToGroupOutId());
            if (toGroup != null) {
                transGroup.setToGroupId(toGroup.getId());
                transGroup.setToGroupCode(toGroup.getGroupCode());
            }
            //是否新建猪群 Treatment
            IsOrNot is = IsOrNot.from(groupRawEvent.getContext());
            transGroup.setIsCreateGroup(is == null ? null : is.getValue());
        }

        //品种
        DoctorBasic basic = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(groupRawEvent.getBreed());
        transGroup.setBreedId(basic == null ? null : basic.getId());
        transGroup.setBreedName(groupRawEvent.getBreed());

        transGroup.setBoarQty(groupRawEvent.getBoarQty());
        transGroup.setSowQty(groupRawEvent.getSowQty());
        transGroup.setQuantity(groupRawEvent.getQuantity());
        transGroup.setAvgWeight(groupRawEvent.getAvgWeight());
        transGroup.setSowEvent(Objects.equals(transGroup.getIsAuto(), IsOrNot.YES.getValue()));
        return transGroup;
    }
}
