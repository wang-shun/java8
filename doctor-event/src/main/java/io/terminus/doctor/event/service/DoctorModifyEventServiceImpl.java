package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupEventHandlers;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigEventHandlers;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Created by xjn on 17/4/14.
 * 编辑和回滚处理实现
 */
@Slf4j
@Service
@RpcProvider
public class DoctorModifyEventServiceImpl implements DoctorModifyEventService {
    @Autowired
    private DoctorModifyPigEventHandlers modifyPigEventHandlers;
    @Autowired
    private DoctorModifyGroupEventHandlers modifyGroupEventHandlers;
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorGroupEventDao doctorGroupEventDao;

    @Override
    @Transactional
    public RespWithEx<Boolean> modifyPigEvent(BasePigEventInputDto inputDto, Long eventId, Integer eventType) {
        try {
            DoctorPigEvent oldPigEvent = doctorPigEventDao.findById(eventId);
            modifyPigEventHandlers.getModifyPigEventHandlerMap().get(eventType).modifyHandle(oldPigEvent, inputDto);
            return RespWithEx.ok(true);
        }catch (InvalidException e) {
            log.error("modify pig event failed , inputDto:{}, cuase:{}", inputDto, Throwables.getStackTraceAsString(e));
            return RespWithEx.exception(e);
        } catch (Exception e) {
            log.error("modify pig event failed , inputDto:{}, cuase:{}", inputDto, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("modify pig event failed");
        }
    }

    @Override
    @Transactional
    public RespWithEx<Boolean> modifyGroupEvent(BaseGroupInput inputDto, Long eventId, Integer eventType) {
        try {
            DoctorGroupEvent oldGroupEvent = doctorGroupEventDao.findById(eventId);
            modifyGroupEventHandlers.getModifyGroupEventHandlerMap().get(eventType).modifyHandle(oldGroupEvent, inputDto);
            return RespWithEx.ok(true);
        }catch (InvalidException e) {
            log.error("modify pig event failed , inputDto:{}, cuase:{}", inputDto, Throwables.getStackTraceAsString(e));
            return RespWithEx.exception(e);
        } catch (Exception e) {
            log.error("modify pig event failed , inputDto:{}, cuase:{}", inputDto, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("modify pig event failed");
        }
    }
}
