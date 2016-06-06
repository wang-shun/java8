package io.terminus.doctor.open.common;

import io.terminus.common.model.Response;
import io.terminus.parana.common.model.ParanaUser;
import io.terminus.doctor.user.util.DoctorUserMaker;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.openplatform.annotations.OpenParamHandler;
import io.terminus.pampas.openplatform.core.ParamPreHandle;
import io.terminus.pampas.openplatform.exceptions.OPClientException;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import io.terminus.session.AFSessionManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.isEmpty;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2016-03-23 10:42 AM  <br>
 * Author: xiao
 */
@OpenParamHandler(patterns = "*", exclusions = {
        "user.login",
        "user.auto.login",
        "server.time",
        "get.session.id",
        "get.user.captcher",
        "get.carousel.figure",  //轮播图接口
        "user.forget.password", //忘记密码
        "user.register",  //用户注册
        "get.mobile.code",
        "user.test.push",
        "get.org.info",
        "get.user.level.one.menu",
        "get.user.basic.info"
})
public class SessionHandle implements ParamPreHandle {

    @Autowired
    private AFSessionManager sessionManager;
    @Autowired
    private UserReadService<User> userReadService;

    /**
     * check params before calling
     *
     * @param params params from http request
     * @throws OPClientException
     */
    @Override
    public void handle(Map<String, Object> params) throws OPClientException {
        if (params.get("sid") == null || isEmpty((String)params.get("sid"))) {
            throw new OPClientException("session.id.miss");
        }
        String sessionId = params.get("sid").toString();
        Map<String, Object> snapshot = sessionManager.findSessionById(Sessions.TOKEN_PREFIX, sessionId);
        if (snapshot == null || snapshot.size() == 0 || snapshot.get(Sessions.USER_ID) == null) {
            throw new OPClientException(400, "session.id.expired");
        }

        checkDeviceId(snapshot.get(Sessions.DEVICE_ID), params.get("deviceId"));

        // refresh
        sessionManager.refreshExpireTime(Sessions.TOKEN_PREFIX, sessionId, Sessions.LONG_INACTIVE_INTERVAL);
        Long uid = Long.parseLong(snapshot.get(Sessions.USER_ID).toString());
        Response<User> res = userReadService.findById(uid);
        if (!res.isSuccess()) {
            throw new OPClientException(400, res.getError());
        }

        ParanaUser paranaUser = DoctorUserMaker.from(res.getResult());
        UserUtil.putCurrentUser(paranaUser);
    }

    //如果存在deviceId, 校验设备号是否匹配
    private void checkDeviceId(Object did, Object deviceId) {
        if (deviceId != null && !Objects.equals(did, deviceId)) {
            throw new OPClientException(400, "device.id.not.match");
        }
    }
}
