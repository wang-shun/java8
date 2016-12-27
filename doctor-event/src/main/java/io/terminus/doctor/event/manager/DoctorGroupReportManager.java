package io.terminus.doctor.event.manager;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dao.DoctorGroupBatchSummaryDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.model.DoctorGroupBatchSummary;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/14
 */
@Slf4j
@Component
public class DoctorGroupReportManager {

    private final DoctorPigTrackDao doctorPigTrackDao;
    private final DoctorGroupBatchSummaryDao doctorGroupBatchSummaryDao;

    @Autowired
    public DoctorGroupReportManager(DoctorPigTrackDao doctorPigTrackDao,
                                    DoctorGroupBatchSummaryDao doctorGroupBatchSummaryDao) {
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorGroupBatchSummaryDao = doctorGroupBatchSummaryDao;
    }

    /**
     * 产房仔猪, 更新下断奶统计数据
     * @param groupTrack 猪群跟踪
     * @param pigType 猪类
     * @return 猪群跟踪
     */
    public DoctorGroupTrack updateGroupTrackReport(DoctorGroupTrack groupTrack, Integer pigType) {
        if (!PigType.FARROW_TYPES.contains(pigType)) {
            return groupTrack;
        }

        int weakQty = 0;
        int unWeanQty = 0;
        int unqQty = 0;

        List<DoctorPigTrack> pigTracks = doctorPigTrackDao.findFeedSowTrackByGroupId(groupTrack.getGroupId());
        for (DoctorPigTrack pigTrack : pigTracks) {
            Map<String, Object> extraMap = pigTrack.getExtraMap();
            weakQty += getIntFromExtra(extraMap, "weakCount");
            unWeanQty += MoreObjects.firstNonNull(pigTrack.getUnweanQty(), 0);
            unqQty += getIntFromExtra(extraMap, "qualifiedCount");
        }

        groupTrack.setWeakQty(weakQty);      //弱仔数
        groupTrack.setUnweanQty(unWeanQty < groupTrack.getQuantity() ? unWeanQty : groupTrack.getQuantity());  //未断奶数
        groupTrack.setUnqQty(unqQty);        //合格数
        return groupTrack;
    }

    private static double divide(double a, double b) {
        return a / (b <= 0D ? 1D : b);
    }

    private static int getIntFromExtra(Map<String, Object> extraMap, String key) {
        try {
            return Integer.valueOf(String.valueOf(extraMap.get(key)));
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 事务创建批次总结
     */
    @Transactional
    public void createGroupBatchSummary(DoctorGroupBatchSummary summary) {
        doctorGroupBatchSummaryDao.deleteByGroupId(summary.getGroupId());
        doctorGroupBatchSummaryDao.create(summary);
    }
}
