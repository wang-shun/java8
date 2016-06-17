package io.terminus.doctor.web.front.controller;

import com.google.common.base.Strings;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.model.UserProfile;
import io.terminus.parana.user.service.UserProfileReadService;
import io.terminus.parana.user.service.UserProfileWriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

    @Autowired
    private UserProfileWriteService userProfileWriteService;

    @Autowired
    private UserProfileReadService userProfileReadService;

    /**
     * 创建或更新 userProfile
     * @param profile 个人信息
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean updateOrCreateProfile(@RequestBody UserProfile profile) {
        checkUserLogin();
        profile.setUserId(UserUtil.getUserId());

        UserProfile existProfile = RespHelper.or500(userProfileReadService.findProfileByUserId(UserUtil.getUserId()));
        if (existProfile == null) {
            return RespHelper.or500(userProfileWriteService.createProfile(profile));
        }
        return RespHelper.or500(userProfileWriteService.updateProfile(profile));
    }

    /**
     * 根据当前登录用户 获取该用户附加信息
     *
     * @return 用户附加信息
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public UserProfile findProfileMine() {
        checkUserLogin();
        return RespHelper.or500(userProfileReadService.findProfileByUserId(UserUtil.getUserId()));
    }

    /**
     * 单独返回头像
     */
    @RequestMapping(value = "/avatar", method = RequestMethod.GET)
    @ResponseBody
    public String findProfileAvatar() {
        checkUserLogin();
        UserProfile profile = RespHelper.or500(userProfileReadService.findProfileByUserId(UserUtil.getUserId()));
        return Strings.nullToEmpty(profile.getAvatar());
    }

    private void checkUserLogin() {
        if (UserUtil.getCurrentUser() == null) {
            throw new JsonResponseException("user.not.login");
        }
    }
}


