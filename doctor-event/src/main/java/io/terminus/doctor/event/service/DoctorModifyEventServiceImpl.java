package io.terminus.doctor.event.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupEventHandlers;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigEventHandlers;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;


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

    @Override
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
}
