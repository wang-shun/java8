package io.terminus.doctor.user.service.mock;

import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RandomUtil;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.parana.user.model.User;
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
    public Response<User> findUserInfoByUserId(Long userId) {
        return Response.ok(mockUser(userId));
    }

    private User mockUser(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setName("测试用户" + userId);
        user.setMobile("13333333333");
        user.setStatus(1);
        user.setType(2);
        user.setPassword("passwd");
        return user;
    }
}
