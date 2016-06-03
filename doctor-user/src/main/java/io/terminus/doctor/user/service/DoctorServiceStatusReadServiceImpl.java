package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorServiceStatusDao;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Code generated by terminus code gen
 * Desc: 用户服务状态表读服务实现类
 * Date: 2016-06-03
 */
@Slf4j
@Service
public class DoctorServiceStatusReadServiceImpl implements DoctorServiceStatusReadService {

    private final DoctorServiceStatusDao doctorServiceStatusDao;

    @Autowired
    public DoctorServiceStatusReadServiceImpl(DoctorServiceStatusDao doctorServiceStatusDao) {
        this.doctorServiceStatusDao = doctorServiceStatusDao;
    }

    @Override
    public Response<DoctorServiceStatus> findServiceStatusById(Long serviceStatusId) {
        try {
            return Response.ok(doctorServiceStatusDao.findById(serviceStatusId));
        } catch (Exception e) {
            log.error("find serviceStatus by id failed, serviceStatusId:{}, cause:{}", serviceStatusId, Throwables.getStackTraceAsString(e));
            return Response.fail("serviceStatus.find.fail");
        }
    }
}
