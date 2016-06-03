package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.model.DoctorStaff;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DoctorStaffReadServiceImpl implements DoctorStaffReadService{
    private final DoctorStaffDao doctorStaffDao;

    @Autowired
    public DoctorStaffReadServiceImpl(DoctorStaffDao doctorStaffDao){
        this.doctorStaffDao = doctorStaffDao;
    }

    @Override
    public Response<DoctorStaff> findStaffByUserId(Long userId) {
        Response<DoctorStaff> response = new Response<>();
        try {
            response.setResult(doctorStaffDao.findByUserId(userId));
        } catch (Exception e) {
            log.error("findStaffByUserId failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.staff.failed");
        }
        return response;
    }

    @Override
    public Response<List<DoctorStaff>> findStaffByOrgId(Long orgId) {
        Response<List<DoctorStaff>> response = new Response<>();
        try {
            response.setResult(doctorStaffDao.findByOrgId(orgId));
        } catch (Exception e) {
            log.error("findStaffByUserId failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.staff.failed");
        }
        return response;
    }
}
