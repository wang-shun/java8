package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.manager.DoctorDepartmentManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by xjn on 17/7/19.
 * 写入实现
 */
@Slf4j
@Service
@RpcProvider
public class DoctorDepartmentWriteServiceImpl implements DoctorDepartmentWriteService{

    @Autowired
    private DoctorDepartmentManager doctorDepartmentManager;

    @Override
    public Response<Boolean> bindDepartment(Long parentId, List<Long> orgIds) {
        try {
            return Response.ok(doctorDepartmentManager.bindDepartment(parentId, orgIds));
        } catch (Exception e) {
            log.error("bind department failed, parentId:{}, orgIds:{}, cause:{}"
                    , parentId, orgIds,  Throwables.getStackTraceAsString(e));
            return Response.fail("bind department failed");
        }

    }

    @Override
    public Response<Boolean> unbindDepartment(Long orgId) {
        try {
            return Response.ok(doctorDepartmentManager.unbindDepartment(orgId));
        } catch (Exception e) {
            log.error("unbind department failed, orgId:{}, cause:{}"
                    , orgId, Throwables.getStackTraceAsString(e));
            return Response.fail("unbind department failed");
        }
    }
}
