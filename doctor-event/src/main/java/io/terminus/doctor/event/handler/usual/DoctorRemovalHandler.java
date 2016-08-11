package io.terminus.doctor.event.handler.usual;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.usual.DoctorRemovalDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
@Slf4j
public class DoctorRemovalHandler extends DoctorAbstractEventHandler{

    @Autowired
    public DoctorRemovalHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao, DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao, DoctorRevertLogDao doctorRevertLogDao) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao);
    }

    @Override
    public Boolean preHandler(DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) throws RuntimeException {
        return Objects.equals(basic.getEventType(), PigEvent.REMOVAL.getKey());
    }

    @Override
    public void handler(DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) throws RuntimeException {
        // create event info
        DoctorPigEvent doctorPigEvent = buildAllPigDoctorEvent(basic, extra);
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(doctorPigEvent.getPigId());

        //添加当前事件发生前猪的状态
        doctorPigEvent.setPigStatusBefore(doctorPigTrack.getStatus());
        //添加时间发生之前母猪的胎次
        doctorPigEvent.setParity(doctorPigTrack.getCurrentParity());
        doctorPigEventDao.create(doctorPigEvent);
        context.put("doctorPigEventId", doctorPigEvent.getId());

        createPigTrackSnapshot(doctorPigEvent, basic, extra, context);
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String,Object> context) {
        doctorPigTrack.addAllExtraMap(extra);
        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        if(Objects.equals(DoctorPig.PIG_TYPE.BOAR.getKey(), basic.getPigType())){
            doctorPigTrack.setStatus(PigStatus.BOAR_LEAVE.getKey());
        }else if(Objects.equals(DoctorPig.PIG_TYPE.SOW.getKey(), basic.getPigType())){
            doctorPigTrack.setStatus(PigStatus.Removal.getKey());
        }else {
            throw new IllegalStateException("basic.pigTypeValue.error");
        }
        doctorPigTrack.setIsRemoval(IsOrNot.YES.getValue());
        return doctorPigTrack;
    }

    @Override
    public void afterHandler(DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) throws RuntimeException {
        // 离场 事件 修改Pig 状态信息
        DoctorPig doctorPig = doctorPigDao.findById(basic.getPigId());
        checkState(!isNull(doctorPig), "input.doctorPigId.error");
        checkState(doctorPigDao.removalPig(doctorPig.getId()), "update.pigRemoval.fail");
    }

    @Override
    protected DoctorPigEvent buildAllPigDoctorEvent(DoctorBasicInputInfoDto basic, Map<String, Object> extra) {
        DoctorPigEvent pigEvent = super.buildAllPigDoctorEvent(basic, extra);
        DoctorRemovalDto removel = BeanMapper.map(extra, DoctorRemovalDto.class);
        pigEvent.setChangeTypeId(removel.getChgTypeId());   //变动类型id
        pigEvent.setPrice(removel.getPrice());      //销售单价(分)
        pigEvent.setAmount(removel.getSum());       //销售总额(分)
        return pigEvent;
    }
}
