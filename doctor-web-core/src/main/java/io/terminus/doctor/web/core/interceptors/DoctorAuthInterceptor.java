package io.terminus.doctor.web.core.interceptors;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.web.core.auth.DoctorAuthChecker;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.engine.ThreadVars;
import io.terminus.parana.user.auth.Req;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * @author Effet
 */
@Slf4j
@Component
public class DoctorAuthInterceptor extends HandlerInterceptorAdapter {

    private final DoctorAuthChecker doctorAuthChecker;

    @Autowired
    public DoctorAuthInterceptor(DoctorAuthChecker doctorAuthChecker) {
        this.doctorAuthChecker = doctorAuthChecker;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod().toUpperCase();
        Map<String, String[]> params = request.getParameterMap();

        // build request data
        Req req = new Req(path, method, params);

        BaseUser user = UserUtil.getCurrentUser();
        if (user == null) {
            return notLoginCase(request, response, req);
        } else {
            return loginCase(request, response, req, user);
        }
    }

    private boolean notLoginCase(HttpServletRequest request, HttpServletResponse response, Req req) throws IOException {
        return doctorAuthChecker.forNotLogin(req) || error401(request, response);
    }

    private boolean loginCase(HttpServletRequest request, HttpServletResponse response, Req req, BaseUser user) {
        return doctorAuthChecker.forLogin(req, user) || error403(request, response);
    }

    private boolean error401(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("user not login for request: {}", request.getRequestURI());
        if (isAjaxRequest(request)) {
            throw new JsonResponseException(401, "user.not.login");
        }
        return redirectToLogin(request, response);
    }

    private boolean error403(HttpServletRequest request, HttpServletResponse response) {
        log.error("user no permission for request: {}", request.getRequestURI());
        // TODO: 重新定义异常
        throw new JsonResponseException(403, "user.no.permission");
    }

    private boolean redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String currentUrl = getCurrentURL(request);
        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString("http://" + ThreadVars.getHost() + "/login?target={target}").build();
        URI uri = uriComponents.expand(currentUrl).encode().toUri();
        response.sendRedirect(uri.toString());
        return false;
    }

    private String getCurrentURL(HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(request.getScheme()).append("://").append(request.getServerName());
        int port = request.getServerPort();
        if (port != 80 && port != 443) {
            builder.append(":").append(port);
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        builder.append(path);
        String qs = request.getQueryString();
        if (!Strings.isNullOrEmpty(qs)) {
            builder.append("?").append(qs);
        }
        return builder.toString();
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        return Objects.equal(request.getHeader(HttpHeaders.X_REQUESTED_WITH), "XMLHttpRequest");
    }
}
