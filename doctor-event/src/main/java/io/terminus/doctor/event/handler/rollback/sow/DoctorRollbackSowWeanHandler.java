package io.terminus.doctor.event.handler.rollback.sow;

import com.google.common.collect.Lists;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.dto.event.sow.DoctorWeanDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorEventRelation;
import io.terminus.doctor.event.model.DoctorGroup;
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
 * Desc: 断奶事件回滚
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/9/26
 */
@Slf4j
@Component
public class DoctorRollbackSowWeanHandler extends DoctorAbstractRollbackPigEventHandler {

    @Autowired
    private DoctorRollbackSowChgLocationEventHandler doctorRollbackSowChgLocationEventHandler;
    @Autowired
    private DoctorRollbackSowToChgLocationEventHandler doctorRollbackSowToChgLocationEventHandler;

    @Override
    protected boolean handleCheck(DoctorPigEvent pigEvent) {
        if (!Objects.equals(pigEvent.getType(), PigEvent.WEAN.getKey())) {
            return false;
        }

        //断奶后如果转舍，判断转舍是否是最新事件
        DoctorWeanDto weanDto = JSON_MAPPER.fromJson(pigEvent.getExtra(), DoctorWeanDto.class);
        if (weanDto.getChgLocationToBarnId() != null) {
            Long toPigEventId = doctorEventRelationDao.findByOriginAndType(pigEvent.getId(), DoctorEventRelation.TargetType.PIG.getValue()).getTriggerEventId();
            DoctorPigEvent toPigEvent = doctorPigEventDao.findById(toPigEventId);

            expectTrue(notNull(toPigEvent), "relate.pig.event.not.null", pigEvent.getId());
            DoctorBarn doctorBarn = doctorBarnDao.findById(weanDto.getChgLocationToBarnId());
            if (Objects.equals(doctorBarn.getPigType(), PigType.MATE_SOW.getValue()) || Objects.equals(doctorBarn.getPigType(), PigType.PREG_SOW.getValue())) {
                return doctorRollbackSowToChgLocationEventHandler.handleCheck(toPigEvent);
            } else {
                return doctorRollbackSowChgLocationEventHandler.handleCheck(toPigEvent);
            }
        }
        return true;
    }

    @Override
    protected void handleRollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        //如果断奶后转舍， 先回滚转舍
        DoctorWeanDto weanDto = JSON_MAPPER.fromJson(pigEvent.getExtra(), DoctorWeanDto.class);
        if (weanDto.getChgLocationToBarnId() != null) {
            Long toPigEventId = doctorEventRelationDao.findByOriginAndType(pigEvent.getId(), DoctorEventRelation.TargetType.PIG.getValue()).getTriggerEventId();
            DoctorPigEvent toPigEvent = doctorPigEventDao.findById(toPigEventId);

            DoctorBarn doctorBarn = doctorBarnDao.findById(weanDto.getChgLocationToBarnId());
            if (Objects.equals(doctorBarn.getPigType(), PigType.MATE_SOW.getValue()) || Objects.equals(doctorBarn.getPigType(), PigType.PREG_SOW.getValue())){
                doctorRollbackSowToChgLocationEventHandler.rollback(toPigEvent, operatorId, operatorName);
            }else {
                doctorRollbackSowChgLocationEventHandler.rollback(toPigEvent, operatorId, operatorName);
            }
        }
        DoctorPigTrack pigTrack = doctorPigTrackDao.findByPigId(pigEvent.getPigId());

        //如果成功断奶，需要回滚状态
        if (!Objects.equals(pigTrack.getStatus(), PigStatus.FEED.getKey())) {
            handleRollbackWithStatus(pigEvent, operatorId, operatorName);
        } else {
            handleRollbackWithoutStatus(pigEvent, operatorId, operatorName);
        }
    }

    @Override
        public List<DoctorRollbackDto> updateReport(DoctorPigEvent pigEvent) {
        DoctorGroup group = doctorGroupDao.findByCurrentBarnId(pigEvent.getBarnId()).stream()
                .filter(g -> Objects.equals(g.getStatus(), DoctorGroup.Status.CREATED.getValue()))
                .findFirst()
                .orElse(null);
        DoctorRollbackDto fromDto = new DoctorRollbackDto();
        fromDto.setOrgId(pigEvent.getOrgId());
        fromDto.setFarmId(pigEvent.getFarmId());
        fromDto.setEventAt(pigEvent.getEventAt());
        fromDto.setEsBarnId(pigEvent.getBarnId());
        fromDto.setEsPigId(pigEvent.getPigId());
        if (group != null) {
            fromDto.setEsGroupId(group.getId());
        }

        //更新统计：猪舍统计，猪群统计, 猪统计, 断奶日报
        fromDto.setRollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN, RollbackType.SEARCH_GROUP,
                RollbackType.SEARCH_PIG, RollbackType.DAILY_WEAN));
        return Lists.newArrayList(fromDto);
    }
}
