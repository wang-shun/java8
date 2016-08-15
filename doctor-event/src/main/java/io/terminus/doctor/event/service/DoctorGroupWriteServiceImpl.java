package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.group.edit.DoctorAntiepidemicGroupEdit;
import io.terminus.doctor.event.dto.event.group.edit.DoctorChangeGroupEdit;
import io.terminus.doctor.event.dto.event.group.edit.DoctorDiseaseGroupEdit;
import io.terminus.doctor.event.dto.event.group.edit.DoctorLiveStockGroupEdit;
import io.terminus.doctor.event.dto.event.group.edit.DoctorMoveInGroupEdit;
import io.terminus.doctor.event.dto.event.group.edit.DoctorNewGroupEdit;
import io.terminus.doctor.event.dto.event.group.edit.DoctorTransEdit;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorSowMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.enums.BoarEntryType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.handler.group.DoctorAntiepidemicGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorChangeGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorCloseGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorCommonGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorDiseaseGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorLiveStockGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorMoveInGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorTransFarmGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorTransGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorTurnSeedGroupEventHandler;
import io.terminus.doctor.event.manager.DoctorGroupEventManager;
import io.terminus.doctor.event.manager.DoctorGroupManager;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.doctor.common.utils.RespHelper.orServEx;

/**
 * Desc: 猪群卡片表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
@RpcProvider
public class DoctorGroupWriteServiceImpl implements DoctorGroupWriteService {

    private final DoctorGroupDao doctorGroupDao;
    private final DoctorGroupTrackDao doctorGroupTrackDao;
    private final DoctorGroupManager doctorGroupManager;
    private final DoctorGroupEventManager doctorGroupEventManager;
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorGroupSnapshotDao doctorGroupSnapshotDao;
    private final DoctorCommonGroupEventHandler doctorCommonGroupEventHandler;
    private final DoctorBarnReadService doctorBarnReadService;
    private final DoctorPigEventWriteService doctorPigEventWriteService;

    @Autowired
    public DoctorGroupWriteServiceImpl(DoctorGroupDao doctorGroupDao,
                                       DoctorGroupTrackDao doctorGroupTrackDao,
                                       DoctorGroupManager doctorGroupManager,
                                       DoctorGroupEventManager doctorGroupEventManager,
                                       DoctorGroupEventDao doctorGroupEventDao,
                                       DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                       DoctorCommonGroupEventHandler doctorCommonGroupEventHandler,
                                       DoctorBarnReadService doctorBarnReadService,
                                       DoctorPigEventWriteService doctorPigEventWriteService) {
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorGroupManager = doctorGroupManager;
        this.doctorGroupEventManager = doctorGroupEventManager;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorCommonGroupEventHandler = doctorCommonGroupEventHandler;
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorPigEventWriteService = doctorPigEventWriteService;
    }

    @Override
    public Response<Long> createNewGroup(DoctorGroup group, @Valid DoctorNewGroupInput newGroupInput) {
        try {
            Long groupId = doctorGroupManager.createNewGroup(group, newGroupInput);
            return Response.ok(groupId);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("create group failed, group:{}, newGroupInput:{}, cause:{}", group, newGroupInput, Throwables.getStackTraceAsString(e));
            return Response.fail("group.create.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventAntiepidemic(DoctorGroupDetail groupDetail, @Valid DoctorAntiepidemicGroupInput antiepidemic) {
        try {
            doctorGroupEventManager.handleEvent(groupDetail, antiepidemic, DoctorAntiepidemicGroupEventHandler.class);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("groupEventAntiepidemic failed, groupDetail:{}, antiepidemic:{}, cause:{}", groupDetail, antiepidemic, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventAntiepidemic failed, groupDetail:{}, antiepidemic:{}, cause:{}", groupDetail, antiepidemic, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.antiepidemic.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventChange(DoctorGroupDetail groupDetail, @Valid DoctorChangeGroupInput change) {
        try {
            doctorGroupEventManager.handleEvent(groupDetail, change, DoctorChangeGroupEventHandler.class);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("groupEventChange failed, groupDetail:{}, change:{}, cause:{}", groupDetail, change, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventChange failed, groupDetail:{}, change:{}, cause:{}", groupDetail, change, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.change.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventClose(DoctorGroupDetail groupDetail, @Valid DoctorCloseGroupInput close) {
        try {
            doctorGroupEventManager.handleEvent(groupDetail, close, DoctorCloseGroupEventHandler.class);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("groupEventClose failed, groupDetail:{}, close:{}, cause:{}", groupDetail, close, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventClose failed, groupDetail:{}, close:{}, cause:{}", groupDetail, close, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.close.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventDisease(DoctorGroupDetail groupDetail, @Valid DoctorDiseaseGroupInput disease) {
        try {
            doctorGroupEventManager.handleEvent(groupDetail, disease, DoctorDiseaseGroupEventHandler.class);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("groupEventDisease failed, groupDetail:{}, disease:{}, cause:{}", groupDetail, disease, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventDisease failed, groupDetail:{}, disease:{}, cause:{}", groupDetail, disease, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.disease.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventLiveStock(DoctorGroupDetail groupDetail, @Valid DoctorLiveStockGroupInput liveStock) {
        try {
            doctorGroupEventManager.handleEvent(groupDetail, liveStock, DoctorLiveStockGroupEventHandler.class);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("groupEventLiveStock failed, groupDetail:{}, liveStock:{}, cause:{}", groupDetail, liveStock, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventLiveStock failed, groupDetail:{}, liveStock:{}, cause:{}", groupDetail, liveStock, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.liveStock.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventMoveIn(DoctorGroupDetail groupDetail, @Valid DoctorMoveInGroupInput moveIn) {
        try {
            doctorGroupEventManager.handleEvent(groupDetail, moveIn, DoctorMoveInGroupEventHandler.class);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("groupEventMoveIn failed, groupDetail:{}, moveIn:{}, cause:{}", groupDetail, moveIn, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventMoveIn failed, groupDetail:{}, moveIn:{}, cause:{}", groupDetail, moveIn, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.moveIn.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventTransFarm(DoctorGroupDetail groupDetail, @Valid DoctorTransFarmGroupInput transFarm) {
        try {
            doctorGroupEventManager.handleEvent(groupDetail, transFarm, DoctorTransFarmGroupEventHandler.class);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("groupEventTransFarm failed, groupDetail:{}, transFarm:{}, cause:{}", groupDetail, transFarm, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventTransFarm failed, groupDetail:{}, transFarm:{}, cause:{}", groupDetail, transFarm, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.transFarm.fail");
        }
    }

    @Override
    public Response<Long> groupEventTransGroup(DoctorGroupDetail groupDetail, @Valid DoctorTransGroupInput transGroup) {
        try {
            doctorGroupEventManager.handleEvent(groupDetail, transGroup, DoctorTransGroupEventHandler.class);
            if (Objects.equals(transGroup.getIsCreateGroup(), IsOrNot.YES.getValue())) {
                DoctorGroup toGroup = doctorGroupDao.findByFarmIdAndGroupCode(groupDetail.getGroup().getFarmId(), transGroup.getToGroupCode());
                return Response.ok(toGroup.getId());
            }
            return Response.ok(transGroup.getToGroupId());
        } catch (ServiceException e) {
            log.error("groupEventTransGroup failed, groupDetail:{}, transGroup:{}, cause:{}", groupDetail, transGroup, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventTransGroup failed, groupDetail:{}, transGroup:{}, cause:{}", groupDetail, transGroup, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.transGroup.fail");
        }
    }

    @Override
    public Response<Boolean> groupEventTurnSeed(DoctorGroupDetail groupDetail, @Valid DoctorTurnSeedGroupInput turnSeed) {
        try {
            // 1. 猪群本身的变化
            doctorGroupEventManager.handleEvent(groupDetail, turnSeed, DoctorTurnSeedGroupEventHandler.class);

            // 2. 判断猪群剩余数量, 如果剩余0, 则触发关闭猪群事件
            DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(groupDetail.getGroup().getId());
            if (groupTrack.getQuantity() == 0) {
                doctorCommonGroupEventHandler.autoGroupEventClose(doctorGroupDao.findById(groupDetail.getGroup().getId()), groupTrack, turnSeed);
            }

            // 3. 触发其他事件
            //由于不确定 group 会不会被修改, 还是重新从数据库查一遍保险
            DoctorGroup group = doctorGroupDao.findById(groupDetail.getGroup().getId());
            DoctorBarn barn = orServEx(doctorBarnReadService.findBarnById(turnSeed.getToBarnId()));
            PigType groupType = PigType.from(group.getPigType());
            switch (groupType) {
                case RESERVE_SOW :
                    if(Objects.equals(barn.getPigType(), PigType.MATE_SOW.getValue())){
                        // 进场事件
                        this.callPigEntryEvent(groupType, turnSeed, group, barn, null);
                    }
                    if(Objects.equals(barn.getPigType(), PigType.PREG_SOW.getValue())){
                        //TODO 触发配种事件
                    }
                    break;
                case RESERVE_BOAR :
                    // 进场事件
                    this.callPigEntryEvent(groupType, turnSeed, group, barn, null);
                    break;
            }

            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("groupEventTurnSeed failed, groupDetail:{}, turnSeed:{}, cause:{}", groupDetail, turnSeed, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventTurnSeed failed, groupDetail:{}, turnSeed:{}, cause:{}", groupDetail, turnSeed, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.turnSeed.fail");
        }
    }

    private void callPigEntryEvent(PigType groupType, DoctorTurnSeedGroupInput turnSeedInput, DoctorGroup group, DoctorBarn barn, Long relEventId){
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

        orServEx(doctorPigEventWriteService.pigEntryEvent(basicDto, farmEntryDto));
    }

    @Override
    public Response<Boolean> editEventNew(DoctorGroupDetail groupDetail, DoctorGroupEvent event, @Valid DoctorNewGroupEdit newEdit) {
        try {
            doctorGroupManager.editNewGroupEvent(groupDetail.getGroup(), groupDetail.getGroupTrack(), event, newEdit);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("edit new event failed, groupDetail:{}, event:{}, newEdit:{}, cause:{}",
                    groupDetail, event, newEdit, Throwables.getStackTraceAsString(e));
            return Response.fail("edit.group.event.fail");
        }
    }

    @Override
    public Response<Boolean> editEventAntiepidemic(DoctorGroupDetail groupDetail, DoctorGroupEvent event, @Valid DoctorAntiepidemicGroupEdit antiepidemic) {
        try {
            doctorGroupEventManager.editEvent(groupDetail, event, antiepidemic, DoctorAntiepidemicGroupEventHandler.class);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("edit antiepidemic event failed, groupDetail:{}, event:{}, antiepidemic:{}, cause:{}",
                    groupDetail, event, antiepidemic, Throwables.getStackTraceAsString(e));
            return Response.fail("edit.group.event.fail");
        }
    }

    @Override
    public Response<Boolean> editEventChange(DoctorGroupDetail groupDetail, DoctorGroupEvent event, @Valid DoctorChangeGroupEdit change) {
        try {
            doctorGroupEventManager.editEvent(groupDetail, event, change, DoctorChangeGroupEventHandler.class);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("edit change event failed, groupDetail:{}, event:{}, change:{}, cause:{}",
                    groupDetail, event, change, Throwables.getStackTraceAsString(e));
            return Response.fail("edit.group.event.fail");
        }
    }

    @Override
    public Response<Boolean> editEventDisease(DoctorGroupDetail groupDetail, DoctorGroupEvent event, @Valid DoctorDiseaseGroupEdit disease) {
        try {
            doctorGroupEventManager.editEvent(groupDetail, event, disease, DoctorDiseaseGroupEventHandler.class);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("edit disease event failed, groupDetail:{}, event:{}, disease:{}, cause:{}",
                    groupDetail, event, disease, Throwables.getStackTraceAsString(e));
            return Response.fail("edit.group.event.fail");
        }
    }

    @Override
    public Response<Boolean> editEventLiveStock(DoctorGroupDetail groupDetail, DoctorGroupEvent event, @Valid DoctorLiveStockGroupEdit liveStock) {
        try {
            doctorGroupEventManager.editEvent(groupDetail, event, liveStock, DoctorLiveStockGroupEventHandler.class);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("edit liveStock event failed, groupDetail:{}, event:{}, liveStock:{}, cause:{}",
                    groupDetail, event, liveStock, Throwables.getStackTraceAsString(e));
            return Response.fail("edit.group.event.fail");
        }
    }

    @Override
    public Response<Boolean> editEventMoveIn(DoctorGroupDetail groupDetail, DoctorGroupEvent event, @Valid DoctorMoveInGroupEdit moveIn) {
        try {
            doctorGroupEventManager.editEvent(groupDetail, event, moveIn, DoctorMoveInGroupEventHandler.class);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("edit moveIn event failed, groupDetail:{}, event:{}, moveIn:{}, cause:{}",
                    groupDetail, event, moveIn, Throwables.getStackTraceAsString(e));
            return Response.fail("edit.group.event.fail");
        }
    }

    @Override
    public Response<Boolean> editEventTrans(DoctorGroupDetail groupDetail, DoctorGroupEvent event, @Valid DoctorTransEdit trans) {
        try {
            doctorGroupEventManager.editEvent(groupDetail, event, trans, DoctorTransGroupEventHandler.class);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("edit trans event failed, groupDetail:{}, event:{}, trans:{}, cause:{}",
                    groupDetail, event, trans, Throwables.getStackTraceAsString(e));
            return Response.fail("edit.group.event.fail");
        }
    }

    @Override
    public Response<Boolean> rollbackGroupEvent(DoctorGroupEvent groupEvent, Long reveterId, String reveterName) {
        try {
            doctorGroupEventManager.rollbackEvent(groupEvent, reveterId, reveterName);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            log.error("rollback group event failed, groupEvent:{}, reveterId:{}, reveterName:{}, cause:{}",
                    groupEvent, reveterId, reveterName, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("rollback group event failed, groupEvent:{}, reveterId:{}, reveterName:{}, cause:{}",
                    groupEvent, reveterId, reveterName, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.rollback.fail");
        }
    }

    @Override
    public Response<Long> sowGroupEventMoveIn(@Valid DoctorSowMoveInGroupInput input) {
        try {
            List<DoctorGroup> groups = doctorGroupDao.findByCurrentBarnId(input.getToBarnId());

            //没有猪群, 新建
            if (!notEmpty(groups)) {
                return Response.ok(doctorCommonGroupEventHandler.sowGroupEventMoveIn(input));
            }

            //多个猪群, 报错
            if (groups.size() > 1) {
                throw new ServiceException("group.count.over.1");
            }

            //已有猪群, 转入
            DoctorGroup group = groups.get(0);
            DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(group.getId());
            RespHelper.orServEx(groupEventMoveIn(new DoctorGroupDetail(group, groupTrack), input));
            return Response.ok(group.getId());

        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("sow event move in group event failed, input:{}, cause:{}", input, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.moveIn.fail");
        }
    }

    @Override
    public Response<Boolean> incrDayAge() {
        try {
            List<DoctorGroup> groups = doctorGroupDao.fingByStatus(DoctorGroup.Status.CREATED.getValue());
            if (notEmpty(groups)) {
                doctorGroupTrackDao.incrDayAge(groups.stream().map(DoctorGroup::getId).collect(Collectors.toList()));
            }
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("incr day age failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("incr.group.dayAge.fail");
        }
    }

    @Override
    public Response<Long> createGroup(DoctorGroup group) {
        try {
            doctorGroupDao.create(group);
            return Response.ok(group.getId());
        } catch (Exception e) {
            log.error("create group failed, group:{}, cause:{}", group, Throwables.getStackTraceAsString(e));
            return Response.fail("group.create.fail");
        }
    }

    @Override
    public Response<Long> createGroupTrack(DoctorGroupTrack groupTrack) {
        try {
            doctorGroupTrackDao.create(groupTrack);
            return Response.ok(groupTrack.getId());
        } catch (Exception e) {
            log.error("create groupTrack failed, groupTrack:{}, cause:{}", groupTrack, Throwables.getStackTraceAsString(e));
            return Response.fail("groupTrack.create.fail");
        }
    }

    @Override
    public Response<Long> createGroupEvent(DoctorGroupEvent groupEvent) {
        try {
            doctorGroupEventDao.create(groupEvent);
            return Response.ok(groupEvent.getId());
        } catch (Exception e) {
            log.error("create groupEvent failed, groupEvent:{}, cause:{}", groupEvent, Throwables.getStackTraceAsString(e));
            return Response.fail("groupEvent.create.fail");
        }
    }

    @Override
    public Response<Long> createGroupSnapShot(DoctorGroupSnapshot groupSnapshot) {
        try {
            doctorGroupSnapshotDao.create(groupSnapshot);
            return Response.ok(groupSnapshot.getId());
        } catch (Exception e) {
            log.error("create groupSnapshot failed, groupSnapshot:{}, cause:{}", groupSnapshot, Throwables.getStackTraceAsString(e));
            return Response.fail("groupSnapshot.create.fail");
        }
    }
}
