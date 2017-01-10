package io.terminus.doctor.event.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.event.CoreEventDispatcher;
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
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorRevertLog;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private CoreEventDispatcher coreEventDispatcher;
    @Autowired(required = false)
    private Publisher publisher;

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private Map<Class<? extends DoctorGroupEventHandler>, DoctorGroupEventHandler> handlerMapping;

    /**
     * 初始化所有实现的
     */
    @PostConstruct
    public void initHandlers() {
        handlerMapping = Maps.newHashMap();
        Map<String, DoctorGroupEventHandler> handlers = applicationContext.getBeansOfType(DoctorGroupEventHandler.class);
        log.info("Doctor group event handlers :{}", handlers);
        if (!handlers.isEmpty()) {
            handlers.values().forEach(handler -> handlerMapping.put(handler.getClass(), handler));
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
        final List<DoctorEventInfo> eventInfoList = Lists.newArrayList();
        getHandler(handlerClass).handle(eventInfoList, groupDetail.getGroup(), groupDetail.getGroupTrack(), input);
        return eventInfoList;
    }

    @Transactional
    public <I extends BaseGroupInput>
    List<DoctorEventInfo> batchHandleEvent(List<DoctorGroupInputInfo> inputInfoList, Class<? extends DoctorGroupEventHandler> handleClass) {
        final List<DoctorEventInfo> eventInfoList = Lists.newArrayList();
        inputInfoList.forEach(inputInfo -> getHandler(handleClass).handle(eventInfoList, inputInfo.getGroupDetail().getGroup(), inputInfo.getGroupDetail().getGroupTrack(), inputInfo.getInput()));
        return eventInfoList;
    }

    /**
     * 回滚猪群事件, 回滚规则: 自动生成的事件不可回滚, 不是最新录入的事件需要先回滚上层事件后再回滚
     * @param groupEvent 回滚的事件id
     */
    @Transactional
    public void rollbackEvent(DoctorGroupEvent groupEvent, Long reverterId, String reverterName) {
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
            throw new ServiceException("group.event.not.the.latest");
        }

        //判断此事件是否是自动生成
        if (Objects.equals(IsOrNot.YES.getValue(), event.getIsAuto())) {
            log.error("group event is auto event, can not rollback, event:{}", event);
            throw new ServiceException("group.event.is.auto");
        }

        //新建猪群事件不可回滚
        if (Objects.equals(GroupEventType.NEW.getValue(), event.getType())) {
            log.error("new group event can not rollback, event:{}", event);
            throw new ServiceException("group.event.new.not.rollback");
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
}
