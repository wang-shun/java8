package io.terminus.doctor.event.handler.usual;

import com.google.common.base.Preconditions;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
@Slf4j
public class DoctorChgFarmHandler extends DoctorAbstractEventHandler{

    private final DoctorBarnReadService doctorBarnReadService;

    @Autowired
    public DoctorChgFarmHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao,
                                DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao,
                                DoctorRevertLogDao doctorRevertLogDao,
                                DoctorBarnReadService doctorBarnReadService) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao);
        this.doctorBarnReadService = doctorBarnReadService;
    }

    @Override
    public Boolean preHandler(DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) throws RuntimeException {
        return Objects.equals(basic.getEventType(), PigEvent.CHG_FARM.getKey());
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String,Object> context) {

        // 当前状态为哺乳状态的母猪不允许转
        Preconditions.checkState(!Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey()), "feedStatus.sowChangFarm.error");

        // 校验转相同的猪舍
        DoctorBarn doctorCurrentBarn = RespHelper.orServEx(doctorBarnReadService.findBarnById(doctorPigTrack.getCurrentBarnId()));
        DoctorBarn doctorToBarn = RespHelper.orServEx(doctorBarnReadService.findBarnById(Long.valueOf(extra.get("toBarnId").toString())));
        Preconditions.checkState(Objects.equals(doctorCurrentBarn.getPigType(), doctorToBarn.getPigType()), "sowChgFarm.diffBarn.error");

        // update barn info
        doctorPigTrack.setFarmId(Long.valueOf(extra.get("toFarmId").toString()));
        doctorPigTrack.setCurrentBarnId(doctorToBarn.getId());
        doctorPigTrack.setCurrentBarnName(doctorToBarn.getName());

        doctorPigTrack.addAllExtraMap(extra);
        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }

    @Override
    protected void afterEventCreateHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack,
                                          DoctorPigSnapshot doctorPigSnapshot, Map<String, Object> extra) {

        // doctor pig update farm info
        DoctorPig doctorPig = doctorPigDao.findById(doctorPigTrack.getPigId());
        doctorPig.setFarmId(Long.valueOf(extra.get("toFarmId").toString()));
        doctorPig.setFarmName(extra.get("toFarmName").toString());
        doctorPigDao.update(doctorPig);
    }
}
