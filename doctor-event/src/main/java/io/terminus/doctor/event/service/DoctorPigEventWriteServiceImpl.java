package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.PostRequest;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.helper.DoctorEventBaseHelper;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static io.terminus.doctor.event.enums.PigEvent.*;
import static io.terminus.doctor.event.handler.DoctorAbstractEventHandler.IGNORE_EVENT;
import static io.terminus.doctor.event.manager.DoctorPigEventManager.checkAndPublishEvent;

/**
 * Created by yaoqijun.
 * Date:2016-05-19
 * Email:yaoqj@terminus.io
 * Descirbe: 猪事件信息录入方式
 */
@Service
@Slf4j
@RpcProvider
public class DoctorPigEventWriteServiceImpl implements DoctorPigEventWriteService {

    @Autowired
    private DoctorPigEventManager doctorPigEventManager;
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private CoreEventDispatcher coreEventDispatcher;
    @Autowired
    private Publisher publisher;
    @Autowired
    private DoctorEventBaseHelper doctorEventBaseHelper;

    @Override
    public RespWithEx<Boolean> pigEventHandle(BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        try {
            List<DoctorEventInfo> eventInfoList = doctorPigEventManager.eventHandle(inputDto, basic);
            checkAndPublishEvent(eventInfoList, coreEventDispatcher, publisher);

            //同步报表数据
            if (!IGNORE_EVENT.contains(inputDto.getEventType())) {
                doctorEventBaseHelper.synchronizeReportPublishForCreate(eventInfoList);
            }
            return RespWithEx.ok(Boolean.TRUE);
        } catch (ServiceException | IllegalStateException e) {
            log.error("pig.event.handle.failed, input:{}, basic:{}, cause by :{}", inputDto, basic, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (InvalidException e) {
            log.error("pig.event.handle.failed, input:{}, basic:{}, cause by :{}", inputDto, basic, Throwables.getStackTraceAsString(e));
            return RespWithEx.exception(e);
        } catch (Exception e) {
            log.error("pig.event.handle.failed, input:{}, basic:{}, cause by :{}", inputDto, basic, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("pig.event.handle.failed");
        }
    }

    @Override
    public RespWithEx<Boolean> batchPigEventHandle(List<BasePigEventInputDto> inputDtos, DoctorBasicInputInfoDto basic) {
        try {
            List<DoctorEventInfo> eventInfoList = doctorPigEventManager.batchEventsHandle(inputDtos, basic);
            checkAndPublishEvent(eventInfoList, coreEventDispatcher, publisher);

            //同步数据
            if (!IGNORE_EVENT.contains(inputDtos.get(0).getEventType())) {
                doctorEventBaseHelper.synchronizeReportPublishForCreate(eventInfoList);
            }
            return RespWithEx.ok(Boolean.TRUE);
        } catch (ServiceException | IllegalStateException e) {
            log.error("batch.pig.event.handle.failed, input:{}, basic:{}, cause by :{}", inputDtos, basic, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail(e.getMessage());
        } catch (InvalidException e) {
            log.error("batch.pig.event.handle.failed, input:{}, basic:{}, cause by :{}", inputDtos, basic, Throwables.getStackTraceAsString(e));
            return RespWithEx.exception(e);
        } catch (Exception e) {
            log.error("batch.pig.event.handle.failed, input:{}, basic:{}, cause by :{}", inputDtos, basic, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("batch.pig.event.handle.failed");
        }
    }


    @Override
    public RespWithEx<Boolean> delete(Long id) {
        try {
            doctorPigEventManager.delete(id);
            return RespWithEx.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("batch.pig.event.delete.failed,id:{} cause by :{}", id, Throwables.getStackTraceAsString(e));
            return RespWithEx.fail("batch.pig.event.delete.failed");
        }
    }

    @Deprecated
    @Override
    public Response<Boolean> createPigEvent(DoctorPigEvent doctorPigEvent) {
        try {
            //通知物联网接口(孔景军)
            /*
            当母猪发生转舍、离场、销售、离场事件时，需调用物联网提供的接口，以此通知物联网。(孔景军)
             */
            if(doctorPigEvent.getType() == CHG_LOCATION.getKey() || doctorPigEvent.getType() == TO_PREG.getKey() || doctorPigEvent.getType() == TO_MATING.getKey()
                    || doctorPigEvent.getType() == TO_FARROWING.getKey()  || doctorPigEvent.getType() == REMOVAL.getKey()  || (doctorPigEvent.getType() == PIGLETS_CHG.getKey() && doctorPigEvent.getChangeTypeId()==109) ){
                Map<String,String> params = Maps.newHashMap();
                params.put("pigId",doctorPigEvent.getPigId().toString());
                new PostRequest().postRequest("/api/iot/pig/sow-leaving",params);
            }
            return Response.ok(doctorPigEventDao.create(doctorPigEvent));
        } catch (Exception e) {
            log.error("create.pig.event.failed, event:{}, basic:{}, cause:{}", doctorPigEvent, Throwables.getStackTraceAsString(e));
            return Response.fail("create.pig.event.failed");
        }
    }

    @Deprecated
    @Override
    public Response<Boolean> updatePigEvents(DoctorPigEvent doctorPigEvent) {
        try {
            doctorPigEventDao.updatePigEvents(doctorPigEvent);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("update.pig.event.failed, event:{}, cause:{}", doctorPigEvent, Throwables.getStackTraceAsString(e));
            return Response.fail("update.pig.event.failed");
        }
    }

    @Deprecated
    @Override
    public Response<Boolean> updatePigEvent(DoctorPigEvent doctorPigEvent) {
        try {
            doctorPigEventDao.update(doctorPigEvent);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("update.pig.event.failed, event:{}, cause:{}", doctorPigEvent, Throwables.getStackTraceAsString(e));
            return Response.fail("update.pig.event.failed");
        }
    }


}
