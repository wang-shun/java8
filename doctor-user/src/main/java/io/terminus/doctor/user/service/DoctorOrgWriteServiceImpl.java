package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.model.DoctorOrg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DoctorOrgWriteServiceImpl implements DoctorOrgWriteService{
    private final DoctorOrgDao doctorOrgDao;

    @Autowired
    public DoctorOrgWriteServiceImpl(DoctorOrgDao doctorOrgDao){
        this.doctorOrgDao = doctorOrgDao;
    }

    @Override
    public Response<Long> createOrg(DoctorOrg org) {
        Response<Long> response = new Response<>();
        try {
            doctorOrgDao.create(org);
            response.setResult(org.getId());
        } catch (Exception e) {
            log.error("create org failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("create.org.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> updateOrg(DoctorOrg org) {
        Response<Boolean> response = new Response<>();
        try {
            response.setResult(doctorOrgDao.update(org));
        } catch (Exception e) {
            log.error("update org failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("update.org.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> deleteOrg(Long orgId) {
        Response<Boolean> response = new Response<>();
        try {
            response.setResult(doctorOrgDao.delete(orgId));
        } catch (Exception e) {
            log.error("delete org failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("delete.org.failed");
        }
        return response;
    }
}
