package io.terminus.doctor.web.admin.role;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.user.service.OperatorReadService;
import io.terminus.pampas.client.Export;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Effet
 */
@Slf4j
@Component
public class OperatorService {

    private final UserReadService<User> userReadService;

    private final OperatorReadService operatorReadService;

    @Autowired
    public OperatorService(UserReadService<User> userReadService, OperatorReadService operatorReadService) {
        this.userReadService = userReadService;
        this.operatorReadService = operatorReadService;
    }

    @Export(paramNames = {"user", "roleId", "pageNo", "pageSize"})
    public Response<Paging<Operator>> pagingOperator(BaseUser user, Long roleId, Integer pageNo, Integer pageSize) {
        try {
            Long userId = getLoginAdminId(user);

            Paging<io.terminus.doctor.user.model.Operator> paging = RespHelper.orServEx(operatorReadService.pagination(roleId, null, pageNo, pageSize));
            List<Operator> result = new ArrayList<>();
            List<Long> userIds = Lists.newArrayList();
            for (io.terminus.doctor.user.model.Operator operator : paging.getData()) {
                userIds.add(operator.getUserId());
            }
            Response<List<User>> userResp = userReadService.findByIds(userIds);
            Map<Long, User> userMap = Maps.newHashMap();
            for (User u : userResp.getResult()) {
                userMap.put(u.getId(), u);
            }
            if (userResp.isSuccess()) {
                for (io.terminus.doctor.user.model.Operator operator : paging.getData()) {
                    Operator op = new Operator();
                    User u = userMap.get(operator.getUserId());
                    if (u != null) {
                        op.setUsername(u.getName());
                    }
                    result.add(op);
                }
            }
            return Response.ok(new Paging<>(paging.getTotal(), result));
        } catch (ServiceException e) {
            log.warn("paging operator failed, user={}, pageNo={}, pageSize={}, error={}",
                    user, pageNo, pageSize, e.getMessage());
            return Response.fail("operator.paging.fail");
        } catch (Exception e) {
            log.error("paging operator failed, user={}, pageNo={}, pageSize={}, cause:{}",
                    user, pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("operator.paging.fail");
        }
    }

    private Long getLoginAdminId(BaseUser user) {
        if (user == null) {
            throw new ServiceException("user.not.login");
        }
        if (!Objects.equals(user.getType(), UserType.ADMIN.value())) {
            throw new ServiceException("user.no.permission");
        }
        return user.getId();
    }
}
