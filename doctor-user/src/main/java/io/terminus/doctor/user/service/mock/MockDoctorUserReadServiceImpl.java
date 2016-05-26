package io.terminus.doctor.user.service.mock;

import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RandomUtil;
import io.terminus.doctor.user.dto.DoctorUserInfoDto;
import io.terminus.doctor.user.model.DoctorStaff;
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
    public Response<DoctorUserInfoDto> findUserInfoByUserId(Long userId) {
        return Response.ok(new DoctorUserInfoDto(mockUser(userId), findUserRoleTypeByUserId(userId).getResult(), 1L, mockStaff(userId)));
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

    private DoctorStaff mockStaff(Long userId) {
        DoctorStaff staff = new DoctorStaff();
        staff.setId(userId);
        staff.setOrgId(userId);
        staff.setOrgName("测试公司"+userId);
        staff.setUserId(userId);
        staff.setRoleId(1L);
        staff.setRoleName("仓库管理员");
        staff.setStatus(1);
        staff.setSex(1);
        staff.setAvatar("http://img.xrnm.com/20150821-ee59df0636a3291405b61f997d314a19.jpg");
        return staff;
    }
}
