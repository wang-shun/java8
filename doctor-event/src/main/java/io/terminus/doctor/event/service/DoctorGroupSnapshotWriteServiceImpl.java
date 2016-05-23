package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 猪群快照表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorGroupSnapshotWriteServiceImpl implements DoctorGroupSnapshotWriteService {

    private final DoctorGroupSnapshotDao doctorGroupSnapshotDao;

    @Autowired
    public DoctorGroupSnapshotWriteServiceImpl(DoctorGroupSnapshotDao doctorGroupSnapshotDao) {
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
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
}
