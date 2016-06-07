package io.terminus.doctor.interceptor;

import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.user.model.DoctorUser;
import io.terminus.pampas.common.UserUtil;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 伪造type为主账号的登录者
 */
public class MockPrimaryLoginInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        DoctorUser doctorUser = new DoctorUser();
        doctorUser.setType(UserType.FARM_ADMIN_PRIMARY.value());
        doctorUser.setId(4L);
        UserUtil.putCurrentUser(doctorUser);
        return true;
    }
}
