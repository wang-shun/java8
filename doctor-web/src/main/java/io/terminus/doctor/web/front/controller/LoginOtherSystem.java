/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.front.controller;

import com.google.common.base.Joiner;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.doctor.common.model.ParanaUser;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.SimpleAESUtils;
import io.terminus.doctor.user.enums.TargetSystem;
import io.terminus.doctor.user.model.TargetSystemModel;
import io.terminus.doctor.user.model.UserBind;
import io.terminus.doctor.user.service.UserBindService;
import io.terminus.doctor.web.core.component.MobilePattern;
import io.terminus.doctor.web.core.service.OtherSystemService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static io.terminus.common.utils.Arguments.isEmpty;
import static io.terminus.common.utils.Arguments.isNull;

/**
 * Author:  陈增辉
 * Date: 2016-05-10
 */
@Slf4j
@RestController
@RequestMapping("/api/user/third")
public class LoginOtherSystem {

    private final UserReadService<User> userReadService;
    private final MobilePattern mobilePattern;
    private final OtherSystemService otherSystemService;
    private final UserBindService userBindService;

    @Autowired
    public LoginOtherSystem(UserReadService<User> userReadService, OtherSystemService otherSystemService,
                            MobilePattern mobilePattern, UserBindService userBindService){
        this.userReadService = userReadService;
        this.mobilePattern = mobilePattern;
        this.otherSystemService = otherSystemService;
        this.userBindService = userBindService;
    }

    private static final String URL_THIRD_USERS_ACCESS = "/api/all/third/access";

    /**
     * 用户通过本系统登录pigmall 或neverest, 调用接口前将要传输的数据加密,然后返回完整的接口地址并附带参数,前台直接访问此URL即可登录目标系统
     * @param padding 加密算法,可选 pkcs5 | pkcs7 , 默认pkcs5
     * @param targetSystem 必要参数,用于区分用户将要登录到哪个系统去,关联枚举  TargetSystem
     * @param redirectPage 登录成功后打开的目标系统的页面,只要域名后面的部分,不要完整的URL
     * @return
     */
    @RequestMapping(value = "/getLoginUrl", method = RequestMethod.GET)
    @ResponseBody
    public String getLoginUrl(
            @RequestParam(value = "padding", required = false, defaultValue = "pkcs5") String padding,
            @RequestParam("targetSystem") Integer targetSystem,
            @RequestParam(value = "redirectPage", required = false, defaultValue = "") String redirectPage){

        ParanaUser paranaUser = UserUtil.getCurrentUser();
        if (isNull(paranaUser)) {
            throw new JsonResponseException(500, "user.not.login");
        }

        User user = RespHelper.or500(userReadService.findById(paranaUser.getId()));
        if (user.getMobile() == null || !mobilePattern.getPattern().matcher(user.getMobile()).matches()) {
            throw new JsonResponseException(500, "mobile.format.error");
        }

        String algStr = SimpleAESUtils.algSelect(padding).or(() -> {
            throw new JsonResponseException(500, "unknown.padding.for.encrypt");
        });
        
        TargetSystem targetSystemEnum = TargetSystem.from(targetSystem);
        if(targetSystemEnum == null){
            throw new JsonResponseException(500, "unknown.target.system");
        }
        UserBind userBind = RespHelper.orServEx(userBindService.findUserBindByUserIdAndTargetSystem(paranaUser.getId(), targetSystemEnum));
        if (userBind == null) {
            throw new JsonResponseException(500, "no.user.bind.found");
        }
        try {
            TargetSystemModel model = otherSystemService.getTargetSystemModel(targetSystemEnum);
            String data = Joiner.on("").join("third_user_id=", userBind.getUuid(),
                    "\ntimestamp=", System.currentTimeMillis() / 1000,
                    "\nmobile=", user.getMobile());
            String encryptedData = SimpleAESUtils.encrypt(data, model.getPassword(), algStr);
            return Joiner.on("").join(model.getDomain(), URL_THIRD_USERS_ACCESS, "/", model.getCorpId(),
                    "?d=", encryptedData, "&padding=", padding, isEmpty(redirectPage) ? "" : "&redirectPage=" + redirectPage);
        } catch (Exception e) {
            throw new JsonResponseException(500, e);
        }
    }

}
