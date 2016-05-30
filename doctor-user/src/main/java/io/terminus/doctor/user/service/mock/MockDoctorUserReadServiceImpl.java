package io.terminus.doctor.user.service.mock;

import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RandomUtil;
import io.terminus.doctor.user.dto.DoctorUserInfoDto;
import io.terminus.doctor.user.service.DoctorUserReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@Slf4j
@Service
@Primary
public class MockDoctorUserReadServiceImpl implements DoctorUserReadService {

    @Override
    public Response<Integer> findUserRoleTypeByUserId(Long userId) {
        return Response.ok(RandomUtil.random(1, 3));
    }

    @Override
    public Response<DoctorUserInfoDto> findUserInfoByUserId(Long userId) {
        return Response.ok();
    }
}
