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
        PigType groupType = this.checkTurnSeedData(group.getPigType(), barn.getPigType());

        //1. 转换转种猪事件
        DoctorTurnSeedGroupEvent turnSeedEvent = BeanMapper.map(turnSeed, DoctorTurnSeedGroupEvent.class);

        //2. 创建转种猪事件
        DoctorGroupEvent<DoctorTurnSeedGroupEvent> event = dozerGroupEvent(group, GroupEventType.TURN_SEED, turnSeed);
        event.setExtraMap(turnSeedEvent);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, event, groupTrack), GroupEventType.TURN_SEED);

        //发布统计事件
        publistGroupAndBarn(group.getOrgId(), group.getFarmId(), group.getId(), group.getCurrentBarnId(), event.getId());

        //触发其他事件
        switch (groupType) {
            case RESERVE_SOW :
                if(Objects.equals(barn.getPigType(), PigType.MATE_SOW.getValue())){
                    // TODO 触发猪进场事件
                }
                if(Objects.equals(barn.getPigType(), PigType.PREG_SOW.getValue())){
                    //TODO 触发去妊娠舍事件
                }
                break;
            case RESERVE_BOAR :
                //TODO 触发猪进场事件
                break;
        }
    }

    @Override
    protected <E extends BaseGroupEdit> void editEvent(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorGroupEvent event, E edit) {

    }

    private PigType checkTurnSeedData(Integer groupType, Integer barnType){
        PigType type = PigType.from(groupType);
        if(type == null){
            throw new ServiceException("group.can.not.turn.seed");
        }

        switch (type) {
            // 当猪的来源是后备群中的种母猪 (PigType.RESERVE_SOW) 时, 转入猪舍只允许为 配种舍(PigType.MATE_SOW) 或 妊娠舍(PigType.PREG_SOW)
            case RESERVE_SOW :
                if(!Objects.equals(barnType, PigType.MATE_SOW.getValue()) && !Objects.equals(barnType, PigType.PREG_SOW.getValue())){
                    throw new ServiceException("barn.can.not.turn.seed");
                }
                break;
            // 当猪的来源是后备群中的种公猪 (PigType.RESERVE_BOAR) 时, 转入猪舍只允许为 种公猪舍(PigType.BOAR)
            case RESERVE_BOAR :
                if(!Objects.equals(barnType, PigType.BOAR.getValue())){
                    throw new ServiceException("barn.can.not.turn.seed");
                }
                break;
            // 当猪的来源不是以上两种时, 抛出异常
            default:
                throw new ServiceException("group.can.not.turn.seed");
        }
        return type;
    }

}
