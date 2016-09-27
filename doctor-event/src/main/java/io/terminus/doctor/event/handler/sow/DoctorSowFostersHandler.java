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
        checkState(Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey()), "foster.currentSowStatus.error");

        //添加当前母猪的健崽猪的数量信息
        Integer unweanCount = MoreObjects.firstNonNull(doctorPigTrack.getUnweanQty(), 0);
        Integer fosterCount = (Integer) extra.get("fostersCount");
        checkState(unweanCount >= fosterCount, "create.fostersBy.notEnough");

        doctorPigTrack.setUnweanQty(unweanCount - fosterCount);  //未断奶数
        doctorPigTrack.setWeanQty(MoreObjects.firstNonNull(doctorPigTrack.getWeanQty(), 0)); //断奶数不变
        extra.put("farrowingLiveCount", doctorPigTrack.getUnweanQty());
        doctorPigTrack.addAllExtraMap(extra);


        //全部断奶后, 初始化所有本次哺乳的信息
        if (doctorPigTrack.getUnweanQty() == 0) {
            doctorPigTrack.setStatus(PigStatus.Wean.getKey());
            doctorPigTrack.setGroupId(-1L);  //groupId = -1 置成 NULL
            doctorPigTrack.setFarrowAvgWeight(0D); //分娩均重(kg)
            doctorPigTrack.setFarrowQty(0);  //分娩数 0
            doctorPigTrack.setWeanAvgWeight(0D);
        } else {
            doctorPigTrack.setStatus(PigStatus.FEED.getKey());
        }
        execution.getExpression().put("leftCount", doctorPigTrack.getUnweanQty());
        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }
}
