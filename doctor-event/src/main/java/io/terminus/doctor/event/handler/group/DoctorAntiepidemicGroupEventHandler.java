package io.terminus.doctor.event.handler.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorAntiepidemicGroupEvent;
import io.terminus.doctor.event.dto.event.group.edit.BaseGroupEdit;
import io.terminus.doctor.event.dto.event.group.edit.DoctorAntiepidemicGroupEdit;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Desc: 防疫事件处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class DoctorAntiepidemicGroupEventHandler extends DoctorAbstractGroupEventHandler {

    private final DoctorGroupEventDao doctorGroupEventDao;

    @Autowired
    public DoctorAntiepidemicGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                               DoctorGroupTrackDao doctorGroupTrackDao,
                                               CoreEventDispatcher coreEventDispatcher,
                                               DoctorGroupEventDao doctorGroupEventDao) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, coreEventDispatcher, doctorGroupEventDao);
        this.doctorGroupEventDao = doctorGroupEventDao;
    }

    @Override
    protected <I extends BaseGroupInput> void handleEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorAntiepidemicGroupInput antiepidemic = (DoctorAntiepidemicGroupInput) input;

        checkQuantity(groupTrack.getQuantity(), antiepidemic.getQuantity());
        //1.转换下防疫信息
        DoctorAntiepidemicGroupEvent antiEvent = BeanMapper.map(antiepidemic, DoctorAntiepidemicGroupEvent.class);

        //2.创建防疫事件
        DoctorGroupEvent<DoctorAntiepidemicGroupEvent> event = dozerGroupEvent(group, GroupEventType.ANTIEPIDEMIC, antiepidemic);
        event.setQuantity(antiepidemic.getQuantity());
        event.setExtraMap(antiEvent);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, event, groupTrack), GroupEventType.ANTIEPIDEMIC);
    }

    @Override
    protected <E extends BaseGroupEdit> void editEvent(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorGroupEvent event, E edit) {
        DoctorAntiepidemicGroupEdit antiEdit = (DoctorAntiepidemicGroupEdit) edit;

        //更新字段
        DoctorAntiepidemicGroupEvent antiEvent = JSON_MAPPER.fromJson(event.getExtra(), DoctorAntiepidemicGroupEvent.class);
        antiEvent.setVaccinResult(antiEdit.getVaccinResult());
        antiEvent.setVaccinItemId(antiEdit.getVaccinItemId());
        antiEvent.setVaccinItemName(antiEdit.getVaccinItemName());
        antiEvent.setVaccinStaffId(antiEdit.getVaccinStaffId());
        antiEvent.setVaccinStaffName(antiEdit.getVaccinStaffName());
        event.setRemark(antiEdit.getRemark());
        event.setExtraMap(antiEvent);

        doctorGroupEventDao.update(event);
    }
}
