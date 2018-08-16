package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.helper.DoctorEventBaseHelper;
import io.terminus.doctor.event.manager.DoctorGroupEventManager;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.event.enums.GroupEventType.REPORT_GROUP_EVENT;
import static io.terminus.doctor.event.handler.DoctorAbstractEventHandler.IGNORE_EVENT;


/**
 * Created by xjn on 17/4/14.
 * 编辑和回滚处理实现
 */
@Slf4j
@Service
@RpcProvider
public class DoctorModifyEventServiceImpl implements DoctorModifyEventService {

    protected final ToJsonMapper TO_JSON_MAPPER = ToJsonMapper.JSON_NON_DEFAULT_MAPPER;
    private static JsonMapperUtil jsonMapper = JsonMapperUtil.nonEmptyMapper();

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
    @Autowired
    private DoctorEventBaseHelper doctorEventBaseHelper;
    private final CoreEventDispatcher coreEventDispatcher;

    @Autowired
    public DoctorModifyEventServiceImpl(CoreEventDispatcher coreEventDispatcher) {
        this.coreEventDispatcher = coreEventDispatcher;
    }

    @Override
    public RespWithEx<Boolean> modifyPigEvent(BasePigEventInputDto inputDto, Long eventId, Integer eventType) {
        try {
            pigEventManager.modifyPigEventHandle(inputDto, eventId, eventType);

            //同步报表数据
            if (!IGNORE_EVENT.contains(eventType)) {
                DoctorPigEvent pigEvent = doctorPigEventDao.findEventById(eventId);
                List<Long> farmIds = Lists.newArrayList(pigEvent.getFarmId());
                if (Objects.equals(pigEvent.getType(), PigEvent.CHG_FARM.getKey())) {
                    DoctorChgFarmDto doctorChgFarmDto = jsonMapper.fromJson(pigEvent.getExtra(), DoctorChgFarmDto.class);
                    if (notNull(doctorChgFarmDto) && notNull(doctorChgFarmDto.getToFarmId())) {
                        farmIds.add(doctorChgFarmDto.getToFarmId());
                    }
                }
                doctorEventBaseHelper.synchronizeReportPublish(farmIds);
            }

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
            DoctorGroupEvent groupEvent = doctorGroupEventDao.findById(eventId);
            int a = doctorGroupEventDao.isCloseGroup(groupEvent.getGroupId());
            if(a != 1){
                throw new InvalidException("猪群已关闭");
            }
            groupEventManager.modifyGroupEventHandle(inputDto, eventId, eventType);

            //同步报表数据
            if (REPORT_GROUP_EVENT.contains(eventType)) {
                //DoctorGroupEvent groupEvent = doctorGroupEventDao.findEventById(eventId);
                List<Long> farmIds = Lists.newArrayList(groupEvent.getFarmId());
                if (Objects.equals(groupEvent.getType(), GroupEventType.TRANS_FARM.getValue())) {
                    DoctorTransFarmGroupInput groupInput = jsonMapper.fromJson(groupEvent.getExtra(), DoctorTransFarmGroupInput.class);
                    if (notNull(groupInput) && notNull(groupInput.getToFarmId())) {
                        farmIds.add(groupInput.getToFarmId());
                    }
                }
                doctorEventBaseHelper.synchronizeReportPublish(farmIds);
            }
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
