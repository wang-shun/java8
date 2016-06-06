package io.terminus.doctor.interceptor;

import io.terminus.doctor.user.model.DoctorUser;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.common.model.ParanaUser;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MockLoginInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        DoctorUser doctorUser = new DoctorUser();
        doctorUser.setShopId(1L);
        doctorUser.setType(1);
        doctorUser.setId(1L);
        UserUtil.putCurrentUser(doctorUser);
        return true;
    }
}
