package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.cache.DoctorPigInfoCache;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.manager.DoctorPigManager;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private final DoctorPigInfoCache doctorPigInfoCache;
    private final DoctorPigManager doctorPigManager;

    @Autowired
    public DoctorPigWriteServiceImpl(DoctorPigDao doctorPigDao,
                                     DoctorPigTrackDao doctorPigTrackDao,
                                     DoctorPigEventDao doctorPigEventDao,
                                     DoctorPigInfoCache doctorPigInfoCache,
                                     DoctorPigManager doctorPigManager) {
        this.doctorPigDao = doctorPigDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorPigInfoCache = doctorPigInfoCache;
        this.doctorPigManager = doctorPigManager;
    }

    @Override
    public Response<Boolean> updatePigCodes(List<DoctorPig> pigs) {
        try {
            if (!notEmpty(pigs)) {
                return Response.ok(Boolean.TRUE);
            }
            return Response.ok(doctorPigManager.updatePigCodes(pigs));
        } catch (ServiceException e) {
            log.error("update pig code failed, pigs:{}, cause:{}", pigs, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("update pig code failed, pigs:{}, cause:{}", pigs, Throwables.getStackTraceAsString(e));
            return Response.fail("update.pig.code.fail");
        }
    }

    @Override
    public Response<Boolean> updateCurrentBarnName(Long currentBarnId, String currentBarnName) {
        try {
            return Response.ok(doctorPigTrackDao.updateCurrentBarnName(currentBarnId, currentBarnName));
        } catch (Exception e) {
            log.error("update current barn name failed, currentBarnId:{}, currentBarnName:{}, cause:{}",
                    currentBarnId, currentBarnName, Throwables.getStackTraceAsString(e));
            return Response.fail("update.current.barn.name.failed");
        }
    }

    @Override
    public Response<Boolean> updatePig(DoctorPig pig, DoctorPigTrack track) {
        try {
            doctorPigManager.updatePig(pig, track);
            return Response.ok(true);
        } catch (Exception e) {
            log.error("update pig failed, pig:{}, track:{}, cause:{}",
                    pig, track, Throwables.getStackTraceAsString(e));
            return Response.fail("update.pig.failed");
        }
    }
}
