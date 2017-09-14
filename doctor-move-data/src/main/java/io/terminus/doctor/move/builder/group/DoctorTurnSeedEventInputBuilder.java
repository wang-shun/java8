package io.terminus.doctor.move.builder.group;

import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListGain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static io.terminus.common.utils.Arguments.isNull;

/**
 * Created by xjn on 17/9/3.
 * 转种猪
 */
@Slf4j
@Component
public class DoctorTurnSeedEventInputBuilder implements DoctorGroupEventInputBuilder{
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;

    @Override
    public BaseGroupInput buildFromMove(DoctorMoveBasicData moveBasicData,
                                        View_EventListGain groupRawEvent) {

        Map<String, DoctorBarn> barnMap = moveBasicData.getBarnMap();
        Map<Integer, Map<String, DoctorBasic>> basicMap = moveBasicData.getBasicMap();
        DoctorTurnSeedGroupInput turnSeed = new DoctorTurnSeedGroupInput();
        builderCommonOperation.fillGroupEventCommonInput(turnSeed, groupRawEvent);
        //转后的猪号
        turnSeed.setPigCode(groupRawEvent.getPigCode());

        //转的时间
        turnSeed.setBirthDate(DateUtil.toDateTimeString(groupRawEvent.getBirthDate()));

        //品种 品系 猪舍 性别
        DoctorBasic breed = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(groupRawEvent.getBreed());
        turnSeed.setBreedId(breed == null ? null : breed.getId());
        turnSeed.setBreedName(groupRawEvent.getBreed());

        DoctorBasic genetic = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(groupRawEvent.getSource()); //这里吗 source 为品系
        turnSeed.setGeneticId(genetic == null ? null : genetic.getId());
        turnSeed.setGeneticName(groupRawEvent.getSource());

        DoctorBarn barn = barnMap.get(groupRawEvent.getContext());
        barn = isNull(barn) ? moveBasicData.getDefaultPregBarn() : barn;
        turnSeed.setToBarnId(barn.getId());
        turnSeed.setToBarnName(barn.getName());
        turnSeed.setToBarnType(barn.getPigType());

        return turnSeed;
    }
}
