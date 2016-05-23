package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.dto.DoctorUserInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@Slf4j
@Service
public class DoctorUserReadServiceImpl implements DoctorUserReadService, DoctorUserReadInterface {
    @Override
    public Response<Integer> findUserRoleTypeByUserId(Long userId) {
        return null;
    }

    @Override
    public Response<DoctorUserInfoDto> findUserInfoByUserId(Long userId) {
        return null;
    }

    @Override
    public Response<String> read(Integer test) {
        return Response.ok("read" + test);
    }
}
