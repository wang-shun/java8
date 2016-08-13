package io.terminus.doctor.event.handler.group;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorTurnSeedGroupEvent;
import io.terminus.doctor.event.dto.event.group.edit.BaseGroupEdit;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.enums.BoarEntryType;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.event.TurnSeedEvent;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorPigEventWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static io.terminus.doctor.common.utils.RespHelper.orServEx;
import static java.util.Objects.isNull;

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
    private final DoctorCommonGroupEventHandler doctorCommonGroupEventHandler;
    private final DoctorPigEventWriteService doctorPigEventWriteService;
    private final DoctorPigEventManager doctorPigEventManager;

    @Autowired
    public DoctorTurnSeedGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                           DoctorGroupEventDao doctorGroupEventDao,
                                           DoctorGroupTrackDao doctorGroupTrackDao,
                                           DoctorBarnReadService doctorBarnReadService,
                                           CoreEventDispatcher coreEventDispatcher,
                                           DoctorCommonGroupEventHandler doctorCommonGroupEventHandler,
                                           DoctorPigEventWriteService doctorPigEventWriteService,
                                           DoctorPigEventManager doctorPigEventManager) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, coreEventDispatcher, doctorGroupEventDao, doctorBarnReadService);
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorCommonGroupEventHandler = doctorCommonGroupEventHandler;
        this.doctorPigEventWriteService = doctorPigEventWriteService;
        this.doctorPigEventManager = doctorPigEventManager;
    }
    
    @Override
    protected <I extends BaseGroupInput> void handleEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorTurnSeedGroupInput turnSeed = (DoctorTurnSeedGroupInput) input;
        TurnSeedEvent eventDataToPublish = new TurnSeedEvent();

        DoctorBarn barn = orServEx(doctorBarnReadService.findBarnById(turnSeed.getToBarnId()));
        // 检查数据
        PigType groupType = this.checkTurnSeedData(group.getPigType(), barn.getPigType());
        checkQuantity(groupTrack.getQuantity(), 1); // 确保 原数量 >= 1
        this.checkSex(groupTrack, groupType);

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

        //5.判断猪群剩余数量, 如果剩余0, 则触发关闭猪群事件
        if (groupTrack.getQuantity() == 0) {
   //         doctorCommonGroupEventHandler.autoGroupEventClose(group, groupTrack, turnSeed);
        }

        //触发其他事件
        switch (groupType) {
            case RESERVE_SOW :
                if(Objects.equals(barn.getPigType(), PigType.MATE_SOW.getValue())){
                    // 进场事件
                    this.callEntryHandler(eventDataToPublish, groupType, turnSeed, group, barn, event.getId());
                }
                if(Objects.equals(barn.getPigType(), PigType.PREG_SOW.getValue())){
                    //TODO 触发配种事件
                }
                break;
            case RESERVE_BOAR :
                // 进场事件
                this.callEntryHandler(eventDataToPublish, groupType, turnSeed, group, barn, event.getId());
                break;
        }
Integer.valueOf("ee");
        //发布统计事件
        publistGroupAndBarn(group.getOrgId(), group.getFarmId(), group.getId(), group.getCurrentBarnId(), event.getId());
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
//                if(!Objects.equals(barnType, PigType.MATE_SOW.getValue()) && !Objects.equals(barnType, PigType.PREG_SOW.getValue())){
//                    throw new ServiceException("barn.can.not.turn.seed");
//                }
                if(!Objects.equals(barnType, PigType.MATE_SOW.getValue())){
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

    private void callEntryHandler(TurnSeedEvent eventDataToPublish, PigType groupType, DoctorTurnSeedGroupInput turnSeedInput, DoctorGroup group, DoctorBarn barn, Long relEventId){
        DoctorBasicInputInfoDto basicDto = new DoctorBasicInputInfoDto();
        DoctorFarmEntryDto farmEntryDto = new DoctorFarmEntryDto();

        switch (groupType) {
            case RESERVE_BOAR:
                basicDto.setPigType(DoctorPig.PIG_TYPE.BOAR.getKey());
                farmEntryDto.setBoarTypeId(BoarEntryType.HGZ.getKey());
                farmEntryDto.setBoarTypeName(BoarEntryType.HGZ.getCode());
                break;
            case RESERVE_SOW:
                basicDto.setPigType(DoctorPig.PIG_TYPE.SOW.getKey());
                farmEntryDto.setParity(1);
                break;
        }

        basicDto.setPigCode(turnSeedInput.getPigCode());
        basicDto.setBarnId(barn.getId());
        basicDto.setBarnName(barn.getName());
        basicDto.setFarmId(group.getFarmId());
        basicDto.setFarmName(group.getFarmName());
        basicDto.setOrgId(group.getOrgId());
        basicDto.setOrgName(group.getOrgName());
        basicDto.setEventType(PigEvent.ENTRY.getKey());
        basicDto.setEventName(PigEvent.ENTRY.getName());
        basicDto.setEventDesc(PigEvent.ENTRY.getDesc());
        basicDto.setRelEventId(relEventId);
        basicDto.setStaffId(turnSeedInput.getCreatorId());
        basicDto.setStaffName(turnSeedInput.getCreatorName());

        farmEntryDto.setPigType(basicDto.getPigType());
        farmEntryDto.setPigCode(turnSeedInput.getPigCode());
        farmEntryDto.setBirthday(DateUtil.toDate(turnSeedInput.getBirthDate()));
        farmEntryDto.setInFarmDate(DateUtil.toDate(turnSeedInput.getTransInAt()));
        farmEntryDto.setBarnId(barn.getId());
        farmEntryDto.setBarnName(barn.getName());
        farmEntryDto.setSource(PigSource.LOCAL.getKey());
        farmEntryDto.setBreed(turnSeedInput.getBreedId());
        farmEntryDto.setBreedName(turnSeedInput.getBreedName());
        farmEntryDto.setBreedType(turnSeedInput.getGeneticId());
        farmEntryDto.setBreedTypeName(turnSeedInput.getGeneticName());
        farmEntryDto.setMotherCode(turnSeedInput.getMotherEarCode());
        farmEntryDto.setEarCode(turnSeedInput.getEarCode());

        // validate 左右乳头数量大于0
        if(Objects.equals(farmEntryDto.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())){
            checkState(isNull(farmEntryDto.getLeft()) || farmEntryDto.getLeft()>=0, "input.sowLeft.error");
            checkState(isNull(farmEntryDto.getRight()) || farmEntryDto.getRight()>=0, "input.sowRight.error");
        }

        Map<String,Object> extra = Maps.newHashMap();
        BeanMapper.copy(farmEntryDto, extra);
        try {
            eventDataToPublish.setCreateCasualPigEventResult(doctorPigEventManager.createCasualPigEvent(basicDto, extra));
            eventDataToPublish.setBasicInputInfoDto(basicDto);

        }catch(IllegalStateException e){
            log.error("pig entry event illegal state fail, basicInfo:{}, doctorFarmEntryDto:{}, cause:{}", basicDto, farmEntryDto, Throwables.getStackTraceAsString(e));
            throw new ServiceException(e.getMessage());
        }catch (Exception e){
            log.error("pig entry event create fail,basicInfo:{}, doctorFarmEntryDto:{}, cause:{}", basicDto, farmEntryDto, Throwables.getStackTraceAsString(e));
            throw new ServiceException("create.entryEvent.fail");
        }

    }

}
