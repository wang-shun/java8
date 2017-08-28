package io.terminus.doctor.move.builder.group;

import com.google.common.base.Strings;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.enums.InType;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorImportBasicData;
import io.terminus.doctor.move.dto.DoctorImportGroupEvent;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListGain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by xjn on 17/8/9.
 * 转入
 */
@Slf4j
@Component
public class DoctorMoveInEventInputBuilder implements DoctorGroupEventInputBuilder{
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;

    @Override
    public BaseGroupInput buildFromMove(DoctorMoveBasicData moveBasicData,
                                        View_EventListGain groupRawEvent) {
        Map<Integer, Map<String, DoctorBasic>> basicMap = moveBasicData.getBasicMap();
        Map<String, DoctorGroup> groupMap = moveBasicData.getGroupMap();

        DoctorMoveInGroupInput moveIn = new DoctorMoveInGroupInput();
        builderCommonOperation.fillGroupEventCommonInput(moveIn, groupRawEvent);
        InType inType = InType.from(groupRawEvent.getInTypeName());
        moveIn.setInType(inType == null ? null : inType.getValue());
        moveIn.setInTypeName(groupRawEvent.getInTypeName());

        //来源
        PigSource source = PigSource.from(groupRawEvent.getSource());
        moveIn.setSource(source == null ? null : source.getKey());
        moveIn.setSex(DoctorGroupTrack.Sex.MIX.getValue());

        //品种
        DoctorBasic basic = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(groupRawEvent.getBreed());
        moveIn.setBreedId(basic == null ? null : basic.getId());
        moveIn.setBreedName(groupRawEvent.getBreed());



        //数量
        moveIn.setQuantity(groupRawEvent.getQuantity());
        moveIn.setBoarQty(groupRawEvent.getBoarQty());
        moveIn.setSowQty(groupRawEvent.getSowQty());

        moveIn.setAvgWeight(groupRawEvent.getAvgWeight());
        moveIn.setAvgDayAge(groupRawEvent.getAvgDayAge());

        //原系统没有字段可以表明转入事件,是未断奶仔猪,只能通过事件详情判断了
        if (notNull(groupRawEvent.getEventDesc())
                && groupRawEvent.getEventDesc().contains("母猪")) {
            moveIn.setSowEvent(true);
        }
        //来源猪舍,猪群
        if (!Strings.isNullOrEmpty(groupRawEvent.getToGroupOutId())) {
            DoctorGroup fromGroup = groupMap.get(groupRawEvent.getToGroupOutId());
            if (notNull(fromGroup)) {
                moveIn.setFromBarnId(fromGroup.getCurrentBarnId());
                moveIn.setFromBarnName(fromGroup.getCurrentBarnName());
                moveIn.setFromBarnType(fromGroup.getPigType());
                moveIn.setFromGroupId(fromGroup.getId());
                moveIn.setFromGroupCode(fromGroup.getGroupCode());
            }
        }


        return moveIn;
    }

    @Override
    public BaseGroupInput buildFromImport(DoctorImportBasicData importBasicData, DoctorImportGroupEvent importGroupEvent) {
        Map<String, Long> breedMap = importBasicData.getBreedMap();

        DoctorMoveInGroupInput moveIn = new DoctorMoveInGroupInput();
        builderCommonOperation.fillGroupEventCommonInput(moveIn, importGroupEvent);

        InType inType = InType.from(importGroupEvent.getInTypeName());
        expectTrue(notNull(inType), "inType");
        moveIn.setInType(inType.getValue());
        moveIn.setInTypeName(importGroupEvent.getInTypeName());

        //来源
        PigSource source = PigSource.from(importGroupEvent.getSource());
        expectTrue(notNull(source), "source");
        moveIn.setSource(source.getKey());

        DoctorGroupTrack.Sex sex = DoctorGroupTrack.Sex.from(importGroupEvent.getSexName());
        expectTrue(notNull(sex), "sex");
        moveIn.setSex(sex.getValue());

        //品种
        moveIn.setBreedId(breedMap.get(importGroupEvent.getBreedName()));
        moveIn.setBreedName(importGroupEvent.getBreedName());

        //数量、平均重量、平均日龄
        moveIn.setQuantity(importGroupEvent.getQuantity());
        moveIn.setAvgWeight(importGroupEvent.getAvgWeight());
        moveIn.setAvgDayAge(importGroupEvent.getAvgDayAge());

        return moveIn;
    }
}
