package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static io.terminus.common.utils.Arguments.notEmpty;

@Slf4j
@Service
@RpcProvider
public class DoctorOrgReadServiceImpl implements DoctorOrgReadService{

    private final DoctorOrgDao doctorOrgDao;
    private final DoctorUserDataPermissionDao doctorUserDataPermissionDao;

    @Autowired
    public DoctorOrgReadServiceImpl(DoctorOrgDao doctorOrgDao,
                                    DoctorUserDataPermissionDao doctorUserDataPermissionDao) {
        this.doctorOrgDao = doctorOrgDao;
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
    }

    @Override
    public Response<DoctorOrg> findOrgById(Long orgId) {
        Response<DoctorOrg> response = new Response<>();
        try {
            response.setResult(doctorOrgDao.findById(orgId));
        } catch (Exception e) {
            log.error("find org by id failed, orgId:{}, cause:{}", orgId, Throwables.getStackTraceAsString(e));
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
            log.error("find org by id failed, orgIds:{}, cause:{}", orgIds, Throwables.getStackTraceAsString(e));
            response.setError("find.org.by.id.failed");
        }
        return response;
    }

    @Override
    public Response<List<DoctorOrg>> findOrgsByUserId(Long userId) {
        try {
            DoctorUserDataPermission permission = doctorUserDataPermissionDao.findByUserId(userId);
            if (permission == null || !notEmpty(permission.getOrgIdsList())) {
                return Response.ok(Collections.emptyList());
            }
            return Response.ok(doctorOrgDao.findByIds(permission.getOrgIdsList()));
        } catch (Exception e) {
            log.error("find orgs by userId failed, userId:{}, cause:{}", userId, Throwables.getStackTraceAsString(e));
            return Response.fail("org.find.fail");
        }
    }
}
