package io.terminus.doctor.web.front.controller;

import com.google.common.base.Strings;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Response;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.UserProfile;
import io.terminus.parana.user.service.UserProfileReadService;
import io.terminus.parana.user.service.UserProfileWriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by cuiwentao on 16/3/9.
 */
@Controller
@RequestMapping("/api/profiles")
public class UserProfiles {

    @Autowired(required = false)
    private UserProfileWriteService userProfileWriteService;

    @Autowired(required = false)
    private UserProfileReadService userProfileReadService;

    @RequestMapping(value = "", method = RequestMethod.PUT)
    @ResponseBody
    public Boolean updateOrCreateProfile(@RequestBody UserProfile profile) {
        BaseUser user = UserUtil.getCurrentUser();

        if (user == null) {
            throw new JsonResponseException(401, "user.not.login");
        }

        Response<UserProfile> findProfileResp = userProfileReadService.findProfileByUserId(user.getId());
        if (!findProfileResp.isSuccess()) {
            throw new JsonResponseException(500, findProfileResp.getError());
        }

        UserProfile existProfile = findProfileResp.getResult();

        if (existProfile == null) {
            profile.setUserId(user.getId());
            Response<Boolean> resp = userProfileWriteService.createProfile(profile);
            if (!resp.isSuccess()) {
                throw new JsonResponseException(500, resp.getError());
            }
            return resp.getResult();
        }

        profile.setUserId(user.getId());
        Response<Boolean> resp = userProfileWriteService.updateProfile(profile);
        if (!resp.isSuccess()) {
            throw new JsonResponseException(500, resp.getError());
        }
        return resp.getResult();
    }


    /**
     * 根据当前登录用户 获取该用户附加信息
     *
     * @return 用户附加信息
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public UserProfile findProfileMine() {

        Response<UserProfile> resp = userProfileReadService.findProfileByUserId(UserUtil.getUserId());
        if (!resp.isSuccess()) {
            throw new JsonResponseException(500, resp.getError());
        }
        return resp.getResult();
    }

    /**
     * 单独返回头像
     */
    @RequestMapping(value = "/avatar", method = RequestMethod.GET)
    @ResponseBody
    public String findProfileAvatar() {
        Long userId = UserUtil.getUserId();
        if (userId == null) {
            throw new JsonResponseException(401, "user.not.login");
        }
        Response<UserProfile> profile = userProfileReadService.findProfileByUserId(userId);
        if (!profile.isSuccess()) {
            throw new JsonResponseException(500, profile.getError());
        }
        if (profile == null) {
            return "";
        }
        return Strings.nullToEmpty(profile.getResult().getAvatar());
    }

}


