package io.terminus.doctor.event.manager;

import com.google.common.base.MoreObjects;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorAntiepidemicGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorChangeGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorCloseGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorDiseaseGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorLiveStockGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorNewGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorTransGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

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
    private final DoctorGroupWriteService doctorGroupWriteService;

    @Autowired
    public DoctorGroupManager(DoctorGroupDao doctorGroupDao,
                              DoctorGroupEventDao doctorGroupEventDao,
                              DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                              DoctorGroupTrackDao doctorGroupTrackDao,
                              DoctorGroupWriteService doctorGroupWriteService) {
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorGroupWriteService = doctorGroupWriteService;
    }

    /**
     * 新建猪群
     * @param group 猪群
     * @param newGroupInput 新建猪群录入信息
     * @return 猪群id
     */
    @Transactional
    public Long createNewGroup(DoctorGroup group, DoctorNewGroupInput newGroupInput) {
        //1. 创建猪群
        doctorGroupDao.create(getNewGroup(group, newGroupInput));
        Long groupId = group.getId();

        //2. 创建新建猪群事件
        DoctorGroupEvent<DoctorNewGroupEvent> groupEvent = getNewGroupEvent(group, newGroupInput);
        doctorGroupEventDao.create(groupEvent);

        //3. 创建猪群跟踪
        DoctorGroupTrack groupTrack = BeanMapper.map(groupEvent, DoctorGroupTrack.class);
        groupTrack.setGroupId(groupId);
        groupTrack.setRelEventId(groupEvent.getId());
        groupTrack.setBoarQty(0);
        groupTrack.setSowQty(0);
        groupTrack.setQuantity(0);
        groupTrack.setSex(EventUtil.getSex(groupTrack.getBoarQty(), groupTrack.getSowQty()));
        doctorGroupTrackDao.create(groupTrack);

        //4. 创建猪群镜像
        createGroupSnapShot(group, groupEvent, groupTrack, GroupEventType.NEW);
        return groupId;
    }

    private DoctorGroup getNewGroup(DoctorGroup group, DoctorNewGroupInput newGroupInput) {
        //设置猪舍
        group.setInitBarnId(newGroupInput.getBarnId());
        group.setInitBarnName(newGroupInput.getBarnName());
        group.setCurrentBarnId(newGroupInput.getBarnId());
        group.setCurrentBarnName(newGroupInput.getBarnName());

        //建群时间与状态
        group.setOpenAt(MoreObjects.firstNonNull(group.getOpenAt(), new Date()));
        group.setStatus(DoctorGroup.Status.CREATED.getValue());
        return group;
    }

    //构造新建猪群事件
    private DoctorGroupEvent<DoctorNewGroupEvent> getNewGroupEvent(DoctorGroup group, DoctorNewGroupInput newGroupInput) {
        DoctorGroupEvent<DoctorNewGroupEvent> groupEvent = new DoctorGroupEvent<>();

        groupEvent.setGroupId(group.getId());   //关联猪群id

        groupEvent.setOrgId(group.getOrgId());
        groupEvent.setOrgName(group.getOrgName());
        groupEvent.setFarmId(group.getFarmId());
        groupEvent.setGroupCode(group.getGroupCode());

        //事件信息
        groupEvent.setEventAt(group.getOpenAt());
        groupEvent.setType(GroupEventType.NEW.getValue());
        groupEvent.setName(GroupEventType.NEW.getDesc());
        groupEvent.setDesc("todo 事件描述");

        groupEvent.setBarnId(group.getInitBarnId());
        groupEvent.setBarnName(group.getInitBarnName());
        groupEvent.setPigType(group.getPigType());

        groupEvent.setCreatorId(group.getCreatorId());
        groupEvent.setCreatorName(group.getCreatorName());
        groupEvent.setRemark(group.getRemark());

        DoctorNewGroupEvent newGroupEvent = new DoctorNewGroupEvent();
        newGroupEvent.setType(GroupEventType.NEW.getValue());
        newGroupEvent.setSource(newGroupInput.getSource());
        groupEvent.setExtraMap(newGroupEvent);
        return groupEvent;
    }

    /**
     * 防疫事件
     */
    @Transactional
    public void groupEventAntiepidemic(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorAntiepidemicGroupInput antiepidemic) {
        //1.转换下防疫信息
        DoctorAntiepidemicGroupEvent antiEvent = BeanMapper.map(antiepidemic, DoctorAntiepidemicGroupEvent.class);

        //2.创建防疫事件
        DoctorGroupEvent<DoctorAntiepidemicGroupEvent> event = dozerGroupEvent(group, GroupEventType.ANTIEPIDEMIC, antiepidemic);
        event.setExtraMap(antiEvent);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        updateGroupTrack(groupTrack, event);

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
        DoctorGroupEvent<DoctorDiseaseGroupEvent> event = dozerGroupEvent(group, GroupEventType.DISEASE, disease);
        event.setExtraMap(diseaseEvent);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        updateGroupTrack(groupTrack, event);

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
        DoctorGroupEvent<DoctorCloseGroupEvent> event = dozerGroupEvent(group, GroupEventType.CLOSE, close);
        event.setExtraMap(closeEvent);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        updateGroupTrack(groupTrack, event);

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
        DoctorGroupEvent<DoctorLiveStockGroupEvent> event = dozerGroupEvent(group, GroupEventType.LIVE_STOCK, liveStock);
        event.setQuantity(groupTrack.getQuantity());  //猪群存栏数量 = 猪群数量
        event.setAvgDayAge(groupTrack.getAvgDayAge());
        event.setAvgWeight(liveStock.getAvgWeight());
        event.setWeight(event.getQuantity() * event.getAvgWeight()); // 总活体重 = 数量 * 均重
        event.setExtraMap(liveStockEvent);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(group, event, groupTrack, GroupEventType.LIVE_STOCK);
    }

    /**
     * 转入猪群事件
     */
    @Transactional
    public void groupEventMoveIn(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorMoveInGroupInput moveIn) {
        //1.转换转入猪群事件
        DoctorMoveInGroupEvent moveInEvent = BeanMapper.map(moveIn, DoctorMoveInGroupEvent.class);

        //2.创建转入猪群事件
        DoctorGroupEvent<DoctorMoveInGroupEvent> event = dozerGroupEvent(group, GroupEventType.MOVE_IN, moveIn);
        event.setQuantity(moveIn.getQuantity());
        event.setAvgDayAge(moveIn.getAvgDayAge());
        event.setAvgWeight(moveIn.getAvgWeight());
        event.setWeight(EventUtil.getWeight(event.getAvgWeight(), event.getQuantity()));
        event.setExtraMap(moveInEvent);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        groupTrack.setQuantity(EventUtil.plusQuantity(groupTrack.getQuantity(), moveIn.getQuantity()));
        groupTrack.setBoarQty(EventUtil.plusQuantity(groupTrack.getBoarQty(), moveIn.getBoarQty()));
        groupTrack.setSowQty(EventUtil.plusQuantity(groupTrack.getSowQty(), moveIn.getSowQty()));

        //重新计算日龄
        int deltaDayAge = EventUtil.deltaDayAge(groupTrack.getAvgDayAge(), groupTrack.getQuantity(), moveIn.getAvgDayAge(), moveIn.getQuantity());
        groupTrack.setAvgDayAge(groupTrack.getAvgDayAge() + deltaDayAge);
        groupTrack.setBirthDate(EventUtil.getBirthDate(groupTrack.getBirthDate(), deltaDayAge));

        //重新计算重量
        groupTrack.setAvgWeight(EventUtil.getAvgWeight(groupTrack.getWeight(), EventUtil.getWeight(moveIn.getAvgWeight(), moveIn.getQuantity()), groupTrack.getQuantity()));
        groupTrack.setWeight(EventUtil.getWeight(groupTrack.getAvgWeight(), groupTrack.getQuantity()));

        //重新计算金额
        groupTrack.setAmount(groupTrack.getAmount() + MoreObjects.firstNonNull(moveIn.getAmount(), 0L));
        groupTrack.setPrice(EventUtil.getPrice(groupTrack.getAmount(), groupTrack.getQuantity()));
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(group, event, groupTrack, GroupEventType.MOVE_IN);
    }

    /**
     * 猪群变动事件 // TODO: 16/5/30 销售要记录销售数量
     */
    @Transactional
    public void groupEventChange(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorChangeGroupInput change) {
        //1.转换猪群变动事件
        DoctorChangeGroupEvent changeEvent = BeanMapper.map(change, DoctorChangeGroupEvent.class);

        //2.创建猪群变动事件
        DoctorGroupEvent<DoctorChangeGroupEvent> event = dozerGroupEvent(group, GroupEventType.CHANGE, change);
        event.setQuantity(change.getQuantity());
        event.setAvgDayAge(groupTrack.getAvgDayAge());  //变动的日龄不需要录入, 直接取猪群的日龄
        event.setWeight(change.getWeight());
        event.setAvgWeight(EventUtil.getAvgWeight(change.getWeight(), change.getQuantity()));
        event.setExtraMap(changeEvent);
        doctorGroupEventDao.create(event);

        Integer oldQuantity = groupTrack.getQuantity();

        //3.更新猪群跟踪
        groupTrack.setQuantity(EventUtil.minusQuantity(groupTrack.getQuantity(), change.getQuantity()));
        groupTrack.setBoarQty(EventUtil.minusQuantity(groupTrack.getBoarQty(), change.getBoarQty()));
        groupTrack.setSowQty(EventUtil.minusQuantity(groupTrack.getSowQty(), change.getSowQty()));

        //重新计算重量
        groupTrack.setWeight(groupTrack.getWeight() - change.getWeight());
        groupTrack.setAvgWeight(EventUtil.getAvgWeight(groupTrack.getWeight(), groupTrack.getQuantity()));

        //重新计算金额
        groupTrack.setAmount(groupTrack.getAmount() - MoreObjects.firstNonNull(change.getAmount(), 0L));
        groupTrack.setPrice(EventUtil.getPrice(groupTrack.getAmount(), groupTrack.getQuantity()));
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(group, event, groupTrack, GroupEventType.CHANGE);

        //5.判断变动数量, 如果 = 猪群数量, 触发关闭猪群事件
        if (Objects.equals(oldQuantity, change.getQuantity())) {
            autoGroupEventClose(group, groupTrack, change);
        }
    }

    /**
     * 转群事件
     */
    @Transactional
    public void groupEventTransGroup(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorTransGroupInput transGroup) {
        //1.转换转群事件
        DoctorTransGroupEvent transGroupEvent = BeanMapper.map(transGroup, DoctorTransGroupEvent.class);

        //2.创建转群事件
        DoctorGroupEvent<DoctorTransGroupEvent> event = dozerGroupEvent(group, GroupEventType.TRANS_GROUP, transGroup);
        event.setQuantity(transGroup.getQuantity());
        event.setAvgDayAge(groupTrack.getAvgDayAge());  //转群的日龄不需要录入, 直接取猪群的日龄
        event.setWeight(transGroup.getWeight());
        event.setAvgWeight(EventUtil.getAvgWeight(transGroup.getWeight(), transGroup.getQuantity()));
        event.setExtraMap(transGroupEvent);
        doctorGroupEventDao.create(event);

        Integer oldQuantity = groupTrack.getQuantity();

        //3.更新猪群跟踪
        groupTrack.setQuantity(EventUtil.minusQuantity(groupTrack.getQuantity(), transGroup.getQuantity()));
        groupTrack.setBoarQty(EventUtil.minusQuantity(groupTrack.getBoarQty(), transGroup.getBoarQty()));
        groupTrack.setSowQty(EventUtil.minusQuantity(groupTrack.getSowQty(), transGroup.getSowQty()));

        //重新计算重量
        groupTrack.setWeight(groupTrack.getWeight() - transGroup.getWeight());
        groupTrack.setAvgWeight(EventUtil.getAvgWeight(groupTrack.getWeight(), groupTrack.getQuantity()));

        updateGroupTrack(groupTrack, event);

        //4.创建镜像 todo
        createGroupSnapShot(group, event, groupTrack, GroupEventType.TRANS_GROUP);

        //5.判断转群数量, 如果 = 猪群数量, 触发关闭猪群事件
        if (Objects.equals(oldQuantity, transGroup.getQuantity())) {
            autoGroupEventClose(group, groupTrack, transGroup);
        }

        //6.判断是否新建群,触发目标群的转入仔猪事件
        if (Objects.equals(transGroup.getIsCreateGroup(), IsOrNot.YES.getValue())) {
//            autoGroupEventNew();
        } else {
//            autoGroupEventMoveIn();
        }
    }

    /**
     * 系统触发的自动关闭猪群事件
     */
    private void autoGroupEventClose(DoctorGroup group, DoctorGroupTrack groupTrack, BaseGroupInput baseInput) {
        DoctorCloseGroupInput closeInput = new DoctorCloseGroupInput();
        closeInput.setIsAuto(IsOrNot.YES.getValue());   //系统触发事件, 属于自动生成
        closeInput.setEventAt(baseInput.getEventAt());
        RespHelper.orServEx(doctorGroupWriteService.groupEventClose(new DoctorGroupDetail(group, groupTrack), closeInput));
    }

    /**
     * 系统触发的自动新建猪群事件
     */
    private void autoGroupEventNew(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorNewGroupInput newGroupInput) {

    }

    /**
     * 系统触发的自动转入转入猪群事件(群间转移)
     */
    private void autoGroupEventMoveIn(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorMoveInGroupInput moveIn) {

    }

    //转换下猪群基本数据
    private DoctorGroupEvent dozerGroupEvent(DoctorGroup group, GroupEventType eventType, BaseGroupInput baseInput) {
        DoctorGroupEvent event = new DoctorGroupEvent();
        event.setEventAt(DateUtil.toDate(baseInput.getEventAt()));
        event.setOrgId(group.getOrgId());       //公司信息
        event.setOrgName(group.getOrgName());
        event.setFarmId(group.getFarmId());     //猪场信息
        event.setFarmName(group.getFarmName());
        event.setGroupId(group.getId());        //猪群信息
        event.setGroupCode(group.getGroupCode());
        event.setType(eventType.getValue());    //事件类型
        event.setName(eventType.getDesc());
        event.setBarnId(group.getCurrentBarnId());      //事件发生猪舍
        event.setBarnName(group.getCurrentBarnName());
        event.setPigType(group.getPigType());           //猪类
        event.setCreatorId(baseInput.getCreatorId());   //创建人
        event.setCreatorName(baseInput.getCreatorName());
        return event;
    }

    //更新猪群跟踪
    private void updateGroupTrack(DoctorGroupTrack groupTrack, DoctorGroupEvent event) {
        groupTrack.setRelEventId(event.getId());    //关联此次的事件id
        groupTrack.setUpdatorId(event.getCreatorId());
        groupTrack.setUpdatorName(event.getCreatorName());
        groupTrack.setSex(EventUtil.getSex(groupTrack.getBoarQty(), groupTrack.getSowQty()));
        doctorGroupTrackDao.update(groupTrack);
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
