package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.model.DoctorBarn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 猪舍表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
@RpcProvider
public class DoctorBarnWriteServiceImpl implements DoctorBarnWriteService {

    private final DoctorBarnDao doctorBarnDao;

    @Autowired
    public DoctorBarnWriteServiceImpl(DoctorBarnDao doctorBarnDao) {
        this.doctorBarnDao = doctorBarnDao;
    }

    @Override
    public Response<Long> createBarn(DoctorBarn barn) {
        try {
            //校验猪舍名称是否重复
            checkBarnNameRepeat(barn.getFarmId(), barn.getName());
            doctorBarnDao.create(barn);
            return Response.ok(barn.getId());
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("create barn failed, barn:{}, cause:{}", barn, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateBarn(DoctorBarn barn) {
        try {
            doctorBarnDao.update(barn);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("update barn failed, barn:{}, cause:{}", barn, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.update.fail");
        }
    }

    @Override
    public Response<Boolean> updateBarnStatus(Long barnId, Integer status) {
        try {
            // TODO: 16/5/24 校验下是否可以更新成这个状态

            DoctorBarn barn = new DoctorBarn();
            barn.setId(barnId);
            barn.setStatus(status);
            doctorBarnDao.update(barn);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("update barn status failed, barnId:{}, status:{}, cause:{}",
                    barnId, status, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.update.fail");
        }
    }

    private void checkBarnNameRepeat(Long farmId, String barnName) {
        if (doctorBarnDao.findByFarmId(farmId).stream().anyMatch(barn -> barnName.equals(barn.getName()))) {
            throw new ServiceException("barn.name.repeat");
        }
    }
}
