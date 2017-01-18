package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.cache.CacheCenter;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.model.DoctorStaff;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@Service
@RpcProvider
public class DoctorStaffReadServiceImpl implements DoctorStaffReadService{
    private final DoctorStaffDao doctorStaffDao;
    private final CacheCenter cacheCenter;

    @Autowired
    public DoctorStaffReadServiceImpl(DoctorStaffDao doctorStaffDao, CacheCenter cacheCenter){
        this.doctorStaffDao = doctorStaffDao;
        this.cacheCenter = cacheCenter;
    }

    @Override
    public Response<DoctorStaff> findStaffByUserId(Long userId) {
        Response<DoctorStaff> response = new Response<>();
        try {
            response.setResult(cacheCenter.getStaff(userId));
        } catch (Exception e) {
            log.error("findStaffByUserId failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.staff.failed");
        }
        return response;
    }

    @Override
    public Response<List<DoctorStaff>> findStaffByOrgIdAndStatus(@NotNull(message = "orgId.not.null") Long orgId, Integer status) {
        Response<List<DoctorStaff>> response = new Response<>();
        try {
            response.setResult(doctorStaffDao.findByOrgIdAndStatus(orgId, status));
        } catch (Exception e) {
            log.error("findStaffByUserId failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.staff.failed");
        }
        return response;
    }

    @Override
    public Response<DoctorStaff> findStaffById(Long staffId) {
        try {
            return Response.ok(doctorStaffDao.findById(staffId));
        } catch (Exception e) {
            log.error("find staff by id failed, staffId:{}, cause:{}", staffId, Throwables.getStackTraceAsString(e));
            return Response.fail("staff.find.fail");
        }
    }
}
