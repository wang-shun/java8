package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventFlowHandler;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.workflow.core.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe: 注意拼窝事件关联事件信息的处理方式
 */
@Component
public class DoctorSowFostersHandler extends DoctorAbstractEventFlowHandler {

    @Autowired
    public DoctorSowFostersHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao,
                                   DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao,
                                   DoctorRevertLogDao doctorRevertLogDao,
                                   DoctorBarnDao doctorBarnDao) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao, doctorBarnDao);
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(Execution execution, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) {

        // 校验当前的母猪状态 status 的存在方式
        Integer currentStatus = doctorPigTrack.getStatus();
        checkState(
                Objects.equals(currentStatus, PigStatus.FEED.getKey()), "foster.currentSowStatus.error");

        //添加当前母猪的健崽猪的数量信息
        Integer healthCount = MoreObjects.firstNonNull(doctorPigTrack.getUnweanQty(), 0);
        Integer fosterCount = (Integer) extra.get("fostersCount");

        Integer afterHealthCount = healthCount - fosterCount;
        checkState(afterHealthCount >= 0, "create.fostersBy.notEnough");
        extra.put("farrowingLiveCount", afterHealthCount);
        doctorPigTrack.addAllExtraMap(extra);

        doctorPigTrack.setUnweanQty(afterHealthCount);  //未断奶数

        // 修改当前的母猪状态信息
        if (afterHealthCount == 0) {
            doctorPigTrack.setStatus(PigStatus.Wean.getKey());
            doctorPigTrack.setGroupId(-1L);  //groupId = -1 置成 NULL
            doctorPigTrack.setFarrowQty(0);  //分娩数 0
        } else {
            doctorPigTrack.setStatus(PigStatus.FEED.getKey());
        }
        execution.getExpression().put("leftCount", afterHealthCount);
        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }
}
