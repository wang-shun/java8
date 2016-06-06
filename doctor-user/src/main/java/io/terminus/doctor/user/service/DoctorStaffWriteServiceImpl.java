package io.terminus.doctor.user.service;


import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.model.DoctorStaff;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DoctorStaffWriteServiceImpl implements DoctorStaffWriteService{
    private final DoctorStaffDao doctorStaffDao;


    @Autowired
    public DoctorStaffWriteServiceImpl(DoctorStaffDao doctorStaffDao){
        this.doctorStaffDao = doctorStaffDao;
    }


    @Override
    public Response<Long> createDoctorStaff(DoctorStaff staff) {
        Response<Long> response = new Response<>();
        try{
            doctorStaffDao.create(staff);
            response.setResult(staff.getId());
        }catch(Exception e){
            log.error("createDoctorStaff failed, cause : {]", Throwables.getStackTraceAsString(e));
            response.setError("create.doctor.staff.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> updateDoctorStaff(DoctorStaff staff) {
        Response<Boolean> response = new Response<>();
        try{
            response.setResult(doctorStaffDao.update(staff));
        }catch(Exception e){
            log.error("createDoctorStaff failed, cause : {]", Throwables.getStackTraceAsString(e));
            response.setError("update.doctor.staff.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> deleteDoctorStaff(Long staffId) {
        Response<Boolean> response = new Response<>();
        try{
            response.setResult(doctorStaffDao.delete(staffId));
        }catch(Exception e){
            log.error("createDoctorStaff failed, cause : {]", Throwables.getStackTraceAsString(e));
            response.setError("delete.doctor.staff.failed");
        }
        return response;
    }
}
