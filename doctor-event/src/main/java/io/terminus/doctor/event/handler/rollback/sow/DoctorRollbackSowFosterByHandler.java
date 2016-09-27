package io.terminus.doctor.event.handler.rollback.sow;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.dto.DoctorPigSnapShotInfo;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFostersDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.rollback.DoctorAbstractRollbackPigEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Desc: 被拼窝事件回滚
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/9/26
 */
@Slf4j
@Component
public class DoctorRollbackSowFosterByHandler extends DoctorAbstractRollbackPigEventHandler {

    @Override
    protected boolean handleCheck(DoctorPigEvent pigEvent) {
        return Objects.equals(pigEvent.getType(), PigEvent.FOSTERS_BY.getKey());
    }

    @Override
    protected void handleRollback(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        DoctorPigSnapshot snapshot = doctorPigSnapshotDao.queryByEventId(pigEvent.getId());
        DoctorPigTrack oldTrack = JSON_MAPPER.fromJson(snapshot.getPigInfo(), DoctorPigSnapShotInfo.class).getPigTrack();

        //如果状态不是不如，说明状态有变化，调用状态回滚
        if (!Objects.equals(oldTrack.getStatus(), PigStatus.FEED.getKey())) {
            handleRollbackWithStatus(pigEvent, operatorId, operatorName);
        }
        handleRollbackWithoutStatus(pigEvent, operatorId, operatorName);
    }

    @Override
    protected List<DoctorRollbackDto> handleReport(DoctorPigEvent pigEvent) {
        //被拼窝：猪舍，猪，猪群搜索，存栏日报，存栏月报
        DoctorFostersDto fostersDto = JSON_MAPPER.fromJson(pigEvent.getExtra(), DoctorFostersDto.class);
        DoctorPigTrack fosterByTrack = doctorPigTrackDao.findByPigId(fostersDto.getFosterSowId());
        DoctorRollbackDto fosterBy = new DoctorRollbackDto();
        fosterBy.setOrgId(pigEvent.getOrgId());
        fosterBy.setFarmId(pigEvent.getFarmId());
        fosterBy.setEventAt(pigEvent.getEventAt());
        fosterBy.setEsBarnId(pigEvent.getBarnId());
        fosterBy.setEsPigId(pigEvent.getPigId());
        fosterBy.setEsGroupId(fosterByTrack.getGroupId());
        fosterBy.setRollbackTypes(Lists.newArrayList(RollbackType.SEARCH_BARN, RollbackType.SEARCH_PIG,
                RollbackType.SEARCH_GROUP, RollbackType.DAILY_LIVESTOCK, RollbackType.MONTHLY_REPORT));
        return Lists.newArrayList(fosterBy);
    }
}
