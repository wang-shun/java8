package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 猪群卡片明细表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorGroupTrackWriteServiceImpl implements DoctorGroupTrackWriteService {

    private final DoctorGroupTrackDao doctorGroupTrackDao;

    @Autowired
    public DoctorGroupTrackWriteServiceImpl(DoctorGroupTrackDao doctorGroupTrackDao) {
        this.doctorGroupTrackDao = doctorGroupTrackDao;
    }

    @Override
    public Response<Long> createGroupTrack(DoctorGroupTrack groupTrack) {
        try {
            doctorGroupTrackDao.create(groupTrack);
            return Response.ok(groupTrack.getId());
        } catch (Exception e) {
            log.error("create groupTrack failed, groupTrack:{}, cause:{}", groupTrack, Throwables.getStackTraceAsString(e));
            return Response.fail("groupTrack.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateGroupTrack(DoctorGroupTrack groupTrack) {
        try {
            return Response.ok(doctorGroupTrackDao.update(groupTrack));
        } catch (Exception e) {
            log.error("update groupTrack failed, groupTrack:{}, cause:{}", groupTrack, Throwables.getStackTraceAsString(e));
            return Response.fail("groupTrack.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteGroupTrackById(Long groupTrackId) {
        try {
            return Response.ok(doctorGroupTrackDao.delete(groupTrackId));
        } catch (Exception e) {
            log.error("delete groupTrack failed, groupTrackId:{}, cause:{}", groupTrackId, Throwables.getStackTraceAsString(e));
            return Response.fail("groupTrack.delete.fail");
        }
    }
}
