package io.terminus.doctor.interceptor;

import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.user.model.DoctorUser;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.engine.ThreadVars;
import io.terminus.pampas.engine.model.App;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

<<<<<<< HEAD
/**
 * 伪造type为主账号的登录者
 */
=======
>>>>>>> 274dede896d4ee14071e5664b729df4bb437eeac
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
        return true;
    }
}
