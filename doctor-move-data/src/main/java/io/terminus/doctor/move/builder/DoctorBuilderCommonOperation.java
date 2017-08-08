package io.terminus.doctor.move.builder;

import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.user.model.DoctorFarm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Created by xjn on 17/8/7.
 * 构建过程中用到的通用方法
 */
@Component
public class DoctorBuilderCommonOperation {
    @Autowired
    private DoctorPigDao doctorPigDao;

    public void fillPigEventCommonInputFromMove(BasePigEventInputDto inputDto,
                                                                DoctorMoveBasicData moveBasicData,
                                                                View_EventListPig rawPigEvent) {

        //事件发生猪舍
        Map<String, DoctorBarn> barnMap = moveBasicData.getBarnMap();
        DoctorBarn barn = barnMap.get(rawPigEvent.getBarnOutId());
        inputDto.setBarnId(barn.getId());
        inputDto.setBarnName(barn.getName());
        inputDto.setBarnType(barn.getPigType());

        //事件
        PigEvent pigEvent = PigEvent.from(rawPigEvent.getEventName());
        inputDto.setEventName(pigEvent.getName());
        inputDto.setEventDesc(pigEvent.getDesc());
        inputDto.setEventType(pigEvent.getKey());
        inputDto.setEventSource(SourceType.MOVE.getValue());

        //猪
        if (!Objects.equals(pigEvent.getKey(), PigEvent.ENTRY.getKey())) {
            DoctorFarm farm = moveBasicData.getDoctorFarm();
            DoctorPig pig = doctorPigDao.findPigByFarmIdAndPigCodeAndSex(farm.getId(), rawPigEvent.getPigCode(),
                    rawPigEvent.getPigSex());
            inputDto.setPigId(pig.getId());
        }
        inputDto.setPigCode(rawPigEvent.getPigCode());
    }
}
