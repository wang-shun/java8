/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.admin.user;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.user.model.*;
import io.terminus.doctor.user.service.DoctorFarmExportReadService;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.user.service.DoctorUserRoleLoader;
import io.terminus.doctor.web.core.Constants;
import io.terminus.doctor.web.core.component.MobilePattern;
import io.terminus.doctor.web.core.events.user.LoginEvent;
import io.terminus.doctor.web.core.events.user.LogoutEvent;
import io.terminus.doctor.web.core.util.DoctorUserMaker;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.auth.core.AclLoader;
import io.terminus.parana.auth.core.PermissionHelper;
import io.terminus.parana.auth.model.Acl;
import io.terminus.parana.auth.model.ParanaThreadVars;
import io.terminus.parana.auth.model.PermissionData;
import io.terminus.parana.common.model.ParanaUser;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.terminus.common.utils.Arguments.isNull;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class Users {

    private static final String ImportExcelRedisKey = "import-excel-result:";

    private final DoctorUserReadService doctorUserReadService;
    private final MobilePattern mobilePattern;
    private final EventBus eventBus;
    private final AclLoader aclLoader;
    private final PermissionHelper permissionHelper;
    private final Publisher publisher;
    private final DoctorUserMaker doctorUserMaker;

    @RpcConsumer
    private DoctorUserRoleLoader doctorUserRoleLoader;
    @RpcConsumer
    private DoctorFarmExportReadService doctorFarmExportReadService;

    @Autowired
    public Users(DoctorUserReadService doctorUserReadService,
                 DoctorUserMaker doctorUserMaker, EventBus eventBus,
                 AclLoader aclLoader,
                 PermissionHelper permissionHelper,
                 MobilePattern mobilePattern,
                 Publisher publisher) {
        this.doctorUserReadService = doctorUserReadService;
        this.doctorUserMaker = doctorUserMaker;
        this.eventBus = eventBus;
        this.aclLoader = aclLoader;
        this.permissionHelper = permissionHelper;
        this.mobilePattern = mobilePattern;
        this.publisher = publisher;
    }

    @RequestMapping("")
    public BaseUser getLoginUser() {
        DoctorUser doctorUser = UserUtil.getCurrentUser();
        try {
            Acl acl = aclLoader.getAcl(ParanaThreadVars.getApp());
            BaseUser user = UserUtil.getCurrentUser();
            PermissionData perm = permissionHelper.getPermissions(acl, user, true);
            perm.setAllRequests(null); // empty it
            doctorUser.setAuth(ToJsonMapper.JSON_NON_EMPTY_MAPPER.toJson(perm));
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
        LoginType loginType;
        if(isNull(type)){
            if(mobilePattern.getPattern().matcher(loginBy).find()){
                loginType = LoginType.MOBILE;
            }
            else if(loginBy.indexOf("@") != -1){
                loginType = LoginType.OTHER;
            }
            else {
                loginType = LoginType.NAME;
            }
        } else {
            loginType = LoginType.from(type);
        }

        Map<String, Object> map = new HashMap<>();

        Response<User> result = doctorUserReadService.login(loginBy, password, loginType);

        if (!result.isSuccess()) {
            log.warn("failed to login with(loginBy={}), error: {}", loginBy, result.getError());
            throw new JsonResponseException(500, result.getError());
        }

        User user = result.getResult();
        //判断下user type, 只允许admin和运维能登录
        if(!Objects.equal(user.getType(), UserType.ADMIN.value()) && !Objects.equal(user.getType(), UserType.OPERATOR.value())){
//            throw new JsonResponseException("authorize.fail");
            throw new JsonResponseException("Users");
        }

        //判断当前用户是否激活
        if (Objects.equal(user.getStatus(), UserStatus.NOT_ACTIVATE.value())) {
            log.warn("user({}) isn't active", user);
        }
        request.getSession().setAttribute(Constants.SESSION_USER_ID, user.getId());

        LoginEvent loginEvent = new LoginEvent(request, response, doctorUserMaker.from(user));
        eventBus.post(loginEvent);
        target = !StringUtils.hasText(target)?"/":target;
        map.put("redirect",target);
        map.put("userId", user.getId());
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

    /**
     * excel导入
     * @param fileUrl Excel文件地址
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.GET)
    public void importExcel(@RequestParam String fileUrl){
        log.info("import excel fileUrl:{}", fileUrl);
        log.error("import excel fileUrl:{}", fileUrl);
        try {
            publisher.publish(DataEvent.toBytes(DataEventType.ImportExcel.getKey(), fileUrl));
            log.error("importExcel:"+DataEvent.toBytes(DataEventType.ImportExcel.getKey(), fileUrl).toString());
        } catch (Exception e) {
            log.info("import excel failed---------------------------", e.getMessage());
            log.error("import excel failed", Throwables.getStackTraceAsString(e));
        }
    }

    @RequestMapping(value = "/list/farmExport", method = RequestMethod.GET)
    public List<DoctorFarmExport> findFarmExportRecord(@RequestParam(required = false) String farmName) {
        Response<List<DoctorFarmExport>> farmExportRecord = doctorFarmExportReadService.findFarmExportRecord(farmName);
        log.error(farmExportRecord.getResult().toString());
        return RespHelper.or500(farmExportRecord);
    }


    /**
     * for herd admin
     * @param id
     * @return
     */
    @RequestMapping(value = "/getUserById/{id}",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public User getUserById(@PathVariable(value = "id") Long id) {
        Response<User> userResponse = doctorUserReadService.findById(id);
        if (!userResponse.isSuccess()){
            throw new JsonResponseException(userResponse.getError());
        }
        return userResponse.getResult();
    }


    @RequestMapping(value = "/{userId}/roles", method = {RequestMethod.GET}, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<DoctorRoleContent> getUserRolesByUserId(@PathVariable Long userId) {
        return doctorUserRoleLoader.hardLoadRoles(userId);
    }
}
