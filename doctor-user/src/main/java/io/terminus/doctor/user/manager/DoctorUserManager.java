package io.terminus.doctor.user.manager;

import io.terminus.doctor.common.enums.UserRole;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.util.UserRoleUtil;
import io.terminus.doctor.user.dao.OperatorDao;
import io.terminus.doctor.user.model.Operator;
import io.terminus.parana.common.utils.Iters;
import io.terminus.parana.user.impl.dao.UserDao;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author Effet
 */
@Slf4j
@Component
public class DoctorUserManager {

    private final UserDao userDao;

    private final OperatorDao operatorDao;

    @Autowired
    public DoctorUserManager(UserDao userDao, OperatorDao operatorDao) {
        this.userDao = userDao;
        this.operatorDao = operatorDao;
    }

    @Transactional
    public Long create(User user) {
        checkState(userDao.create(user), "create user failed, %", user);
        Long userId = user.getId();
        // TODO: update roles for different user type
        if (Objects.equals(user.getType(), UserType.OPERATOR.value())) {
            Long roleId = null;// TODO: read roleId from user.getRoles()
            for (String role : Iters.nullToEmpty(user.getRoles())) {
                List<String> richRole = UserRoleUtil.roleConsFrom(role);
                if (richRole.get(0).equalsIgnoreCase("ADMIN") && richRole.size() > 1) {
                    roleId = Long.parseLong(UserRoleUtil.roleConsFrom(richRole.get(1)).get(1));
                }
            }

            Operator operator = new Operator();
            operator.setUserId(userId);
            operator.setRoleId(roleId);
            operatorDao.create(operator);
        } else if (Objects.equals(user.getType(), UserType.NORMAL.value())) {
            if (user.getRoles().contains(UserRole.BUYER.name())) {
                // 买家
            }

            if (user.getRoles().contains(UserRole.SELLER.name())) {
                // 卖家
            }
        }
        return userId;
    }

    @Transactional
    public Boolean update(User user) {
        return userDao.update(user);
    }
}
