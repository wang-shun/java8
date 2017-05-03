/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.front.interceptors;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorUser;
import io.terminus.doctor.web.core.Constants;
import io.terminus.doctor.web.core.util.DoctorUserMaker;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.engine.common.WebUtil;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import io.terminus.parana.user.service.UserWriteService;
import io.terminus.session.AFSession;
import io.terminus.session.AFSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

import java.util.Map;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-28
 */
@Slf4j
@Component("loginInterceptor")
public class DoctorLoginInterceptor extends HandlerInterceptorAdapter {

    private final LoadingCache<Long, Response<User>> userCache;
    private final DoctorUserMaker doctorUserMaker;
    @Autowired
    private AFSessionManager sessionManager;
    @RpcConsumer
    private UserWriteService<User> userWriteService;
    
    @Autowired
    public DoctorLoginInterceptor(final UserReadService<User> userReadService, DoctorUserMaker doctorUserMaker) {
        userCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).maximumSize(10000).build(new CacheLoader<Long, Response<User>>() {
            @Override
            public Response<User> load(Long userId) throws Exception {
                return userReadService.findById(userId);
            }
        });
        this.doctorUserMaker = doctorUserMaker;
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
                    // 2017.5.2 从cookie里获取 orgId 和 farmId
                    DoctorUser doctorUser = doctorUserMaker.fromExt(user);

                    Cookie[] cookies = request.getCookies();
                    if (cookies != null) {

                        Map<String, String> extra;
                        if(user.getExtra() == null || user.getExtra().isEmpty()){
                            extra = Maps.newHashMap();
                        }else{
                            extra = user.getExtra();
                        }
                        for (int i = 0; i < cookies.length; i++) {
                            Cookie cookie = cookies[i];
                            if (cookie.getName().equals("farmId")) {
                                extra.put("farmId", cookie.getValue());
                                doctorUser.setFarmId(Long.parseLong(cookie.getValue()));
                            }
                            if (cookie.getName().equals("orgId")) {
                                extra.put("orgId", cookie.getValue());
                                doctorUser.setOrgId(Long.parseLong(cookie.getValue()));
                            }
                        }
                        User toUpdateUser = new User();
                        toUpdateUser.setId(userId);
                        toUpdateUser.setExtra(extra);
                        userWriteService.update(toUpdateUser);
                    }
                    UserUtil.putCurrentUser(doctorUser);

                    // TODO: 2017/2/9 先手动刷下session过期时间 
                    AFSession afSession = (AFSession) session;
                    sessionManager.refreshExpireTime(afSession, afSession.getMaxInactiveInterval());
                }
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        WebUtil.clear();
        UserUtil.clearCurrentUser();
    }
}
