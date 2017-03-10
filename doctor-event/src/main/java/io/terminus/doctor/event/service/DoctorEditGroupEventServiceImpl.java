package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.editHandler.group.DoctorEditGroupEventHandlers;
import io.terminus.doctor.event.manager.DoctorEditGroupEventManager;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 21:45 17/3/8
 */

@Slf4j
@Service
@RpcProvider
public class DoctorEditGroupEventServiceImpl implements DoctorEditGroupEventService{

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private DoctorEditGroupEventHandlers doctorEditGroupEventHandlers;

    private DoctorGroupReadService doctorGroupReadService;

    private DoctorGroupSnapshotDao doctorGroupSnapshotDao;

    private DoctorEditGroupEventManager doctorEditGroupEventManager;

    @Autowired
    public DoctorEditGroupEventServiceImpl(DoctorEditGroupEventHandlers doctorEditGroupEventHandlers,
                                                DoctorGroupReadService doctorGroupReadService,
                                                DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                                DoctorEditGroupEventManager doctorEditGroupEventManager){
        this.doctorEditGroupEventHandlers = doctorEditGroupEventHandlers;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorEditGroupEventManager = doctorEditGroupEventManager;
    }

    @Override
    public Response<Boolean> elicitDoctorGroupTrack(DoctorGroupEvent doctorGroupEvent) {
        try {
            log.info("elicitDoctorGroupTrack start, doctorGroupEvent: {}", doctorGroupEvent);
            Response<List<DoctorGroupEvent>> doctorGroupEventResp = doctorGroupReadService.findLinkedGroupEventsByGroupId(doctorGroupEvent.getGroupId());
            if (!doctorGroupEventResp.isSuccess() || Arguments.isNullOrEmpty(doctorGroupEventResp.getResult())) {
                log.info("find linked group events failed, groupId: {}", doctorGroupEvent.getGroupId());

                return Response.fail("find linked group events failed, groupId");
            }
            List<DoctorGroupEvent> doctorGroupEventList = doctorGroupEventResp.getResult();
            List<DoctorGroupEvent> taskDoctorGroupEventList = Lists.newArrayList();
            DoctorGroupTrack doctorGroupTrack = new DoctorGroupTrack();

            taskDoctorGroupEventList = doctorGroupEventList.stream().filter(
                    doctorGroupEvent1 -> doctorGroupEvent1.getId() > doctorGroupEvent.getId()
            ).collect(Collectors.toList());
            taskDoctorGroupEventList.add(0, doctorGroupEvent);
            taskDoctorGroupEventList = taskDoctorGroupEventList.stream().sorted(
                    (doctorGroupEvent1, doctorGroupEvent2)-> {
                        if(doctorGroupEvent1.getEventAt().compareTo(doctorGroupEvent2.getEventAt()) == 0){
                            return doctorGroupEvent1.getType().compareTo(doctorGroupEvent2.getType());
                        }

                        return doctorGroupEvent1.getEventAt().compareTo(doctorGroupEvent2.getEventAt());
                    }).collect(Collectors.toList());
            DoctorGroupEvent preDoctorGroupEvent = doctorGroupEventList.stream().sorted(
                    (doctorGroupEvent1, doctorGroupEvent2)-> doctorGroupEvent2.getId().compareTo(doctorGroupEvent1.getId()))
                    .filter(doctorGroupEvent1 -> doctorGroupEvent1.getId() < doctorGroupEvent.getId()).findFirst().get();


            //要编辑的事件不是第一个事件
            if (!Arguments.isNull(preDoctorGroupEvent)) {
                DoctorGroupSnapshot preDoctorGroupSnapshot = doctorGroupSnapshotDao.findGroupSnapshotByToEventId(preDoctorGroupEvent.getId());
                if (Arguments.isNull(preDoctorGroupSnapshot)) {
                    log.info("find DoctorGroupSnapshot failed, no DoctorGroupSnapshot, toEventId={}", preDoctorGroupEvent.getId());
                    return Response.fail("find DoctorGroupSnapshot failed, no DoctorGroupSnapshot");
                }
                DoctorGroupSnapShotInfo doctorGroupSnapShotInfo = JSON_MAPPER.fromJson(preDoctorGroupSnapshot.getToInfo(), DoctorGroupSnapShotInfo.class);
                if (Arguments.isNull(doctorGroupSnapShotInfo) || Arguments.isNull(doctorGroupSnapShotInfo.getGroupTrack())) {
                    log.info("DoctorGroupSnapShotInfo broken, toEventId={}", preDoctorGroupEvent.getId());
                    return Response.fail("DoctorGroupSnapShotInfo broken");
                }
                doctorGroupTrack = doctorGroupSnapShotInfo.getGroupTrack();
            }


            //将需要推演的groupEvent.status = 1
            doctorEditGroupEventManager.updateOldDoctorGroupEvents(taskDoctorGroupEventList);

            for(DoctorGroupEvent handlerDoctorGroupEvent: taskDoctorGroupEventList){
                doctorGroupTrack = doctorEditGroupEventManager.elicitDoctorGroupTrack(doctorGroupTrack, handlerDoctorGroupEvent);
            }

        }catch(Exception e){
            log.info("edit event failed, cause: {}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("edit.group.event.failed");
        }

        return Response.ok();
    }
}
