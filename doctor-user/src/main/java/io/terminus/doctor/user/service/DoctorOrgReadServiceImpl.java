package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.parana.common.utils.RespHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RpcProvider
public class DoctorOrgReadServiceImpl implements DoctorOrgReadService{

    private final DoctorOrgDao doctorOrgDao;
    private final DoctorStaffReadService doctorStaffReadService;

    @Autowired
    public DoctorOrgReadServiceImpl(DoctorOrgDao doctorOrgDao, DoctorStaffReadService doctorStaffReadService){
        this.doctorOrgDao = doctorOrgDao;
        this.doctorStaffReadService = doctorStaffReadService;
    }

    @Override
    public Response<DoctorOrg> findOrgById(Long orgId) {
        Response<DoctorOrg> response = new Response<>();
        try {
            response.setResult(doctorOrgDao.findById(orgId));
        } catch (Exception e) {
            log.error("find org by id failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.org.by.id.failed");
        }
        return response;
    }
    @Override
    public Response<List<DoctorOrg>> findOrgByIds(List<Long> orgIds) {
        Response<List<DoctorOrg>> response = new Response<>();
        try {
            response.setResult(doctorOrgDao.findByIds(orgIds));
        } catch (Exception e) {
            log.error("find org by id failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.org.by.id.failed");
        }
        return response;
    }

    @Override
    public Response<DoctorOrg> findOrgByUserId(Long userId) {
        Response<DoctorOrg> response = new Response<>();
        try {
            DoctorStaff staff = RespHelper.orServEx(doctorStaffReadService.findStaffByUserId(userId));
            if (staff != null && staff.getOrgId() != null) {
                response.setResult(doctorOrgDao.findById(staff.getOrgId()));
            } else {
                response.setResult(null);
            }
        } catch (ServiceException e) {
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("find org by userId failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.org.by.userId.failed");
        }
        return response;
    }
}
