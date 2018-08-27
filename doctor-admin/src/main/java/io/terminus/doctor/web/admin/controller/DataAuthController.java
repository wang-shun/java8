package io.terminus.doctor.web.admin.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.warehouseV2.DataAuth;
import io.terminus.doctor.basic.service.warehouseV2.DataAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * @ClassName DataAuthController
 * @Description TODO
 * @Author Danny
 * @Date 2018/8/24 17:02
 */
@Slf4j
@RestController
@RequestMapping("/api/dataAuth")
public class DataAuthController {

    @RpcConsumer
    private DataAuthService dataAuthService;

    /**
     * 查询用户角色数据
     * @return
     */
    @RequestMapping(value = "/userRoleInfo", method = RequestMethod.POST)
    @ResponseBody
    public Response getUserRoleInfo(@RequestBody(required = false) Map<String,String> params){
        return dataAuthService.getUserRoleInfo(params);
    }

    /**
     * 查询单个用户角色数据
     * @return
     */
    @RequestMapping(value = "/userSingleRoleInfo/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public Response userSingleRoleInfo(@PathVariable("userId") Integer userId){
        return dataAuthService.userSingleRoleInfo(userId);
    }

    /**
     * 添加或修改用户角色数据
     * @return
     */
    @RequestMapping(value = "/editUserRoleInfo", method = RequestMethod.POST)
    @ResponseBody
    public Response editUserRoleInfo(@RequestBody(required = false) Map<String,String> params){
        return dataAuthService.editUserRoleInfo(params);
    }

    /**
     * 查询用户数据范围授权
     * @return
     */
    @RequestMapping(value = "/getDataSubRoles/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public Response getDataSubRoles(@PathVariable("userId") Integer userId){
        return dataAuthService.getDataSubRoles(userId);
    }

    /**
     * 批量保存用户数据范围授权
     * @return
     */
    @RequestMapping(value = "/saveDataSubRoles", method = RequestMethod.POST)
    @ResponseBody
    public Response saveDataSubRoles(@RequestBody(required = false) DataAuth dataSubRoles){
        return dataAuthService.saveDataSubRoles(dataSubRoles);
    }


}
