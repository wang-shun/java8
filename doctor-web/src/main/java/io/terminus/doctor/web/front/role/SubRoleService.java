package io.terminus.doctor.web.front.role;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.SubRole;
import io.terminus.doctor.user.service.SubRoleReadService;
import io.terminus.pampas.client.Export;
import io.terminus.pampas.engine.ThreadVars;
import io.terminus.parana.common.utils.RespHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * Desc: 子账号角色建立
 * Mail: houly@terminus.io
 * Data: 下午3:34 16/5/25
 * Author: houly
 */
@Slf4j
@Component
public class SubRoleService {

    private final SubRoleReadService subRoleReadService;
    private final SubService subService;

    @Autowired
    public SubRoleService(SubRoleReadService subRoleReadService,
                          SubService subService) {
        this.subRoleReadService = subRoleReadService;
        this.subService = subService;
    }

    /**
     * 子账号角色分页
     *
     * @param user     登陆用户
     * @param id       角色 ID
     * @param status   角色状态
     * @param pageNo   页码
     * @param pageSize 查询数量
     * @return 分页结果
     */
    @Export(paramNames = {"user", "id", "status", "roleName", "pageNo", "pageSize"})
    public Response<Paging<SubRole>> pagination(BaseUser user, Long id, Integer status, String roleName, Integer pageNo, Integer pageSize) {
        try {
            if (id != null) {
                SubRole role = RespHelper.orServEx(subRoleReadService.findById(id));
                if(role == null){
                    return Response.ok(Paging.empty());
                }
                if(pageNo > 1){
                    //当按照主键id查询时,只应该有一页
                    return Response.ok(new Paging<>(1L, Lists.newArrayList()));
                }
                return Response.ok(new Paging<>(1L, Lists.newArrayList(role)));
            }
            return subRoleReadService.pagination(ThreadVars.getAppKey(), subService.getPrimaryUserId(user), status, roleName, pageNo, pageSize);
        } catch (ServiceException e) {
            log.warn("paging sub roles failed, user={}, id={}, status={}, pageNo={}, pageSize={}, error={}",
                    user, id, status, pageNo, pageSize, e.getMessage());
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("paging sub roles failed, user={}, id={}, status={}, pageNo={}, pageSize={}, cause:{}",
                    user, id, status, pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.role.paging.fail");
        }
    }

    /**
     * 子账号角色分页
     *
     * @param farmId   猪场id
     * @param id       角色 ID
     * @param status   角色状态
     * @param pageNo   页码
     * @param pageSize 查询数量
     * @return 分页结果
     */
    @Export(paramNames = {"farmId", "id", "status", "roleName", "pageNo", "pageSize"})
    public Response<Paging<SubRole>> pagingRole(Long farmId, Long id, Integer status, String roleName, Integer pageNo, Integer pageSize) {
        try {
            if (id != null) {
                SubRole role = RespHelper.orServEx(subRoleReadService.findById(id));
                if(role == null){
                    return Response.ok(Paging.empty());
                }
                if(pageNo > 1){
                    //当按照主键id查询时,只应该有一页
                    return Response.ok(new Paging<>(1L, Lists.newArrayList()));
                }
                return Response.ok(new Paging<>(1L, Lists.newArrayList(role)));
            }
            return subRoleReadService.pagingRole(ThreadVars.getAppKey(), farmId, status, roleName, pageNo, pageSize);
        } catch (ServiceException e) {
            log.warn("paging sub roles failed, farmId={}, id={}, status={}, pageNo={}, pageSize={}, error={}",
                    farmId, id, status, pageNo, pageSize, e.getMessage());
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("paging sub roles failed, farmId={}, id={}, status={}, pageNo={}, pageSize={}, cause:{}",
                    farmId, id, status, pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.role.paging.fail");
        }
    }

    /**
     * 提供给更新角色页面
     *
     * 通过 ID 查询角色
     *
     * @param user 登陆用户
     * @param id   角色 ID
     * @return 子账号角色
     */
    @Export(paramNames = {"user", "id"})
    public Response<SubRole> findByIdForUpdate(BaseUser user, @Nullable Long id) {
        try {
            // id 为 null, 为创建页面调用, 直接返回 SUB 权限树
            if (id == null) {
                SubRole role = new SubRole();
                return Response.ok(role);
            }
            SubRole role = RespHelper.orServEx(subRoleReadService.findById(id));
            if (role == null) {
                log.warn("sub role not id={}", id);
                return Response.fail("sub.role.not.found");
            }
            return Response.ok(role);
        } catch (ServiceException e) {
            log.warn("find sub role by id={} failed, error={}", id, e.getMessage());
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("find sub role by id={} failed, cause:{}",
                    id, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.role.find.fail");
        }
    }
}
