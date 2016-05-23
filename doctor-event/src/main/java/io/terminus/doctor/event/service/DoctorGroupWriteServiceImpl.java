package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupDao;
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

    @Autowired
    public DoctorGroupWriteServiceImpl(DoctorGroupDao doctorGroupDao) {
        this.doctorGroupDao = doctorGroupDao;
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
}
