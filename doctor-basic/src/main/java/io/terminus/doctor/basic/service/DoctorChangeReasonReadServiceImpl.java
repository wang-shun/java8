package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorChangeReasonDao;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 变动类型表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorChangeReasonReadServiceImpl implements DoctorChangeReasonReadService {

    private final DoctorChangeReasonDao doctorChangeReasonDao;

    @Autowired
    public DoctorChangeReasonReadServiceImpl(DoctorChangeReasonDao doctorChangeReasonDao) {
        this.doctorChangeReasonDao = doctorChangeReasonDao;
    }

    @Override
    public Response<DoctorChangeReason> findChangeReasonById(Long changeReasonId) {
        try {
            return Response.ok(doctorChangeReasonDao.findById(changeReasonId));
        } catch (Exception e) {
            log.error("find changeReason by id failed, changeReasonId:{}, cause:{}", changeReasonId, Throwables.getStackTraceAsString(e));
            return Response.fail("changeReason.find.fail");
        }
    }

}
