/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.front.interceptors;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.util.DoctorUserMaker;
import io.terminus.doctor.web.core.Constants;
import io.terminus.doctor.web.core.login.Sessions;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.engine.common.WebUtil;
import io.terminus.parana.common.model.ParanaUser;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import io.terminus.session.AFSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static io.terminus.common.utils.Arguments.isEmpty;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-28
 */
@Slf4j
@Component("loginInterceptor")
public class DoctorLoginInterceptor extends HandlerInterceptorAdapter {

    private final LoadingCache<Long, Response<User>> userCache;
    private final AFSessionManager sessionManager;

    @Autowired
    public DoctorLoginInterceptor(final UserReadService<User> userReadService, AFSessionManager sessionManager) {
        userCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build(new CacheLoader<Long, Response<User>>() {
            @Override
            public Response<User> load(Long userId) throws Exception {
                return userReadService.findById(userId);
            }
        });
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        WebUtil.putRequestAndResponse(request, response);
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userIdInSession = session.getAttribute(Constants.SESSION_USER_ID);
            if (userIdInSession != null) {

                final Long userId = Long.valueOf(userIdInSession.toString());
                Response<? extends User> result = userCache.getUnchecked(userId);
                if (!result.isSuccess()) {
                    userCache.invalidate(userId);
                    log.warn("failed to find user where id={},error code:{}", userId, result.getError());
                    return false;
                }
                User user = result.getResult();
                if (user != null) {
                    ParanaUser paranaUser = DoctorUserMaker.from(user);
                    UserUtil.putCurrentUser(paranaUser);
                }
            }
            return true;
        }

        if (request.getAttribute("sid") == null || isEmpty((String)request.getAttribute("sid"))) {
            log.warn("session id miss");
            return true;
        }

        String sessionId = request.getAttribute("sid").toString();
        Map<String, Object> snapshot = sessionManager.findSessionById(Sessions.TOKEN_PREFIX, sessionId);
        if (snapshot == null || snapshot.size() == 0 || snapshot.get(Sessions.USER_ID) == null) {
            log.error("session expired sid:{}", sessionId);
            return false;
        }

        //如果存在设备号，判断设备号是否一致
        Object sessionDeviceId = snapshot.get(Sessions.DEVICE_ID);
        Object requestDeviceId = request.getAttribute("deviceId");
        if (sessionDeviceId != null && !Objects.equals(sessionDeviceId, requestDeviceId)) {
            log.error("device id not match, sessionDeviceId:{}, requestDeviceId:{}", sessionDeviceId, requestDeviceId);
            return false;
        }

        //刷新session过期时间
        sessionManager.refreshExpireTime(Sessions.TOKEN_PREFIX, sessionId, Sessions.LONG_INACTIVE_INTERVAL);
        Long uid = Long.parseLong(snapshot.get(Sessions.USER_ID).toString());
        Response<User> res = userCache.getUnchecked(uid);
        if (!res.isSuccess()) {
            return false;
        }

        ParanaUser paranaUser = DoctorUserMaker.from(res.getResult());
        UserUtil.putCurrentUser(paranaUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        WebUtil.clear();
        UserUtil.clearCurrentUser();
    }
}
