package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dto.DoctorDepartmentDto;
import io.terminus.doctor.user.manager.DoctorDepartmentManager;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by xjn on 17/7/19.
 * 读取实现
 */
@Slf4j
@Service
@RpcProvider
public class DoctorDepartmentReadServiceImpl implements DoctorDepartmentReadService{
    @Autowired
    private DoctorDepartmentManager doctorDepartmentManager;

    @Override
    public Response<List<DoctorFarm>> findAllFarmsByOrgId(@NotNull(message = "orgId.not.null") Long orgId) {
        try {
            return Response.ok(doctorDepartmentManager.findAllFarmsByOrgId(orgId));
        } catch (Exception e) {
            log.error("find all farms by orgId failed, orgId:{}, cause:{}", orgId, Throwables.getStackTraceAsString(e));
            return Response.fail("farm.find.fail");
        }
    }

    @Override
    public Response<DoctorDepartmentDto> findCliqueTree(@NotNull(message = "orgId.not.null") Long orgId) {
        try {
            return Response.ok(doctorDepartmentManager.findCliqueTree(orgId));
        } catch (Exception e) {
            log.error("find clique tree failed, orgId:{}, cause:{}", orgId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.clique.tree.failed");
        }
    }
}
