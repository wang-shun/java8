package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.manager.DoctorFarmManager;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class DoctorFarmWriteServiceImpl implements DoctorFarmWriteService{
    private final DoctorFarmDao doctorFarmDao;
    private final DoctorFarmManager doctorFarmManager;

    @Autowired
    public DoctorFarmWriteServiceImpl (DoctorFarmDao doctorFarmDao,
                                       DoctorFarmManager doctorFarmManager) {
        this.doctorFarmDao = doctorFarmDao;
        this.doctorFarmManager = doctorFarmManager;
    }


    @Override
    public Response<Long> createFarm(DoctorFarm farm) {
        Response<Long> response = new Response<>();
        try {
            doctorFarmDao.create(farm);
            response.setResult(farm.getId());
        } catch (Exception e) {
            log.error("create farm failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("create.farm.failed");
        }
        return response;
    }

    @Override
    public Response<List<DoctorFarm>> addFarms4PrimaryUser(Long userId, Long orgId, List<DoctorFarm> farms) {
        try {
            return Response.ok(doctorFarmManager.addFarms4PrimaryUser(userId, orgId, farms));
        } catch (Exception e) {
            log.error("create farm failed, userId:{}, orgId:{}, farms:{}, cause:{}", userId, orgId, farms, Throwables.getStackTraceAsString(e));
            return Response.fail("create.farm.failed");
        }
    }
}
