package io.terminus.doctor.web.admin.role;

import com.google.common.collect.Lists;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * @author Effet
 */
@Slf4j
@RestController
@RequestMapping("/api/operator")
public class Operators {

    private final UserWriteService<User> userWriteService;

    @Autowired
    public Operators(UserWriteService<User> userWriteService) {
        this.userWriteService = userWriteService;
    }

    /**
     * ADMIN 创建运营
     *
     * @param operator 运营信息
     * @return 运营用户 ID
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Long createOperator(@RequestBody Operator operator) {
        Long adminId = getLoginAdminId();

        User user = new User();
        user.setName(operator.getUsername());
        user.setPassword(operator.getPassword());
        user.setType(UserType.OPERATOR.value());
        user.setStatus(UserStatus.NORMAL.value());
        // TODO: 自定义角色冗余进 user 表
        List<String> roles = Lists.newArrayList("ADMIN");
        if (operator.getRoleId() != null) {
            roles.add("ADMIN(SUB(" + operator.getRoleId() + "))");
        }
        user.setRoles(roles);
        return RespHelper.or500(userWriteService.create(user));
    }

    private Long getLoginAdminId() {
        BaseUser user = UserUtil.getCurrentUser();
        if (user == null) {
            throw new JsonResponseException(401, "user.not.login");
        }
        if (!Objects.equals(user.getType(), UserType.ADMIN.value())) {
            throw new JsonResponseException(403, "user.no.permission");
        }
        return user.getId();
    }
}
