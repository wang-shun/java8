package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.parana.user.model.UserProfile;
import io.terminus.parana.user.service.UserProfileReadService;

import java.util.List;

/**
 * Desc: 用户个人信息的扩展
 * Mail: houly@terminus.io
 * Data: 下午9:26 16/6/6
 * Author: houly
 */
public interface DoctorUserProfileReadService extends UserProfileReadService {

    /**
     * 通过用户ID集合查询 用户个人信息集合
     * @param userIds
     * @return
     */
    Response<List<UserProfile>> findProfileByUserIds(List<Long> userIds);
}
