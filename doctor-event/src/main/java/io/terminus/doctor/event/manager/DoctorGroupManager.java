package io.terminus.doctor.event.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorNewGroupEvent;
import io.terminus.doctor.event.dto.event.group.edit.DoctorNewGroupEdit;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.event.ListenedBarnEvent;
import io.terminus.doctor.event.event.ListenedGroupEvent;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.util.EventUtil;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final DoctorGroupReadService doctorGroupReadService;
    private final CoreEventDispatcher coreEventDispatcher;

    @Autowired(required = false)
    private Publisher publisher;

    @Autowired
    public DoctorGroupManager(DoctorGroupDao doctorGroupDao,
                              DoctorGroupEventDao doctorGroupEventDao,
                              DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                              DoctorGroupTrackDao doctorGroupTrackDao,
                              DoctorGroupReadService doctorGroupReadService,
                              CoreEventDispatcher coreEventDispatcher) {
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorGroupReadService = doctorGroupReadService;
        this.coreEventDispatcher = coreEventDispatcher;
    }

    /**
     * 新建猪群
     * @param group 猪群
     * @param newGroupInput 新建猪群录入信息
     * @return 猪群id
     */
    @Transactional
    public Long createNewGroup(DoctorGroup group, DoctorNewGroupInput newGroupInput) {
        //0.校验猪群号是否重复
        checkGroupCodeExist(newGroupInput.getFarmId(), newGroupInput.getGroupCode());

        //1. 创建猪群
        doctorGroupDao.create(getNewGroup(group, newGroupInput));
        Long groupId = group.getId();

        //2. 创建新建猪群事件
        DoctorGroupEvent<DoctorNewGroupEvent> groupEvent = getNewGroupEvent(group, newGroupInput);
        doctorGroupEventDao.create(groupEvent);

        //3. 创建猪群跟踪
        DoctorGroupTrack groupTrack = BeanMapper.map(groupEvent, DoctorGroupTrack.class);
        groupTrack.setExtraEntity(DoctorGroupTrack.Extra.builder().newAt(DateUtil.toDate(newGroupInput.getEventAt())).build());  //dozer不需要转换extra字段
        groupTrack.setGroupId(groupId);
        groupTrack.setRelEventId(groupEvent.getId());
        groupTrack.setBoarQty(0);
        groupTrack.setSowQty(0);
        groupTrack.setQuantity(0);
        groupTrack.setAvgDayAge(0);             //日龄
        groupTrack.setBirthDate(new Date());    //出生日期(用于计算日龄)
        groupTrack.setAvgWeight(0D);            //均重
        groupTrack.setWeight(0D);               //总重
        groupTrack.setPrice(0L);                //单价
        groupTrack.setAmount(0L);               //金额
        groupTrack.setSex(EventUtil.getSex(groupTrack.getBoarQty(), groupTrack.getSowQty()));
        doctorGroupTrackDao.create(groupTrack);

        //4. 创建猪群镜像
        DoctorGroupSnapshot groupSnapshot = new DoctorGroupSnapshot();
        groupSnapshot.setEventType(GroupEventType.NEW.getValue());  //猪群事件类型
        groupSnapshot.setToGroupId(group.getId());
        groupSnapshot.setToEventId(groupEvent.getId());
        groupSnapshot.setToInfo(JSON_MAPPER.toJson(DoctorGroupSnapShotInfo.builder()
                .group(group)
                .groupEvent(groupEvent)
                .groupTrack(groupTrack)
                .build()));
        doctorGroupSnapshotDao.create(groupSnapshot);

        //发布统计事件
        publistGroupAndBarn(group.getOrgId(), group.getFarmId(), group.getId(), group.getCurrentBarnId(), groupEvent.getId());
        return groupId;
    }

    /**
     * 编辑新建猪群事件
     * @param group         猪群
     * @param event         猪群事件
     * @param newEdit       编辑信息
     */
    @Transactional
    public void editNewGroupEvent(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorGroupEvent event, DoctorNewGroupEdit newEdit) {
        //1. 更新猪群
        group.setGroupCode(newEdit.getGroupCode());
        group.setBreedId(newEdit.getBreedId());
        group.setBreedName(newEdit.getBreedName());
        group.setGeneticId(newEdit.getGeneticId());
        group.setGeneticName(newEdit.getGeneticName());
        doctorGroupDao.update(group);

        //2. 更新事件
        event.setRemark(newEdit.getRemark());
        doctorGroupEventDao.update(event);

        //3. 更新所有事件的猪群号(如果有变更)
        if (!Objects.equals(group.getGroupCode(), newEdit.getGroupCode())) {
            doctorGroupEventDao.updateGroupCodeByGroupId(group.getId(), group.getGroupCode());
        }

        //4. 更新镜像
        DoctorGroupSnapshot snapshot = doctorGroupSnapshotDao.findGroupSnapshotByToEventId(event.getId());
        snapshot.setToInfo(JSON_MAPPER.toJson(new DoctorGroupSnapShotInfo(group, event, groupTrack)));
        doctorGroupSnapshotDao.update(snapshot);
    }

    private DoctorGroup getNewGroup(DoctorGroup group, DoctorNewGroupInput newGroupInput) {
        //设置猪舍
        group.setInitBarnId(newGroupInput.getBarnId());
        group.setInitBarnName(newGroupInput.getBarnName());
        group.setCurrentBarnId(newGroupInput.getBarnId());
        group.setCurrentBarnName(newGroupInput.getBarnName());

        //建群时间与状态
        group.setOpenAt(DateUtil.toDate(newGroupInput.getEventAt()));
        group.setStatus(DoctorGroup.Status.CREATED.getValue());
        return group;
    }

    //构造新建猪群事件
    private DoctorGroupEvent<DoctorNewGroupEvent> getNewGroupEvent(DoctorGroup group, DoctorNewGroupInput newGroupInput) {
        DoctorGroupEvent<DoctorNewGroupEvent> groupEvent = new DoctorGroupEvent<>();

        groupEvent.setGroupId(group.getId());   //关联猪群id
        groupEvent.setRelGroupEventId(newGroupInput.getRelGroupEventId()); //关联猪群事件id
        groupEvent.setRelPigEventId(newGroupInput.getRelPigEventId());     //关联猪事件id(比如分娩时的新建猪群)

        groupEvent.setOrgId(group.getOrgId());
        groupEvent.setOrgName(group.getOrgName());
        groupEvent.setFarmId(group.getFarmId());
        groupEvent.setFarmName(group.getFarmName());
        groupEvent.setGroupCode(group.getGroupCode());

        //事件信息
        groupEvent.setEventAt(group.getOpenAt());
        groupEvent.setType(GroupEventType.NEW.getValue());
        groupEvent.setName(GroupEventType.NEW.getDesc());
        groupEvent.setDesc(newGroupInput.generateEventDesc());

        groupEvent.setBarnId(group.getInitBarnId());
        groupEvent.setBarnName(group.getInitBarnName());
        groupEvent.setPigType(group.getPigType());

        groupEvent.setIsAuto(newGroupInput.getIsAuto());
        groupEvent.setCreatorId(group.getCreatorId());
        groupEvent.setCreatorName(group.getCreatorName());
        groupEvent.setRemark(group.getRemark());

        DoctorNewGroupEvent newGroupEvent = new DoctorNewGroupEvent();
        newGroupEvent.setSource(newGroupInput.getSource());
        groupEvent.setExtraMap(newGroupEvent);
        return groupEvent;
    }

    //校验猪群号是否重复
    private void checkGroupCodeExist(Long farmId, String groupCode) {
        List<DoctorGroup> groups = RespHelper.or500(doctorGroupReadService.findGroupsByFarmId(farmId));
        if (groups.stream().map(DoctorGroup::getGroupCode).collect(Collectors.toList()).contains(groupCode)) {
            throw new ServiceException("group.code.exist");
        }
    }

    //发布猪群猪舍事件
    private void publistGroupAndBarn(Long orgId, Long farmId, Long groupId, Long barnId, Long eventId) {
        publishZookeeperEvent(ListenedGroupEvent.builder().doctorGroupEventId(eventId).farmId(farmId).orgId(orgId).groupId(groupId).build());
        publishZookeeperEvent(ListenedBarnEvent.builder().barnId(barnId).build());
    }

    //发布zk事件, 用于更新es索引
    private <T> void publishZookeeperEvent(T data){
//        if(notNull(publisher)) {
//            try {
//                publisher.publish(DataEvent.toBytes(eventType, data));
//            } catch (Exception e) {
//                log.error("publish zk event, eventType:{}, data:{} cause:{}", eventType, data, Throwables.getStackTraceAsString(e));
//            }
//        } else {
            coreEventDispatcher.publish(data);
        //}
    }
}
