package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.model.DoctorStaff;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RpcProvider
public class DoctorStaffReadServiceImpl implements DoctorStaffReadService{
    private final DoctorStaffDao doctorStaffDao;

    @Autowired
    public DoctorStaffReadServiceImpl(DoctorStaffDao doctorStaffDao) {
        this.doctorStaffDao = doctorStaffDao;
    }

    @Override
    public Response<DoctorStaff> findStaffByFarmIdAndUserId(Long farmId, Long userId) {
        try {
            return Response.ok(doctorStaffDao.findByFarmIdAndUserId(farmId, userId));
        } catch (Exception e) {
            log.error("findStaffByFarmIdAndUserId failed, farmId:{}, userId:{}, cause:{}",
                    farmId, userId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.staff.failed");
        }
    }

    @Override
    public Response<List<DoctorStaff>> findStaffByFarmIdAndStatus(Long farmId, Integer status) {
        try {
            return Response.ok(doctorStaffDao.findByFarmIdAndStatus(farmId, status));
        } catch (Exception e) {
            log.error("findStaffByFarmIdAndStatus failed, farmId:{}, status:{}, cause:{}",
                    farmId, status, Throwables.getStackTraceAsString(e));
            return Response.fail("find.staff.failed");
        }
    }
}
