package io.terminus.doctor.event.manager;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dao.DoctorGroupBatchSummaryDao;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.model.DoctorGroupBatchSummary;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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
    private final DoctorKpiDao doctorKpiDao;

    @Autowired
    public DoctorGroupReportManager(DoctorPigTrackDao doctorPigTrackDao,
                                    DoctorGroupBatchSummaryDao doctorGroupBatchSummaryDao,
                                    DoctorKpiDao doctorKpiDao) {
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorGroupBatchSummaryDao = doctorGroupBatchSummaryDao;
        this.doctorKpiDao = doctorKpiDao;
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
        groupTrack.setWeakQty(0);      //弱仔数
        groupTrack.setUnweanQty(doctorKpiDao.getGroupUnWean(groupTrack.getGroupId(), new Date()));  //未断奶数
        groupTrack.setWeanQty(doctorKpiDao.getGroupWean(groupTrack.getGroupId(), new Date()));  //未断奶数
        groupTrack.setUnqQty(0);        //合格数
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
