package io.terminus.doctor.event.handler.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorTransFarmGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
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
 * Desc: 转场事件处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class DoctorTransFarmGroupEventHandler extends DoctorAbstractGroupEventHandler {

    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorCommonGroupEventHandler doctorCommonGroupEventHandler;
    private final DoctorGroupManager doctorGroupManager;
    private final DoctorBarnReadService doctorBarnReadService;

    @Autowired
    public DoctorTransFarmGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
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
    }

    @Override
    protected <I extends BaseGroupInput> void handleEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorTransFarmGroupInput transFarm = (DoctorTransFarmGroupInput) input;

        //校验能否转群, 数量, 日龄差, 转群总重
        checkCanTransBarn(group.getPigType(), transFarm.getToBarnId());
        checkCanTransGroup(transFarm.getToGroupId(), transFarm.getToBarnId());
        checkFarrowGroupUnique(transFarm.getIsCreateGroup(), transFarm.getToBarnId());
        checkQuantity(groupTrack.getQuantity(), transFarm.getQuantity());
        checkQuantityEqual(transFarm.getQuantity(), transFarm.getBoarQty(), transFarm.getSowQty());
        //checkDayAge(groupTrack.getAvgDayAge(), transFarm);

        //转入猪舍
        DoctorBarn toBarn = getBarn(transFarm.getToBarnId());

        //1.转换转场事件
        DoctorTransFarmGroupEvent transFarmEvent = BeanMapper.map(transFarm, DoctorTransFarmGroupEvent.class);
        checkBreed(group.getBreedId(), transFarmEvent.getBreedId());
        transFarmEvent.setToBarnType(toBarn.getPigType());

        //2.创建转场事件
        DoctorGroupEvent<DoctorTransFarmGroupEvent> event = dozerGroupEvent(group, GroupEventType.TRANS_FARM, transFarm);
        event.setQuantity(transFarm.getQuantity());
        event.setAvgDayAge(groupTrack.getAvgDayAge());  //转群的日龄不需要录入, 直接取猪群的日龄
        event.setWeight(transFarm.getWeight());
        event.setAvgWeight(EventUtil.getAvgWeight(transFarm.getWeight(), transFarm.getQuantity()));
        event.setTransGroupType(DoctorGroupEvent.TransGroupType.OUT.getValue());   //转场肯定是外转
        event.setOtherBarnId(toBarn.getId());          //目标猪舍id
        event.setOtherBarnType(toBarn.getPigType());   //目标猪舍类型
        event.setExtraMap(transFarmEvent);
        doctorGroupEventDao.create(event);
        transFarm.setRelGroupEventId(event.getRelGroupEventId());

        Integer oldQuantity = groupTrack.getQuantity();

        //3.更新猪群跟踪
        groupTrack.setQuantity(EventUtil.minusQuantity(groupTrack.getQuantity(), transFarm.getQuantity()));

        //如果公猪数量 lt 0 按 0 计算
        Integer boarQty = EventUtil.minusQuantity(groupTrack.getBoarQty(), transFarm.getBoarQty());
        boarQty = boarQty > groupTrack.getQuantity() ? groupTrack.getQuantity() : boarQty;
        groupTrack.setBoarQty(boarQty < 0 ? 0 : boarQty);
        groupTrack.setSowQty(EventUtil.minusQuantity(groupTrack.getQuantity(), groupTrack.getBoarQty()));

        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, event, groupTrack), GroupEventType.TRANS_FARM);

        //5.判断转场数量, 如果 = 猪群数量, 触发关闭猪群事件, 同时生成批次总结
        if (Objects.equals(oldQuantity, transFarm.getQuantity())) {
            doctorCommonGroupEventHandler.autoGroupEventClose(group, groupTrack, transFarm, event.getEventAt(), transFarm.getFcrFeed());

            DoctorGroupEvent closeEvent = doctorGroupEventDao.findByRelGroupEventId(event.getId());
            transFarm.setRelGroupEventId(closeEvent.getId());    //如果发生关闭猪群事件，关联事件id要换下
        }

        //设置来源为外场
        transFarm.setSource(PigSource.OUTER.getKey());

        //6.判断是否新建群,触发目标群的转入仔猪事件
        if (Objects.equals(transFarm.getIsCreateGroup(), IsOrNot.YES.getValue())) {
            //新建猪群
            Long toGroupId = autoTransFarmEventNew(group, groupTrack, transFarm, toBarn);
            transFarm.setToGroupId(toGroupId);

            //刷新最新事件id
            DoctorGroupEvent newGroupEvent = doctorGroupEventDao.findLastEventByGroupId(toGroupId);
            transFarm.setRelGroupEventId(newGroupEvent.getId());

            //转入猪群
            doctorCommonGroupEventHandler.autoTransEventMoveIn(group, groupTrack, transFarm);
        } else {
            doctorCommonGroupEventHandler.autoTransEventMoveIn(group, groupTrack, transFarm);
        }

        //发布统计事件
        publistGroupAndBarn(group.getOrgId(), group.getFarmId(), group.getId(), group.getCurrentBarnId(), event.getId());
    }

    /**
     * 系统触发的自动新建猪群事件(转场触发)
     */
    private Long autoTransFarmEventNew(DoctorGroup fromGroup, DoctorGroupTrack fromGroupTrack, DoctorTransFarmGroupInput transFarm, DoctorBarn toBarn) {
        DoctorNewGroupInput newGroupInput = new DoctorNewGroupInput();
        newGroupInput.setFarmId(transFarm.getToFarmId());
        newGroupInput.setGroupCode(transFarm.getToGroupCode());    //录入猪群号
        newGroupInput.setEventAt(transFarm.getEventAt());          //事件发生日期
        newGroupInput.setBarnId(transFarm.getToBarnId());          //转到的猪舍id
        newGroupInput.setBarnName(transFarm.getToBarnName());
        newGroupInput.setPigType(toBarn.getPigType());    //猪类取猪舍的猪类
        newGroupInput.setSex(fromGroupTrack.getSex());
        newGroupInput.setBreedId(transFarm.getBreedId());           //品种
        newGroupInput.setBreedName(fromGroup.getBreedName());
        newGroupInput.setGeneticId(fromGroup.getGeneticId());
        newGroupInput.setGeneticName(fromGroup.getGeneticName());
        newGroupInput.setSource(PigSource.OUTER.getKey());          //来源:外购
        newGroupInput.setIsAuto(IsOrNot.YES.getValue());
        newGroupInput.setRemark(transFarm.getRemark());
        newGroupInput.setRelGroupEventId(transFarm.getRelGroupEventId());

        DoctorGroup toGroup = BeanMapper.map(newGroupInput, DoctorGroup.class);
        toGroup.setFarmName(transFarm.getToFarmName());
        toGroup.setOrgId(fromGroup.getOrgId());       //转入公司
        toGroup.setOrgName(fromGroup.getOrgName());
        toGroup.setCreatorId(0L);    //创建人id = 0, 标识系统自动创建
        return doctorGroupManager.createNewGroup(toGroup, newGroupInput);
    }

    private DoctorBarn getBarn(Long barnId) {
        return RespHelper.orServEx(doctorBarnReadService.findBarnById(barnId));
    }
}
