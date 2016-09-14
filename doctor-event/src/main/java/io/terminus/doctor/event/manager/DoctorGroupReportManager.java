package io.terminus.doctor.event.manager;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    public DoctorGroupReportManager(DoctorPigTrackDao doctorPigTrackDao) {
        this.doctorPigTrackDao = doctorPigTrackDao;
    }

    /**
     * 产房仔猪, 更新下断奶统计数据
     * @param groupTrack 猪群跟踪
     * @param pigType 猪类
     * @return 猪群跟踪
     */
    public DoctorGroupTrack updateFarrowGroupTrack(DoctorGroupTrack groupTrack, Integer pigType) {
        if (!PigType.FARROW_TYPES.contains(pigType)) {
            return groupTrack;
        }

        double weanWeight = 0D;
        double birthWeight = 0D;
        int farrowQty = 0;
        int weakQty = 0;
        int unWeanQty = 0;
        int weanQty = 0;
        int unqQty = 0;

        List<DoctorPigTrack> pigTracks = doctorPigTrackDao.findFeedSowTrackByGroupId(groupTrack.getGroupId());
        for (DoctorPigTrack pigTrack : pigTracks) {
            Map<String, Object> extraMap = pigTrack.getExtraMap();
            weanWeight += MoreObjects.firstNonNull(pigTrack.getWeanAvgWeight(), 0D) * MoreObjects.firstNonNull(pigTrack.getWeanQty(), 0);
            birthWeight += MoreObjects.firstNonNull(pigTrack.getFarrowAvgWeight(), 0D) * MoreObjects.firstNonNull(pigTrack.getFarrowQty(), 0);
            farrowQty += MoreObjects.firstNonNull(pigTrack.getFarrowQty(), 0);
            weakQty += getIntFromExtra(extraMap, "weakCount");
            unWeanQty += MoreObjects.firstNonNull(pigTrack.getUnweanQty(), 0);
            weanQty += MoreObjects.firstNonNull(pigTrack.getWeanQty(), 0);
            unqQty += getIntFromExtra(extraMap, "notQualifiedCount");
        }

        groupTrack.setWeanAvgWeight(divide(weanWeight, weanQty));       //断奶均重kg
        groupTrack.setBirthAvgWeight(divide(birthWeight, farrowQty));  //出生均重kg
        groupTrack.setWeakQty(weakQty);      //弱仔数
        groupTrack.setUnweanQty(unWeanQty < groupTrack.getQuantity() ? unWeanQty : groupTrack.getQuantity());  //未断奶数
        groupTrack.setUnqQty(unqQty);        //不合格数
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
}
