package io.terminus.doctor.event.handler.group;

import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorTurnSeedGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
@Component
public class DoctorTurnSeedGroupEventHandler extends DoctorAbstractGroupEventHandler {

    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorCommonGroupEventHandler doctorCommonGroupEventHandler;

    @Autowired
    public DoctorTurnSeedGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                           DoctorGroupEventDao doctorGroupEventDao,
                                           DoctorGroupTrackDao doctorGroupTrackDao,
                                           DoctorBarnReadService doctorBarnReadService,
                                           CoreEventDispatcher coreEventDispatcher,
                                           DoctorCommonGroupEventHandler doctorCommonGroupEventHandler) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, coreEventDispatcher, doctorGroupEventDao, doctorBarnReadService);
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorCommonGroupEventHandler = doctorCommonGroupEventHandler;
    }
    
    @Override
    protected <I extends BaseGroupInput> void handleEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorTurnSeedGroupInput turnSeed = (DoctorTurnSeedGroupInput) input;
        DoctorBarn toBarn = getBarnById(turnSeed.getToBarnId());

        //0. 校验数据
        checkQuantity(groupTrack.getQuantity(), 1); // 确保 原数量 >= 1
        checkTurnSeedData(group.getPigType(), toBarn.getPigType());

        //1. 转换转种猪事件
        DoctorTurnSeedGroupEvent turnSeedEvent = BeanMapper.map(turnSeed, DoctorTurnSeedGroupEvent.class);

        //2. 创建转种猪事件
        DoctorGroupEvent<DoctorTurnSeedGroupEvent> event = dozerGroupEvent(group, GroupEventType.TURN_SEED, turnSeed);
        event.setExtraMap(turnSeedEvent);
        event.setQuantity(1);
        event.setAvgDayAge(groupTrack.getAvgDayAge());  //日龄取猪群的平均日龄
        event.setWeight(turnSeed.getWeight());
        event.setAvgWeight(turnSeed.getWeight());
        event.setOtherBarnId(toBarn.getId());          //目标猪舍id
        event.setOtherBarnType(toBarn.getPigType());   //目标猪舍类型
        doctorGroupEventDao.create(event);
        turnSeed.setRelGroupEventId(event.getId());

        //获取本次转种猪的性别
        DoctorPig.PIG_TYPE sex = getSex(toBarn.getPigType());

        //3.更新猪群跟踪
        //数量 - 1
        groupTrack.setQuantity(groupTrack.getQuantity() - 1);
        groupTrack.setBoarQty(getBoarQty(sex, groupTrack.getBoarQty()));
        groupTrack.setSowQty(groupTrack.getQuantity() - groupTrack.getBoarQty());
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, event, groupTrack), GroupEventType.TURN_SEED);

        //5.判断猪群数量, 如果=0 触发关闭猪群事件, 同时生成批次总结
        if (Objects.equals(groupTrack.getQuantity(), 0)) {
            doctorCommonGroupEventHandler.autoGroupEventClose(group, groupTrack, turnSeed, event.getEventAt(), turnSeed.getFcrFeed());

            DoctorGroupEvent closeEvent = doctorGroupEventDao.findByRelGroupEventId(event.getId());
            turnSeed.setRelGroupEventId(closeEvent.getId());    //如果发生关闭猪群事件，关联事件id要换下
        }

        //6.判断公母猪, 触发进场事件
        doctorCommonGroupEventHandler.autoPigEntryEvent(sex, turnSeed, group, toBarn);


        //发布统计事件
        publistGroupAndBarn(group.getOrgId(), group.getFarmId(), group.getId(), group.getCurrentBarnId(), event.getId());
    }

    //后备舍又他妈不分公母了, 艹
    //后备猪 => 配种舍/妊娠舍/种公猪舍
    private static PigType checkTurnSeedData(Integer groupType, Integer barnType){
        PigType type = PigType.from(groupType);

        //校验猪群类型: 后备群
        if(type == null || !PigType.HOUBEI_TYPES.contains(type.getValue())){
            throw new ServiceException("group.can.not.turn.seed");
        }

        //校验转入猪舍类型
        if (!PigType.MATING_TYPES.contains(barnType) && barnType != PigType.BOAR.getValue()) {
            throw new ServiceException("barn.can.not.turn.seed");

        }
        return type;
    }

    //获取转种猪性别
    private static DoctorPig.PIG_TYPE getSex(Integer toBarnType) {
        if (PigType.MATING_TYPES.contains(toBarnType)) {
            return DoctorPig.PIG_TYPE.SOW;
        }
        return DoctorPig.PIG_TYPE.BOAR;
    }

    //如果是公猪并且数量大于0 就 -1
    private static int getBoarQty(DoctorPig.PIG_TYPE sex, Integer oldQty) {
        if (oldQty <= 0) {
            return 0;
        }
        if (sex.equals(DoctorPig.PIG_TYPE.BOAR)) {
            return oldQty - 1;
        }
        return oldQty;
    }
}
