package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.dto.DoctorMenuDto;
import io.terminus.doctor.user.model.DoctorFarm;

import java.util.List;

/**
 * 手机一级菜单读取
 */
public interface DoctorMobileMenuReadService {

    /**
     * 通过用户ID和菜单等级获取菜单项
     * @param userId
     * @param level
     * @return
     */
    Response<List<DoctorMenuDto>> findMenuByUserIdAndLevel(Long userId, Integer level);
}
