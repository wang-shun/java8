package io.terminus.doctor.event.handler.sow;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventFlowHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.workflow.core.Execution;
import org.joda.time.DateTime;
import org.joda.time.Days;
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
public class DoctorSowWeanHandler extends DoctorAbstractEventFlowHandler {

    @Autowired
    public DoctorSowWeanHandler(DoctorPigDao doctorPigDao,
                                DoctorPigEventDao doctorPigEventDao,
                                DoctorPigTrackDao doctorPigTrackDao,
                                DoctorPigSnapshotDao doctorPigSnapshotDao,
                                DoctorRevertLogDao doctorRevertLogDao,
                                DoctorBarnDao doctorBarnDao) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao, doctorBarnDao);
    }

    @Override
    protected IsOrNot eventCreatePreHandler(Execution execution, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto, Map<String, Object> extra, Map<String, Object> context) {
        DoctorPigEvent lastFarrow = doctorPigEventDao.queryLastFarrowing(doctorPigTrack.getPigId());
        //分娩时间
        DateTime farrowingDate = new DateTime(Long.valueOf(lastFarrow.getExtraMap().get("farrowingDate").toString()));

        //断奶时间
        DateTime partWeanDate = new DateTime(Long.valueOf(extra.get("partWeanDate").toString()));
        doctorPigEvent.setPartweanDate(partWeanDate.toDate());

        //哺乳天数
        doctorPigEvent.setFeedDays(Math.abs(Days.daysBetween(farrowingDate, partWeanDate).getDays()));

        //断奶只数和断奶均重
        doctorPigEvent.setWeanCount(Ints.tryParse(Objects.toString(extra.get("partWeanPigletsCount"))));
        doctorPigEvent.setWeanAvgWeight(Doubles.tryParse(Objects.toString(extra.get("partWeanAvgWeight"))));
        return IsOrNot.NO;
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(Execution execution, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) {
        // 校验断奶的数量信息
        Map<String, Object> extraMap = doctorPigTrack.getExtraMap();
        Integer healthCount = (Integer) extraMap.get("farrowingLiveCount");
        Integer toWeanCount = (Integer) extra.get("partWeanPigletsCount");
        Double weanAvgWeight = (Double) extra.get("partWeanAvgWeight");

        // 历史数据修改信息内容
        if (extraMap.containsKey("partWeanPigletsCount")) {
            // 已经包含断奶信息
            toWeanCount += (Integer) extraMap.get("partWeanPigletsCount");
            weanAvgWeight += (Double) extraMap.get("partWeanAvgWeight");
            weanAvgWeight = weanAvgWeight / 2;
        }
        checkState(toWeanCount <= healthCount, "wean.countInput.error");

        // update info
        extra.put("partWeanPigletsCount", toWeanCount);
        extra.put("partWeanAvgWeight", weanAvgWeight);

        extra.put("weanToMate", true);
        doctorPigTrack.addAllExtraMap(extra);

        if (Objects.equals(toWeanCount, healthCount)) {
            doctorPigTrack.setStatus(PigStatus.Wean.getKey());
        }

        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        execution.getExpression().put("leftCount", (healthCount - toWeanCount));
        return doctorPigTrack;
    }
}
