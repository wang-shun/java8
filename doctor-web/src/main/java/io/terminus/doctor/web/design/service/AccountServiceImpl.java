package io.terminus.doctor.web.design.service;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.EncryptUtil;
import io.terminus.doctor.user.enums.TargetSystem;
import io.terminus.parana.config.ConfigCenter;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 陈增辉 16/5/16.
 */
@Service
@Slf4j
public class AccountServiceImpl implements AccountService{

    @Autowired
    private ConfigCenter configCenter;


    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    @Override
    public Response<User> findBindAccount(Long userId, Integer targetSystem) {
        TargetSystem.Bean targetSystemBean;
        try {
            targetSystemBean = this.getTargetSystemBean(targetSystem);
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
        String encryptedUserId = EncryptUtil.MD5(userId.toString());
        String url = targetSystemBean.getDomain() + "/api/all/third/findBindAccount?thirdPartUserId=" + encryptedUserId
                + "&corpId=" + targetSystemBean.getCorpId();
        String result = HttpRequest.get(url).body();
        Response<Map<String, Object>> response = JSON_MAPPER.fromJson(result, Response.class);
        if (!response.isSuccess()) {
            return Response.fail(response.getError());
        }
        return Response.ok(this.makeUserFromHttpResponse(response));
    }
    private User makeUserFromHttpResponse(Response<Map<String, Object>> response){
        Map<String, Object> map = response.getResult();
        User user = new User();
        try {
            if (map.get("id") != null) {
                user.setId(Long.valueOf(map.get("id").toString()));
            }
            user.setName((String)map.get("nickname"));
            user.setMobile((String)map.get("mobile"));
            user.setEmail((String)map.get("email"));
            if (map.get("type") != null) {
                user.setType(Integer.valueOf(map.get("type").toString()));
            }
            if (map.get("status") != null) {
                user.setStatus(Integer.valueOf(map.get("status").toString()));
            }
        } catch (Exception e) {
            log.error("makeUserFromHttpResponse error, please check, cause:{}", Throwables.getStackTraceAsString(e));
        }
        return user;
    }
    @Override
    public Response<User> bindAccount(Long userId, Integer targetSystem, String account, String password) {
        TargetSystem.Bean targetSystemBean;
        try {
            targetSystemBean = this.getTargetSystemBean(targetSystem);
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
        String encryptedUserId = EncryptUtil.MD5(userId.toString());
        String url = targetSystemBean.getDomain() + "/api/all/third/bindAccount?thirdPartUserId=" + encryptedUserId
                + "&corpId=" + targetSystemBean.getCorpId() + "&account=" + account + "&password=" + password;
        String result = HttpRequest.get(url).body();
        Response<Map<String, Object>> response = JSON_MAPPER.fromJson(result, Response.class);
        if (!response.isSuccess()) {
            return Response.fail(response.getError());
        }
        return Response.ok(this.makeUserFromHttpResponse(response));
    }

    @Override
    public Response<User> unbindAccount(Long userId, Integer targetSystem) {
        TargetSystem.Bean targetSystemBean;
        try {
            targetSystemBean = this.getTargetSystemBean(targetSystem);
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
        String encryptedUserId = EncryptUtil.MD5(userId.toString());
        String url = targetSystemBean.getDomain() + "/api/all/third/unbindAccount?thirdPartUserId=" + encryptedUserId
                + "&corpId=" + targetSystemBean.getCorpId();
        String result = HttpRequest.get(url).body();
        Response<Map<String, Object>> response = JSON_MAPPER.fromJson(result, Response.class);
        if (!response.isSuccess()) {
            return Response.fail(response.getError());
        }
        return Response.ok(this.makeUserFromHttpResponse(response));
    }

    @Override
    public TargetSystem.Bean getTargetSystemBean(Integer targetSystem) throws Exception {
        TargetSystem targetSystemEnum = TargetSystem.from(targetSystem);
        if(targetSystemEnum == null){
            throw new JsonResponseException("unknown.target.system");
        }
        TargetSystem.Bean bean = targetSystemEnum.getTargetSystemBean();
        String[] keys = targetSystemEnum.toString().split(";");
        bean.setDomain(this.getConfigValue(keys[0]));
        bean.setPassword(this.getConfigValue(keys[1]));
        bean.setCorpId(Long.valueOf(this.getConfigValue(keys[2])));
        return bean;
    }
    private String getConfigValue(String key) throws Exception {
        Optional<String> optional = configCenter.get(key);
        if (!optional.isPresent()) {
            log.error("required config is missing, key = {}", key);
            throw new JsonResponseException("required.config.missing");
        }
        return optional.get();
    }

}
