package io.terminus.doctor.event.manager;

import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorAntiepidemicGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Slf4j
@Component
public class DoctorGroupManager {

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private final DoctorGroupDao doctorGroupDao;
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorGroupSnapshotDao doctorGroupSnapshotDao;
    private final DoctorGroupTrackDao doctorGroupTrackDao;

    @Autowired
    public DoctorGroupManager(DoctorGroupDao doctorGroupDao,
                                       DoctorGroupEventDao doctorGroupEventDao,
                                       DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                       DoctorGroupTrackDao doctorGroupTrackDao) {
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
    }

    /**
     * 新建猪群
     * @param group 猪群
     * @param groupEvent 猪群事件
     * @param groupTrack 猪群跟踪
     * @return 猪群id
     */
    @Transactional
    public Long createNewGroup(DoctorGroup group, DoctorGroupEvent groupEvent, DoctorGroupTrack groupTrack) {
        //1. 创建猪群
        doctorGroupDao.create(group);
        Long groupId = group.getId();

        //2. 创建新建猪群事件
        groupEvent.setGroupId(groupId);
        doctorGroupEventDao.create(groupEvent);

        //3. 创建猪群跟踪
        groupTrack.setGroupId(groupId);
        groupTrack.setRelEventId(groupEvent.getId());
        doctorGroupTrackDao.create(groupTrack);

        //4. 创建猪群镜像
        doctorGroupSnapshotDao.create(getCreateNewGroupSnapShot(group, groupEvent, groupTrack));
        return groupId;
    }

    /**
     * 防疫事件
     */
    @Transactional
    public void groupEventAntiepidemic(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorAntiepidemicGroupInput antiepidemic) {
        //1.转换下防疫信息
        DoctorAntiepidemicGroupEvent antiEvent = BeanMapper.map(antiepidemic, DoctorAntiepidemicGroupEvent.class);

        //2.创建防疫事件
        DoctorGroupEvent<DoctorAntiepidemicGroupEvent> event = new DoctorGroupEvent<>();
        // TODO: 16/5/27 dozer 下事件
        event.setEventAt(DateUtil.toDate(antiepidemic.getVaccinAt()));
        event.setExtraMap(antiEvent);

        //3.更新猪群跟踪
        groupTrack.setRelEventId(event.getId());
        doctorGroupTrackDao.update(groupTrack);
    }


    //创建新建猪群镜像信息
    private DoctorGroupSnapshot getCreateNewGroupSnapShot(DoctorGroup group, DoctorGroupEvent groupEvent, DoctorGroupTrack groupTrack) {
        DoctorGroupSnapshot groupSnapshot = new DoctorGroupSnapshot();
        groupSnapshot.setEventType(GroupEventType.NEW.getValue());  //新建猪群事件
        groupSnapshot.setToGroupId(group.getId());
        groupSnapshot.setToEventId(groupEvent.getId());
        groupSnapshot.setToInfo(JSON_MAPPER.toJson(DoctorGroupSnapShotInfo.builder()
                .group(group)
                .groupEvent(groupEvent)
                .groupTrack(groupTrack)
                .build()));
        return groupSnapshot;
    }
}
