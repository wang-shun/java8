package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.cache.DoctorPigInfoCache;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.manager.DoctorPigManager;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static io.terminus.common.utils.Arguments.notEmpty;

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
    private final DoctorPigManager doctorPigManager;

    @Autowired
    public DoctorPigWriteServiceImpl(DoctorPigDao doctorPigDao,
                                     DoctorPigTrackDao doctorPigTrackDao,
                                     DoctorPigEventDao doctorPigEventDao,
                                     DoctorPigSnapshotDao doctorPigSnapshotDao,
                                     DoctorPigInfoCache doctorPigInfoCache,
                                     DoctorPigManager doctorPigManager) {
        this.doctorPigDao = doctorPigDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorPigSnapshotDao = doctorPigSnapshotDao;
        this.doctorPigInfoCache = doctorPigInfoCache;
        this.doctorPigManager = doctorPigManager;
    }

    @Override
    public Response<Long> createPig(DoctorPig pig) {
        try {
            checkState(doctorPigInfoCache.judgePigCodeNotContain(pig.getFarmId(), pig.getPigCode()), "validate.pigCode.fail");
            doctorPigDao.create(pig);
            doctorPigInfoCache.addPigCodeToFarm(pig.getFarmId(), pig.getPigCode());
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
    public Response<Boolean> updatePigCodes(List<DoctorPig> pigs) {
        try{
            if (!notEmpty(pigs)) {
                return Response.ok(Boolean.TRUE);
            }
            return Response.ok(doctorPigManager.updatePigCodes(pigs));
        }catch(Exception e){
            log.error("update pig code failed, pigs:{}, cause:{}", pigs, Throwables.getStackTraceAsString(e));
            return Response.fail("update.pig.code.fail");
        }
    }
}
