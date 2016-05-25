package io.terminus.doctor.web.front.role;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.service.PrimaryUserReadService;
import io.terminus.pampas.client.Export;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Desc: 子账号相关服务
 * Mail: houly@terminus.io
 * Data: 下午5:25 16/5/25
 * Author: houly
 */
@Slf4j
@Component
public class SubService {

    private final UserReadService<User> userReadService;

    private final PrimaryUserReadService primaryUserReadService;

    @Autowired
    public SubService(UserReadService<User> userReadService, PrimaryUserReadService primaryUserReadService) {
        this.userReadService = userReadService;
        this.primaryUserReadService = primaryUserReadService;
    }

    @Export(paramNames = {"user", "roleId", "pageNo", "pageSize"})
    public Response<Paging<Sub>> pagingSubs(BaseUser user, Long roleId, Integer pageNo, Integer pageSize) {
        try {
            Long userId = user.getId();

            Paging<io.terminus.doctor.user.model.Sub> paging = RespHelper.orServEx(primaryUserReadService.subPagination(userId, roleId, null, pageNo, pageSize));

            List<Long> userIds = paging.getData().stream().map(s -> s.getId()).collect(Collectors.toList());
            Response<List<User>> userResp = userReadService.findByIds(userIds);

            Map<Long, User> userMap = userResp.getResult().stream().collect(Collectors.toMap(User::getId, u -> u));

            List<Sub> result = Lists.newArrayList();
            if (userResp.isSuccess()) {
                result = paging.getData().stream().map(s -> {
                    Sub op = new Sub();
                    User u = userMap.get(s.getUserId());
                    if (u != null) {
                        op.setUsername(u.getName());
                    }
                    return op;
                }).collect(Collectors.toList());
            }

            return Response.ok(new Paging<>(paging.getTotal(), result));
        } catch (ServiceException e) {
            log.warn("paging sub failed, user={}, pageNo={}, pageSize={}, error={}",
                    user, pageNo, pageSize, e.getMessage());
            return Response.fail("sub.paging.fail");
        } catch (Exception e) {
            log.error("paging sub failed, user={}, pageNo={}, pageSize={}, cause:{}",
                    user, pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.paging.fail");
        }
    }
}
