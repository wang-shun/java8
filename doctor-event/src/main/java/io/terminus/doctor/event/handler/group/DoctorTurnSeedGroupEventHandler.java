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
import io.terminus.doctor.event.dto.event.group.edit.BaseGroupEdit;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.terminus.doctor.common.utils.RespHelper.orServEx;

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
    private final DoctorBarnReadService doctorBarnReadService;

    @Autowired
    public DoctorTurnSeedGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                           DoctorGroupEventDao doctorGroupEventDao,
                                           DoctorGroupTrackDao doctorGroupTrackDao,
                                           DoctorBarnReadService doctorBarnReadService,
                                           CoreEventDispatcher coreEventDispatcher) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, coreEventDispatcher, doctorGroupEventDao, doctorBarnReadService);
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorBarnReadService = doctorBarnReadService;
    }
    
    @Override
    protected <I extends BaseGroupInput> void handleEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorTurnSeedGroupInput turnSeed = (DoctorTurnSeedGroupInput) input;

        DoctorBarn barn = orServEx(doctorBarnReadService.findBarnById(turnSeed.getToBarnId()));
        // 检查数据
        checkQuantity(groupTrack.getQuantity(), 1); // 确保 原数量 >= 1
        this.checkSex(groupTrack, PigType.from(group.getPigType()));

        //1. 转换转种猪事件
        DoctorTurnSeedGroupEvent turnSeedEvent = BeanMapper.map(turnSeed, DoctorTurnSeedGroupEvent.class);

        //2. 创建转种猪事件
        DoctorGroupEvent<DoctorTurnSeedGroupEvent> event = dozerGroupEvent(group, GroupEventType.TURN_SEED, turnSeed);
        event.setExtraMap(turnSeedEvent);
        event.setQuantity(1);
        event.setAvgDayAge(groupTrack.getAvgDayAge());  //转群的日龄不需要录入, 直接取猪群的日龄
        event.setWeight(turnSeed.getWeight());
        event.setAvgWeight(turnSeed.getWeight());
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        groupTrack.setQuantity(groupTrack.getQuantity() - 1);
        if(turnSeed.getWeight() != null && turnSeed.getWeight() > 0 && groupTrack.getWeight() != null && groupTrack.getWeight() >= turnSeed.getWeight()){
            groupTrack.setWeight(groupTrack.getWeight() - turnSeed.getWeight());
        }
        checkQuantityEqual(groupTrack.getQuantity(), groupTrack.getBoarQty(), groupTrack.getSowQty());
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, event, groupTrack), GroupEventType.TURN_SEED);

        //发布统计事件
        publistGroupAndBarn(group.getOrgId(), group.getFarmId(), group.getId(), group.getCurrentBarnId(), event.getId());
    }

    @Override
    protected <E extends BaseGroupEdit> void editEvent(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorGroupEvent event, E edit) {

    }

    private void checkSex(DoctorGroupTrack groupTrack, PigType groupType){
        switch (groupType) {
            case RESERVE_BOAR:
                checkQuantity(groupTrack.getBoarQty(), 1); // 确保 原公猪数量 >= 1
                groupTrack.setBoarQty(groupTrack.getBoarQty() - 1);
                break;
            case RESERVE_SOW:
                checkQuantity(groupTrack.getSowQty(), 1); // 确保 原母猪数量 >= 1
                groupTrack.setSowQty(groupTrack.getSowQty() - 1);
                break;
        }
    }

}
