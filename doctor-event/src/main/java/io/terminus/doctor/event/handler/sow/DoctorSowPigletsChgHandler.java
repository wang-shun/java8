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

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by yaoqijun.
 * Date:2016-06-01
 * Email:yaoqj@terminus.io
 * Descirbe: 仔猪变动事件 （仔猪转出信息的录入方式）
 */
public class DoctorSowPigletsChgHandler extends DoctorAbstractEventFlowHandler{

    public DoctorSowPigletsChgHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao,
                                      DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao,
                                      DoctorRevertLogDao doctorRevertLogDao) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao);
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(Execution execution, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra) {
        // 校验母猪的状态信息
        checkState(Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey()), "piglets.chgLocation.error");

        // 校验转出的数量信息
        Map<String,Object> extraMap = doctorPigTrack.getExtraMap();
        Integer healthPigCount = (Integer) extraMap.get("healthCount");
        Integer partWeanPigletsCount = 0;
        if(extraMap.containsKey("partWeanPigletsCount")){
            partWeanPigletsCount = (Integer) extraMap.get("partWeanPigletsCount");
        }
        Integer pigletsCount = (Integer) extra.get("pigletsCount"); //对应的仔猪变动的数量
        checkState((healthPigCount - partWeanPigletsCount)>= pigletsCount, "piglets.chgCount.error");

        healthPigCount = healthPigCount - pigletsCount; // 当前健康的数量
        extra.put("healthCount", healthPigCount);
        doctorPigTrack.addAllExtraMap(extra);

        return doctorPigTrack;
    }
}
