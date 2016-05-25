package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Splitters;
import io.terminus.parana.user.impl.dao.UserDao;
import io.terminus.parana.user.impl.service.UserReadServiceImpl;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@Slf4j
@Service
@Primary
public class DoctorUserReadServiceImpl extends UserReadServiceImpl{

    private final UserDao userDao;

    @Autowired
    public DoctorUserReadServiceImpl(UserDao userDao) {
        super(userDao);
        this.userDao = userDao;
    }

    /**
     * 根据用户标识查询客户
     *
     * @param loginId   用户标识
     * @param loginType 用户标志类型
     * @return 对应的用户
     */
    @Override
    public Response<User> findBy(String loginId, LoginType loginType) {
        try {
            User user;
            switch (loginType) {
                case NAME:
                    user = userDao.findByName(loginId);
                    break;
                case EMAIL:
                    user = userDao.findByEmail(loginId);
                    break;
                case MOBILE:
                    user = userDao.findByMobile(loginId);
                    break;
                default:
                    user = subAccountCheck(loginId);
                    break;
            }
            if (user == null) {
                log.error("user(loginId={}, loginType={}) not found", loginId, loginType);
                return Response.fail("user.not.found");
            }
            return Response.ok(user);
        } catch (Exception e) {
            log.error("failed to find user(loginId={}, loginType={}), cause:{}",
                    loginId, loginType, Throwables.getStackTraceAsString(e));
            return Response.fail("user.find.fail");
        }
    }

    private User subAccountCheck(String loginId){
        List<String> strings = Splitters.AT.splitToList(loginId);
        if(strings.size() != 2){
            throw new ServiceException("sub.account.not.avalid");
        }
        //检查主账号是否存在
        User parentUser = userDao.findByMobile(strings.get(1));
        if (parentUser == null) {
            log.error("user(loginId={}, loginType=subaccount check puser) not found", loginId);
            throw new ServiceException("puser.not.found");
        }
        //检查子账号
        User user = userDao.findByName(loginId);
        return user;
    }
}
