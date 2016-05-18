package io.terminus.doctor.web.design.service;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.utils.EncryptUtil;
import io.terminus.doctor.user.enums.TargetSystem;
import io.terminus.parana.config.ConfigCenter;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 陈增辉 16/5/16.
 */
@Service
@Slf4j
public class AccountServiceImpl implements AccountService{

    private final ConfigCenter configCenter;

    @Autowired
    public AccountServiceImpl (ConfigCenter configCenter) {
        this.configCenter = configCenter;
    }

    private static final String URL_FINDBINDACCOUNT = "/api/all/third/findBindAccount";
    private static final String URL_BINDACCOUNT = "/api/all/third/bindAccount";
    private static final String URL_BINDACCOUNT_NOPASSWORD = "/api/all/third/bindAccount/noPassword";
    private static final String URL_UNBINDACCOUNT = "/api/all/third/unbindAccount";

    @Override
    public Response<User> bindAccount(Long userId, TargetSystem targetSystem, String account, String password) {
        return this._bindAccount(userId, targetSystem, account, password, true);
    }
    @Override
    public Response<User> bindAccount(Long userId, TargetSystem targetSystem, String account){
        return this._bindAccount(userId, targetSystem, account, null, false);
    }

    private Response<User> _bindAccount(Long userId, TargetSystem targetSystem, String account, String password, boolean checkPassword){
        Response<User> response = new Response<>();
        try {
            TargetSystem.Bean targetSystemBean = this.getTargetSystemBean(targetSystem);
            String encryptedUserId = EncryptUtil.MD5(userId.toString());
            String url = targetSystemBean.getDomain() + (checkPassword ? URL_BINDACCOUNT : URL_BINDACCOUNT_NOPASSWORD);
            Map<String, String> params = MapBuilder.<String, String>of()
                    .put("thirdPartUserId", encryptedUserId)
                    .put("corpId", targetSystemBean.getCorpId().toString())
                    .put("account", account)
                    .put("password", password)
                    .map();
            HttpRequest request = HttpRequest.post(url).form(params);
            String body = request.body();
            if (request.code() != 200) {
                throw new ServiceException(body);
            } else {
                User user = JsonMapper.nonEmptyMapper().fromJson(body, User.class);
                response.setResult(user);
            }
        } catch (ServiceException e) {
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("bindAccount failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("bind.account.failed");
        }
        return response;
    }

    @Override
    public Response<User> unbindAccount(Long userId, TargetSystem targetSystem) {
        return this.httpRequestGet(userId, targetSystem, URL_UNBINDACCOUNT);
    }
    @Override
    public Response<User> findBindAccount(Long userId, TargetSystem targetSystem) {
        return this.httpRequestGet(userId, targetSystem, URL_FINDBINDACCOUNT);
    }
    private Response<User> httpRequestGet(Long userId, TargetSystem targetSystem, String shortUrl){
        Response<User> response = new Response<>();
        try {
            TargetSystem.Bean targetSystemBean = this.getTargetSystemBean(targetSystem);
            String encryptedUserId = EncryptUtil.MD5(userId.toString());
            String url = targetSystemBean.getDomain() + shortUrl +"/" + encryptedUserId + "/" + targetSystemBean.getCorpId();
            HttpRequest request = HttpRequest.get(url);
            String body = request.body();
            if (request.code() != 200) {
                throw new ServiceException(body);
            } else {
                User user = JsonMapper.nonEmptyMapper().fromJson(body, User.class);
                response.setResult(user);
            }
        } catch (ServiceException e) {
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("unbindAccount failed, cause:{}", Throwables.getStackTraceAsString(e));
            response.setError("unbind.account.failed");
        }
        return response;
    }

    @Override
    public TargetSystem.Bean getTargetSystemBean(TargetSystem targetSystem){
        TargetSystem.Bean bean = targetSystem.getTargetSystemBean();
        String[] keys = targetSystem.toString().split(";");
        bean.setDomain(this.getConfigValue(keys[0]));
        bean.setPassword(this.getConfigValue(keys[1]));
        bean.setCorpId(Long.valueOf(this.getConfigValue(keys[2])));
        return bean;
    }
    private String getConfigValue(String key){
        Optional<String> optional = configCenter.get(key);
        return optional.or(() -> {
            throw new ServiceException("required.config.is.missing");
        });
    }

}
