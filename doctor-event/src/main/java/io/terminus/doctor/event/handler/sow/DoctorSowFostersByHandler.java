package io.terminus.doctor.event.handler.sow;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by yaoqijun.
 * Date:2016-06-08
 * Email:yaoqj@terminus.io
 * Descirbe:  被拼窝的母猪事件信息的录入
 */
@Component
@Slf4j
public class DoctorSowFostersByHandler extends DoctorAbstractEventFlowHandler{

    @Autowired
    public DoctorSowFostersByHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao,
                                     DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao,
                                     DoctorRevertLogDao doctorRevertLogDao) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao);
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(Execution execution, DoctorPigTrack doctorPigTrack,
                                                   DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) {

        // 校验当前的母猪状态 status 的存在方式
        Integer currentStatus = doctorPigTrack.getStatus();
        checkState(
                Objects.equals(currentStatus, PigStatus.FEED.getKey()), "foster.currentSowStatus.error");

        //添加当前母猪的健崽猪的数量信息
        Map<String,Object> extraMap = doctorPigTrack.getExtraMap();
        Integer healthCount = (Integer) extraMap.get("healthCount");
        Integer partWeanCount = extraMap.containsKey("partWeanPigletsCount") ? (Integer) extraMap.get("partWeanPigletsCount") : 0;
        Integer fosterCount= (Integer) extra.get("fostersCount");

        Integer afterHealthCount = healthCount - fosterCount;
        checkState(afterHealthCount >= partWeanCount, "create.fostersBy.notEnough");
        extra.put("healthCount", afterHealthCount);
        doctorPigTrack.addAllExtraMap(extra);

        // 修改当前的母猪状态信息
        if(Objects.equals(afterHealthCount, partWeanCount)){
            doctorPigTrack.setStatus(PigStatus.Wean.getKey());
        }else {
            doctorPigTrack.setStatus(PigStatus.FEED.getKey());
        }
        execution.getExpression().put("status", doctorPigTrack.getStatus());
        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }
}
