package io.terminus.doctor.event.handler.sow;

import com.google.common.collect.Maps;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventFlowHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.workflow.core.Execution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

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

    @Autowired
    public DoctorSowChgLocationHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao,
                                       DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao,
                                       DoctorRevertLogDao doctorRevertLogDao, DoctorBarnReadService doctorBarnReadService) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao);
        this.doctorBarnReadService = doctorBarnReadService;
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(Execution execution,DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra) {
        doctorPigTrack.addAllExtraMap(extra);

        Long toBarnId = (Long) extra.get("chgLocationToBarnId");
        DoctorBarn doctorToBarn = RespHelper.orServEx(doctorBarnReadService.findBarnById(toBarnId));
        checkState(!isNull(toBarnId), "input.toBarnId.fail");

        doctorPigTrack.setCurrentBarnId(toBarnId);
        doctorPigTrack.setCurrentBarnName(doctorToBarn.getName());
        doctorPigTrack.setUpdatorId(basic.getStaffId());
        doctorPigTrack.setUpdatorName(basic.getStaffName());

        // 修改对应的状态信息
        if(Objects.equals(basic.getEventType(), PigEvent.TO_MATING.getKey())){
            doctorPigTrack.setStatus(PigStatus.Entry.getKey());

            //清空对应的Map 信息内容 （有一次生产过程）
            doctorPigTrack.setExtraMap(Maps.newHashMap());

            // 断奶后添加对应的胎次信息
            if(Objects.equals(doctorPigTrack.getStatus(), PigStatus.Wean.getKey())){
                // 断奶进入配种
                doctorPigTrack.setCurrentParity(doctorPigTrack.getCurrentParity() + 1);
            }
        }else if(Objects.equals(basic.getEventType(), PigEvent.TO_PREG.getKey())){
            // 状态妊娠检查相关， 而不是转舍相关
        }else if(Objects.equals(basic.getEventType(), PigEvent.TO_FARROWING.getKey())){
            doctorPigTrack.setStatus(PigStatus.Farrow.getKey());
        }
        return doctorPigTrack;
    }
}
