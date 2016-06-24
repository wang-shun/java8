package io.terminus.doctor.open.rest.user;

import com.github.cage.Cage;
import com.github.cage.token.RandomTokenGenerator;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import io.terminus.boot.session.properties.SessionProperties;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.common.redis.utils.JedisTemplate;
import io.terminus.common.utils.MapBuilder;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.open.common.CaptchaGenerator;
import io.terminus.doctor.open.common.MobilePattern;
import io.terminus.doctor.open.common.Sessions;
import io.terminus.doctor.open.enums.MobileDeviceType;
import io.terminus.doctor.open.util.OPRespHelper;
import io.terminus.doctor.user.util.DoctorUserMaker;
import io.terminus.doctor.web.core.events.user.RegisterEvent;
import io.terminus.lib.sms.SmsException;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;
import io.terminus.pampas.openplatform.exceptions.OPClientException;
import io.terminus.parana.common.utils.EncryptUtil;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.model.UserDevice;
import io.terminus.parana.user.service.DeviceReadService;
import io.terminus.parana.user.service.DeviceWriteService;
import io.terminus.parana.user.service.UserReadService;
import io.terminus.parana.user.service.UserWriteService;
import io.terminus.parana.web.msg.MsgWebService;
import io.terminus.session.AFSessionManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMethod;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.terminus.common.utils.Arguments.isEmpty;
import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notNull;

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
    private UserWriteService<User> userWriteService;
    @Autowired
    private DeviceWriteService deviceWriteService;
    @Autowired
    private MsgWebService smsWebService;
    @Autowired
    private MsgWebService emailWebService;
    @Autowired
    private MsgWebService appPushWebService;
    @Autowired
    private MobilePattern mobilePattern;
    @Autowired
    private CoreEventDispatcher coreEventDispatcher;
    @Autowired
    private DeviceReadService deviceReadService;
    @Autowired
    private JedisTemplate jedisTemplate;


    public OPUsers() {
        String hostIp;
        try {
            hostIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            hostIp = UUID.randomUUID().toString();
        }
        hostIpMd5 = Hashing.md5().hashString(hostIp, Charsets.UTF_8).toString().substring(0,8);
    }

    /**
     * 获取sessionID
     * @param key
     * @return
     */
    @OpenMethod(key = "get.session.id", paramNames = {"key"})
    public Map<String, String> getSessionId(String key) {
        if (isEmpty(key)) {
            throw new OPClientException("key.miss");
        }
        return ImmutableMap.of("sessionId", generateId(key));
    }

    /**
     * sessionId 产生规则
     * @param key
     * @return
     */
    private String generateId(String key) {
        StringBuilder builder = new StringBuilder(30);
        String clientKey =  Hashing.md5().hashString(key, Charsets.UTF_8).toString().substring(0, 8);
        builder.append(clientKey).append(SEP).append(hostIpMd5)
                .append(SEP).append(Long.toHexString(System.currentTimeMillis()))
                .append(SEP).append(UUID.randomUUID().toString().substring(0,4));
        return builder.toString();
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
     * 获取手机验证码(不需要sessionId)
     * @param mobile
     * @return
     */
    @OpenMethod(key = "get.mobile.code", paramNames = {"mobile", "sid"})
    public Boolean sendSms(@NotEmpty(message = "user.mobile.miss") String mobile, @NotEmpty(message = "session.id.miss")String sessionId) {
        if (mobilePattern.getPattern().matcher(mobile).matches()) {
            Map<String, Object> snapshot = sessionManager.findSessionById(Sessions.CODE_PREFIX, sessionId);
            String activateCode = null;
            if(notNull(snapshot)){
                activateCode = Params.get(snapshot, "code");
            }
            Response<Boolean> result = null;

            if (!Strings.isNullOrEmpty(activateCode)) { //判断是否需要重新发送激活码
                List<String> parts = Splitters.AT.splitToList(activateCode);
                long sendTime = Long.parseLong(parts.get(1));
                if (System.currentTimeMillis() - sendTime < TimeUnit.MINUTES.toMillis(1)) { //
                    log.error("could not send sms, sms only can be sent once in one minute");
                    throw new OPClientException("1分钟内只能获取一次验证码");
                } else {
                    String code = generateMsgCode();

                    // 将code存放到session当中
                    sessionManager.save(Sessions.MSG_PREFIX, sessionId,
                            ImmutableMap.of("code", code + "@" + System.currentTimeMillis()+"@"+mobile),
                            Sessions.SHORT_INACTIVE_INTERVAL);
                    // 发送验证码
                    result = doSendSms(code, mobile);
                }
            } else { //新发送激活码
                String code = generateMsgCode();
                // 将code存放到session当中
                sessionManager.save(Sessions.MSG_PREFIX, sessionId,
                        ImmutableMap.of("code", code + "@" + System.currentTimeMillis()+"@"+mobile),
                        Sessions.SHORT_INACTIVE_INTERVAL);
                // 发送验证码
                result = doSendSms(code, mobile);
            }
            if(!result.isSuccess()) {
                log.warn("send sms single fail, cause:{}", result.getError());
                throw new OPClientException(result.getError());
            }
            return result.getResult();
        } else {
            throw new OPClientException("mobile.format.error");
        }
    }

    /**
     * 产生手机验证码
     * @return
     */
    private String generateMsgCode(){
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
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
        checkPasswordFormat(password);
        User user = null;
        // 校验手机验证码
        validateSmsCode(code, mobile, sessionId);
        user = registerByMobile(mobile, password, null);
        coreEventDispatcher.publish(new RegisterEvent(null, null, DoctorUserMaker.from(user)));
        return user.getId();
    }

    /**
     * 校验密码格式
     * @param password
     */
    private void checkPasswordFormat(String password){
        if (!password.matches("[\\s\\S]{6,16}")){
            throw new OPClientException("user.password.6to16");
        }
    }

    /**
     * 校验手机验证码
     *
     * @param code    输入的验证码
     * @param mobile  手机号
     * @param sessionId
     */
    private void validateSmsCode(String code, String mobile, String sessionId) {
        Map<String, Object> msgSession = sessionManager.findSessionById(Sessions.MSG_PREFIX, sessionId);
        // session verify, value = code@time@mobile
        String codeInSession = Params.get(msgSession, "code");
        if(isEmpty(codeInSession)){
            throw new OPClientException("sms.token.error");
        }
        String expectedCode = Splitters.AT.splitToList(codeInSession).get(0);
        if(!Objects.equals(code, expectedCode)){
            throw new OPClientException("sms.token.error");
        }
        String expectedMobile = Splitters.AT.splitToList(codeInSession).get(2);
        if(!Objects.equals(mobile, expectedMobile)){
            throw new OPClientException("sms.token.error");
        }
        // 如果验证成功则删除之前的code
        sessionManager.deletePhysically(Sessions.MSG_PREFIX, sessionId);
    }

    /**
     * 手机注册
     *
     * @param mobile 手机号
     * @param password 密码
     * @param userName 用户名
     * @return 注册成功之后的用户
     */
    private User registerByMobile(String mobile, String password, String userName) {
        Response<User> result = userReadService.findBy(mobile, LoginType.MOBILE);
        // 检测手机号是否已存在
        if(result.isSuccess() && result.getResult() != null){
            throw new OPClientException("user.register.mobile.has.been.used");
        }
        // 设置用户信息
        User user = new User();
        user.setMobile(mobile);
        user.setPassword(password);
        user.setName(userName);

        // 用户状态 0: 未激活, 1: 正常, -1: 锁定, -2: 冻结, -3: 删除
        user.setStatus(UserStatus.NORMAL.value());

        user.setType(UserType.FARM_ADMIN_PRIMARY.value());

        // 注册用户默认成为猪场管理员
        user.setRoles(Lists.newArrayList("PRIMARY", "PRIMARY(OWNER)"));

        Response<Long> resp = userWriteService.create(user);
        if(!resp.isSuccess()){
            throw new OPClientException(resp.getError());
        }
        user.setId(resp.getResult());
        return user;
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
    public Token login(String name, String password, String code, String sessionId, String deviceId) {
        if (isEmpty(name)) {
            throw new OPClientException("user.mobile.miss");
        }
        if (isEmpty(password)) {
            throw new OPClientException("user.password.miss");
        }
        if (isEmpty(sessionId)) {
            throw new OPClientException("session.id.miss");
        }
        if (isEmpty(deviceId)) {
            throw new OPClientException("device.id.miss");
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
        User user = doLogin(name, password, sessionId);
        log.info("login user info :{}", user);

        // 登录成功记录 sessionId 和 deviceId, 防止其他设备获得sessionId, 伪造登录
        String prefix = Sessions.TOKEN_PREFIX;
        Map<String, Object> map = ImmutableMap.of(Sessions.USER_ID, (Object) user.getId(), Sessions.DEVICE_ID, (Object) deviceId);
        Integer ttl = Sessions.LONG_INACTIVE_INTERVAL;
        log.info("before prefix:{}, map:{}, ttl:{}", prefix, map, ttl);
        sessionManager.save(Sessions.TOKEN_PREFIX, sessionId, map, 99999);
        log.info("after prefix:{}, map:{}, ttl:{}", prefix, map, ttl);

        ////////// test log
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.expire(Sessions.TOKEN_PREFIX + ":" + sessionId, 888);
                log.info("expire session:{}", sessionId);
            }
        });

        // 清除 limit & code
        sessionManager.deletePhysically(Sessions.LIMIT_PREFIX, sessionId);
        sessionManager.deletePhysically(Sessions.CODE_PREFIX, sessionId);

        // 返回登录的凭证
        Token token = new Token();
        token.setName(user.getName());
        token.setDomain(sessionProperties.getCookieDomain());
        token.setExpiredAt(DateTime.now().plusSeconds(Sessions.LONG_INACTIVE_INTERVAL)
                .toString(DateTimeFormat.forPattern("yyyyMMddHHmmss")));
        token.setSessionId(sessionId);
        token.setDeviceId(deviceId);
        token.setCookieName(sessionProperties.getCookieName());
        log.info("login token:{}", token);
        return token;
    }

    /**
     * 用户自动登录, 需要传入 sessionId 和 deviceId 防止sessionId泄露
     * @param sessionId
     * @param deviceId
     * @return
     */
    @OpenMethod(key="user.auto.login", paramNames = {"sid", "deviceId"})
    public Token autologin(String sessionId, String deviceId) {
        if (isEmpty(sessionId)) {
            throw new OPClientException("session.id.miss");
        }
        if (isEmpty(deviceId)) {
            throw new OPClientException("device.id.miss");
        }

        Map<String, Object> snapshot = sessionManager.findSessionById(Sessions.TOKEN_PREFIX, sessionId);
        if (snapshot == null || snapshot.size() == 0 || snapshot.get(Sessions.USER_ID) == null) {
            throw new OPClientException(400, "session.id.expired");
        }

        //校验下设备号是否匹配
        checkDeviceId(snapshot, deviceId);

        // refresh
        sessionManager.refreshExpireTime(Sessions.TOKEN_PREFIX, sessionId, Sessions.LONG_INACTIVE_INTERVAL);
        Long uid = Long.parseLong(snapshot.get(Sessions.USER_ID).toString());
        Response<User> res = userReadService.findById(uid);
        if (!res.isSuccess()) {
            throw new OPClientException(400, res.getError());
        }

        // 返回登录的凭证
        Token token = new Token();
        token.setName(res.getResult().getName());
        token.setDomain(sessionProperties.getCookieDomain());
        token.setExpiredAt(DateTime.now().plusSeconds(Sessions.LONG_INACTIVE_INTERVAL)
                .toString(DateTimeFormat.forPattern("yyyyMMddHHmmss")));
        token.setSessionId(sessionId);
        token.setDeviceId(deviceId);
        token.setCookieName(sessionProperties.getCookieName());
        return token;
    }

    //校验设备号是否匹配
    private void checkDeviceId(Map<String, Object> snapshot, String deviceId) {
        if (snapshot == null || snapshot.size() == 0 || snapshot.get(Sessions.DEVICE_ID) == null) {
            throw new OPClientException(400, "device.id.expired");
        }

        if (!Objects.equals(deviceId, String.valueOf(snapshot.get(Sessions.DEVICE_ID)))) {
            throw new OPClientException(400, "device.id.not.match");
        }
    }

    //手机登录
    private User doLogin(String mobile, String password, String sessionId) {
        return doLogin(mobile, password, "3", sessionId);
    }

    private User doLogin(String name, String password, String type, String sessionId) {
        LoginType loginType = null;

        if(isNull(type)){
            if(mobilePattern.getPattern().matcher(name).find()){
                loginType = LoginType.MOBILE;
            }
            else if(name.indexOf("@") != -1){
                loginType = LoginType.OTHER;
            }
            else {
                loginType = LoginType.NAME;
            }
        } else {
            loginType = LoginType.from(Integer.parseInt(type));
        }
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

    @OpenMethod(key="user.device.bind", paramNames = {"deviceToken", "deviceType"})
    public void bindDevice(String deviceToken, Integer deviceType) {
        if (isEmpty(deviceToken)) {
            throw new OPClientException("device.token.miss");
        }
        MobileDeviceType type = MobileDeviceType.from(deviceType);
        if(type == null){
            throw new OPClientException("device.type.error");
        }
        Long userId = UserUtil.getUserId();

        //先查询下是否已经有绑定
        for(UserDevice userDevice : OPRespHelper.orOPEx(deviceReadService.findByUserId(userId))){
            if(Objects.equals(userDevice.getDeviceToken(), deviceToken)){
                return;
            }
        }

        //没有重复数据,则添加新数据
        UserDevice userDevice = new UserDevice();
        userDevice.setUserId(userId);
        userDevice.setUserName(UserUtil.getCurrentUser().getName());
        userDevice.setDeviceToken(deviceToken);
        userDevice.setDeviceType(type.name().toLowerCase());
        OPRespHelper.orOPEx(deviceWriteService.create(userDevice));
    }

    @OpenMethod(key="user.device.unbind", paramNames = {"deviceToken"})
    public void unbindDevice(String deviceToken){
        if (isEmpty(deviceToken)) {
            throw new OPClientException("device.token.miss");
        }
        Long userId = UserUtil.getUserId();
        for(UserDevice userDevice : OPRespHelper.orOPEx(deviceReadService.findByUserId(userId))){
            if(Objects.equals(userDevice.getDeviceToken(), deviceToken)){
                OPRespHelper.orOPEx(deviceWriteService.delete(userDevice.getId()));
            }
        }
    }

    @OpenMethod(key="user.logout", paramNames = {"sid"})
    public void logout(String sessionId) {
        log.info("logout session :{}", sessionId);
        sessionManager.deletePhysically(Sessions.TOKEN_PREFIX, sessionId);
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

        //1.从session中获取用户信息
        Map<String, Object> snapshot = sessionManager.findSessionById(Sessions.TOKEN_PREFIX, sessionId);
        if (snapshot == null || snapshot.size() == 0 || snapshot.get(Sessions.USER_ID) == null) {
            throw new OPClientException(400, "session.id.expired");
        }
        //2.获取用户
        Response<User> userResp = userReadService.findById(Long.valueOf(snapshot.get(Sessions.USER_ID).toString()));
        if(!userResp.isSuccess()){
            throw new OPClientException(userResp.getError());
        }
        User user = userResp.getResult();
        //3.加密密码
        checkPassword(oldPassword, user.getPassword());

        //4.更新密码
        user.setPassword(EncryptUtil.encrypt(newPassword));
        Response<Boolean> res = userWriteService.update(user);
        if (!res.isSuccess()) {
            throw new OPClientException(res.getError());
        }
        return Boolean.TRUE;
    }

    /**
     * 检查密码
     * @param inputPassword
     * @param dbpassword
     */
    private void checkPassword(String inputPassword, String dbpassword){
        if(!EncryptUtil.match(inputPassword, dbpassword)){
            throw new OPClientException("user.password.error");
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
        //检查密码格式
        checkPasswordFormat(newPassword);
        User user = null;
        // 校验手机验证码
        validateSmsCode(code, mobile, sessionId);
        Response<User> userResp = userReadService.findBy(mobile, LoginType.MOBILE);
        if(!userResp.isSuccess()){
            throw new OPClientException(userResp.getError());
        }
        user = userResp.getResult();
        user.setPassword(EncryptUtil.encrypt(newPassword));
        Response<Boolean> res = userWriteService.update(user);
        if (!res.isSuccess()) {
            throw new OPClientException(res.getError());
        }
        return Boolean.TRUE;
    }


    /**
     * 发送短信验证码
     *
     * @param code   验证码
     * @param mobile 手机号
     * @return 发送结果
     */
    private Response<Boolean> doSendSms(String code, String mobile){
        Response<Boolean> r = new Response<Boolean>();
        try {
            Map<String, Serializable> context=new HashMap<>();
            context.put("code",code);
            String result=smsWebService.send(mobile, "user.register.code",context, null);
            log.info("send sms result : {}", result);
            r.setResult(Boolean.TRUE);
            return r;
        }catch (SmsException e) {
            log.info("send sms failed, error : {} ", e.getMessage());
            throw new JsonResponseException(500, "sms.send.fail");
        }catch (Exception e) {
            log.error("send sms failed , error : {}", e.getMessage());
            throw new JsonResponseException(500, "sms.send.fail");
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



    @Data
    class Token implements Serializable {
        String name;
        String expiredAt;
        String sessionId;
        String deviceId;
        String cookieName;
        String domain;
    }
}
