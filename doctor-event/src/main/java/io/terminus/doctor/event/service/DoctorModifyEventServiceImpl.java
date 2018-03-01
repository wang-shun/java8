package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.manager.DoctorGroupEventManager;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.reportBi.listener.DoctorReportBiReaTimeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

import static io.terminus.common.utils.JsonMapper.JSON_NON_DEFAULT_MAPPER;


/**
 * Created by xjn on 17/4/14.
 * 编辑和回滚处理实现
 */
@Slf4j
@Service
@RpcProvider
public class DoctorModifyEventServiceImpl implements DoctorModifyEventService {

    protected final ToJsonMapper TO_JSON_MAPPER = ToJsonMapper.JSON_NON_DEFAULT_MAPPER;
    private static JsonMapper jsonMapper = JSON_NON_DEFAULT_MAPPER;

    @Autowired
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorGroupEventDao doctorGroupEventDao;
    @Autowired
    private DoctorPigEventManager pigEventManager;
    @Autowired
    private DoctorGroupEventManager groupEventManager;
    @Autowired
    private DoctorPigEventManager doctorPigEventManager;

    private final CoreEventDispatcher coreEventDispatcher;

    @Autowired
    public DoctorModifyEventServiceImpl(CoreEventDispatcher coreEventDispatcher) {
        this.coreEventDispatcher = coreEventDispatcher;
    }

    @Override
    public RespWithEx<Boolean> modifyPigEvent(BasePigEventInputDto inputDto, Long eventId, Integer eventType) {
        try {
            pigEventManager.modifyPigEventHandle(inputDto, eventId, eventType);
            String messageId = UUID.randomUUID().toString().replace("-", "");
            coreEventDispatcher.publish(new DoctorReportBiReaTimeEvent(doctorPigEventDao.findEventById(eventId).getOrgId(), messageId));
            return RespWithEx.ok(true);
        } catch (InvalidException e) {
            log.error("modify pig event failed , inputDto:{}, cuase:{}", inputDto, Throwables.getStackTraceAsString(e));
            return RespWithEx.exception(e);
        } catch (Exception e) {
            log.error("modify pig event failed , inputDto:{}, cuase:{}", inputDto, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("modify pig event failed");
        }
    }

    @Override
    public RespWithEx<Boolean> modifyGroupEvent(BaseGroupInput inputDto, Long eventId, Integer eventType) {
        try {
            groupEventManager.modifyGroupEventHandle(inputDto, eventId, eventType);
            String messageId = UUID.randomUUID().toString().replace("-", "");
            coreEventDispatcher.publish(new DoctorReportBiReaTimeEvent(doctorGroupEventDao.findEventById(eventId).getOrgId(), messageId));
            return RespWithEx.ok(true);
        } catch (InvalidException e) {
            log.error("modify pig event failed , inputDto:{}, cuase:{}", inputDto, Throwables.getStackTraceAsString(e));
            return RespWithEx.exception(e);
        } catch (Exception e) {
            log.error("modify pig event failed , inputDto:{}, cuase:{}", inputDto, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("modify pig event failed");
        }
    }


    @Override
    public RespWithEx<Boolean> modifyPigEvent(String oldPigEvent, DoctorPigEvent pigEvent) {
        try {
            doctorPigEventManager.modifyPigEvent(pigEvent, oldPigEvent);
            String messageId = UUID.randomUUID().toString().replace("-", "");
            coreEventDispatcher.publish(new DoctorReportBiReaTimeEvent(pigEvent.getOrgId(), messageId));
            return RespWithEx.ok(true);
        } catch (Exception e) {
            log.error("modify pig event failed , inputDto:{}, cause:{}", oldPigEvent, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("modify pig event failed");
        }
    }

    @Override
    public RespWithEx<Boolean> modifyGroupEvent(String oldPigEvent, DoctorGroupEvent groupEvent) {
        try {
            doctorPigEventManager.modifyGroupEvent(groupEvent, oldPigEvent);
            return RespWithEx.ok(true);
        } catch (Exception e) {
            log.error("modify group event failed , inputDto:{}, cause:{}", oldPigEvent, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("modify group event failed");
        }
    }

    public void fillIfChanged(Map<String, Object> map, DoctorPigEvent pigEvent) {
        if (map.containsKey("breed") && !map.get("breed").equals(pigEvent.getBreedId())) {

        }
    }
}
