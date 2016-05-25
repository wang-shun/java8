package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.model.DoctorGroup;
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
    public Response<Long> createNewGroup(DoctorGroup group) {
        try {
            doctorGroupDao.create(group);
            return Response.ok(group.getId());
        } catch (Exception e) {
            log.error("create group failed, group:{}, cause:{}", group, Throwables.getStackTraceAsString(e));
            return Response.fail("group.create.fail");
        }
    }

}
