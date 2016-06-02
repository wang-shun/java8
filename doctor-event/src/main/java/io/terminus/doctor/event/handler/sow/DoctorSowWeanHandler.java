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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
public class DoctorSowWeanHandler extends DoctorAbstractEventFlowHandler{

    @Autowired
    public DoctorSowWeanHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao, DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao, DoctorRevertLogDao doctorRevertLogDao) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao);
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra) {
        // 校验断奶的数量信息
        Map<String,Object> extraMap = doctorPigTrack.getExtraMap();
        Integer healthCount = (Integer) extraMap.get("healthCount");
        Integer toWeanCount = (Integer)extra.get("partWeanPigletsCount");
        Double weanAvgWeight = (Double)extra.get("partWeanAvgWeight");

        // 历史数据修改信息内容
        if(extraMap.containsKey("partWeanPigletsCount")){
            // 已经包含断奶信息
            toWeanCount += (Integer) extraMap.get("partWeanPigletsCount");
            weanAvgWeight += (Double) extraMap.get("partWeanAvgWeight");
            weanAvgWeight = weanAvgWeight/2;
        }
        checkState(toWeanCount<= healthCount, "wean.countInput.error");

        // update info
        extra.put("partWeanPigletsCount", toWeanCount);
        extra.put("partWeanAvgWeight", weanAvgWeight);
        doctorPigTrack.addAllExtraMap(extra);

        if(Objects.equals(toWeanCount, healthCount)){
            doctorPigTrack.setStatus(PigStatus.Wean.getKey());
        }
        return doctorPigTrack;
    }
}
