package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 猪群卡片表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorGroupWriteServiceImpl implements DoctorGroupWriteService {

    private final DoctorGroupDao doctorGroupDao;
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorGroupSnapshotDao doctorGroupSnapshotDao;
    private final DoctorGroupTrackDao doctorGroupTrackDao;

    @Autowired
    public DoctorGroupWriteServiceImpl(DoctorGroupDao doctorGroupDao,
                                       DoctorGroupEventDao doctorGroupEventDao,
                                       DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                       DoctorGroupTrackDao doctorGroupTrackDao) {
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
    }

    @Override
    public Response<Long> createGroup(DoctorGroup group) {
        try {
            doctorGroupDao.create(group);
            return Response.ok(group.getId());
        } catch (Exception e) {
            log.error("create group failed, group:{}, cause:{}", group, Throwables.getStackTraceAsString(e));
            return Response.fail("group.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateGroup(DoctorGroup group) {
        try {
            return Response.ok(doctorGroupDao.update(group));
        } catch (Exception e) {
            log.error("update group failed, group:{}, cause:{}", group, Throwables.getStackTraceAsString(e));
            return Response.fail("group.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteGroupById(Long groupId) {
        try {
            return Response.ok(doctorGroupDao.delete(groupId));
        } catch (Exception e) {
            log.error("delete group failed, groupId:{}, cause:{}", groupId, Throwables.getStackTraceAsString(e));
            return Response.fail("group.delete.fail");
        }
    }
    @Override
    public Response<Long> createGroupEvent(DoctorGroupEvent groupEvent) {
        try {
            doctorGroupEventDao.create(groupEvent);
            return Response.ok(groupEvent.getId());
        } catch (Exception e) {
            log.error("create groupEvent failed, groupEvent:{}, cause:{}", groupEvent, Throwables.getStackTraceAsString(e));
            return Response.fail("groupEvent.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateGroupEvent(DoctorGroupEvent groupEvent) {
        try {
            return Response.ok(doctorGroupEventDao.update(groupEvent));
        } catch (Exception e) {
            log.error("update groupEvent failed, groupEvent:{}, cause:{}", groupEvent, Throwables.getStackTraceAsString(e));
            return Response.fail("groupEvent.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteGroupEventById(Long groupEventId) {
        try {
            return Response.ok(doctorGroupEventDao.delete(groupEventId));
        } catch (Exception e) {
            log.error("delete groupEvent failed, groupEventId:{}, cause:{}", groupEventId, Throwables.getStackTraceAsString(e));
            return Response.fail("groupEvent.delete.fail");
        }
    }

    @Override
    public Response<Long> createGroupSnapshot(DoctorGroupSnapshot groupSnapshot) {
        try {
            doctorGroupSnapshotDao.create(groupSnapshot);
            return Response.ok(groupSnapshot.getId());
        } catch (Exception e) {
            log.error("create groupSnapshot failed, groupSnapshot:{}, cause:{}", groupSnapshot, Throwables.getStackTraceAsString(e));
            return Response.fail("groupSnapshot.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateGroupSnapshot(DoctorGroupSnapshot groupSnapshot) {
        try {
            return Response.ok(doctorGroupSnapshotDao.update(groupSnapshot));
        } catch (Exception e) {
            log.error("update groupSnapshot failed, groupSnapshot:{}, cause:{}", groupSnapshot, Throwables.getStackTraceAsString(e));
            return Response.fail("groupSnapshot.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteGroupSnapshotById(Long groupSnapshotId) {
        try {
            return Response.ok(doctorGroupSnapshotDao.delete(groupSnapshotId));
        } catch (Exception e) {
            log.error("delete groupSnapshot failed, groupSnapshotId:{}, cause:{}", groupSnapshotId, Throwables.getStackTraceAsString(e));
            return Response.fail("groupSnapshot.delete.fail");
        }
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
