package io.terminus.doctor.move.builder.group;

import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListGain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static io.terminus.common.utils.Arguments.isNull;

/**
 * Created by xjn on 17/8/9.
 * 新建
 */
@Slf4j
@Component
public class DoctorNewEventInputBuilder implements DoctorGroupEventInputBuilder{
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;
    @Override
    public BaseGroupInput buildFromMove(DoctorMoveBasicData moveBasicData,
                                        View_EventListGain groupRawEvent) {
        Map<Integer, Map<String, DoctorBasic>> basicMap = moveBasicData.getBasicMap();
        Map<String, DoctorBarn> barnMap = moveBasicData.getBarnMap();

        DoctorNewGroupInput newEvent = new DoctorNewGroupInput();
        builderCommonOperation.fillGroupEventCommonInputFromMove(newEvent, groupRawEvent);
        newEvent.setFarmId(moveBasicData.getDoctorFarm().getId());
        newEvent.setGroupCode(groupRawEvent.getGroupCode());
        DoctorGroupTrack.Sex sex = DoctorGroupTrack.Sex.from(groupRawEvent.getSexName());
        newEvent.setSex(isNull(sex) ? null : sex.getValue());
        PigSource source = PigSource.from(groupRawEvent.getSource());
        newEvent.setSource(isNull(source) ? null : source.getKey());
        DoctorBasic breed = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(groupRawEvent.getBreed());
        newEvent.setBreedId(breed == null ? null : breed.getId());
        newEvent.setBreedName(groupRawEvent.getBreed());

        DoctorBarn barn = barnMap.get(groupRawEvent.getToBarnOutId());
        if (isNull(barn)) {
            // TODO: 17/8/23 临时操作 
//            throw InvalidException();
            barn = moveBasicData.getBarnMap().get("0072b170-13a7-4334-bd22-219d8cf39a82");
        }
        newEvent.setBarnId(barn.getId());
        newEvent.setBarnName(barn.getName());
        newEvent.setGroupOutId(groupRawEvent.getGroupOutId());
        return newEvent;
    }
}
