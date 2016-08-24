package io.terminus.doctor.web.front.auth;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Desc: 猪群权限中心
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/20
 */
@Slf4j
@Component
public class DoctorFarmAuthCenter {

    private final DoctorFarmReadService doctorFarmReadService;

    @Autowired
    public DoctorFarmAuthCenter(DoctorFarmReadService doctorFarmReadService) {
        this.doctorFarmReadService = doctorFarmReadService;
    }

    /**
     * 根据登录用户, 校验是否有操作此猪场的权限
     * @param farmId 猪场id
     */
    public User checkFarmAuth(Long farmId) {
        Long userId = UserUtil.getUserId();
        if (userId == null) {
            throw new ServiceException("user.not.login");
        }
        List<Long> farmIds = RespHelper.orServEx(doctorFarmReadService.findFarmIdsByUserId(userId));
        if (!farmIds.contains(farmId)) {
            throw new ServiceException("user.not.auth.farm");
        }
        return UserUtil.getCurrentUser();
    }
}
