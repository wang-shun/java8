package io.terminus.doctor.event.handler.rollback;

import com.google.common.base.Throwables;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.common.utils.Arguments.notNull;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/21
 */
@Slf4j
public class DoctorRollbackReportHandler {

    @Autowired private CoreEventDispatcher coreEventDispatcher;
    @Autowired(required = false) private Publisher publisher;

    /**
     * 校验携带数据正确性，发布事件
     */
    protected void checkAndPublishRollback(List<DoctorRollbackDto> dtos) {
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
