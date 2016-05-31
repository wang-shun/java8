package io.terminus.doctor.user.service.mock;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorFarmWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
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
public class MockDoctorFarmWriteServiceImpl implements DoctorFarmWriteService {

    @Override
    public Response<Long> createFarm(DoctorFarm farm) {
        return Response.ok(1L);
    }

    @Override
    public Response<Integer> createFarms(List<DoctorFarm> farms) {
        return Response.ok(farms.size());
    }

    @Override
    public Response<Boolean> updateFarm(DoctorFarm farm) {
        return Response.ok(Boolean.TRUE);
    }

    @Override
    public Response<Boolean> deleteFarm(Long farmId) {
        return Response.ok(Boolean.TRUE);
    }
}
