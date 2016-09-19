package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
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
import io.terminus.doctor.event.model.DoctorPigSnapshot;
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
        checkState(Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey()), "not.feed.sow");

        //未断奶数
        Integer unweanCount = doctorPigTrack.getUnweanQty();    //未断奶数量
        Integer weanCount = doctorPigTrack.getWeanQty();        //断奶数量
        Integer toWeanCount = (Integer) extra.get("partWeanPigletsCount");
        checkState(toWeanCount <= unweanCount, "wean.countInput.error");
        doctorPigTrack.setUnweanQty(unweanCount - toWeanCount); //未断奶数减
        doctorPigTrack.setWeanQty(weanCount + toWeanCount);     //断奶数加

        //断奶均重
        Double toWeanAvgWeight = (Double) extra.get("partWeanAvgWeight");
        checkState(toWeanAvgWeight != null, "weight.not.null");
        Double weanAvgWeight = ((MoreObjects.firstNonNull(doctorPigTrack.getWeanAvgWeight(), 0D) * weanCount) + toWeanAvgWeight * toWeanCount ) / (weanCount + toWeanCount);
        doctorPigTrack.setWeanAvgWeight(weanAvgWeight);

        //更新extra字段
        extra.put("partWeanAvgWeight", weanAvgWeight);
        extra.put("partWeanPigletsCount", doctorPigTrack.getWeanQty());
        extra.put("farrowingLiveCount", doctorPigTrack.getUnweanQty());
        extra.put("hasWeanToMating", true);

        //不合格数 累加
        extra.put("notQualifiedCount", getIntFromExtra(doctorPigTrack.getExtraMap(), "notQualifiedCount") + getIntFromExtra(extra, "notQualifiedCount"));
        doctorPigTrack.addAllExtraMap(extra);

        //全部断奶后, 初始化所有本次哺乳的信息
        if (doctorPigTrack.getUnweanQty() == 0) {
            doctorPigTrack.setStatus(PigStatus.Wean.getKey());
            doctorPigTrack.setGroupId(-1L);  //groupId = -1 置成 NULL
            doctorPigTrack.setFarrowAvgWeight(0D);
            doctorPigTrack.setFarrowQty(0);  //分娩数 0
            doctorPigTrack.setWeanAvgWeight(0D);
        }

        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        execution.getExpression().put("leftCount", doctorPigTrack.getUnweanQty());
        return doctorPigTrack;
    }

    private static int getIntFromExtra(Map<String, Object> extraMap, String key) {
        try {
            return Integer.valueOf(String.valueOf(extraMap.get(key)));
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    protected void afterEventCreateHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, DoctorPigSnapshot doctorPigSnapshot, Map<String, Object> extra) {
        //触发一下修改猪群的事件
        Integer pigType = doctorBarnDao.findById(doctorPigTrack.getCurrentBarnId()).getPigType();
        updateFarrowGroupTrack(doctorPigTrack.getGroupId(), pigType);
    }
}
