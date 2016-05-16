/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.front.controller;

import com.google.common.base.Optional;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.model.ParanaUser;
import io.terminus.doctor.common.utils.EncryptUtil;
import io.terminus.doctor.user.enums.TargetSystem;
import io.terminus.doctor.web.core.component.MobilePattern;
import io.terminus.doctor.web.core.util.SimpleAESUtils;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.config.ConfigCenter;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.isEmpty;

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
    @Autowired
    private ConfigCenter configCenter;

    @Autowired
    public LoginOtherSystem(UserReadService<User> userReadService,
                            MobilePattern mobilePattern){
        this.userReadService = userReadService;
        this.mobilePattern = mobilePattern;
    }


    /**
     * 用户通过本系统登录pigmall 或neverest, 调用接口前将要传输的数据加密,然后返回完整的接口地址并附带参数,前台直接访问此URL即可登录目标系统
     * @param padding 加密算法,可选 pkcs5 | pkcs7 , 默认pkcs5
     * @param targetSystem 必要参数,用于区分用户将要登录到哪个系统去,关联枚举  TargetSystem
     * @param redirectPage 登录成功后打开的目标系统的页面,只要域名后面的部分,不要完整的URL
     * @return
     */
    @RequestMapping(value = "/getLoginUrl", method = RequestMethod.GET)
    @ResponseBody
    public Response<String> getLoginUrl(
            @RequestParam(value = "padding", required = false, defaultValue = "pkcs5") String padding,
            @RequestParam("targetSystem") Integer targetSystem,
            @RequestParam(value = "redirectPage", required = false, defaultValue = "") String redirectPage){

        ParanaUser paranaUser = UserUtil.getCurrentUser();
        if (isNull(paranaUser)) {
            return Response.fail("user.not.login");
        }

        Response<User> result = userReadService.findById(paranaUser.getId());
        if (!result.isSuccess()) {
            return Response.fail(result.getError());
        }
        User user = result.getResult();
        if (user == null) {
            return Response.fail("user.not.found");
        }
        if (user.getMobile() == null || !mobilePattern.getPattern().matcher(user.getMobile()).matches()) {
            return Response.fail("mobile.format.error");
        }

        Optional alg = SimpleAESUtils.algSelect(padding);
        if (!alg.isPresent()) {
            return Response.fail("unknown.padding.for.encrypt");
        }

        TargetSystem targetSystemEnum = TargetSystem.from(targetSystem);
        if(isNull(targetSystemEnum)){
            return Response.fail("unknown.target.system");
        }
        try {
            TargetSystemBean targetSystemBean = this.getTargetSystemBean(targetSystemEnum);

            String encryptedUserId = EncryptUtil.MD5(paranaUser.getId().toString()); //给userId加密
            String data = "third_user_id=" + encryptedUserId
                    + "\ntimestamp=" + (System.currentTimeMillis() / 1000)
                    + "\nmobile=" + user.getMobile();
            String encryptedData = SimpleAESUtils.encrypt(data, targetSystemBean.getPassword(), (String) alg.get());
            return Response.ok(targetSystemBean.getDomain() + "/api/all/third/access/" + targetSystemBean.getCorpId()
                    + "?d=" + encryptedData
                    + "&padding=" + padding
                    + (isEmpty(redirectPage) ? "" : "&redirectPage=" + redirectPage));
        } catch (Exception e) {
            throw new JsonResponseException(e);
        }
    }

    private class TargetSystemBean{
        @Getter @Setter
        private String domain;
        @Getter @Setter
        private String password;
        @Getter @Setter
        private Long corpId;
    }

    private TargetSystemBean getTargetSystemBean(TargetSystem targetSystemEnum){
        TargetSystemBean bean = new TargetSystemBean();
        String[] keys = targetSystemEnum.toString().split(";");
        bean.setDomain(this.getConfigValue(keys[0]));
        bean.setPassword(this.getConfigValue(keys[1]));
        bean.setCorpId(Long.parseLong(this.getConfigValue(keys[2])));
        return bean;
    }
    private String getConfigValue(String key){
        Optional<String> optional = configCenter.get(key);
        if (!optional.isPresent()) {
            log.error("required config is missing, key = {}", key);
            throw new JsonResponseException("required.config.missing");
        }
        return optional.get();
    }

}
