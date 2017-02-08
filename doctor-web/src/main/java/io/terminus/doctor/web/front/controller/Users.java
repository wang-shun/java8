/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.front.controller;

import com.google.common.base.CharMatcher;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.utils.RandomUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.msg.enums.SmsCodeType;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorRoleContent;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import io.terminus.doctor.user.model.DoctorUser;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.user.service.DoctorServiceStatusReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.user.service.DoctorUserRoleLoader;
import io.terminus.doctor.web.core.Constants;
import io.terminus.doctor.web.core.component.CaptchaGenerator;
import io.terminus.doctor.web.core.component.MobilePattern;
import io.terminus.doctor.web.core.login.DoctorCommonSessionBean;
import io.terminus.doctor.web.core.login.Sessions;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.auth.core.AclLoader;
import io.terminus.parana.auth.core.PermissionHelper;
import io.terminus.parana.auth.model.Acl;
import io.terminus.parana.auth.model.ParanaThreadVars;
import io.terminus.parana.auth.model.PermissionData;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserWriteService;
import io.terminus.session.AFSessionManager;
import io.terminus.session.util.WebUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class Users {

    private final UserWriteService<User> userWriteService;
    private final DoctorUserReadService doctorUserReadService;
    private final CaptchaGenerator captchaGenerator;
    private final MobilePattern mobilePattern;
    private final AclLoader aclLoader;
    private final PermissionHelper permissionHelper;
    private final DoctorCommonSessionBean doctorCommonSessionBean;
    private final AFSessionManager sessionManager;
    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;
    private final DoctorOrgReadService doctorOrgReadService;
    @RpcConsumer
    private DoctorUserRoleLoader doctorUserRoleLoader;
    @RpcConsumer
    private DoctorServiceStatusReadService doctorServiceStatusReadService;

    @Autowired
    public Users(UserWriteService<User> userWriteService,
                 DoctorUserReadService doctorUserReadService,
                 CaptchaGenerator captchaGenerator,
                 MobilePattern mobilePattern,
                 AclLoader aclLoader,
                 PermissionHelper permissionHelper,
                 DoctorCommonSessionBean doctorCommonSessionBean,
                 AFSessionManager sessionManager, DoctorUserDataPermissionReadService doctorUserDataPermissionReadService, DoctorOrgReadService doctorOrgReadService) {
        this.userWriteService = userWriteService;
        this.doctorUserReadService = doctorUserReadService;
        this.captchaGenerator = captchaGenerator;
        this.mobilePattern = mobilePattern;
        this.doctorCommonSessionBean = doctorCommonSessionBean;
        this.aclLoader = aclLoader;
        this.permissionHelper = permissionHelper;
        this.sessionManager = sessionManager;
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
        this.doctorOrgReadService = doctorOrgReadService;
    }

    /**
     * 用户
     * @return
     */
    @RequestMapping("")
    public BaseUser getLoginUser() {
        DoctorUser doctorUser = UserUtil.getCurrentUser();
        try {
            Acl acl = aclLoader.getAcl(ParanaThreadVars.getApp());
            BaseUser user = UserUtil.getCurrentUser();
            PermissionData perm = permissionHelper.getPermissions(acl, user, true);
            perm.setAllRequests(null); // empty it
            doctorUser.setAuth(JsonMapper.nonEmptyMapper().toJson(perm));
            return doctorUser;
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, JsonResponseException.class);
            log.error("get permissions of user failed, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("auth.permission.find.fail");
        }
    }
    @RequestMapping(value = "/getUserById/{id}",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public UserWithServiceStatus getUserById(@PathVariable(value = "id") Long id) {
        Response<User> userResponse=doctorUserReadService.findById(id);
        if (!userResponse.isSuccess()){
            throw new JsonResponseException(userResponse.getError());
        }
        UserWithServiceStatus uss = BeanMapper.map(userResponse.getResult(), UserWithServiceStatus.class);

        //设置下猪场软件服务的审核状态  0 未申请, 2 待审核(已提交申请), 1 通过，-1 不通过, -2 冻结申请资格
        Response<DoctorServiceStatus> response = doctorServiceStatusReadService.findByUserId(uss.getId());
        if (response.isSuccess() && response.getResult() != null) {
            uss.setReviewStatusDoctor(response.getResult().getPigdoctorReviewStatus());
        }
        return uss;
    }
    @RequestMapping(value = "/{userId}/roles", method = {RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<DoctorRoleContent> getUserRolesByUserId(@PathVariable Long userId) {
        return doctorUserRoleLoader.hardLoadRoles(userId);
    }
    /**
     * 生成sessionId
     */
    @RequestMapping(value = "/getSid", method = RequestMethod.POST)
    public String getSessionId(@RequestParam(value = "key", required = false) String key) {
        return doctorCommonSessionBean.getSessionId(MoreObjects.firstNonNull(key, String.valueOf(RandomUtil.random(1000, 99999))));
    }

    /**
     * 用户注册
     *
     * @param password   密码
     * @param mobile     手机号
     * @param code       手机验证码
     * @return 注册成功之后的用户ID
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Long register(@RequestParam("password") String password,
                         @RequestParam("mobile") String mobile,
                         @RequestParam("code") String code, HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new JsonResponseException(500, "session.expired");
        }
        Cookie cookie=WebUtil.findCookie(request,"msid");
        if (cookie==null){
            throw new JsonResponseException(500, "session.expired");
        }
        Object sessionId = cookie.getValue();
        return doctorCommonSessionBean.register(password, mobile, code, String.valueOf(sessionId));
    }

    /**
     * 登录
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> login(@RequestParam("loginBy") String name,
                                     @RequestParam("password") String password,
                                     @RequestParam(value = "deviceId", required = false, defaultValue = "PC") String deviceId,
                                     @RequestParam(value = "sid", required = false) String sid,
                                     @RequestParam(value = "code", required = false) String code,
                                     @RequestParam(value = "target", required = false) String target,
                                     HttpServletRequest request) {

        String sessionId = MoreObjects.firstNonNull(sid, doctorCommonSessionBean.getSessionId(name));
        DoctorCommonSessionBean.Token token = doctorCommonSessionBean.login(name, password, code, sessionId, deviceId);

        // 存一份 http session
        Map<String, Object> snapshot = sessionManager.findSessionById(Sessions.TOKEN_PREFIX, sessionId);
        request.getSession().setAttribute(Constants.SESSION_USER_ID, snapshot.get(Sessions.USER_ID));

        //将后台生成的sessionId返回给前台，用于以后的sid
        return MapBuilder.<String, Object>of()
                .put("userId",snapshot.get(Sessions.USER_ID))
                .put("sid", token.getSessionId())
                .put("redirect", !StringUtils.hasText(target) ? "/" : target)
                .put("expiredAt", token.getExpiredAt())
                .put("roles", token.getRoles())
                .map();
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(@RequestParam("sid") String sessionId) {
        doctorCommonSessionBean.logout(sessionId);
        return "/";
    }

    /**
     * 验证用户信息是否重复
     *
     * @param type      验证字段，有name，email，mobile
     * @param loginBy   输入内容
     * @param operation 1为创建时验证，2为修改时验证
     * @return 是否已存在
     */
    @RequestMapping(value = "/verify", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean verifyBy(@RequestParam("type") Integer type,
                            @RequestParam("loginBy") String loginBy,
                            @RequestParam(value = "operation", defaultValue = "1") Integer operation) {
        LoginType loginType = LoginType.from(type);
        Long userId = UserUtil.getUserId();
        if (loginType == null) {
            throw new JsonResponseException("unknown login type:" + type);
        }
        if (!Objects.equal(operation, 1) && !Objects.equal(operation, 2)) {
            throw new JsonResponseException("unknown operation");
        }
        Response<User> result = doctorUserReadService.findBy(loginBy, loginType);
        if (Objects.equal(operation, 1)) {
            if (result.isSuccess()) {
                log.warn("user info {} already exists", loginBy);
                return false;
            }
        } else {
            if (result.isSuccess() && !Objects.equal(result.getResult().getId(), userId)) {
                log.warn("user info {} already exists", loginBy);
                return false;
            }
        }
        return true;
    }

    /**
     * 生成图片验证码
     *
     * @param request 请求
     * @return 图片验证码
     */
    @RequestMapping(value = "/imgVerify", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> imgVerify(HttpServletRequest request) {
        // 生成验证码
        byte[] imgCache;
        HttpSession session = request.getSession();
        imgCache = captchaGenerator.captcha(session);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(imgCache, headers, HttpStatus.CREATED);
    }

    /**
     * 发送短信验证码
     *
     * @param mobile 手机号
     * @param smsCodeType 短信模板ID
     * @return 短信发送结果
     */
    @RequestMapping(value = "/sms", method = RequestMethod.POST)
    @ResponseBody
    public Boolean sendSms(@RequestParam("mobile") String mobile,
                           @RequestParam("smsCodeType") Integer smsCodeType, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Cookie cookie=WebUtil.findCookie(request,"msid");
        if (cookie==null){
            return false;
        }
        Object sessionId = cookie.getValue();

        return doctorCommonSessionBean.sendSmsCode(mobile, String.valueOf(sessionId), SmsCodeType.from(smsCodeType).template());
    }

    /**
     * 修改密码
     * @param oldPassword  旧密码
     * @param newPassword  新密码
     * @param sessionId    会话session
     * @return 是否成功
     */
    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    @ResponseBody
    public Boolean changePassword(@RequestParam("oldPassword") String oldPassword,
                                  @RequestParam("newPassword") String newPassword,
                                  @RequestParam("sid") String sessionId) {
        return doctorCommonSessionBean.changePassword(oldPassword, newPassword, sessionId);
    }

    /**
     * 忘记密码
     *
     * @param mobile      手机号
     * @param code        验证码
     * @param newPassword 新密码
     * @param request     请求
     * @return 是否成功
     */
    @RequestMapping(value = "/forgetPassword", method = RequestMethod.POST)
    public Boolean forgetPassword(@RequestParam("mobile") String mobile,
                                  @RequestParam("code") String code,
                                  @RequestParam("newPassword") String newPassword,
                                  HttpServletRequest request) {
        Cookie cookie=WebUtil.findCookie(request,"msid");
        if (cookie==null){
            throw new JsonResponseException("miss.msid");
        }
        Object sessionId = cookie.getValue();
        return  doctorCommonSessionBean.forgetPassword(mobile, code, newPassword, String.valueOf(sessionId));
    }

    @RequestMapping(value = "changeMobile", method = RequestMethod.POST)
    public Boolean changeMobile(@RequestParam("mobile") String mobile){
        BaseUser baseUser = UserUtil.getCurrentUser();
        if(baseUser == null){
            throw new JsonResponseException("user.not.login");
        }
        if (!mobilePattern.getPattern().matcher(mobile).matches()) {
            throw new JsonResponseException("mobile.format.error");
        }

        //校验手机号是否已经注册
        checkMobileExist(mobile);

        //更新user
        User user = RespHelper.or500(doctorUserReadService.findById(baseUser.getId()));
        user.setMobile(mobile);
        Map<String, String> extraMap = user.getExtra() == null ? new HashMap<>() : user.getExtra();
        extraMap.put("contact", mobile);
        user.setExtra(extraMap);
        return RespHelper.or500(userWriteService.update(user));
    }

    private void checkMobileExist(String mobile) {
        Response<User> exist = doctorUserReadService.findBy(mobile, LoginType.MOBILE);
        if (exist.isSuccess()) {
            log.error("change mobile exist, loginerId:{} mobile:{}", UserUtil.getUserId(), mobile);
            throw new JsonResponseException("mobile.already.exist");
        }
    }

    @RequestMapping(value = "/suggest", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<User> suggestUser(@RequestParam String query) {
        LoginType type;
        if (CharMatcher.DIGIT.matchesAllOf(query)) {
            type = LoginType.MOBILE;
        } else if (CharMatcher.is('@').matchesAnyOf(query)) {
            type = LoginType.EMAIL;
        } else {
            type = LoginType.NAME;
        }
        Response<User> resp = doctorUserReadService.findBy(query, type);
        if (!resp.isSuccess() || resp.getResult() == null) {
            return Collections.emptyList();
        }
        resp.getResult().setPassword(null); // for security
        return Lists.newArrayList(resp.getResult());
    }

    @RequestMapping(value = "/orgList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DoctorOrg> orgList() {
        BaseUser baseUser = UserUtil.getCurrentUser();
        if(baseUser == null){
            throw new JsonResponseException("user.not.login");
        }
        Response<DoctorUserDataPermission> dataPermissionResponse=doctorUserDataPermissionReadService.findDataPermissionByUserId(baseUser.getId());
        if (!dataPermissionResponse.isSuccess()){
            throw new JsonResponseException("user.not.permission");
        }
        List<Long> orgIds=dataPermissionResponse.getResult().getOrgIdsList();
        Response<List<DoctorOrg>> result=doctorOrgReadService.findOrgByIds(orgIds);
        if (!result.isSuccess()){
            throw new JsonResponseException(result.getError());
        }
        return result.getResult();
    }

    @Data
    private static class UserWithServiceStatus extends User implements Serializable {
        private static final long serialVersionUID = -4515482071656393479L;
        /**
         * 猪场软件服务的审核状态
         * @see DoctorServiceReview.Status
         */
        private Integer reviewStatusDoctor;
    }
}
