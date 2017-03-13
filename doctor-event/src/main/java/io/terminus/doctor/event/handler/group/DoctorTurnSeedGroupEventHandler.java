package io.terminus.doctor.event.handler.group;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.DoctorTurnSeedGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorEventRelation;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static io.terminus.doctor.common.utils.Checks.expectNotNull;

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
                                           DoctorBarnDao doctorBarnDao,
                                           DoctorCommonGroupEventHandler doctorCommonGroupEventHandler) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, doctorGroupEventDao, doctorBarnDao);
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorCommonGroupEventHandler = doctorCommonGroupEventHandler;
    }

    @Override
    public <I extends BaseGroupInput> DoctorGroupEvent buildGroupEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.TURN_SEED.getValue());

        DoctorTurnSeedGroupInput turnSeed = (DoctorTurnSeedGroupInput) input;
        DoctorBarn toBarn = getBarnById(turnSeed.getToBarnId());

        //0. 校验数据
        checkQuantity(groupTrack.getQuantity(), 1); // 确保 原数量 >= 1
        checkTurnSeedData(group.getPigType(), toBarn.getPigType());

        //1. 转换转种猪事件
        DoctorTurnSeedGroupEvent turnSeedEvent = BeanMapper.map(turnSeed, DoctorTurnSeedGroupEvent.class);
        turnSeedEvent.setToBarnType(toBarn.getPigType());


        //2. 创建转种猪事件
        DoctorGroupEvent<DoctorTurnSeedGroupEvent> event = dozerGroupEvent(group, GroupEventType.TURN_SEED, turnSeed);
        event.setExtraMap(turnSeedEvent);
        event.setQuantity(1);

        event.setWeight(turnSeed.getWeight());
        event.setAvgWeight(turnSeed.getWeight());
        event.setOtherBarnId(toBarn.getId());          //目标猪舍id
        event.setOtherBarnType(toBarn.getPigType());   //目标猪舍类型
        return event;
    }

    @Override
    public DoctorGroupTrack elicitGroupTrack(DoctorGroupEvent preEvent, DoctorGroupEvent event, DoctorGroupTrack track) {
        DoctorTurnSeedGroupEvent doctorTurnSeedGroupEvent = JSON_MAPPER.fromJson(event.getExtra(), DoctorTurnSeedGroupEvent.class);
        if(Arguments.isNull(doctorTurnSeedGroupEvent)) {
            log.error("parse doctorTurnSeedGroupEvent faild, doctorGroupEvent = {}", event);
            throw new JsonResponseException("group.event.info.broken");
        }

        DoctorPig.PigSex sex = getSex(doctorTurnSeedGroupEvent.getToBarnType());

        track.setQuantity(track.getQuantity() - 1);
        track.setBoarQty(getBoarQty(sex, track.getBoarQty()));
        track.setSowQty(track.getQuantity() - track.getBoarQty());
        return track;
    }

    @Override
    protected <I extends BaseGroupInput> void handleEvent(List<DoctorEventInfo> eventInfoList, DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.TURN_SEED.getValue());

        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorTurnSeedGroupInput turnSeed = (DoctorTurnSeedGroupInput) input;
        DoctorBarn toBarn = getBarnById(turnSeed.getToBarnId());

        //0. 校验数据
        checkQuantity(groupTrack.getQuantity(), 1); // 确保 原数量 >= 1
        checkTurnSeedData(group.getPigType(), toBarn.getPigType());

        //1. 转换转种猪事件
        DoctorTurnSeedGroupEvent turnSeedEvent = BeanMapper.map(turnSeed, DoctorTurnSeedGroupEvent.class);
        turnSeedEvent.setToBarnType(toBarn.getPigType());


        //2. 创建转种猪事件
        DoctorGroupEvent<DoctorTurnSeedGroupEvent> event = dozerGroupEvent(group, GroupEventType.TURN_SEED, turnSeed);
        event.setExtraMap(turnSeedEvent);
        event.setQuantity(1);

        event.setWeight(turnSeed.getWeight());
        event.setAvgWeight(turnSeed.getWeight());
        event.setOtherBarnId(toBarn.getId());          //目标猪舍id
        event.setOtherBarnType(toBarn.getPigType());   //目标猪舍类型
        doctorGroupEventDao.create(event);
        turnSeed.setRelGroupEventId(event.getId());

        //获取本次转种猪的性别
        DoctorPig.PigSex sex = getSex(toBarn.getPigType());

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
            doctorCommonGroupEventHandler.autoGroupEventClose(eventInfoList, group, groupTrack, turnSeed, event.getEventAt(), turnSeed.getFcrFeed());

            Long toGroupEventId = doctorEventRelationDao.findByOriginAndType(event.getId(), DoctorEventRelation.TargetType.GROUP.getValue()).getTriggerEventId();
            DoctorGroupEvent closeEvent = doctorGroupEventDao.findById(toGroupEventId);
            turnSeed.setRelGroupEventId(closeEvent.getId());    //如果发生关闭猪群事件，关联事件id要换下
        }

        //6.判断公母猪, 触发进场事件
        doctorCommonGroupEventHandler.autoPigEntryEvent(eventInfoList, sex, turnSeed, group, toBarn);


        //发布统计事件
        //publistGroupAndBarn(event);
    }

    //后备舍又他妈不分公母了, 艹
    //后备猪 => 配种舍/妊娠舍/种公猪舍
    private static PigType checkTurnSeedData(Integer groupType, Integer barnType){
        PigType type = expectNotNull(PigType.from(groupType), "pig.type.not.null");

        //校验猪群类型: 后备群
        if(!PigType.HOUBEI_TYPES.contains(type.getValue())){
            throw new InvalidException("group.can.not.turn.seed", type.getDesc());
        }
        //校验转入猪舍类型
        if (!PigType.MATING_TYPES.contains(barnType) && barnType != PigType.BOAR.getValue()) {
            throw new InvalidException("barn.can.not.turn.seed", PigType.from(barnType).getDesc());

        }
        return type;
    }

    //获取转种猪性别
    private static DoctorPig.PigSex getSex(Integer toBarnType) {
        if (PigType.MATING_TYPES.contains(toBarnType)) {
            return DoctorPig.PigSex.SOW;
        }
        return DoctorPig.PigSex.BOAR;
    }

    //如果是公猪并且数量大于0 就 -1
    private static int getBoarQty(DoctorPig.PigSex sex, Integer oldQty) {
        if (oldQty <= 0) {
            return 0;
        }
        if (sex.equals(DoctorPig.PigSex.BOAR)) {
            return oldQty - 1;
        }
        return oldQty;
    }

    @Override
    public boolean checkDoctorGroupEvent(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent) {
        if(doctorGroupTrack.getQuantity() < 1){
            return false;
        }
        return true;
    }
}
