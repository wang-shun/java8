package io.terminus.doctor.event.manager;

import com.google.common.base.Throwables;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.handler.DoctorRollbackGroupEventHandler;
import io.terminus.doctor.event.handler.DoctorRollbackPigEventHandler;
import io.terminus.doctor.event.handler.rollback.DoctorRollbackHandlerChain;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.common.utils.Arguments.notNull;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/9/28
 */
@Slf4j
@Component
public class DoctorRollbackManager {

    @Autowired
    private DoctorRollbackHandlerChain doctorRollbackHandlerChain;

    @Autowired
    private CoreEventDispatcher coreEventDispatcher;

    @Autowired(required = false)
    private Publisher publisher;

    /**
     * 事务回滚猪群
     */
    @Transactional
    public List<DoctorRollbackDto> rollbackGroup(DoctorGroupEvent groupEvent, Long operatorId, String operatorName) {
        //获取拦截器链, 判断能否回滚,执行回滚操作, 返回需要更新的报表
        for (DoctorRollbackGroupEventHandler handler : doctorRollbackHandlerChain.getRollbackGroupEventHandlers()) {
            if (handler.canRollback(groupEvent)) {
                handler.rollback(groupEvent, operatorId, operatorName);
                return handler.updateReport(groupEvent);
            }
        }
        throw new ServiceException("rollback.group.not.allow");
    }

    /**
     * 事务回滚猪
     */
    @Transactional
    public List<DoctorRollbackDto> rollbackPig(DoctorPigEvent pigEvent, Long operatorId, String operatorName) {
        //获取拦截器链, 判断能否回滚, 执行回滚操作, 返回需要更新的报表
        for (DoctorRollbackPigEventHandler handler : doctorRollbackHandlerChain.getRollbackPigEventHandlers()) {
            if (handler.canRollback(pigEvent)) {
                handler.rollback(pigEvent, operatorId, operatorName);
                return handler.updateReport(pigEvent);
            }
        }
        throw new ServiceException("rollback.pig.not.allow");
    }

    /**
     * 校验携带数据正确性，发布事件
     */
    public void checkAndPublishRollback(List<DoctorRollbackDto> dtos) {
        if (notEmpty(dtos)) {
            checkFarmIdAndEventAt(dtos);
            publishRollbackEvent(dtos);
        }
    }

    //发布zk事件, 用于更新回滚后操作
    private void publishRollbackEvent(List<DoctorRollbackDto> dtos) {
        String rollbackJson = JsonMapper.nonEmptyMapper().toJson(dtos);
        if (notNull(publisher)) {
            try {
                publisher.publish(DataEvent.toBytes(DataEventType.RollBackReport.getKey(), rollbackJson));
            } catch (Exception e) {
                log.error("publish rollback group zk event, DoctorRollbackDtos:{}, cause:{}", dtos, Throwables.getStackTraceAsString(e));
            }
        } else {
            coreEventDispatcher.publish(DataEvent.make(DataEventType.RollBackReport.getKey(), rollbackJson));
        }
    }

    private void checkFarmIdAndEventAt(List<DoctorRollbackDto> dtos) {
        dtos.forEach(dto -> {
            if (dto.getFarmId() == null || dto.getEventAt() == null) {
                throw new ServiceException("publish.rollback.not.null");
            }
        });
    }
}
