package io.terminus.doctor.interceptor;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import io.terminus.common.utils.Joiners;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.constant.Front;
import io.terminus.doctor.open.common.CaptchaGenerator;
import io.terminus.doctor.user.model.DoctorUser;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.engine.ThreadVars;
import io.terminus.pampas.engine.model.App;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MockPrimaryLoginInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        DoctorUser doctorUser = new DoctorUser();
        doctorUser.setType(UserType.FARM_ADMIN_PRIMARY.value());
        doctorUser.setId(2L);
        doctorUser.setMobile("18888888889");
        UserUtil.putCurrentUser(doctorUser);
        App app = new App();
        app.setKey("MOBILE");
        ThreadVars.setApp(app);

        request.getSession(true).setAttribute(CaptchaGenerator.CAPTCHA_TOKEN, Front.SESSION_IMG_CODE);
        request.getSession(false).setAttribute("code", Joiner.on("@").join(Front.SESSION_MSG_CODE,  System.currentTimeMillis(), Front.MOBILE));
        return true;
    }
}
