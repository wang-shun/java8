package io.terminus.doctor.web.front.role;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.user.model.DoctorUser;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * Desc: 子账号
 * Mail: houly@terminus.io
 * Data: 下午5:24 16/5/25
 * Author: houly
 */
@Slf4j
@RestController
@RequestMapping("/api/sub")
public class Subs {


    private final UserWriteService<User> userWriteService;

    private final SubService subService;

    public static final Joiner AT = Joiner.on("@").skipNulls();

    @Autowired
    public Subs(UserWriteService<User> userWriteService, SubService subService) {
        this.userWriteService = userWriteService;
        this.subService = subService;
    }

    /**
     * ADMIN 创建子账号
     *
     * @param sub 子账号信息
     * @return 子账号 ID
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Long createOperator(@RequestBody Sub sub) {
        Long primaryId = getPrimaryUserId();

        User user = new User();
        //子账号@主账号
        user.setName(AT.join(sub.getUsername(), ((DoctorUser)(UserUtil.getCurrentUser())).getMobile()));
        user.setPassword(sub.getPassword());
        user.setType(UserType.FARM_SUB.value());
        user.setStatus(UserStatus.NORMAL.value());
        // TODO: 自定义角色冗余进 user 表
        List<String> roles = Lists.newArrayList("SUB");
        if (sub.getRoleId() != null) {
            roles.add("SUB(" + sub.getRoleId() + ")");
        }
        user.setRoles(roles);
        user.setExtra(MapBuilder.<String, String>of().put("pid", primaryId.toString()).map());
        return RespHelper.or500(userWriteService.create(user));
    }

    /**
     * 分页查询子用户
     * @param roleId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/pagination", method = RequestMethod.POST)
    public Paging<Sub> pagingSubs(@RequestParam(required = false) Long roleId,
                                  @RequestParam(required = false) Integer pageNo,
                                  @RequestParam(required = false) Integer pageSize) {
        checkAuth();
        return RespHelper.or500(subService.pagingSubs(UserUtil.getCurrentUser(), roleId, pageNo, pageSize));
    }

    /**
     * 检查用户权限
     */
    private void checkAuth(){

        if (UserUtil.getCurrentUser() == null) {
            throw new JsonResponseException(401, "user.not.login");
        }

        if(!Objects.equals(UserUtil.getCurrentUser().getType(), UserType.FARM_ADMIN_PRIMARY.value())){
            throw new JsonResponseException(403, "user.no.permission");
        }
    }

    private Long getPrimaryUserId() {
        BaseUser user = UserUtil.getCurrentUser();
        return user.getId();
    }
}
