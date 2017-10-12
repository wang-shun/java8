package io.terminus.doctor.web.front.role;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Paging;
import io.terminus.doctor.user.dto.IotUserRoleInfo;
import io.terminus.doctor.user.model.IotRole;
import io.terminus.doctor.user.model.IotUserRole;
import io.terminus.doctor.user.service.IotUserRoleReadService;
import io.terminus.doctor.user.service.IotUserRoleWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by xjn on 17/10/12.
 */
@Api("物联网角色操作")
@Slf4j
@RestController
@RequestMapping("/iot/user/role")
public class IotUserRoles {

    @RpcConsumer
    private IotUserRoleReadService roleReadService;
    @RpcConsumer
    private IotUserRoleWriteService roleWriteService;

    /**
     * 分页查询用户与物联网角色
     * @param realName 用户真实姓名
     * @param pageNo 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    @ApiOperation("分页查询用户与物联网角色")
    @RequestMapping(value = "/paging/userRole", method = RequestMethod.GET)
    public Paging<IotUserRoleInfo> pagingUserRole(@RequestParam(required = false) @ApiParam("用户真实姓名") String realName,
                                                  @RequestParam @ApiParam("页码") Integer pageNo,
                                                  @RequestParam @ApiParam("页大小") Integer pageSize) {
        return null;
    }

    /**
     * 根据关联关系id查询用户与角色关联关系"
     * @param id 关联关系id
     * @return 关联关系
     */
    @ApiOperation("根据关联关系id查询用户与角色关联关系")
    @RequestMapping(value = "/findIotUserRole/{id}", method = RequestMethod.GET)
    public IotUserRole findIotUserRoleById(@PathVariable @ApiParam("关联关系id") Long id) {
        return null;
    }

    /**
     * 创建或更新用户与角色关系
     * @param iotUserRole 用户与角色关系
     * @return 是否成功
     */
    @ApiOperation("创建或更新用户与角色关系")
    @RequestMapping(value = "/createOrUpdate/iotUserRole", method = RequestMethod.POST)
    public Boolean createOrUpdateIotUserRole(@RequestBody @ApiParam("用户与角色关系") IotUserRoles iotUserRole) {
        return null;
    }

    /**
     * 列出所有有效的角色
     * @return 所有有效角色列表
     */
    @ApiOperation("列出所有有效的角色")
    @RequestMapping(value = "/list/effectedRole", method = RequestMethod.GET)
    public List<IotRole> listEffectedRole() {
        return null;
    }

    /**
     * 根据角色id查询物联网角色
     * @param id 角色id
     * @return 物联网角色
     */
    @ApiOperation("根据角色id查询物联网角色")
    @RequestMapping(value = "/findIotRole/{id}", method = RequestMethod.GET)
    public IotRole findIotRoleById(@PathVariable @ApiParam("角色id") Long id) {
        return null;
    }

    /**
     * 创建或更新物联网角色
     * @param iotRole 物联网角色
     * @return 是否成功
     */
    @ApiOperation("创建或更新物联网角色")
    @RequestMapping(value = "/createOrUpdate/iotRole", method = RequestMethod.POST)
    public Boolean createOrUpdateIotRole(@RequestBody @ApiParam("物联网角色") IotRole iotRole){
        return null;
    }
}
