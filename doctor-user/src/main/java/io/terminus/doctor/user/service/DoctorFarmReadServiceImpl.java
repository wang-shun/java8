package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.parana.common.utils.RespHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@Slf4j
@Service
@RpcProvider
public class DoctorFarmReadServiceImpl implements DoctorFarmReadService{

    private final DoctorFarmDao doctorFarmDao;
    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;

    @Autowired
    public DoctorFarmReadServiceImpl(DoctorFarmDao doctorFarmDao,
                                     DoctorUserDataPermissionReadService doctorUserDataPermissionReadService){
        this.doctorFarmDao = doctorFarmDao;
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
    }

    @Override
    public Response<DoctorFarm> findFarmById(Long farmId) {
        Response<DoctorFarm> response = new Response<>();
        try {
            response.setResult(doctorFarmDao.findById(farmId));
        } catch (Exception e) {
            log.error("find farm by id failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.farm.by.id.failed");
        }
        return response;
    }

    @Override
    public Response<List<DoctorFarm>> findFarmsByUserId(Long userId) {
        Response<List<DoctorFarm>> response = new Response<>();
        try {
            response.setResult(doctorFarmDao.findByIds(RespHelper.orServEx(this.findFarmIdsByUserId(userId))));
        } catch (ServiceException e) {
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("find farms by userId failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.farms.by.userId.failed");
        }
        return response;
    }

    @Override
    public Response<List<Long>> findFarmIdsByUserId(Long userId){
        Response<List<Long>> response = new Response<>();
        try {
            DoctorUserDataPermission permission = RespHelper.orServEx(doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));
            if (permission != null) {
                response.setResult(permission.getFarmIdsList());
            } else {
                response.setResult(Lists.newArrayList());
            }
        } catch (ServiceException e) {
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("find farms by userId failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.farms.by.userId.failed");
        }
        return response;
    }

    @Override
    public Response<List<DoctorFarm>> findAllFarms() {
        try {
            return Response.ok(doctorFarmDao.findAll());
        } catch (Exception e) {
            log.error("find all farms failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("farm.find.fail");
        }
    }

    @Override
    public Response<List<DoctorFarm>> findFarmsByOrgId(@NotNull(message = "orgId.not.null") Long orgId) {
        try {
            return Response.ok(doctorFarmDao.findByOrgId(orgId));
        } catch (Exception e) {
            log.error("find farms by orgId failed, orgId:{}, cause:{}", orgId, Throwables.getStackTraceAsString(e));
            return Response.fail("farm.find.fail");
        }
    }
}
