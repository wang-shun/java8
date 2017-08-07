package io.terminus.doctor.move.builder.pig;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.enums.MatingType;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.move.model.View_EventListSow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by xjn on 17/8/4.
 * 配种
 */
@Component
public class DoctorMateInputBuilder implements DoctorPigEventInputBuilder {
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;

    @Override
    public BasePigEventInputDto buildPigEventInputFromMove(DoctorMoveBasicData moveBasicData,
                                                           View_EventListPig pigRawEvent) {
        View_EventListSow event = (View_EventListSow) pigRawEvent;
        Map<String, Long> subMap = moveBasicData.getSubMap();
        Map<String, DoctorPig> boarMap = moveBasicData.getBoarMap();

        DoctorMatingDto mating = new DoctorMatingDto();
        builderCommonOperation.fillPigEventCommonInputFromMove(mating, moveBasicData, pigRawEvent);

        mating.setMatingDate(event.getEventAt());                 // 配种日期
        mating.setOperatorName(event.getStaffName());             // 配种人员
        mating.setOperatorId(subMap.get(event.getStaffName()));
        mating.setMattingMark(event.getRemark());                  // 配种mark
        mating.setJudgePregDate(event.getFarrowDate());            //预产日期

        // 配种类型
        MatingType type = MatingType.from(event.getServiceType());
        mating.setMatingType(type == null ? null : type.getKey());

        //配种公猪
        DoctorPig matingPig = boarMap.get(event.getBoarCode());
        mating.setMatingBoarPigId(matingPig == null ? null : matingPig.getId());
        mating.setMatingBoarPigCode(event.getBoarCode());
        return mating;
    }
}
