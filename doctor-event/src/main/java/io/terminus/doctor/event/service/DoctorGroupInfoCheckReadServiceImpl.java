package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.event.dao.DoctorGroupInfoCheckDao;
import io.terminus.doctor.event.model.DoctorGroupInfoCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.ArgumentConvertingMethodInvoker;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Code generated by terminus code gen
 * Desc: 猪群数据校验表读服务实现类
 * Date: 2017-03-25
 */
@Slf4j
@Service
public class DoctorGroupInfoCheckReadServiceImpl implements DoctorGroupInfoCheckReadService {

    private final DoctorGroupInfoCheckDao doctorGroupInfoCheckDao;

    @Autowired
    public DoctorGroupInfoCheckReadServiceImpl(DoctorGroupInfoCheckDao doctorGroupInfoCheckDao) {
        this.doctorGroupInfoCheckDao = doctorGroupInfoCheckDao;
    }

    @Override
    public Response<DoctorGroupInfoCheck> findDoctorGroupInfoCheckById(Long doctorGroupInfoCheckId) {
        try {
            return Response.ok(doctorGroupInfoCheckDao.findById(doctorGroupInfoCheckId));
        } catch (Exception e) {
            log.error("find doctorGroupInfoCheck by id failed, doctorGroupInfoCheckId:{}, cause:{}", doctorGroupInfoCheckId, Throwables.getStackTraceAsString(e));
            return Response.fail("doctorGroupInfoCheck.find.fail");
        }
    }

}
