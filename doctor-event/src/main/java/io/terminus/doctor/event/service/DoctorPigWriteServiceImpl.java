package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.cache.DoctorPigInfoCache;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.workflow.core.WorkFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/21
 */
@Slf4j
@Service
@RpcProvider
public class DoctorPigWriteServiceImpl implements DoctorPigWriteService {

    private final DoctorPigDao doctorPigDao;
    private final DoctorPigTrackDao doctorPigTrackDao;
    private final DoctorPigEventDao doctorPigEventDao;
    private final DoctorPigSnapshotDao doctorPigSnapshotDao;
    private final DoctorPigInfoCache doctorPigInfoCache;
    private final WorkFlowService workFlowService;

    @Autowired
    public DoctorPigWriteServiceImpl(DoctorPigDao doctorPigDao,
                                     DoctorPigTrackDao doctorPigTrackDao,
                                     DoctorPigEventDao doctorPigEventDao,
                                     DoctorPigSnapshotDao doctorPigSnapshotDao,
                                     DoctorPigInfoCache doctorPigInfoCache,
                                     WorkFlowService workFlowService) {
        this.doctorPigDao = doctorPigDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorPigSnapshotDao = doctorPigSnapshotDao;
        this.doctorPigInfoCache = doctorPigInfoCache;
        this.workFlowService=workFlowService;
    }

    @Override
    public Response<Long> createPig(DoctorPig pig) {
        try {
            doctorPigDao.create(pig);
            checkState(doctorPigInfoCache.judgePigCodeNotContain(pig.getOrgId(), pig.getPigCode()), "validate.pigCode.fail");
            doctorPigInfoCache.addPigCodeToFarm(pig.getOrgId(), pig.getPigCode());
            return Response.ok(pig.getId());
        } catch (IllegalStateException e){
            log.info("illegal state doctor pig info cache error, pig:{}, cause:{}", pig, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("create pig failed, pig:{}, cause:{}", pig, Throwables.getStackTraceAsString(e));
            return Response.fail("pig.create.fail");
        }
    }

    @Override
    public Response<Long> createPigTrack(DoctorPigTrack pigTrack) {
        try {
            doctorPigTrackDao.create(pigTrack);
            return Response.ok(pigTrack.getId());
        } catch (Exception e) {
            log.error("create pigTrack failed, pigTrack:{}, cause:{}", pigTrack, Throwables.getStackTraceAsString(e));
            return Response.fail("pigTrack.create.fail");
        }
    }

    @Override
    public Response<Integer> updatePigTrackExtraMessage(DoctorPigTrack pigTrack) {
        try{
            return Response.ok(doctorPigTrackDao.updateExtraMessage(pigTrack));
        } catch (Exception e) {
            log.error("update pig track filed, pig id is {}, cause by {}", pigTrack.getPigId(), Throwables.getStackTraceAsString(e));
            return Response.fail("pigTrack.update.fail");
        }
    }

    @Override
    public Response<Long> createPigEvent(DoctorPigEvent pigEvent) {
        try {
            doctorPigEventDao.create(pigEvent);
            return Response.ok(pigEvent.getId());
        } catch (Exception e) {
            log.error("create pigEvent failed, pigEvent:{}, cause:{}", pigEvent, Throwables.getStackTraceAsString(e));
            return Response.fail("pigEvent.create.fail");
        }
    }

    @Override
    public Response<Long> createPigSnapShot(DoctorPigSnapshot pigSnapshot) {
        try {
            doctorPigSnapshotDao.create(pigSnapshot);
            return Response.ok(pigSnapshot.getId());
        } catch (Exception e) {
            log.error("create pigSnapshot failed, pigSnapshot:{}, cause:{}", pigSnapshot, Throwables.getStackTraceAsString(e));
            return Response.fail("pigSnapshot.create.fail");
        }
    }

    @Override
    public Response<Boolean> deploy() {
        try {
            workFlowService.getFlowDefinitionService().deploy(new ClassPathResource("flow/sow.xml").getInputStream());
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("deploy() failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("deploy() fail");
        }
    }

    @Override
    public Response<Boolean> refreshPigStatus() {
        try {
            for (int i = 0;; i++) {
                Map<String, Object> map = Maps.newHashMap();
                map.put("status", PigStatus.Entry.getKey());
                Paging<DoctorPigTrack> trackPage = doctorPigTrackDao.paging(i, 1000, map);
                trackPage.getData().forEach(doctorPigTrack -> {
                    if (doctorPigEventDao.queryAllEventsByPigId(doctorPigTrack.getPigId()).size() > 1){
                        doctorPigTrack.setStatus(PigStatus.Wean.getKey());
                        doctorPigTrack.setUpdatedAt(new Date());
                        doctorPigTrackDao.update(doctorPigTrack);
                    }
                });
                if (trackPage.getData().size() < 1000){
                    break;
                }
            }
            return Response.ok(Boolean.TRUE);
        } catch (Exception e){
            log.error("refresh.pig.status.failed, cause{}", Throwables.getStackTraceAsString(e));
            return Response.fail("refresh.pig.status.failed");
        }
    }
}
