package io.terminus.doctor.web.admin.role;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.OperatorRole;
import io.terminus.doctor.user.service.OperatorRoleReadService;
import io.terminus.doctor.web.core.auth.AuthLoader;
import io.terminus.pampas.client.Export;
import io.terminus.pampas.engine.ThreadVars;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.auth.CompiledTree;
import io.terminus.parana.auth.parser.ParseResult;
import io.terminus.parana.auth.util.CompiledTreeHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * @author Effet
 */
@Slf4j
@Component
public class OperatorRoleService {

    private final OperatorRoleReadService operatorRoleReadService;

    private final AuthLoader authLoader;

    @Autowired
    public OperatorRoleService(OperatorRoleReadService OperatorRoleReadService, AuthLoader authLoader) {
        this.operatorRoleReadService = OperatorRoleReadService;
        this.authLoader = authLoader;
    }

    /**
     * 运营角色分页
     *
     * @param user     登陆用户
     * @param id       角色 ID
     * @param status   角色状态
     * @param pageNo   页码
     * @param pageSize 查询数量
     * @return 分页结果
     */
    @Export(paramNames = {"user", "id", "status", "pageNo", "pageSize"})
    public Response<Paging<OperatorRole>> pagination(BaseUser user, Long id, Integer status, Integer pageNo, Integer pageSize) {
        try {
            if (id != null) {
                OperatorRole role = RespHelper.orServEx(operatorRoleReadService.findById(id));
                return Response.ok(new Paging<>(1L, Lists.newArrayList(role)));
            }
            return operatorRoleReadService.pagination(status, pageNo, pageSize);
        } catch (ServiceException e) {
            log.warn("paging operator roles failed, user={}, id={}, status={}, pageNo={}, pageSize={}, error={}",
                    user, id, status, pageNo, pageSize, e.getMessage());
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("paging operator roles failed, user={}, id={}, status={}, pageNo={}, pageSize={}, cause:{}",
                    user, id, status, pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("operator.role.paging.fail");
        }
    }

    /**
     * 提供给更新角色页面
     *
     * 通过 ID 查询角色
     *
     * @param user 登陆用户
     * @param id   角色 ID
     * @return 卖家角色
     */
    @Export(paramNames = {"user", "id"})
    public Response<OperatorRole> findByIdForUpdate(BaseUser user, @Nullable Long id) {
        try {
            CompiledTree operatorTree = getOperatorAuthTree();

            // id 为 null, 为创建页面调用, 直接返回 OPERATOR 权限树
            if (id == null) {
                OperatorRole role = new OperatorRole();
                role.setAllow(Lists.newArrayList(operatorTree));
                return Response.ok(role);
            }
            OperatorRole role = RespHelper.orServEx(operatorRoleReadService.findById(id));
            if (role == null) {
                log.warn("operator role not id={}", id);
                return Response.fail("operator.role.not.found");
            }
            // combine operatorTree and role.getAllow()
            role.setAllow(CompiledTreeHelper.combine(Lists.newArrayList(operatorTree), role.getAllow()));
            return Response.ok(role);
        } catch (ServiceException e) {
            log.warn("find operator role by id={} failed, error={}", id, e.getMessage());
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("find operator role by id={} failed, cause:{}",
                    id, Throwables.getStackTraceAsString(e));
            return Response.fail("operator.role.find.fail");
        }
    }

    private CompiledTree getOperatorAuthTree() {
        ParseResult result = authLoader.getTree(ThreadVars.getApp());
        for (CompiledTree compiledTree : result.getCompiledTrees()) {
            if ("ADMIN".equalsIgnoreCase(compiledTree.getScope())) {
                return compiledTree;
            }
        }
        throw new ServiceException("auth.tree.not.found");
    }
}
