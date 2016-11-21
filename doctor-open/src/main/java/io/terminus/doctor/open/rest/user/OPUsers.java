package io.terminus.doctor.open.rest.user;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.web.core.login.DoctorCommonSessionBean;
import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;
import io.terminus.pampas.openplatform.exceptions.OPClientException;
import io.terminus.parana.web.msg.MsgWebService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.Serializable;
import java.util.Map;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2016-03-15 4:06 PM  <br>
 * Author: xiao
 */
@Slf4j
@OpenBean
@SuppressWarnings("all")
public class OPUsers {

    @Autowired
    private MsgWebService appPushWebService;
    @Autowired
    private DoctorCommonSessionBean doctorCommonSessionBean;

    /**
     * 获取sessionID
     * @param key
     * @return
     */
    @OpenMethod(key = "get.session.id", paramNames = {"key"})
    public Map<String, String> getSessionId(String key) {
        try{
            return ImmutableMap.of("sessionId", doctorCommonSessionBean.getSessionId(key));
        } catch(Exception e) {
            throw new OPClientException(e.getMessage());
        }
    }

    /**
     * 获取服务器时间
     * @return
     */
    @OpenMethod(key="server.time", paramNames = {})
    public Map<String, String> serverTime() {
        return ImmutableMap.of("time", DateTime.now().toString(DateTimeFormat.forPattern("yyyyMMddHHmmss")));
    }

    /**
     * 发送短信验证码
     * @param mobile
     * @param sessionId
     * @param type 枚举 io.terminus.doctor.msg.enums.SmsCodeType.
     *             允许type == null, 此时认为是注册场景
     * @return
     */
    @OpenMethod(key = "get.mobile.code", paramNames = {"mobile", "sid", "type"})
    public Boolean getMobileCode(@NotEmpty(message = "user.mobile.miss") String mobile,
                                 @NotEmpty(message = "session.id.miss")String sessionId,
                                 Integer type) {
        try{
            return doctorCommonSessionBean.getMobileCode(mobile, sessionId, type);
        } catch(Exception e) {
            throw new OPClientException(e.getMessage());
        }
    }

    /**
     * 获取手机验证码
     * @param mobile
     * @return
     */
    @OpenMethod(key = "send.sms.code", paramNames = {"mobile", "sid", "templateName"})
    public Boolean sendSmsCode(@NotEmpty(message = "user.mobile.miss") String mobile,
                               @NotEmpty(message = "session.id.miss")String sessionId,
                               @NotEmpty(message = "template.name.miss")String templateName) {
        try{
            return doctorCommonSessionBean.sendSmsCode(mobile, sessionId, templateName);
        } catch(Exception e) {
            throw new OPClientException(e.getMessage());
        }
    }

    /**
     * 用户注册
     *
     * @param password   密码
     * @param mobile     手机号
     * @param code       手机验证码
     * @return 注册成功之后的用户ID
     */
    @OpenMethod(key = "user.register", httpMethods = RequestMethod.POST, paramNames = {"password", "mobile", "code", "sid"})
    public Long register(@NotEmpty(message = "user.password.miss") String password,
                         @NotEmpty(message = "user.mobile.miss") String mobile,
                         @NotEmpty(message = "user.code.miss") String code,
                         @NotEmpty(message = "session.id.miss")String sessionId) {
        try {
            return doctorCommonSessionBean.register(password, mobile, code, sessionId);
        } catch(Exception e) {
            throw new OPClientException(e.getMessage());
        }
    }


    /**
     * 用户登录
     * @param mobile
     * @param password
     * @param code
     * @param sessionId
     * @param deviceId
     * @return
     */
    @OpenMethod(key="user.login", paramNames = {"name", "password", "code", "sid", "deviceId"})
    public DoctorCommonSessionBean.Token login(String name, String password, String code, String sessionId,
                                               @NotEmpty(message = "deviceId.not.empty") String deviceId) {
        try {
            return doctorCommonSessionBean.login(name, password, code, sessionId, deviceId);
        } catch(Exception e) {
            throw new OPClientException(e.getMessage());
        }
    }

    /**
     * 用户自动登录, 需要传入 sessionId 和 deviceId 防止sessionId泄露
     * @param sessionId
     * @param deviceId
     * @return
     */
    @OpenMethod(key="user.auto.login", paramNames = {"sid", "deviceId"})
    public DoctorCommonSessionBean.Token autologin(String sessionId, String deviceId) {
        try {
            return doctorCommonSessionBean.autologin(sessionId, deviceId);
        } catch(Exception e) {
            throw new OPClientException(e.getMessage());
        }
    }

    @OpenMethod(key="get.user.captcher", paramNames = {"sid"})
    public Map<String, String> captcher(String sessionId) {
        try {
            return doctorCommonSessionBean.captcher(sessionId);
        } catch(Exception e) {
            throw new OPClientException(e.getMessage());
        }
    }


    @OpenMethod(key="user.device.bind", paramNames = {"deviceToken", "deviceType"})
    public void bindDevice(String deviceToken, Integer deviceType) {
        doctorCommonSessionBean.bindDevice(deviceToken, deviceType);
    }

    @OpenMethod(key="user.device.unbind", paramNames = {"deviceToken"})
    public void unbindDevice(String deviceToken){
        doctorCommonSessionBean.unbindDevice(deviceToken);
    }

    @OpenMethod(key="user.logout", paramNames = {"sid"})
    public void logout(String sessionId) {
        doctorCommonSessionBean.logout(sessionId);
    }

    /**
     * 用户修改密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    @OpenMethod(key = "user.change.password", httpMethods = RequestMethod.POST, paramNames = {"oldPassword", "newPassword", "sid"})
    public Boolean changePassword(@NotEmpty(message = "oldPassword.not.empty") String oldPassword,
                                  @NotEmpty(message = "newPassword.not.empty") String newPassword,
                                  @NotEmpty(message = "session.id.miss")String sessionId) {
        try {
            return doctorCommonSessionBean.changePassword(oldPassword, newPassword, sessionId);
        } catch(Exception e) {
            throw new OPClientException(e.getMessage());
        }
    }

    /**
     * 忘记密码
     * @param mobile  手机号
     * @param code    验证码
     * @param newPassword  新密码
     * @return 是否成功
     */
    @OpenMethod(key = "user.forget.password", httpMethods = RequestMethod.POST, paramNames = {"mobile", "code", "newPassword", "sid"})
    public Boolean forgetPassword(@NotEmpty(message = "mobile.not.empty") String mobile,
                                  @NotEmpty(message = "code.not.empty") String code,
                                  @NotEmpty(message = "newPassword.not.empty") String newPassword,
                                  @NotEmpty(message = "session.id.miss")String sessionId) {
        try {
            return doctorCommonSessionBean.forgetPassword(mobile, code, newPassword, sessionId);
        } catch(Exception e) {
            throw new OPClientException(e.getMessage());
        }
    }

    /**
     * 测试APP push的测试
     * @param ids
     * @return
     */
    @OpenMethod(key = "user.test.push", httpMethods = RequestMethod.GET, paramNames = {"ids"})
    public String testpush(String ids) {
        String result = appPushWebService.send("[1]", "user.register.code", MapBuilder.<String, Serializable>of().put("code", "推送123456").map(), null);
        log.debug(result);
        return result;
    }

}
