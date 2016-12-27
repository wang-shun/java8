package io.terminus.doctor.web.front.auth;

import com.google.common.base.MoreObjects;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;

    @Autowired
    public DoctorFarmAuthCenter(DoctorFarmReadService doctorFarmReadService,
                                DoctorUserDataPermissionReadService doctorUserDataPermissionReadService) {
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
    }

    /**
     * 根据登录用户, 校验是否有操作此猪场的权限
     * @param farmId 猪场id
     */
    public BaseUser checkFarmAuth(Long farmId) {
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

    /**
     * 根据登录用户, 校验是否有操作此猪舍的权限
     * @param barnId 猪舍id
     */
    public BaseUser checkBarnAuth(Long barnId) {
        Long userId = UserUtil.getUserId();
        if (userId == null) {
            throw new ServiceException("user.not.login");
        }

        DoctorUserDataPermission permission = RespHelper.orServEx(doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));
        if(permission == null){
            throw new ServiceException("user.not.auth.barn");
        }

        List<Long> barnIds = MoreObjects.firstNonNull(permission.getBarnIdsList(), new ArrayList<Long>());
        if (!barnIds.contains(barnId)) {
            throw new ServiceException("user.not.auth.barn");
        }
        return UserUtil.getCurrentUser();
    }
}
