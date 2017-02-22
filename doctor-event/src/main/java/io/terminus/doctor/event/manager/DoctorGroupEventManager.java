package io.terminus.doctor.event.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorGroupInputInfo;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.handler.DoctorGroupEventHandler;
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
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorRevertLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
@Component
public class DoctorGroupEventManager {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private DoctorGroupDao doctorGroupDao;
    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired
    private DoctorGroupEventDao doctorGroupEventDao;
    @Autowired
    private DoctorRevertLogDao doctorRevertLogDao;
    @Autowired
    private DoctorGroupSnapshotDao doctorGroupSnapshotDao;
    @Autowired
    private DoctorCommonGroupEventHandler doctorCommonGroupEventHandler;

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private Map<Class<? extends DoctorGroupEventHandler>, DoctorGroupEventHandler> handlerMapping;

    private Map<Integer, DoctorGroupEventHandler> handlerMap;

    /**
     * 初始化所有实现的
     */
    @PostConstruct
    public void initHandlers() {
        handlerMapping = Maps.newHashMap();
        handlerMap = Maps.newHashMap();
        Map<String, DoctorGroupEventHandler> handlers = applicationContext.getBeansOfType(DoctorGroupEventHandler.class);
        log.info("Doctor group event handlers :{}", handlers);
        if (!handlers.isEmpty()) {
            handlers.values().forEach(handler -> handlerMapping.put(handler.getClass(), handler));
            handlerMap.put(GroupEventType.MOVE_IN.getValue(), handlerMapping.get(DoctorMoveInGroupEventHandler.class));
            handlerMap.put(GroupEventType.CHANGE.getValue(), handlerMapping.get(DoctorChangeGroupEventHandler.class));
            handlerMap.put(GroupEventType.TRANS_GROUP.getValue(), handlerMapping.get(DoctorTransGroupEventHandler.class));
            handlerMap.put(GroupEventType.TURN_SEED.getValue(), handlerMapping.get(DoctorTurnSeedGroupEventHandler.class));
            handlerMap.put(GroupEventType.LIVE_STOCK.getValue(), handlerMapping.get(DoctorLiveStockGroupEventHandler.class));
            handlerMap.put(GroupEventType.DISEASE.getValue(), handlerMapping.get(DoctorDiseaseGroupEventHandler.class));
            handlerMap.put(GroupEventType.ANTIEPIDEMIC.getValue(), handlerMapping.get(DoctorAntiepidemicGroupEventHandler.class));
            handlerMap.put(GroupEventType.TRANS_FARM.getValue(), handlerMapping.get(DoctorTransFarmGroupEventHandler.class));
            handlerMap.put(GroupEventType.CLOSE.getValue(), handlerMapping.get(DoctorCloseGroupEventHandler.class));
        }
    }

    /**
     * 事务方式执行创建猪群事件
     * @param groupDetail  猪群明细
     * @param input        录入信息
     * @param handlerClass 事件handler的实现类
     * @see GroupEventType
     */
    @Transactional
    public <I extends BaseGroupInput>
    List<DoctorEventInfo> handleEvent(DoctorGroupDetail groupDetail, I input, Class<? extends DoctorGroupEventHandler> handlerClass) {
        log.info("group event handle starting, handler class", handlerClass);
        final List<DoctorEventInfo> eventInfoList = Lists.newArrayList();
        getHandler(handlerClass).handle(eventInfoList, groupDetail.getGroup(), groupDetail.getGroupTrack(), input);
        log.info("group event handle ending, handler class", handlerClass);
        return eventInfoList;
    }

    /**
     * 批量猪群事件
     * @param inputInfoList
     * @param eventType
     * @return
     */
    @Transactional
    public List<DoctorEventInfo> batchHandleEvent(List<DoctorGroupInputInfo> inputInfoList, Integer eventType) {
        //eventRepeatCheck(inputInfoList); // TODO: 17/1/20 暂时移除猪群事件的重复性校验
        log.info("batch group event handle starting, eventType:{}", eventType);
        final List<DoctorEventInfo> eventInfoList = Lists.newArrayList();
        inputInfoList.forEach(inputInfo -> {
            try {
                getHandler(eventType)
                        .handle(eventInfoList, doctorGroupDao.findById(inputInfo.getGroupDetail().getGroup().getId()), doctorGroupTrackDao.findById(inputInfo.getGroupDetail().getGroupTrack().getId()), inputInfo.getInput());
            } catch (InvalidException e) {
                throw new InvalidException(true, e.getError(), inputInfo.getGroupDetail().getGroup().getGroupCode(), e.getParams());
            }
        });
        log.info("batch group event handle ending, eventType:{}", eventType);
        return eventInfoList;
    }

    /**
     * 回滚猪群事件, 回滚规则: 自动生成的事件不可回滚, 不是最新录入的事件需要先回滚上层事件后再回滚
     * @param groupEvent 回滚的事件id
     */
    @Transactional
    public void rollbackEvent(DoctorGroupEvent groupEvent, Long reverterId, String reverterName) {
        log.info("rollback group event starting, group event:{}", groupEvent);
        //校验能否回滚
        checkCanRollback(groupEvent);
        DoctorGroupSnapshot snapshot = doctorGroupSnapshotDao.findGroupSnapshotByToEventId(groupEvent.getId());

        //记录回滚日志
        createRevertLog(snapshot, reverterId, reverterName);

        DoctorGroupSnapShotInfo info = JSON_MAPPER.fromJson(snapshot.getFromInfo(), DoctorGroupSnapShotInfo.class);

        //删除此事件 -> 回滚猪群跟踪 -> 回滚猪群 -> 删除镜像
        doctorGroupEventDao.delete(groupEvent.getId());
        doctorGroupTrackDao.update(info.getGroupTrack());
        doctorGroupDao.update(info.getGroup());
        doctorGroupSnapshotDao.delete(snapshot.getId());
    }

    //校验能否回滚
    private void checkCanRollback(DoctorGroupEvent event) {
        DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(event.getGroupId());

        //判断此事件是否是最新事件
        if (!Objects.equals(event.getId(), groupTrack.getRelEventId())) {
            log.error("group event not the latest, can not rollback, event:{}", event);
            throw new InvalidException("group.event.not.the.latest", event.getId());
        }

        //判断此事件是否是自动生成
        if (Objects.equals(IsOrNot.YES.getValue(), event.getIsAuto())) {
            log.error("group event is auto event, can not rollback, event:{}", event);
            throw new InvalidException("group.event.is.auto");
        }

        //新建猪群事件不可回滚
        if (Objects.equals(GroupEventType.NEW.getValue(), event.getType())) {
            log.error("new group event can not rollback, event:{}", event);
            throw new InvalidException("group.event.new.not.rollback", event.getId());
        }
    }

    //记录回滚日志
    private void createRevertLog(DoctorGroupSnapshot snapshot, Long reverterId, String reverterName) {
        DoctorRevertLog revertLog = new DoctorRevertLog();
        revertLog.setType(DoctorRevertLog.Type.GROUP.getValue());
        revertLog.setFromInfo(snapshot.getToInfo());
        revertLog.setToInfo(snapshot.getFromInfo());
        revertLog.setReverterId(reverterId);
        revertLog.setReverterName(reverterName);
        doctorRevertLogDao.create(revertLog);
    }

    /**
     * 获取事件处理器
     * @param interfaceClass 处理的实现类
     * @return 事件处理器
     */
    private DoctorGroupEventHandler getHandler(Class<? extends DoctorGroupEventHandler> interfaceClass) {
        if (!handlerMapping.containsKey(interfaceClass) || handlerMapping.get(interfaceClass) == null) {
            log.error("Not any event handler found for illegal class:{}", interfaceClass.getName());
            throw new ServiceException("handler.not.found");
        }
        return handlerMapping.get(interfaceClass);
    }

    /**
     * 获取事件处理器
     * @param eventType 实现类型
     * @return 事件处理器
     */
    private DoctorGroupEventHandler getHandler(Integer eventType) {
        if (!handlerMap.containsKey(eventType) || handlerMap.get(eventType) == null) {
            log.error("Not any event handler found for illegal eventType:{}", eventType);
            throw new ServiceException("handler.not.found");
        }
        return handlerMap.get(eventType);
    }

    /**
     * 批量事件的重复性校验
     * @param inputList 批量事件输入
     */
    private void eventRepeatCheck(List<DoctorGroupInputInfo> inputList) {
        if (Arguments.isNullOrEmpty(inputList)) {
            throw new ServiceException("batch.event.input.empty");
        }
        if (Objects.equals(inputList.get(0).getInput().getEventType(), GroupEventType.TURN_SEED.getValue())) {
            return;
        }
        Set<String> inputSet = inputList.stream().map(groupInputInfo -> groupInputInfo.getGroupDetail().getGroup().getGroupCode()).collect(Collectors.toSet());
        if (inputList.size() != inputSet.size()) {
            throw new ServiceException("batch.event.groupCode.not.repeat");
        }
    }
}
