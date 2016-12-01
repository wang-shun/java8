package io.terminus.doctor.event.handler.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorTransGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.manager.DoctorGroupManager;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private final DoctorBarnReadService doctorBarnReadService;
    private final DoctorGroupTrackDao doctorGroupTrackDao;

    @Autowired
    public DoctorTransGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                        DoctorGroupTrackDao doctorGroupTrackDao,
                                        CoreEventDispatcher coreEventDispatcher,
                                        DoctorGroupEventDao doctorGroupEventDao,
                                        DoctorCommonGroupEventHandler doctorCommonGroupEventHandler,
                                        DoctorGroupManager doctorGroupManager,
                                        DoctorBarnReadService doctorBarnReadService) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, coreEventDispatcher, doctorGroupEventDao, doctorBarnReadService);
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorCommonGroupEventHandler = doctorCommonGroupEventHandler;
        this.doctorGroupManager = doctorGroupManager;
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
    }

    @Override
    protected <I extends BaseGroupInput> void handleEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorTransGroupInput transGroup = (DoctorTransGroupInput) input;

        //校验能否转群, 数量, 日龄差, 转群总重
        checkCanTransBarn(group.getPigType(), transGroup.getToBarnId());
        checkCanTransGroup(transGroup.getToGroupId(), transGroup.getToBarnId());
        checkFarrowGroupUnique(transGroup.getIsCreateGroup(), transGroup.getToBarnId());
        checkQuantity(groupTrack.getQuantity(), transGroup.getQuantity());
        checkQuantityEqual(transGroup.getQuantity(), transGroup.getBoarQty(), transGroup.getSowQty());
        Double realWeight = transGroup.getAvgWeight() * transGroup.getQuantity();   //后台计算的总重
        //checkDayAge(groupTrack.getAvgDayAge(), transGroup);

        //转入猪舍
        DoctorBarn toBarn = getBarn(transGroup.getToBarnId());

        //1.转换转群事件
        DoctorTransGroupEvent transGroupEvent = BeanMapper.map(transGroup, DoctorTransGroupEvent.class);
        checkBreed(group.getBreedId(), transGroupEvent.getBreedId());
        transGroupEvent.setToBarnType(toBarn.getPigType());

        //2.创建转群事件
        DoctorGroupEvent<DoctorTransGroupEvent> event = dozerGroupEvent(group, GroupEventType.TRANS_GROUP, transGroup);
        event.setQuantity(transGroup.getQuantity());
        event.setAvgDayAge(groupTrack.getAvgDayAge());  //转群的日龄不需要录入, 直接取猪群的日龄
        event.setAvgWeight(transGroup.getAvgWeight());  //均重
        event.setWeight(realWeight);                    //总重
        event.setTransGroupType(getTransType(null, group.getPigType(), toBarn).getValue());   //区别内转还是外转(null是因为不用判断转入类型)
        event.setOtherBarnId(toBarn.getId());          //目标猪舍id
        event.setOtherBarnType(toBarn.getPigType());   //目标猪舍类型
        event.setExtraMap(transGroupEvent);
        doctorGroupEventDao.create(event);
        transGroup.setRelGroupEventId(event.getId());

        Integer oldQuantity = groupTrack.getQuantity();

        //3.更新猪群跟踪
        groupTrack.setQuantity(EventUtil.minusQuantity(groupTrack.getQuantity(), transGroup.getQuantity()));

        //如果公猪数量 lt 0 按 0 计算
        Integer boarQty = EventUtil.minusQuantity(groupTrack.getBoarQty(), transGroup.getBoarQty());
        boarQty = boarQty > groupTrack.getQuantity() ? groupTrack.getQuantity() : boarQty;
        groupTrack.setBoarQty(boarQty < 0 ? 0 : boarQty);
        groupTrack.setSowQty(EventUtil.minusQuantity(groupTrack.getQuantity(), groupTrack.getBoarQty()));

        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, event, groupTrack), GroupEventType.TRANS_GROUP);

        //5.判断转群数量, 如果 = 猪群数量, 触发关闭猪群事件, 同时生成批次总结
        if (Objects.equals(oldQuantity, transGroup.getQuantity())) {
            doctorCommonGroupEventHandler.autoGroupEventClose(group, groupTrack, transGroup, event.getEventAt(), transGroup.getFcrFeed());

            DoctorGroupEvent closeEvent = doctorGroupEventDao.findByRelGroupEventId(event.getId());
            transGroup.setRelGroupEventId(closeEvent.getId());    //如果发生关闭猪群事件，关联事件id要换下
        }

        //设置来源为本场
        transGroup.setSource(PigSource.LOCAL.getKey());

        //6.判断是否新建群,触发目标群的转入仔猪事件
        if (Objects.equals(transGroup.getIsCreateGroup(), IsOrNot.YES.getValue())) {
            //新建猪群
            Long toGroupId = autoTransGroupEventNew(group, groupTrack, transGroup, toBarn);
            transGroup.setToGroupId(toGroupId);

            //刷新最新事件id
            DoctorGroupEvent newGroupEvent = doctorGroupEventDao.findLastEventByGroupId(toGroupId);
            transGroup.setRelGroupEventId(newGroupEvent.getId());

            //转入猪群
            doctorCommonGroupEventHandler.autoTransEventMoveIn(group, groupTrack, transGroup);
        } else {
            doctorCommonGroupEventHandler.autoTransEventMoveIn(group, groupTrack, transGroup);
        }

        //发布统计事件
        publistGroupAndBarn(group.getOrgId(), group.getFarmId(), group.getId(), group.getCurrentBarnId(), event.getId());
    }

    /**
     * 系统触发的自动新建猪群事件(转群触发)
     */
    private Long autoTransGroupEventNew(DoctorGroup fromGroup, DoctorGroupTrack fromGroupTrack, DoctorTransGroupInput transGroup, DoctorBarn toBarn) {
        DoctorNewGroupInput newGroupInput = new DoctorNewGroupInput();
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
        return doctorGroupManager.createNewGroup(toGroup, newGroupInput);
    }

    private DoctorBarn getBarn(Long barnId) {
        return RespHelper.orServEx(doctorBarnReadService.findBarnById(barnId));
    }
}
