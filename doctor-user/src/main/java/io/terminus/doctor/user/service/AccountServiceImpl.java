package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.EncryptUtil;
import io.terminus.parana.user.model.User;

/**
 * 陈增辉 16/5/16.
 */
public class AccountServiceImpl implements AccountService{

    @Override
    public Response<User> findBindAccount(Long userId, Integer targetSystem) {
        String encryptedUserId = EncryptUtil.MD5(userId.toString());
        return null;
    }

    @Override
    public Response<User> bindAccount(Long userId, Integer targetSystem, String account, String password) {
        return null;
    }

    @Override
    public Response<User> unbindAccount(Long userId, Integer targetSystem) {
        return null;
    }

}
