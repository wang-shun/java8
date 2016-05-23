package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.model.DoctorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc: 猪群卡片表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorGroupReadServiceImpl implements DoctorGroupReadService {

    private final DoctorGroupDao doctorGroupDao;

    @Autowired
    public DoctorGroupReadServiceImpl(DoctorGroupDao doctorGroupDao) {
        this.doctorGroupDao = doctorGroupDao;
    }

    @Override
    public Response<DoctorGroup> findGroupById(Long groupId) {
        try {
            return Response.ok(doctorGroupDao.findById(groupId));
        } catch (Exception e) {
            log.error("find group by id failed, groupId:{}, cause:{}", groupId, Throwables.getStackTraceAsString(e));
            return Response.fail("group.find.fail");
        }
    }

    @Override
    public Response<List<DoctorGroup>> findGroupsByFarmId(Long farmId) {
        try {
            return Response.ok(doctorGroupDao.findByFarmId(farmId));
        } catch (Exception e) {
            log.error("find group by farm id fail, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("group.find.fail");
        }
    }
}
