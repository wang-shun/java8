package io.terminus.doctor.open.rest.user;

import com.github.cage.Cage;
import com.github.cage.token.RandomTokenGenerator;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import io.terminus.boot.session.properties.SessionProperties;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RandomUtil;
import io.terminus.doctor.open.common.CaptchaGenerator;
import io.terminus.doctor.open.common.Sessions;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;
import io.terminus.pampas.openplatform.exceptions.OPClientException;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.model.UserDevice;
import io.terminus.parana.user.service.DeviceWriteService;
import io.terminus.parana.user.service.UserReadService;
import io.terminus.session.AFSessionManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.UUID;

import static io.terminus.common.utils.Arguments.isEmpty;
import static io.terminus.common.utils.Arguments.isNull;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2016-03-15 4:06 PM  <br>
 * Author: xiao
 */
@Slf4j
@OpenBean
@SuppressWarnings("all")
public class OPUsers {
    public static final Character SEP='Z';
    private final String hostIpMd5;

    @Autowired
    private CaptchaGenerator captchaGenerator;
    private final Cage cage = new Cage();
    @Autowired
    private AFSessionManager sessionManager;
    @Autowired
    private SessionProperties sessionProperties;
    @Autowired
    private UserReadService<User> userReadService;
    @Autowired
    private DeviceWriteService deviceWriteService;

    public OPUsers() {
        String hostIp;
        try {
            hostIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            hostIp = UUID.randomUUID().toString();
        }
        hostIpMd5 = Hashing.md5().hashString(hostIp, Charsets.UTF_8).toString().substring(0,8);
    }

    @OpenMethod(key = "get.session.id", paramNames = {"key"})
    public Map<String, String> getSessionId(String key) {
        if (isEmpty(key)) {
            throw new OPClientException("key.miss");
        }
        return ImmutableMap.of("sessionId", generateId(key));
    }

    private String generateId(String key) {
        StringBuilder builder = new StringBuilder(30);
        String clientKey =  Hashing.md5().hashString(key, Charsets.UTF_8).toString().substring(0, 8);
        builder.append(clientKey).append(SEP).append(hostIpMd5)
                .append(SEP).append(Long.toHexString(System.currentTimeMillis()))
                .append(SEP).append(UUID.randomUUID().toString().substring(0,4));
        return builder.toString();
    }

    @OpenMethod(key="server.time", paramNames = {})
    public Map<String, String> serverTime() {
        return ImmutableMap.of("time", DateTime.now().toString(DateTimeFormat.forPattern("yyyyMMddHHmmss")));
    }

    /**
     * 用户注册
     *
     * @param password   密码
     * @param userName   用户名
     * @param mobile     手机号
     * @param code       手机验证码
     * @param request    请求
     * @param response   响应
     * @return 注册成功之后的用户ID
     */
    @OpenMethod(key = "user.register", httpMethods = RequestMethod.POST, paramNames = {"password", "userName", "mobile", "code"})
    public Long register(@NotEmpty(message = "password.not.empty") String password,
                         @NotEmpty(message = "username.not.empty") String userName,
                         @NotEmpty(message = "mobile.not.empty") String mobile,
                         @NotEmpty(message = "code.not.empty") String code,
                         HttpServletRequest request, HttpServletResponse response) {
        return Long.valueOf(RandomUtil.random(1, 10));
    }

    @OpenMethod(key="user.login", paramNames = {"name", "password", "type", "code", "sid"})
    public Token login(String mobile, String password, String code, String sessionId) {
        if (isEmpty(mobile)) {
            throw new OPClientException("user.mobile.miss");
        }
        if (isEmpty(password)) {
            throw new OPClientException("user.password.miss");
        }
        if (isEmpty(sessionId)) {
            throw new OPClientException("session.id.miss");
        }

        // 当用户次数超过指定次数之后,需要校验code
        if (arriveErrorLimit(sessionId)) {
            if (isEmpty(code)) {
                throw new OPClientException("user.code.miss");
            }
        }
        // 判断CODE是否匹配
        Map<String, Object> snapshot = sessionManager.findSessionById(Sessions.CODE_PREFIX, sessionId);
        if (code != null && snapshot.get("code") != null && !snapshot.get("code").equals(code)) {
            throw new OPClientException("user.code.mismatch");
        }

        // 手机 密码登录
        User user = doLogin(mobile, password, sessionId);
        // 登录成功记录 session
        sessionManager.save(Sessions.TOKEN_PREFIX, sessionId, ImmutableMap.of(Sessions.USER_ID, (Object) user.getId()), Sessions.LONG_INACTIVE_INTERVAL);
        // 清除 limit & code
        sessionManager.deletePhysically(Sessions.LIMIT_PREFIX, sessionId);
        sessionManager.deletePhysically(Sessions.CODE_PREFIX, sessionId);

        // 返回登录的凭证
        Token token = new Token();
        token.setName(mobile);
        token.setDomain(sessionProperties.getCookieDomain());
        token.setExpiredAt(DateTime.now().plusSeconds(Sessions.LONG_INACTIVE_INTERVAL)
                .toString(DateTimeFormat.forPattern("yyyyMMddHHmmss")));
        token.setSessionId(sessionId);
        token.setCookieName(sessionProperties.getCookieName());
        return token;
    }

    //手机登录
    private User doLogin(String mobile, String password, String sessionId) {
        return doLogin(mobile, password, "3", sessionId);
    }

    private User doLogin(String name, String password, String type, String sessionId) {
        LoginType loginType = isNull(type) ? LoginType.NAME : LoginType.from(Integer.parseInt(type));
        Response<User> result = userReadService.login(name, password, loginType);
        if (!result.isSuccess()) {
            plusErrorCount(sessionId);
            refreshCaptcher(sessionId);
            // 登录失败, 记录登录失败次数
            throw new OPClientException(result.getError());
        }
        return result.getResult();
    }

    private void plusErrorCount(String sessionId) {
        Map<String, Object> snapshot = sessionManager.findSessionById(Sessions.LIMIT_PREFIX, sessionId);
        boolean arriveLimit = false;

        if (snapshot.size() == 0) {
            snapshot = Maps.newHashMap();
            snapshot.put("count", 1);
        } else {
            Integer count = (Integer) snapshot.get("count");
            count = count + 1;
            snapshot.put("count", count);
            if (count > 3) {
                arriveLimit = true;
            }
        }
        sessionManager.save(Sessions.LIMIT_PREFIX, sessionId, snapshot, Sessions.MIDDLE_INACTIVE_INTERVAL);
    }

    private boolean arriveErrorLimit(String sessionId) {
        Map<String, Object> snapshot = sessionManager.findSessionById(Sessions.LIMIT_PREFIX, sessionId);
        boolean arriveLimit = false;

        if (snapshot.size() >= 0 && snapshot.get("count") != null) {
            Integer count = (Integer) snapshot.get("count");
            if (count > 2) {
                arriveLimit = true;
            }
        }
        return arriveLimit;
    }

    @OpenMethod(key="get.user.captcher", paramNames = {"sid"})
    public Map<String, String> captcher(String sessionId) {
        if (isEmpty(sessionId)) {
            throw new OPClientException("session.id.miss");
        }
        String code = refreshCaptcher(sessionId);
        byte[] data = captchaGenerator.captcha(code);
        return ImmutableMap.of("captcher", Base64Utils.encodeToString(data));
    }

    private String refreshCaptcher(String sessionId) {
        // 将图片验证码存入session
        RandomTokenGenerator tokenGenerator = new RandomTokenGenerator(null, 4, 2);
        String code = tokenGenerator.next();
        Map<String, Object> snapshot = Maps.newHashMap();
        snapshot.put("code", code);
        sessionManager.save(Sessions.CODE_PREFIX, sessionId, snapshot, Sessions.SHORT_INACTIVE_INTERVAL);
        return code;
    }

    @OpenMethod(key="user.device.bind", paramNames = {"deviceToken"})
    public void bindDevice(String deviceToken) {
        if (isEmpty(deviceToken)) {
            throw new OPClientException("device.token.miss");
        }
        UserDevice userDevice = new UserDevice();
        userDevice.setUserId(UserUtil.getUserId());
        userDevice.setUserName(UserUtil.getCurrentUser().getName());
        userDevice.setDeviceToken(deviceToken);
        userDevice.setDeviceType("app");
        Response<Long> res = deviceWriteService.create(userDevice);
        if (!res.isSuccess()) {
            throw new OPClientException(res.getError());
        }
    }

    @OpenMethod(key="user.logout", paramNames = {"sid"})
    public void logout(String sessionId) {
        sessionManager.deletePhysically(Sessions.TOKEN_PREFIX, sessionId);

        // unbind user device
        Long uid = UserUtil.getUserId();
        Response<Integer> res = deviceWriteService.deleteByUserIdAndDeviceType(uid, "app");
        if (!res.isSuccess()) {
            throw new OPClientException("user.device.unbind.fail");
        }
    }

    /**
     * 用户修改密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    @OpenMethod(key = "user.change.password", httpMethods = RequestMethod.POST, paramNames = {"oldPassword", "newPassword"})
    public Boolean changePassword(@NotEmpty(message = "oldPassword.not.empty") String oldPassword,
                                  @NotEmpty(message = "newPassword.not.empty") String newPassword) {
        return Boolean.TRUE;
    }

    /**
     * 忘记密码
     * @param mobile  手机号
     * @param code    验证码
     * @param newPassword  新密码
     * @return 是否成功
     */
    @OpenMethod(key = "user.forget.password", httpMethods = RequestMethod.POST, paramNames = {"mobile", "code", "newPassword"})
    public Boolean forgetPassword(@NotEmpty(message = "mobile.not.empty") String mobile,
                                  @NotEmpty(message = "code.not.empty") String code,
                                  @NotEmpty(message = "newPassword.not.empty") String newPassword) {
        return Boolean.TRUE;
    }

    @Data
    class Token implements Serializable {
        String name;
        String expiredAt;
        String sessionId;
        String cookieName;
        String domain;
    }
}
