package io.terminus.doctor.web.admin.service;

import com.fasterxml.jackson.databind.JavaType;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Throwables;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.enums.TargetSystem;
import io.terminus.doctor.user.model.UserBind;
import io.terminus.doctor.user.service.DoctorUserService;
import io.terminus.doctor.web.core.service.OtherSystemService;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * 陈增辉 16/5/16.
 */
@Service
@Slf4j
public class AccountServiceImpl implements AccountService{

    private final OtherSystemService otherSystemService;
    private final DoctorUserService doctorUserService;

    @Autowired
    public AccountServiceImpl (OtherSystemService otherSystemService, DoctorUserService doctorUserService) {
        this.otherSystemService = otherSystemService;
        this.doctorUserService = doctorUserService;
    }

    private static final String URL_FINDBINDACCOUNT = "/api/all/third/findBindAccount";
    private static final String URL_BINDACCOUNT = "/api/all/third/bindAccount";
    private static final String URL_BINDACCOUNT_NOPASSWORD = "/api/all/third/bindAccount/noPassword";
    private static final String URL_UNBINDACCOUNT = "/api/all/third/unbindAccount";
    private static JavaType javaType = JsonMapper.nonEmptyMapper().createCollectionType(Map.class, String.class, String.class);


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
            Response<UserBind> bindResponse = doctorUserService.findUserBindUnique(userId, targetSystem);
            UserBind userBind = RespHelper.orServEx(bindResponse);
            if (userBind != null) {
                return Response.fail("user.bind.already");
            }
            otherSystemService.setTargetSystemValue(targetSystem);
            String simpleUUID = UUID.randomUUID().toString().replace("-", "");
            String url = targetSystem.getValueOfDomain() + (checkPassword ? URL_BINDACCOUNT : URL_BINDACCOUNT_NOPASSWORD);
            Map<String, Object> params = MapBuilder.<String, Object>of()
                    .put("thirdPartUserId", simpleUUID)
                    .put("corpId", targetSystem.getValueOfCorpId())
                    .put("account", account)
                    .put("password", password)
                    .map();
            HttpRequest request = HttpRequest.post(url).form(params);
            String body = request.body();
            if (request.code() != 200) {
                throw new ServiceException(body);
            } else {
                //这是对方系统的user
                User user = this.makeUserFromJson(body);

                //在自己系统记录绑定关系
                userBind = new UserBind();
                userBind.setTargetSystem(targetSystem.value());
                userBind.setUserId(userId);
                userBind.setUuid(simpleUUID);
                userBind.setTargetUserName(user.getName());
                userBind.setTargetUserMobile(user.getMobile());
                userBind.setTargetUserEmail(user.getEmail());
                doctorUserService.createUserBind(userBind);
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
    private User makeUserFromJson(String json){
        Map<String, String> map = JsonMapper.nonEmptyMapper().fromJson(json, javaType);
        User user = new User();
        user.setName(map.get("nickname"));
        user.setMobile(map.get("mobile"));
        user.setEmail(map.get("email"));
        return user;
    }
    @Override
    public Response<User> unbindAccount(Long userId, TargetSystem targetSystem) {
        Response<User> response = new Response<>();
        try {
            Response<UserBind> bindResponse = doctorUserService.findUserBindUnique(userId, targetSystem);
            UserBind userBind = RespHelper.orServEx(bindResponse);
            if (userBind == null) {
                return Response.fail("no.user.bind.found");
            }
            otherSystemService.setTargetSystemValue(targetSystem);
            String url = targetSystem.getValueOfDomain() + URL_UNBINDACCOUNT + "/" + userBind.getUuid() + "/" + targetSystem.getValueOfCorpId();
            HttpRequest request = HttpRequest.get(url);
            String body = request.body();
            if (request.code() != 200) {
                throw new ServiceException(body);
            } else {
                //这是对方系统的user
                User user = this.makeUserFromJson(body);

                //删除在本系统记录的账户绑定关系
                doctorUserService.deleteUserBindById(userBind.getId());
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

}
