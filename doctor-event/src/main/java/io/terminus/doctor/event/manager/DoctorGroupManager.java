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
import io.terminus.doctor.event.dto.event.group.DoctorCloseGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorDiseaseGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorLiveStockGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
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
        createGroupSnapShot(group, groupEvent, groupTrack, GroupEventType.NEW);
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
        DoctorGroupEvent<DoctorAntiepidemicGroupEvent> event = dozerGroupEvent(group, GroupEventType.ANTIEPIDEMIC);
        event.setGroupId(group.getId());    //猪群id
        event.setEventAt(DateUtil.toDate(antiepidemic.getVaccinAt()));
        event.setExtraMap(antiEvent);
        event.setCreatorId(antiepidemic.getCreatorId());
        event.setCreatorName(antiepidemic.getCreatorName());
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        groupTrack.setRelEventId(event.getId());
        doctorGroupTrackDao.update(groupTrack);

        //4.创建镜像
        createGroupSnapShot(group, event, groupTrack, GroupEventType.ANTIEPIDEMIC);
    }

    /**
     * 疾病事件
     */
    @Transactional
    public void groupEventDisease(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorDiseaseGroupInput disease) {
        //1.转换下疾病信息
        DoctorDiseaseGroupEvent diseaseEvent = BeanMapper.map(disease, DoctorDiseaseGroupEvent.class);

        //2.创建疾病事件
        DoctorGroupEvent<DoctorDiseaseGroupEvent> event = dozerGroupEvent(group, GroupEventType.DISEASE);
        event.setGroupId(group.getId());    //猪群id
        event.setEventAt(DateUtil.toDate(disease.getDiseaseAt()));
        event.setExtraMap(diseaseEvent);
        event.setCreatorId(disease.getCreatorId());
        event.setCreatorName(disease.getCreatorName());
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        groupTrack.setRelEventId(event.getId());
        doctorGroupTrackDao.update(groupTrack);

        //4.创建镜像
        createGroupSnapShot(group, event, groupTrack, GroupEventType.DISEASE);
    }

    /**
     * 关闭猪群事件
     */
    @Transactional
    public void groupEventClose(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorCloseGroupInput close) {
        //1.转换下信息
        DoctorCloseGroupEvent closeEvent = BeanMapper.map(close, DoctorCloseGroupEvent.class);

        //2.创建关闭猪群事件
        DoctorGroupEvent<DoctorCloseGroupEvent> event = dozerGroupEvent(group, GroupEventType.CLOSE);
        event.setGroupId(group.getId());    //猪群id
        event.setEventAt(DateUtil.toDate(close.getCloseAt()));
        event.setExtraMap(closeEvent);
        event.setCreatorId(close.getCreatorId());
        event.setCreatorName(close.getCreatorName());
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        groupTrack.setRelEventId(event.getId());
        doctorGroupTrackDao.update(groupTrack);

        //4.猪群状态改为关闭
        group.setStatus(DoctorGroup.Status.CLOSED.getValue());
        doctorGroupDao.update(group);

        //5.创建镜像
        createGroupSnapShot(group, event, groupTrack, GroupEventType.CLOSE);
    }

    /**
     * 猪只存栏事件
     */
    @Transactional
    public void groupEventLiveStock(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorLiveStockGroupInput liveStock) {
        //1.转换下猪只存栏信息
        DoctorLiveStockGroupEvent liveStockEvent = BeanMapper.map(liveStock, DoctorLiveStockGroupEvent.class);

        //2.创建猪只存栏事件
        DoctorGroupEvent<DoctorLiveStockGroupEvent> event = dozerGroupEvent(group, GroupEventType.LIVE_STOCK);
        event.setGroupId(group.getId());    //猪群id
        event.setEventAt(DateUtil.toDate(liveStock.getMeasureAt()));
        event.setQuantity(groupTrack.getQuantity());  //猪群存栏数量 = 猪群数量
        event.setWeight(event.getQuantity() * event.getAvgWeight()); // 总活体重 = 数量 * 均重
        event.setExtraMap(liveStockEvent);
        event.setCreatorId(liveStock.getCreatorId());
        event.setCreatorName(liveStock.getCreatorName());
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        groupTrack.setRelEventId(event.getId());
        doctorGroupTrackDao.update(groupTrack);

        //4.创建镜像
        createGroupSnapShot(group, event, groupTrack, GroupEventType.LIVE_STOCK);
    }

    //转换下猪群基本数据
    private DoctorGroupEvent dozerGroupEvent(DoctorGroup group, GroupEventType eventType) {
        DoctorGroupEvent event = new DoctorGroupEvent();
        event.setOrgId(group.getOrgId());
        event.setOrgName(group.getOrgName());
        event.setFarmId(group.getFarmId());
        event.setFarmName(group.getFarmName());
        event.setGroupId(group.getId());
        event.setGroupCode(group.getGroupCode());
        event.setType(eventType.getValue());    //事件类型
        event.setName(eventType.getDesc());
        event.setBarnId(group.getCurrentBarnId());
        event.setBarnName(group.getCurrentBarnName());
        event.setPigType(group.getPigType());  // todo 猪类是否需要转换?
        return event;
    }

    //创建猪群镜像信息
    private void createGroupSnapShot(DoctorGroup group, DoctorGroupEvent groupEvent, DoctorGroupTrack groupTrack, GroupEventType eventType) {
        DoctorGroupSnapshot groupSnapshot = new DoctorGroupSnapshot();
        groupSnapshot.setEventType(eventType.getValue());  //猪群事件类型
        groupSnapshot.setToGroupId(group.getId());
        groupSnapshot.setToEventId(groupEvent.getId());
        groupSnapshot.setToInfo(JSON_MAPPER.toJson(DoctorGroupSnapShotInfo.builder()
                .group(group)
                .groupEvent(groupEvent)
                .groupTrack(groupTrack)
                .build()));
        doctorGroupSnapshotDao.create(groupSnapshot);
    }
}
