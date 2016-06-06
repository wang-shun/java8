/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.admin.user;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.user.model.DoctorUser;
import io.terminus.doctor.user.util.DoctorUserMaker;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.auth.core.AclLoader;
import io.terminus.parana.auth.core.PermissionHelper;
import io.terminus.parana.auth.model.Acl;
import io.terminus.parana.auth.model.ParanaThreadVars;
import io.terminus.parana.auth.model.PermissionData;
import io.terminus.parana.common.model.ParanaUser;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import io.terminus.doctor.web.core.Constants;
import io.terminus.doctor.web.core.events.user.LoginEvent;
import io.terminus.doctor.web.core.events.user.LogoutEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

import static io.terminus.common.utils.Arguments.*;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class Users {


    private final UserReadService<User> userReadService;

    private final EventBus eventBus;

    private final AclLoader aclLoader;

    private final PermissionHelper permissionHelper;

    @Autowired
    public Users(UserReadService<User> userReadService,
                 EventBus eventBus,
                 AclLoader aclLoader,
                 PermissionHelper permissionHelper) {
        this.userReadService = userReadService;
        this.eventBus = eventBus;
        this.aclLoader = aclLoader;
        this.permissionHelper = permissionHelper;
    }

    @RequestMapping("")
    public BaseUser getLoginUser() {
        DoctorUser doctorUser = (DoctorUser) UserUtil.getCurrentUser();
        try {
            Acl acl = aclLoader.getAcl(ParanaThreadVars.getApp());
            BaseUser user = UserUtil.getCurrentUser();
            PermissionData perm = permissionHelper.getPermissions(acl, user, true);
            perm.setAllRequests(null); // empty it
            doctorUser.setAuth(JsonMapper.nonEmptyMapper().toJson(perm));
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, JsonResponseException.class);
            log.error("get permissions of user failed, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("auth.permission.find.fail");
        }
        return doctorUser;
    }

    /**
     * 登录
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> login(@RequestParam("loginBy") String loginBy, @RequestParam("password") String password,
                                     @RequestParam(value = "target", required = false) String target,
                                     @RequestParam(value = "type", required = false) Integer type,
                                     HttpServletRequest request, HttpServletResponse response) {
        loginBy = loginBy.toLowerCase();
        LoginType loginType = isNull(type) ? LoginType.EMAIL : LoginType.from(type);
        Map<String, Object> map = new HashMap<>();

        Response<User> result = userReadService.login(loginBy, password, loginType);

        if (!result.isSuccess()) {
            log.warn("failed to login with(loginBy={}), error: {}", loginBy, result.getError());
            throw new JsonResponseException(500, result.getError());
        }

        User user = result.getResult();
        //判断当前用户是否激活
        if (Objects.equal(user.getStatus(), UserStatus.NOT_ACTIVATE.value())) {
            log.warn("user({}) isn't active", user);
        }
        request.getSession().setAttribute(Constants.SESSION_USER_ID, user.getId());

        LoginEvent loginEvent = new LoginEvent(request, response, DoctorUserMaker.from(user));
        eventBus.post(loginEvent);
        target = !StringUtils.hasText(target)?"/":target;
        map.put("redirect",target);
        return map;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            ParanaUser loginUser = UserUtil.getCurrentUser();
            if (loginUser != null) {
                //delete login token cookie
                LogoutEvent logoutEvent = new LogoutEvent(request, response, loginUser);
                eventBus.post(logoutEvent);
            }
            return "/";
        } catch (Exception e) {
            log.error("failed to logout user,cause:", e);
            throw new JsonResponseException(500, "user.logout.fail");
        }
    }

}
