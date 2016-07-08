package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.UserProfileExtraDao;
import io.terminus.parana.user.impl.dao.UserDao;
import io.terminus.parana.user.impl.dao.UserProfileDao;
import io.terminus.parana.user.impl.service.UserProfileReadServiceImpl;
import io.terminus.parana.user.model.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc:
 * Mail: houly@terminus.io
 * Data: 下午9:34 16/6/6
 * Author: houly
 */
@Service
@Slf4j
@Primary
@RpcProvider
public class DoctorUserProfileReadServiceImpl extends UserProfileReadServiceImpl implements DoctorUserProfileReadService {
    private final UserProfileExtraDao userProfileExtraDao;

    @Autowired
    public DoctorUserProfileReadServiceImpl(UserProfileDao userProfileDao, UserDao userDao, UserProfileExtraDao userProfileExtraDao){
        super(userProfileDao, userDao);
        this.userProfileExtraDao = userProfileExtraDao;
    }

    @Override
    public Response<List<UserProfile>> findProfileByUserIds(List<Long> userIds) {
        Response<List<UserProfile>> resp = new Response<List<UserProfile>>();
        try {
            List<UserProfile> profiles = userProfileExtraDao.findByUserIds(userIds);
            resp.setResult(profiles);
        } catch (Exception e){
            log.error("failed to find user profile by users(ids={}), cause: {}", userIds, Throwables.getStackTraceAsString(e));
            resp.setError("user.profile.find.fail");
        }
        return resp;
    }
}
