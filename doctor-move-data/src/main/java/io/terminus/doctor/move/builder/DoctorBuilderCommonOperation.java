package io.terminus.doctor.move.builder;

import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.move.dto.DoctorImportBasicData;
import io.terminus.doctor.move.dto.DoctorImportGroupEvent;
import io.terminus.doctor.move.dto.DoctorImportPigEvent;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListGain;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by xjn on 17/8/7.
 * 构建过程中用到的通用方法
 */
@Slf4j
@Component
public class DoctorBuilderCommonOperation {
    @Autowired
    private DoctorPigDao doctorPigDao;

    public void fillPigEventCommonInput(BasePigEventInputDto inputDto,
                                        DoctorMoveBasicData moveBasicData,
                                        View_EventListPig rawPigEvent) {

        //事件发生猪舍
        Map<String, DoctorBarn> barnMap = moveBasicData.getBarnMap();
        DoctorBarn barn = barnMap.get(rawPigEvent.getBarnOutId());
        // TODO: 17/8/8 临时操作
        if (isNull(barn)) {
//            throw InvalidException();
            barn = moveBasicData.getBarnMap().get("4f5d0051-a4c4-4bfb-9a9e-d1c3e1317c45");
        }

        inputDto.setBarnId(barn.getId());
        inputDto.setBarnName(barn.getName());
        inputDto.setBarnType(barn.getPigType());

        //事件
        PigEvent pigEvent = PigEvent.from(rawPigEvent.getEventName());
        inputDto.setEventName(pigEvent.getName());
        inputDto.setEventDesc(pigEvent.getDesc());
        inputDto.setEventType(pigEvent.getKey());
        inputDto.setEventSource(SourceType.MOVE.getValue());
        inputDto.setOutId(rawPigEvent.getEventOutId());

        //猪
        if (!Objects.equals(pigEvent.getKey(), PigEvent.ENTRY.getKey())) {
            DoctorFarm farm = moveBasicData.getDoctorFarm();
            DoctorPig pig = doctorPigDao.findPigByFarmAndOutId(farm.getId(),
                    rawPigEvent.getPigOutId());
            inputDto.setPigId(pig.getId());
        }
        inputDto.setPigCode(rawPigEvent.getPigCode());
        inputDto.setPigType(rawPigEvent.getPigSex());
    }

    public void fillGroupEventCommonInput(BaseGroupInput groupInput,
                                          View_EventListGain rawGroupEvent) {
        groupInput.setEventAt(DateUtil.toDateString(rawGroupEvent.getEventAt()));
        GroupEventType groupEventType = GroupEventType.from(rawGroupEvent.getEventTypeName());
        groupInput.setEventType(groupEventType.getValue());
        groupInput.setRemark(rawGroupEvent.getRemark());
        groupInput.setEventSource(SourceType.MOVE.getValue());
        groupInput.setOutId(rawGroupEvent.getGroupEventOutId());
        groupInput.setIsAuto(rawGroupEvent.getIsAuto());
    }

    public void fillPigEventCommonInput(BasePigEventInputDto inputDto,
                                        DoctorImportBasicData importBasicData,
                                        DoctorImportPigEvent importPigEvent) {
        //事件发生猪舍
        Map<String, DoctorBarn> barnMap = importBasicData.getBarnMap();
        DoctorBarn barn = barnMap.get(importPigEvent.getBarnName());
        inputDto.setBarnId(barn.getId());
        inputDto.setBarnName(barn.getName());
        inputDto.setBarnType(barn.getPigType());

        //事件
        PigEvent pigEvent = PigEvent.from(importPigEvent.getEventName());
        expectTrue(notNull(pigEvent), "pigEvent.not.fund", importPigEvent.getEventName());
        inputDto.setEventName(pigEvent.getName());
        inputDto.setEventDesc(pigEvent.getDesc());
        inputDto.setEventType(pigEvent.getKey());
        inputDto.setEventSource(SourceType.IMPORT.getValue());

        //猪
        DoctorPig.PigSex pigSex = DoctorPig.PigSex.from(importPigEvent.getPigSex());
        expectTrue(notNull(pigSex), "pigSex.not.fund", importPigEvent.getPigSex());
        if (!Objects.equals(pigEvent.getKey(), PigEvent.ENTRY.getKey())) {
            DoctorFarm farm = importBasicData.getDoctorFarm();
            DoctorPig pig = doctorPigDao.findPigByFarmIdAndPigCodeAndSex(farm.getId()
                    , importPigEvent.getPigCode(), pigSex.getKey());
            inputDto.setPigId(pig.getId());
        }
        inputDto.setPigCode(importPigEvent.getPigCode());
        inputDto.setPigType(pigSex.getKey());
    }

    public void fillGroupEventCommonInput(BaseGroupInput groupInput,
                                          DoctorImportGroupEvent importGroupEvent) {
        groupInput.setEventAt(DateUtil.toDateString(importGroupEvent.getEventAt()));
        GroupEventType groupEventType = GroupEventType.from(importGroupEvent.getEventName());
        expectTrue(notNull(groupEventType), "groupEventType");
        groupInput.setEventType(groupEventType.getValue());
        groupInput.setRemark(importGroupEvent.getRemark());
        groupInput.setEventSource(SourceType.IMPORT.getValue());
        groupInput.setIsAuto(IsOrNot.NO.getValue());
    }

}
