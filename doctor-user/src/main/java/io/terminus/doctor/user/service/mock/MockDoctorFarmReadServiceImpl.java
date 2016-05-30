package io.terminus.doctor.user.service.mock;

import com.google.common.collect.Lists;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorFarmReadService;
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
public class MockDoctorFarmReadServiceImpl implements DoctorFarmReadService {

    @Override
    public Response<DoctorFarm> findFarmById(Long farmId) {
        return Response.ok(mockFarm(farmId));
    }

    @Override
    public Response<List<DoctorFarm>> findFarmsByUserId(Long userId) {
        return Response.ok(Lists.newArrayList(mockFarm(userId), mockFarm(userId + 1)));
    }

    private DoctorOrg mockOrg(Long id) {
        DoctorOrg org = new DoctorOrg();
        org.setId(id);
        org.setName("测试公司名称" + id);
        org.setLicense("http://img.xrnm.com/20151111-63d5ee1188e258249fa723892019ae18.jpg");
        return org;
    }

    private DoctorFarm mockFarm(Long id) {
        DoctorFarm farm = new DoctorFarm();
        farm.setId(id);
        farm.setName("测试猪场名称" + id);
        farm.setOrgId(id);
        farm.setOrgName("测试公司名称" + id);
        return farm;
    }
}
