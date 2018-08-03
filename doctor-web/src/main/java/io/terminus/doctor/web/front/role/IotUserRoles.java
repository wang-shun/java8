package io.terminus.doctor.web.front.role;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.dto.IotUserDto;
import io.terminus.doctor.user.model.IotRole;
import io.terminus.doctor.user.model.IotUser;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.user.service.IotUserRoleReadService;
import io.terminus.doctor.user.service.IotUserRoleWriteService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.Objects.isNull;

/**
 * Created by xjn on 17/10/12.
 */
@Api("物联网角色操作")
@Slf4j
@RestController
@RequestMapping("/api/iot/user/role")
public class IotUserRoles {

    @RpcConsumer
    private IotUserRoleReadService roleReadService;
    @RpcConsumer
    private IotUserRoleWriteService roleWriteService;

    @RpcConsumer
    private DoctorUserReadService doctorUserReadService;


    @Autowired
    private SubService subService;

    /**
     * 分页查询物联网运营账户
     *
     * @param realName 用户真实姓名
     * @param pageNo   页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    @ApiOperation("分页查询物联网运营账户")
    @RequestMapping(value = "/paging/iotUser", method = RequestMethod.GET)
    public Paging<IotUser> pagingUserRole(@RequestParam(required = false) @ApiParam("用户真实姓名") String realName,
                                          @RequestParam(required = false) @ApiParam("状态，多个状态通过下划线分隔") Integer status,
                                          @RequestParam(required = false) @ApiParam("页码") Integer pageNo,
                                          @RequestParam(required = false) @ApiParam("页大小") Integer pageSize) {
        return RespHelper.or500(roleReadService.paging(realName, status, IotUser.TYPE.IOT_OPERATOR.getValue(), pageNo, pageSize));
    }

    /**
     * 根据关联关系id查询用户与角色关联关系"
     *
     * @param id 关联关系id
     * @return 关联关系
     */
    @ApiOperation("根据id查询物联网运营用户")
    @RequestMapping(value = "/findIotUserRole/{id}", method = RequestMethod.GET)
    public IotUser findIotUserRoleById(@PathVariable @ApiParam("物联网运营用户id") Long id) {
        return RespHelper.or500(roleReadService.findIotUserRoleById(id));
    }

    /**
     * 创建或更新物联网运营用户"
     *
     * @param iotUserDto 物联网运营用户"
     * @return 是否成功
     */
    @ApiOperation("创建或更新物联网运营用户")
    @RequestMapping(value = "/createOrUpdate/iotUserRole", method = RequestMethod.POST)
    public Boolean createOrUpdateIotUserRole(@RequestBody @ApiParam("物联网运营用户") IotUserDto iotUserDto) {
        if (isNull(iotUserDto.getUserId())) {

            log.info("start to create iot user");
            Response<Boolean> checkUser = doctorUserReadService.checkExist(iotUserDto.getMobile(), iotUserDto.getUserName());
            if (!checkUser.isSuccess()) {
                log.warn("user name :{} or mobile :{} already existed", iotUserDto.getUserName(), iotUserDto.getMobile());
                throw new JsonResponseException(checkUser.getError());
            }

            return RespHelper.or500(roleWriteService.createIotUser(iotUserDto));
        }
        return RespHelper.or500(roleWriteService.updateIotUser(iotUserDto));
    }

    /**
     * 重置员工密码
     * @param userId 员工用户ID
     * @param resetPassword 重置的密码
     * @return
     */
    @RequestMapping(value = "/reset/{userId}", method = RequestMethod.POST)
    public Boolean resetPassword(@PathVariable Long userId, @RequestParam String resetPassword){
        return RespHelper.or500(subService.resetPassword(UserUtil.getCurrentUser(), userId, resetPassword));
    }

    /**
     * 列出所有有效的角色
     *
     * @return 所有有效角色列表
     */
    @ApiOperation("列出所有有效的角色")
    @RequestMapping(value = "/list/effectedRole", method = RequestMethod.GET)
    public List<IotRole> listEffectedRole() {
        return RespHelper.or500(roleReadService.listEffected());
    }

    /**
     * 根据角色id查询物联网角色
     *
     * @param id 角色id
     * @return 物联网角色
     */
    @ApiOperation("根据角色id查询物联网角色")
    @RequestMapping(value = "/findIotRole/{id}", method = RequestMethod.GET)
    public IotRole findIotRoleById(@PathVariable @ApiParam("角色id") Long id) {
        return RespHelper.or500(roleReadService.findIotRoleById(id));
    }

    /**
     * 创建或更新物联网角色
     *
     * @param iotRole 物联网角色
     * @return 是否成功
     */
    @ApiOperation("创建或更新物联网角色")
    @RequestMapping(value = "/createOrUpdate/iotRole", method = RequestMethod.POST)
    public Boolean createOrUpdateIotRole(@RequestBody @ApiParam("物联网角色") IotRole iotRole) {
        if (isNull(iotRole.getId())) {
            return RespHelper.or500(roleWriteService.createIotRole(iotRole));
        }
        return RespHelper.or500(roleWriteService.updateIotRole(iotRole));
    }
}
