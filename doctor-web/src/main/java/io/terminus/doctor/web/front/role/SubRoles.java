package io.terminus.doctor.web.front.role;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.user.model.SubRole;
import io.terminus.doctor.user.service.SubRoleReadService;
import io.terminus.doctor.user.service.SubRoleWriteService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.engine.ThreadVars;
import io.terminus.parana.common.utils.RespHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

import static io.terminus.parana.common.utils.RespHelper.or500;

/**
 * Desc: 子账号
 * Mail: houly@terminus.io
 * Data: 下午3:35 16/5/25
 * Author: houly
 */
@RestController
@RequestMapping("/api/sub/role")
public class SubRoles {

    private final SubRoleReadService subRoleReadService;

    private final SubRoleWriteService subRoleWriteService;

    private final SubRoleService subRoleService;

    @Autowired
    public SubRoles(SubRoleReadService subRoleReadService, SubRoleWriteService subRoleWriteService, SubRoleService subRoleService) {
        this.subRoleReadService = subRoleReadService;
        this.subRoleWriteService = subRoleWriteService;
        this.subRoleService = subRoleService;
    }

    /**
     * 创建子账号角色
     *
     * @param role 运营角色
     * @return 角色主键 ID
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Long createRole(@RequestBody SubRole role) {
        role.setUserId(UserUtil.getUserId());
        checkRolePermission(role);
        role.setAppKey(ThreadVars.getAppKey());
        role.setStatus(1);
        return or500(subRoleWriteService.createRole(role));
    }

    /**
     * 更新子账号角色
     *
     * @param role 角色授权内容
     * @return 是否更新成功
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public Boolean updateRole(@RequestBody SubRole role) {
        if(role.getId() == null){
            throw new JsonResponseException(500, "sub.role.id.miss");
        }
        //检查用户提交的数据
        checkRolePermission(role);

        SubRole existRole = RespHelper.orServEx(subRoleReadService.findById(role.getId()));
        if (existRole == null) {
            throw new JsonResponseException(500, "sub.role.not.exist");
        }
        //检查数据库查询出来的数据
        checkRolePermission(existRole);

        role.setId(role.getId());
        role.setAppKey(null); // prevent update
        return or500(subRoleWriteService.updateRole(role));
    }

    /**
     * 拿所有符合的子角色
     *
     * @return 角色列表
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<SubRole> findAllRoles() {
        checkAuth();
        return RespHelper.or500(subRoleReadService.findByUserIdAndStatus(ThreadVars.getAppKey(), UserUtil.getUserId(), 1));
    }

    /**
     * 分页查询子账号角色
     * @param id
     * @param status
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/pagination", method = RequestMethod.GET)
    public Paging<SubRole> pagination(@RequestParam(required = false) Long id,
                                      @RequestParam(required = false) Integer status,
                                      @RequestParam(required = false) String roleName,
                                      @RequestParam(required = false) Integer pageNo,
                                      @RequestParam(required = false) Integer pageSize) {
        checkAuth();
        return or500(subRoleService.pagination(UserUtil.getCurrentUser(), id, status, roleName, pageNo, pageSize));
    }

    /**
     * 提供给更新角色页面
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public SubRole findByIdForUpdate(@PathVariable Long id) {
        checkAuth();
        return or500(subRoleService.findByIdForUpdate(UserUtil.getCurrentUser(), id));
    }

    /**
     * 校验创建的角色信息是否有权限创建
     * 当前登录用户只能创建当前登录的子账号角色
     * @param role
     */
    private void checkRolePermission(SubRole role){
        checkAuth();
        if(role.getId()!=null){
            SubRole dbRole = RespHelper.or500(subRoleReadService.findById(role.getId()));
            role.setUserId(dbRole.getUserId());
        }

        if(!Objects.equals(UserUtil.getUserId(), role.getUserId())){
            throw new JsonResponseException(403, "user.no.permission");
        }
    }

    private void checkAuth(){
        if(!Objects.equals(UserUtil.getCurrentUser().getType(), UserType.FARM_ADMIN_PRIMARY.value())){
            throw new JsonResponseException(403, "user.no.permission");
        }
    }


}
