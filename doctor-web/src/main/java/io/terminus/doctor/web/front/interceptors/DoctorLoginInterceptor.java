/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.front.interceptors;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.UserRole;
import io.terminus.doctor.common.util.UserRoleUtil;
import io.terminus.doctor.user.util.DoctorUserMaker;
import io.terminus.pampas.common.UserUtil;
import io.terminus.doctor.common.model.ParanaUser;
import io.terminus.doctor.common.utils.Iters;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import io.terminus.doctor.web.core.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-28
 */
@Slf4j
@Component
public class DoctorLoginInterceptor extends HandlerInterceptorAdapter {

    private final LoadingCache<Long, Response<User>> userCache;


    @Autowired
    public DoctorLoginInterceptor(final UserReadService<User> userReadService) {
        userCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build(new CacheLoader<Long, Response<User>>() {
            @Override
            public Response<User> load(Long userId) throws Exception {
                return userReadService.findById(userId);
            }
        });
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userIdInSession = session.getAttribute(Constants.SESSION_USER_ID);
            if (userIdInSession != null) {

                final Long userId = Long.valueOf(userIdInSession.toString());
                Response<? extends User> result = userCache.getUnchecked(userId);
                if (!result.isSuccess()) {
                    // TODO: 开发阶段先不缓存错误数据
                    userCache.invalidate(userId);
                    log.warn("failed to find user where id={},error code:{}", userId, result.getError());
                    return false;
                }
                User user = result.getResult();
                if (user != null) {
                    ParanaUser paranaUser = DoctorUserMaker.from(user);
                    // TODO: 默认现在只有一个店铺
                    Long shopId = null;
                    for (String role : Iters.nullToEmpty(user.getRoles())) {
                        List<String> richRole = UserRoleUtil.roleConsFrom(role);
                        if (richRole.size() > 1 && Objects.equals(richRole.get(0), UserRole.SELLER.name())) {
                            for (String inner : richRole.subList(1, richRole.size())) {
                                List<String> subRole = UserRoleUtil.roleConsFrom(inner);
                                if (subRole.size() > 1 && Objects.equals(subRole.get(0), "SHOP")) {
                                    shopId = Long.parseLong(subRole.get(1));
                                    break;
                                }
                            }
                        }
                        if (shopId != null) {
                            break;
                        }
                    }
                    paranaUser.setShopId(shopId);
                    UserUtil.putCurrentUser(paranaUser);
                }
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserUtil.clearCurrentUser();
    }
}
