package io.terminus.doctor.event.handler.rollback;

import com.google.common.base.Throwables;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.RollbackType;
import io.terminus.doctor.event.handler.DoctorRollbackGroupEventHandler;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorRevertLog;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorRevertLogWriteService;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;

/**
 * Desc: 猪群事件回滚处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/20
 */
@Slf4j
public abstract class DoctorAbstractRollbackGroupEventHandler implements DoctorRollbackGroupEventHandler {

    @Autowired protected DoctorGroupReadService doctorGroupReadService;
    @Autowired private DoctorRevertLogWriteService doctorRevertLogWriteService;
    @Autowired private CoreEventDispatcher coreEventDispatcher;
    @Autowired(required = false) private Publisher publisher;

    /**
     * 判断能否回滚(1.手动事件 2.三个月内的事件 3.最新事件 4.子类根据事件类型特殊处理)
     */
    @Override
    public final boolean canRollback(DoctorGroupEvent groupEvent) {
        return Objects.equals(groupEvent.getIsAuto(), IsOrNot.YES.getValue()) &&
                groupEvent.getEventAt().after(DateTime.now().plusMonths(-3).toDate()) &&
                RespHelper.orFalse(doctorGroupReadService.isLastEvent(groupEvent.getGroupId(), groupEvent.getId())) &&
                handleCheck(groupEvent);
    }

    /**
     * 带事务的回滚操作
     */
    @Override @Transactional
    public final void rollback(DoctorGroupEvent groupEvent) {
        DoctorRevertLog revertLog = handleRollback(groupEvent);
        RespHelper.orServEx(doctorRevertLogWriteService.createRevertLog(revertLog));
    }

    /**
     * 更新统计报表, es搜索(发zk事件)
     */
    @Override
    public final void updateReport(DoctorGroupEvent groupEvent) {
        DoctorRollbackDto dto = handleReport();
        if (dto != null) {
            publishRollbackEvent(dto);
        }
    }

    /**
     * 每个子类根据事件类型 判断是否应该由此handler执行回滚
     */
    protected abstract boolean handleCheck(DoctorGroupEvent groupEvent);

    /**
     * 处理回滚操作
     */
    protected abstract DoctorRevertLog handleRollback(DoctorGroupEvent groupEvent);

    /**
     * 需要更新的统计
     * @see RollbackType
     */
    protected abstract DoctorRollbackDto handleReport();

    //发布zk事件, 用于更新回滚后操作
    private void publishRollbackEvent(DoctorRollbackDto dto) {
        if (notNull(publisher)) {
            try {
                publisher.publish(DataEvent.toBytes(DataEventType.RollBackReport.getKey(), dto));
            } catch (Exception e) {
                log.error("publish rollback group zk event, DoctorRollbackDto:{}, cause:{}", dto, Throwables.getStackTraceAsString(e));
            }
        } else {
            coreEventDispatcher.publish(DataEvent.make(DataEventType.RollBackReport.getKey(), dto));
        }
    }
}
