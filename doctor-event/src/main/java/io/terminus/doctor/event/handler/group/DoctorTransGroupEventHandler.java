package io.terminus.doctor.event.handler.group;

import com.google.common.base.MoreObjects;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.DoctorTransGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.manager.DoctorGroupManager;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorEventRelation;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Desc: 转群事件处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class DoctorTransGroupEventHandler extends DoctorAbstractGroupEventHandler {

    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorCommonGroupEventHandler doctorCommonGroupEventHandler;
    private final DoctorGroupManager doctorGroupManager;
    private final DoctorBarnDao doctorBarnDao;

    @Autowired
    public DoctorTransGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                        DoctorGroupTrackDao doctorGroupTrackDao,
                                        DoctorGroupEventDao doctorGroupEventDao,
                                        DoctorCommonGroupEventHandler doctorCommonGroupEventHandler,
                                        DoctorGroupManager doctorGroupManager,
                                        DoctorBarnDao doctorBarnDao) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, doctorGroupEventDao, doctorBarnDao);
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorCommonGroupEventHandler = doctorCommonGroupEventHandler;
        this.doctorGroupManager = doctorGroupManager;
        this.doctorBarnDao = doctorBarnDao;
    }

    @Override
    public <I extends BaseGroupInput> DoctorGroupEvent buildGroupEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.TRANS_GROUP.getValue());

        DoctorTransGroupInput transGroup = (DoctorTransGroupInput) input;

        //同舍不可转群
        if (Objects.equals(group.getCurrentBarnId(), transGroup.getToBarnId())) {
            throw new InvalidException("same.barn.can.not.trans");
        }

        //校验能否转群, 数量, 日龄差, 转群总重
        checkCanTransBarn(group.getPigType(), transGroup.getToBarnId());
        checkCanTransGroup(transGroup.getToGroupId(), transGroup.getToBarnId(), transGroup.getIsCreateGroup());
        checkFarrowGroupUnique(transGroup.getIsCreateGroup(), transGroup.getToBarnId());
        checkQuantity(groupTrack.getQuantity(), transGroup.getQuantity());
        checkQuantityEqual(transGroup.getQuantity(), transGroup.getBoarQty(), transGroup.getSowQty());
        Double realWeight = transGroup.getAvgWeight() * transGroup.getQuantity();   //后台计算的总重

        //转入猪舍
        DoctorBarn toBarn = getBarn(transGroup.getToBarnId());
        if (!input.isSowEvent()) {
            checkUnweanTrans(group.getPigType(), toBarn.getPigType(), groupTrack, transGroup.getQuantity());
        }

        //1.转换转群事件
        DoctorTransGroupEvent transGroupEvent = BeanMapper.map(transGroup, DoctorTransGroupEvent.class);
        checkBreed(group.getBreedId(), transGroupEvent.getBreedId());
        transGroupEvent.setToBarnType(toBarn.getPigType());

        //2.创建转群事件
        DoctorGroupEvent<DoctorTransGroupEvent> event = dozerGroupEvent(group, GroupEventType.TRANS_GROUP, transGroup);
        event.setQuantity(transGroup.getQuantity());

        int deltaDays = DateUtil.getDeltaDaysAbs(event.getEventAt(), new Date());
        event.setAvgDayAge(getGroupEventAge(groupTrack.getAvgDayAge(), deltaDays));  //重算日龄

        event.setAvgWeight(transGroup.getAvgWeight());  //均重
        event.setWeight(realWeight);                    //总重
        event.setTransGroupType(getTransType(null, group.getPigType(), toBarn).getValue());   //区别内转还是外转(null是因为不用判断转入类型)
        event.setOtherBarnId(toBarn.getId());          //目标猪舍id
        event.setOtherBarnType(toBarn.getPigType());   //目标猪舍类型
        event.setExtraMap(transGroupEvent);
        return event;
    }

    @Override
    public DoctorGroupTrack elicitGroupTrack(DoctorGroupEvent preEvent, DoctorGroupEvent event, DoctorGroupTrack track) {
        DoctorTransGroupEvent doctorTransGroupEvent = JSON_MAPPER.fromJson(event.getExtra(), DoctorTransGroupEvent.class);
        if(Arguments.isNull(doctorTransGroupEvent)) {
            log.error("parse doctorTransGroupEvent faild, doctorGroupEvent = {}", event);
            throw new InvalidException("transgroup.event.info.broken", event.getId());
        }

        //更新quanity
        track.setQuantity(EventUtil.minusQuantity(track.getQuantity(), event.getQuantity()));

        //如果公猪数量 lt 0 按 0 计算
        Integer boarQty = EventUtil.minusQuantity(track.getBoarQty(), doctorTransGroupEvent.getBoarQty());
        boarQty = boarQty > track.getQuantity() ? track.getQuantity() : boarQty;
        track.setBoarQty(boarQty < 0 ? 0 : boarQty);
        track.setSowQty(EventUtil.minusQuantity(track.getQuantity(), track.getBoarQty()));

        //如果是母猪触发的转群事件，窝数-1，活仔，健仔数累减
        if (event.getIsAuto() == IsOrNot.YES.getValue()) {
            track.setNest(EventUtil.plusInt(track.getNest(), -1));
            track.setLiveQty(EventUtil.plusInt(track.getLiveQty(), - event.getQuantity()));
            track.setHealthyQty(track.getLiveQty() - MoreObjects.firstNonNull(track.getWeakQty(), 0));
            track.setUnweanQty(EventUtil.plusInt(track.getUnweanQty(), -event.getQuantity()));
            track.setBirthWeight(EventUtil.plusDouble(track.getBirthWeight(), - event.getAvgWeight() * event.getQuantity()));
        }
        return track;
    }

    @Override
    protected <I extends BaseGroupInput> void handleEvent(List<DoctorEventInfo> eventInfoList, DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.TRANS_GROUP.getValue());

        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorTransGroupInput transGroup = (DoctorTransGroupInput) input;

        //同舍不可转群
        if (Objects.equals(group.getCurrentBarnId(), transGroup.getToBarnId())) {
            throw new InvalidException("same.barn.can.not.trans");
        }

        //校验能否转群, 数量, 日龄差, 转群总重
        checkCanTransBarn(group.getPigType(), transGroup.getToBarnId());
        checkCanTransGroup(transGroup.getToGroupId(), transGroup.getToBarnId(), transGroup.getIsCreateGroup());
        checkFarrowGroupUnique(transGroup.getIsCreateGroup(), transGroup.getToBarnId());
        checkQuantity(groupTrack.getQuantity(), transGroup.getQuantity());
        checkQuantityEqual(transGroup.getQuantity(), transGroup.getBoarQty(), transGroup.getSowQty());
        Double realWeight = transGroup.getAvgWeight() * transGroup.getQuantity();   //后台计算的总重
        //checkDayAge(groupTrack.getAvgDayAge(), transGroup);

        //转入猪舍
        DoctorBarn toBarn = getBarn(transGroup.getToBarnId());
        if (!input.isSowEvent()) {
            checkUnweanTrans(group.getPigType(), toBarn.getPigType(), groupTrack, transGroup.getQuantity());
        }

        //1.转换转群事件
        DoctorTransGroupEvent transGroupEvent = BeanMapper.map(transGroup, DoctorTransGroupEvent.class);
        checkBreed(group.getBreedId(), transGroupEvent.getBreedId());
        transGroupEvent.setToBarnType(toBarn.getPigType());

        //2.创建转群事件
        DoctorGroupEvent<DoctorTransGroupEvent> event = dozerGroupEvent(group, GroupEventType.TRANS_GROUP, transGroup);
        event.setQuantity(transGroup.getQuantity());

        int deltaDays = DateUtil.getDeltaDaysAbs(event.getEventAt(), new Date());
        event.setAvgDayAge(getGroupEventAge(groupTrack.getAvgDayAge(), deltaDays));  //重算日龄

        event.setAvgWeight(transGroup.getAvgWeight());  //均重
        event.setWeight(realWeight);                    //总重
        event.setTransGroupType(getTransType(null, group.getPigType(), toBarn).getValue());   //区别内转还是外转(null是因为不用判断转入类型)
        event.setOtherBarnId(toBarn.getId());          //目标猪舍id
        event.setOtherBarnType(toBarn.getPigType());   //目标猪舍类型
        event.setExtraMap(transGroupEvent);
        doctorGroupEventDao.create(event);

        //创建关联关系
        createEventRelation(event);

        transGroup.setRelGroupEventId(event.getId());

        Integer oldQuantity = groupTrack.getQuantity();

        //3.更新猪群跟踪
        groupTrack.setQuantity(EventUtil.minusQuantity(groupTrack.getQuantity(), transGroup.getQuantity()));

        //如果公猪数量 lt 0 按 0 计算
        Integer boarQty = EventUtil.minusQuantity(groupTrack.getBoarQty(), transGroup.getBoarQty());
        boarQty = boarQty > groupTrack.getQuantity() ? groupTrack.getQuantity() : boarQty;
        groupTrack.setBoarQty(boarQty < 0 ? 0 : boarQty);
        groupTrack.setSowQty(EventUtil.minusQuantity(groupTrack.getQuantity(), groupTrack.getBoarQty()));

        //如果是母猪触发的转群事件，窝数-1，活仔，健仔数累减
        if (transGroup.isSowEvent()) {
            groupTrack.setNest(EventUtil.plusInt(groupTrack.getNest(), -1));
            groupTrack.setLiveQty(EventUtil.plusInt(groupTrack.getLiveQty(), -transGroup.getQuantity()));
            groupTrack.setHealthyQty(groupTrack.getLiveQty() - MoreObjects.firstNonNull(groupTrack.getWeakQty(), 0));
            groupTrack.setUnweanQty(EventUtil.plusInt(groupTrack.getUnweanQty(), -transGroup.getQuantity()));
            groupTrack.setBirthWeight(EventUtil.plusDouble(groupTrack.getBirthWeight(), -realWeight));
        }

        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        DoctorGroupSnapshot doctorGroupSnapshot = createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, event, groupTrack), GroupEventType.TRANS_GROUP);

        //5.判断转群数量, 如果 = 猪群数量, 触发关闭猪群事件, 同时生成批次总结
        if (Objects.equals(oldQuantity, transGroup.getQuantity())) {
            doctorCommonGroupEventHandler.autoGroupEventClose(eventInfoList, group, groupTrack, transGroup, event.getEventAt(), transGroup.getFcrFeed());

            Long toGroupEventId = doctorEventRelationDao.findByOriginAndType(event.getId(), DoctorEventRelation.TargetType.GROUP.getValue()).getTriggerEventId();
            DoctorGroupEvent closeEvent = doctorGroupEventDao.findById(toGroupEventId);
            transGroup.setRelGroupEventId(closeEvent.getId());    //如果发生关闭猪群事件，关联事件id要换下
        }

        //设置来源为本场
        transGroup.setSource(PigSource.LOCAL.getKey());

        //6.判断是否新建群,触发目标群的转入仔猪事件
        if (Objects.equals(transGroup.getIsCreateGroup(), IsOrNot.YES.getValue())) {
            //新建猪群
            Long toGroupId = autoTransGroupEventNew(eventInfoList, group, groupTrack, transGroup, toBarn);
            transGroup.setToGroupId(toGroupId);

            //更新事件
            transGroupEvent.setToGroupId(toGroupId);
            event.setExtraMap(transGroupEvent);
            doctorGroupEventDao.update(event);

            //更新镜像
            DoctorGroupSnapShotInfo toInfo = JsonMapper.JSON_NON_EMPTY_MAPPER.fromJson(doctorGroupSnapshot.getToInfo(), DoctorGroupSnapShotInfo.class);
            toInfo.setGroupEvent(event);
            doctorGroupSnapshot.setToInfo(JsonMapperUtil.JSON_NON_DEFAULT_MAPPER.toJson(toInfo));
            doctorGroupSnapshotDao.update(doctorGroupSnapshot);

            //刷新最新事件id
            DoctorGroupEvent newGroupEvent = doctorGroupEventDao.findLastEventByGroupId(toGroupId);
            transGroup.setRelGroupEventId(newGroupEvent.getId());

            //转入猪群
            doctorCommonGroupEventHandler.autoTransEventMoveIn(eventInfoList, group, groupTrack, transGroup);
        } else {
            doctorCommonGroupEventHandler.autoTransEventMoveIn(eventInfoList, group, groupTrack, transGroup);
        }

        //发布统计事件
        //publistGroupAndBarn(event);
    }

    /**
     * 系统触发的自动新建猪群事件(转群触发)
     */
    private Long autoTransGroupEventNew(List<DoctorEventInfo> eventInfoList, DoctorGroup fromGroup, DoctorGroupTrack fromGroupTrack, DoctorTransGroupInput transGroup, DoctorBarn toBarn) {
        DoctorNewGroupInput newGroupInput = new DoctorNewGroupInput();
        newGroupInput.setSowCode(transGroup.getSowCode());
        newGroupInput.setFarmId(fromGroup.getFarmId());
        newGroupInput.setGroupCode(transGroup.getToGroupCode());    //录入猪群号
        newGroupInput.setEventAt(transGroup.getEventAt());          //事件发生日期
        newGroupInput.setBarnId(transGroup.getToBarnId());          //转到的猪舍id
        newGroupInput.setBarnName(transGroup.getToBarnName());
        newGroupInput.setPigType(toBarn.getPigType());    //猪类取猪舍的猪类
        newGroupInput.setSex(fromGroupTrack.getSex());
        newGroupInput.setBreedId(transGroup.getBreedId());          //品种
        newGroupInput.setBreedName(transGroup.getBreedName());
        newGroupInput.setGeneticId(fromGroup.getGeneticId());
        newGroupInput.setGeneticName(fromGroup.getGeneticName());
        newGroupInput.setSource(PigSource.LOCAL.getKey());          //来源:本场
        newGroupInput.setIsAuto(IsOrNot.YES.getValue());
        newGroupInput.setRemark(transGroup.getRemark());
        newGroupInput.setRelGroupEventId(transGroup.getRelGroupEventId()); //由什么事件触发的新建猪群事件

        DoctorGroup toGroup = BeanMapper.map(newGroupInput, DoctorGroup.class);
        toGroup.setFarmName(fromGroup.getFarmName());
        toGroup.setOrgId(fromGroup.getOrgId());
        toGroup.setOrgName(fromGroup.getOrgName());
        toGroup.setCreatorId(transGroup.getCreatorId());    //创建人取录入转群事件的人
        toGroup.setCreatorName(transGroup.getCreatorName());
        return doctorGroupManager.createNewGroup(eventInfoList, toGroup, newGroupInput);
    }

    private DoctorBarn getBarn(Long barnId) {
        return doctorBarnDao.findById(barnId);
    }


    @Override
    public boolean checkDoctorGroupEvent(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent) {
        if(doctorGroupTrack.getQuantity() < doctorGroupEvent.getQuantity()){
            return false;
        }
        return true;
    }

    @Override
    public List<DoctorGroupEvent> triggerGroupEvent(List<DoctorGroupEvent> triggerDoctorGroupEventList,DoctorGroupEvent oldEvent, DoctorGroupEvent newEvent){
        //修改转入猪舍,影响原猪舍
        return triggerDoctorGroupEventList;
    }
}
