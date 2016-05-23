package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 猪群快照表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorGroupSnapshotReadServiceImpl implements DoctorGroupSnapshotReadService {

    private final DoctorGroupSnapshotDao doctorGroupSnapshotDao;

    @Autowired
    public DoctorGroupSnapshotReadServiceImpl(DoctorGroupSnapshotDao doctorGroupSnapshotDao) {
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
    }

    @Override
    public Response<DoctorGroupSnapshot> findGroupSnapshotById(Long groupSnapshotId) {
        try {
            return Response.ok(doctorGroupSnapshotDao.findById(groupSnapshotId));
        } catch (Exception e) {
            log.error("find groupSnapshot by id failed, groupSnapshotId:{}, cause:{}", groupSnapshotId, Throwables.getStackTraceAsString(e));
            return Response.fail("groupSnapshot.find.fail");
        }
    }

}
