/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.front.controller;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.model.ParanaUser;
import io.terminus.doctor.common.utils.EncryptUtil;
import io.terminus.doctor.web.core.util.SimpleAESUtils;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static io.terminus.common.utils.Arguments.isNull;

/**
 * Author:  陈增辉
 * Date: 2016-05-10
 */
@Slf4j
@RestController
@RequestMapping("/api/user/third")
public class LoginOtherSystem {

    //TODO 这些配置应当写在配置文件里,或者在数据库里配置
    private static final String PASSWORD_LOGIN_PIGMALL = "pigmall"; //通过本系统登录PigMall的密码
    private static final long CORP_ID_IN_PIGMALL = 1L; //本系统在PigMall系统的 corp_id
    private static final String DOMAIN_PIGMALL = "http://www.pigmall.com";

    private static final String PASSWORD_LOGIN_NEVEREST = "neverest"; //通过本系统登录NEVEREST的密码
    private static final long CORP_ID_IN_NEVEREST = 1L; //本系统在NEVEREST系统的 corp_id
    private static final String DOMAIN_NEVEREST = "http://www.neverest.com";

    public enum TargetSystem {
        //名称必须与上面的常量的后缀相同
        PIGMALL    (1, "pigmall电商系统"),
        NEVEREST   (2, "neverest大数据系统");

        private int value;
        private String desc;
        private TargetSystem(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }
        public int value() {
            return value;
        }
        public static TargetSystem from(int number) {
            for (TargetSystem targetSystem : TargetSystem.values()) {
                if (Objects.equal(targetSystem.value, number)) {
                    return targetSystem;
                }
            }
            return null;
        }
        @Override
        public String toString() {
            return desc;
        }
    }

    /**
     * 用户通过本系统登录pigmall 或neverest, 调用接口前将要传输的数据加密,然后返回完整的接口地址并附带参数,前台直接访问此URL即可登录目标系统
     * 注意:只有已开通目标系统服务后(即 与目标系统绑定账号之后),才可以访问此URL,否则会被转向目标系统的注册页面
     * @param padding 加密算法,可选 pkcs5 | pkcs7 , 默认pkcs5
     * @param targetSystem 必要参数,用于区分用户将要登录到哪个系统去,关联枚举  LoginOtherSystem.TargetSystem
     * @param redirectPage 登录成功后打开的目标系统的页面,只要域名后面的部分,不要完整的URL
     * @return
     */
    @RequestMapping(value = "/getLoginUrl", method = RequestMethod.GET)
    @ResponseBody
    public Response<String> getLoginUrl(
            @RequestParam(value = "padding", required = false, defaultValue = "pkcs5") String padding,
            @RequestParam("targetSystem") Integer targetSystem,
            @RequestParam("mobile") String mobile,
            @RequestParam("email") String email,
            @RequestParam(value = "redirectPage", required = false, defaultValue = "") String redirectPage){

        ParanaUser user = UserUtil.getCurrentUser();
        if (isNull(user)) {
            return Response.fail("user.not.login");
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
            String password = this.getTargetSystemPassword(targetSystemEnum); //系统间调接口的密码,不是用户的密码
            String corpId = this.getCorpIdInTargetSystem(targetSystemEnum);
            String domain = this.getTargetSystemDomain(targetSystemEnum);

            String encryptedUserId = EncryptUtil.MD5Short(user.getId().toString()); //给userId加密
            String data = "third_user_id=" + encryptedUserId
                    + "\ntimestamp=" + (System.currentTimeMillis() / 1000)
                    + "\nmobile=" + mobile
                    + "\nemail=" + email;
            String encryptedData = SimpleAESUtils.encrypt(data, password, (String) alg.get());
            return Response.ok(domain + "/api/all/third/access/" + corpId
                    + "?d=" + encryptedData
                    + "&padding=" + padding
                    + (redirectPage == null ? "" : "&redirectPage=" + redirectPage));
        } catch (Exception e) {
            throw new JsonResponseException(e);
        }
    }

    private String getTargetSystemPassword(TargetSystem targetSystemEnum) throws NoSuchFieldException, IllegalAccessException {
        return this.getClass().getDeclaredField("PASSWORD_LOGIN_" + targetSystemEnum.name()).get(this).toString();
    }
    private String getCorpIdInTargetSystem(TargetSystem targetSystemEnum) throws NoSuchFieldException, IllegalAccessException {
        return this.getClass().getDeclaredField("CORP_ID_IN_" + targetSystemEnum.name()).get(this).toString();
    }
    private String getTargetSystemDomain(TargetSystem targetSystemEnum) throws NoSuchFieldException, IllegalAccessException {
        return this.getClass().getDeclaredField("DOMAIN_" + targetSystemEnum.name()).get(this).toString();
    }

    /**
     * 给指定的用户开通指定系统的服务,所谓开通服务其实就是在目标系统注册账号并绑定
     * @param userId
     * @param targetSystem 必要参数,用于区分要开通哪个系统的服务,关联枚举  @see LoginOtherSystem.TargetSystem
     * @return
     */
    @RequestMapping(value = "/openService", method = RequestMethod.GET)
    @ResponseBody
    public Response openService(@RequestParam("userId") Long userId, @RequestParam("targetSystem") Integer targetSystem){
        TargetSystem targetSystemEnum = TargetSystem.from(targetSystem);
        if(isNull(targetSystemEnum)){
            return Response.fail("unknown.target.system");
        }
        try {
            //仅在本系统记录已开通即可,此处不必调用目标系统的接口
        } catch (Exception e) {
            throw new JsonResponseException(e);
        }
        return null;
    }

    //以下代码仅供测试时生成链接使用
    public static void main(String[] args) throws Exception{
        ParanaUser user = new ParanaUser();
        user.setId(33333L);
        UserUtil.putCurrentUser(user);

        String redirectPage = null;
        String loginURL = new LoginOtherSystem().getLoginUrl("pkcs5", 1,"18888888888", "888@888.com", redirectPage).getResult();
        System.out.println(loginURL);
    }
}
