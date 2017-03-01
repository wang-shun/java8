package io.terminus.doctor.event.handler.rollback.sow;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFostersDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.handler.rollback.group.DoctorRollbackGroupTransHandler;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Desc: 拼窝事件回滚
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/9/26
 */
@Slf4j
@Component
public class DoctorRollbackSowFosterHandler extends DoctorAbstractRollbackPigEventHandler {

    @Autowired
    private DoctorRollbackGroupTransHandler doctorRollbackGroupTransHandler;

    @Autowired
    private DoctorRollbackSowFosterByHandler doctorRollbackSowFosterByHandler;

    @Override
    protected boolean handleCheck(DoctorPigEvent pigEvent) {
        if (!Objects.equals(pigEvent.getType(), PigEvent.FOSTERS.getKey())) {
            return false;
        }
        //判断母猪被拼窝事件是最新事件 && 判断仔猪转群事件之后的仔猪转入是否是最新事件
        DoctorPigEvent toPigEvent = doctorPigEventDao.findByRelPigEventId(pigEvent.getId());
        expectTrue(notNull(toPigEvent), "relate.pig.event.not.null" , pigEvent.getId());
        if (!Objects.equals(pigEvent.getBarnId(), toPigEvent.getBarnId())) {
            //查找由被拼窝触发的转群事件及其关联事件
            DoctorGroupEvent toGroupEvent = doctorGroupEventDao.findByRelPigEventId(toPigEvent.getId());
            expectTrue(notNull(toGroupEvent), "relate.group.event.not.null" , pigEvent.getId());
            return isRelLastGroupEvent(toGroupEvent);
        }
        return true;
    }

    @Override
    protected void handleRollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        DoctorPigEvent toPigEvent = doctorPigEventDao.findByRelPigEventId(pigEvent.getId());


        //1. 不同猪舍是回滚仔猪转群
        if (!Objects.equals(pigEvent.getBarnId(), toPigEvent.getBarnId())) {
            DoctorGroupEvent toGroupEvent = doctorGroupEventDao.findByRelPigEventId(toPigEvent.getId());
            doctorRollbackGroupTransHandler.rollback(toGroupEvent, operatorId, operatorName);
        }

        //2. 被拼窝
        doctorRollbackSowFosterByHandler.rollback(toPigEvent, operatorId, operatorName);

        //3. 拼窝
        handleRollbackWithoutStatus(pigEvent, operatorId, operatorName);
    }

    @Override
        public List<DoctorRollbackDto> updateReport(DoctorPigEvent pigEvent) {
        //拼窝：猪舍，猪，猪群搜索
        DoctorPigTrack fosterTrack = doctorPigTrackDao.findByPigId(pigEvent.getPigId());
        DoctorRollbackDto foster = new DoctorRollbackDto();
        foster.setOrgId(pigEvent.getOrgId());
        foster.setFarmId(pigEvent.getFarmId());
        foster.setEventAt(pigEvent.getEventAt());
        foster.setEsBarnId(pigEvent.getBarnId());
        foster.setEsPigId(pigEvent.getPigId());
        foster.setEsGroupId(fosterTrack.getGroupId());
        foster.setRollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN, RollbackType.SEARCH_PIG, RollbackType.SEARCH_GROUP));

        //被拼窝：猪舍，猪，猪群搜索，存栏日报，存栏月报
        DoctorFostersDto fostersDto = JSON_MAPPER.fromJson(pigEvent.getExtra(), DoctorFostersDto.class);
        DoctorPigTrack fosterByTrack = doctorPigTrackDao.findByPigId(fostersDto.getFosterSowId());
        DoctorRollbackDto fosterBy = new DoctorRollbackDto();
        fosterBy.setOrgId(pigEvent.getOrgId());
        fosterBy.setFarmId(pigEvent.getFarmId());
        fosterBy.setEventAt(pigEvent.getEventAt());
        fosterBy.setEsBarnId(fosterByTrack.getCurrentBarnId());
        fosterBy.setEsPigId(fostersDto.getFosterSowId());
        fosterBy.setEsGroupId(fosterByTrack.getGroupId());
        fosterBy.setRollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN, RollbackType.SEARCH_PIG,
                RollbackType.SEARCH_GROUP, RollbackType.DAILY_LIVESTOCK, RollbackType.MONTHLY_REPORT));
        return Lists.newArrayList(foster, fosterBy);
    }
}
