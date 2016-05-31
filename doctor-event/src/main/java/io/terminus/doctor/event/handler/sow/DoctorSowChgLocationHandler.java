package io.terminus.doctor.event.handler.sow;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.enums.SowStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventFlowHandler;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-31
 * Email:yaoqj@terminus.io
 * Descirbe: 对应的母猪状态信息流转(转舍)
 */
@Component
@Slf4j
public class DoctorSowChgLocationHandler extends DoctorAbstractEventFlowHandler {

    private final DoctorBarnReadService doctorBarnReadService;

    public DoctorSowChgLocationHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao,
                                       DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao,
                                       DoctorRevertLogDao doctorRevertLogDao, DoctorBarnReadService doctorBarnReadService) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao);
        this.doctorBarnReadService = doctorBarnReadService;
    }





    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra) {
        doctorPigTrack.setExtraMap(extra);
        Long fromBarnId = (Long) extra.get("chgLocationFromBarnId");
        Long toBarnId = (Long) extra.get("chgLocationToBarnId");
        return null;
    }

    // 通过转舍信息， 判定母猪状态信息
    private Integer judgeSowPigStatus(Long fromBarnId, Long toBarnId){
        Integer fromBarnType = RespHelper.orServEx(doctorBarnReadService.findBarnById(fromBarnId)).getPigType();
        Integer toBarnType = RespHelper.orServEx(doctorBarnReadService.findBarnById(toBarnId)).getPigType();
        if(Objects.equals(fromBarnType, PigType.MATE_SOW.getValue()) || Objects.equals(toBarnType, PigType.PREG_SOW.getValue())){
            return SowStatus.Hy.getKey();
        }
        return 0;
    }

}
