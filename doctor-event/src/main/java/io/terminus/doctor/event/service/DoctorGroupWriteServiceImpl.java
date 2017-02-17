package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorGroupInputInfo;
import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInputInfo;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.handler.group.DoctorAntiepidemicGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorChangeGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorCloseGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorDiseaseGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorLiveStockGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorMoveInGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorTransFarmGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorTransGroupEventHandler;
import io.terminus.doctor.event.handler.group.DoctorTurnSeedGroupEventHandler;
import io.terminus.doctor.event.manager.DoctorGroupEventManager;
import io.terminus.doctor.event.manager.DoctorGroupManager;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

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

    @Autowired
    private DoctorGroupDao doctorGroupDao;
    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired
    private DoctorGroupManager doctorGroupManager;
    @Autowired
    private DoctorGroupEventManager doctorGroupEventManager;
    @Autowired
    private DoctorGroupEventDao doctorGroupEventDao;
    @Autowired
    private DoctorGroupSnapshotDao doctorGroupSnapshotDao;
    @Autowired
    private CoreEventDispatcher coreEventDispatcher;
    @Autowired(required = false)
    private Publisher publisher;

    @Override
    public RespWithEx<Long> createNewGroup(DoctorGroup group, @Valid DoctorNewGroupInput newGroupInput) {
        try {
            List<DoctorEventInfo> eventInfoList = Lists.newArrayList();
            Long groupId = doctorGroupManager.createNewGroup(eventInfoList, group, newGroupInput);
            DoctorPigEventManager.checkAndPublishEvent(eventInfoList, coreEventDispatcher, publisher);
            return RespWithEx.ok(groupId);
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("create group failed, group:{}, newGroupInput:{}, cause:{}", group, newGroupInput, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("group.create.fail");
        }
    }

    @Override
    public RespWithEx<Boolean> groupEventAntiepidemic(DoctorGroupDetail groupDetail, @Valid DoctorAntiepidemicGroupInput antiepidemic) {
        try {
            List<DoctorEventInfo> eventInfoList = doctorGroupEventManager.handleEvent(groupDetail, antiepidemic, DoctorAntiepidemicGroupEventHandler.class);
            DoctorPigEventManager.checkAndPublishEvent(eventInfoList, coreEventDispatcher, publisher);

            return RespWithEx.ok(Boolean.TRUE);
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            log.error("groupEventAntiepidemic failed, groupDetail:{}, antiepidemic:{}, cause:{}", groupDetail, antiepidemic, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventAntiepidemic failed, groupDetail:{}, antiepidemic:{}, cause:{}", groupDetail, antiepidemic, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("group.event.antiepidemic.fail");
        }
    }

    @Override
    public RespWithEx<Boolean> groupEventChange(DoctorGroupDetail groupDetail, @Valid DoctorChangeGroupInput change) {
        try {
            List<DoctorEventInfo> eventInfoList = doctorGroupEventManager.handleEvent(groupDetail, change, DoctorChangeGroupEventHandler.class);
            DoctorPigEventManager.checkAndPublishEvent(eventInfoList, coreEventDispatcher, publisher);

            return RespWithEx.ok(Boolean.TRUE);
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            log.error("groupEventChange failed, groupDetail:{}, change:{}, cause:{}", groupDetail, change, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventChange failed, groupDetail:{}, change:{}, cause:{}", groupDetail, change, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("group.event.change.fail");
        }
    }

    @Override
    public RespWithEx<Boolean> groupEventClose(DoctorGroupDetail groupDetail, @Valid DoctorCloseGroupInput close) {
        try {
            List<DoctorEventInfo> eventInfoList = doctorGroupEventManager.handleEvent(groupDetail, close, DoctorCloseGroupEventHandler.class);
            DoctorPigEventManager.checkAndPublishEvent(eventInfoList, coreEventDispatcher, publisher);

            return RespWithEx.ok(Boolean.TRUE);
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            log.error("groupEventClose failed, groupDetail:{}, close:{}, cause:{}", groupDetail, close, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventClose failed, groupDetail:{}, close:{}, cause:{}", groupDetail, close, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("group.event.close.fail");
        }
    }

    @Override
    public RespWithEx<Boolean> groupEventDisease(DoctorGroupDetail groupDetail, @Valid DoctorDiseaseGroupInput disease) {
        try {
            List<DoctorEventInfo> eventInfoList = doctorGroupEventManager.handleEvent(groupDetail, disease, DoctorDiseaseGroupEventHandler.class);
            DoctorPigEventManager.checkAndPublishEvent(eventInfoList, coreEventDispatcher, publisher);

            return RespWithEx.ok(Boolean.TRUE);
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            log.error("groupEventDisease failed, groupDetail:{}, disease:{}, cause:{}", groupDetail, disease, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventDisease failed, groupDetail:{}, disease:{}, cause:{}", groupDetail, disease, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("group.event.disease.fail");
        }
    }

    @Override
    public RespWithEx<Boolean> groupEventLiveStock(DoctorGroupDetail groupDetail, @Valid DoctorLiveStockGroupInput liveStock) {
        try {
            List<DoctorEventInfo> eventInfoList = doctorGroupEventManager.handleEvent(groupDetail, liveStock, DoctorLiveStockGroupEventHandler.class);
            DoctorPigEventManager.checkAndPublishEvent(eventInfoList, coreEventDispatcher, publisher);

            return RespWithEx.ok(Boolean.TRUE);
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            log.error("groupEventLiveStock failed, groupDetail:{}, liveStock:{}, cause:{}", groupDetail, liveStock, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventLiveStock failed, groupDetail:{}, liveStock:{}, cause:{}", groupDetail, liveStock, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("group.event.liveStock.fail");
        }
    }

    @Override
    public RespWithEx<Boolean> groupEventMoveIn(DoctorGroupDetail groupDetail, @Valid DoctorMoveInGroupInput moveIn) {
        try {
            List<DoctorEventInfo> eventInfoList = doctorGroupEventManager.handleEvent(groupDetail, moveIn, DoctorMoveInGroupEventHandler.class);
            DoctorPigEventManager.checkAndPublishEvent(eventInfoList, coreEventDispatcher, publisher);

            return RespWithEx.ok(Boolean.TRUE);
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            log.error("groupEventMoveIn failed, groupDetail:{}, moveIn:{}, cause:{}", groupDetail, moveIn, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventMoveIn failed, groupDetail:{}, moveIn:{}, cause:{}", groupDetail, moveIn, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("group.event.moveIn.fail");
        }
    }

    @Override
    public RespWithEx<Boolean> groupEventTransFarm(DoctorGroupDetail groupDetail, @Valid DoctorTransFarmGroupInput transFarm) {
        try {
            List<DoctorEventInfo> eventInfoList = doctorGroupEventManager.handleEvent(groupDetail, transFarm, DoctorTransFarmGroupEventHandler.class);
            DoctorPigEventManager.checkAndPublishEvent(eventInfoList, coreEventDispatcher, publisher);

            return RespWithEx.ok(Boolean.TRUE);
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            log.error("groupEventTransFarm failed, groupDetail:{}, transFarm:{}, cause:{}", groupDetail, transFarm, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventTransFarm failed, groupDetail:{}, transFarm:{}, cause:{}", groupDetail, transFarm, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("group.event.transFarm.fail");
        }
    }

    @Override
    public RespWithEx<Long> groupEventTransGroup(DoctorGroupDetail groupDetail, @Valid DoctorTransGroupInput transGroup) {
        try {
            List<DoctorEventInfo> eventInfoList = doctorGroupEventManager.handleEvent(groupDetail, transGroup, DoctorTransGroupEventHandler.class);
            DoctorPigEventManager.checkAndPublishEvent(eventInfoList, coreEventDispatcher, publisher);

            if (Objects.equals(transGroup.getIsCreateGroup(), IsOrNot.YES.getValue())) {
                DoctorGroup toGroup = doctorGroupDao.findByFarmIdAndGroupCode(groupDetail.getGroup().getFarmId(), transGroup.getToGroupCode());
                return RespWithEx.ok(toGroup.getId());
            }
            return RespWithEx.ok(transGroup.getToGroupId());
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            log.error("groupEventTransGroup failed, groupDetail:{}, transGroup:{}, cause:{}", groupDetail, transGroup, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventTransGroup failed, groupDetail:{}, transGroup:{}, cause:{}", groupDetail, transGroup, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("group.event.transGroup.fail");
        }
    }

    @Override
    public RespWithEx<Boolean> groupEventTurnSeed(DoctorGroupDetail groupDetail, @Valid DoctorTurnSeedGroupInput turnSeed) {
        try {
            List<DoctorEventInfo> eventInfoList = doctorGroupEventManager.handleEvent(groupDetail, turnSeed, DoctorTurnSeedGroupEventHandler.class);
            DoctorPigEventManager.checkAndPublishEvent(eventInfoList, coreEventDispatcher, publisher);

            return RespWithEx.ok(Boolean.TRUE);
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            log.error("groupEventTurnSeed failed, groupDetail:{}, turnSeed:{}, cause:{}", groupDetail, turnSeed, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("groupEventTurnSeed failed, groupDetail:{}, turnSeed:{}, cause:{}", groupDetail, turnSeed, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("groupEventTurnSeed failed, groupDetail:{}, turnSeed:{}, cause:{}", groupDetail, turnSeed, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("group.event.turnSeed.fail");
        }
    }

    @Override
    public RespWithEx<Boolean> batchNewGroupEventHandle(List<DoctorNewGroupInputInfo> inputInfoList) {
        try {
            List<DoctorEventInfo> eventInfoList = doctorGroupManager.batchNewGroupEventHandle(inputInfoList);
            DoctorPigEventManager.checkAndPublishEvent(eventInfoList, coreEventDispatcher, publisher);

            return RespWithEx.ok(Boolean.TRUE);
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            log.error("batch.new.group.event.failed, inputInfoList:{}, cause:{}", inputInfoList, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("batch.new.group.event.failed, inputInfoList:{}, cause:{}", inputInfoList, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("batch.new.group.event.fail");
        }
    }

    @Override
    public RespWithEx<Boolean> batchGroupEventHandle(List<DoctorGroupInputInfo> inputInfoList, Integer eventType) {
        try {
            List<DoctorEventInfo> eventInfoList = doctorGroupEventManager.batchHandleEvent(inputInfoList, eventType);
            DoctorPigEventManager.checkAndPublishEvent(eventInfoList, coreEventDispatcher, publisher);

            return RespWithEx.ok(Boolean.TRUE);
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            log.error("batch.group.event.handle.failed, inputInfoList:{}, eventType:{}, cause:{}", inputInfoList, eventType, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("batch.group.event.handle.failed, inputInfoList:{}, eventType:{}, cause:{}", inputInfoList, eventType, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("batch.group.event.handle.failed, inputInfoList:{}, eventType:{}, cause:{}", inputInfoList, eventType, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("batch.group.event.handle.failed");
        }
    }

    @Override
    public RespWithEx<Boolean> rollbackGroupEvent(DoctorGroupEvent groupEvent, Long reveterId, String reveterName) {
        try {
            doctorGroupEventManager.rollbackEvent(groupEvent, reveterId, reveterName);
            return RespWithEx.ok(Boolean.TRUE);
        } catch (InvalidException e) {
            return RespWithEx.exception(e);
        } catch (ServiceException e) {
            log.error("rollback group event failed, groupEvent:{}, reveterId:{}, reveterName:{}, cause:{}",
                    groupEvent, reveterId, reveterName, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (Exception e) {
            log.error("rollback group event failed, groupEvent:{}, reveterId:{}, reveterName:{}, cause:{}",
                    groupEvent, reveterId, reveterName, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("group.event.rollback.fail");
        }
    }

    @Override
    public RespWithEx<Boolean> incrDayAge() {
        try {
            List<DoctorGroup> groups = doctorGroupDao.fingByStatus(DoctorGroup.Status.CREATED.getValue());
            if (notEmpty(groups)) {
                doctorGroupTrackDao.incrDayAge(groups.stream().map(DoctorGroup::getId).collect(Collectors.toList()));
            }
            return RespWithEx.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("incr day age failed, cause:{}", Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("incr.group.dayAge.fail");
        }
    }

    @Override
    public RespWithEx<Long> createGroup(DoctorGroup group) {
        try {
            doctorGroupDao.create(group);
            return RespWithEx.ok(group.getId());
        } catch (Exception e) {
            log.error("create group failed, group:{}, cause:{}", group, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("group.create.fail");
        }
    }

    @Override
    public RespWithEx<Long> createGroupTrack(DoctorGroupTrack groupTrack) {
        try {
            doctorGroupTrackDao.create(groupTrack);
            return RespWithEx.ok(groupTrack.getId());
        } catch (Exception e) {
            log.error("create groupTrack failed, groupTrack:{}, cause:{}", groupTrack, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("groupTrack.create.fail");
        }
    }

    @Override
    public RespWithEx<Long> createGroupEvent(DoctorGroupEvent groupEvent) {
        try {
            doctorGroupEventDao.create(groupEvent);
            return RespWithEx.ok(groupEvent.getId());
        } catch (Exception e) {
            log.error("create groupEvent failed, groupEvent:{}, cause:{}", groupEvent, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("groupEvent.create.fail");
        }
    }

    @Override
    public RespWithEx<Long> createGroupSnapShot(DoctorGroupSnapshot groupSnapshot) {
        try {
            doctorGroupSnapshotDao.create(groupSnapshot);
            return RespWithEx.ok(groupSnapshot.getId());
        } catch (Exception e) {
            log.error("create groupSnapshot failed, groupSnapshot:{}, cause:{}", groupSnapshot, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("groupSnapshot.create.fail");
        }
    }

    @Deprecated
    @Override
    public RespWithEx<Boolean> updateGroupEvent(DoctorGroupEvent event) {
        try {
            return RespWithEx.ok(doctorGroupEventDao.update(event));
        } catch (Exception e) {
            log.error("update group event failed, cause by {}", Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("update.group.event.fail");
        }
    }
}
