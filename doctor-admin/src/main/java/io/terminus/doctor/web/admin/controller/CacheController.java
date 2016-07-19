package io.terminus.doctor.web.admin.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.service.DoctorStaffReadService;
import io.terminus.doctor.user.service.DoctorStaffWriteService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionWriteService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by chenzenghui on 16/7/19.
 */

@Slf4j
@RestController
@RequestMapping("/api/admin/cache")
public class CacheController {
    @RpcConsumer
    private DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService;
    @RpcConsumer
    private DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;
    @RpcConsumer
    private DoctorStaffWriteService doctorStaffWriteService;
    @RpcConsumer
    private DoctorStaffReadService doctorStaffReadService;

    /**
     * 刷新用户的 数据权限 和 职工信息 缓存
     * @param userId
     * @return 重新从数据库查询并添加到缓存中的数据
     */
    @RequestMapping(value = "/refresh", method = RequestMethod.GET)
    public Map<String, Object> refreshUserCache(@RequestParam Long userId){
        this.checkUserTypeOperator();
        Map<String, Object> result = new HashMap<>();

        doctorUserDataPermissionWriteService.clearUserCache(userId);
        result.put("DoctorUserDataPermission", RespHelper.or500(doctorUserDataPermissionReadService.findDataPermissionByUserId(userId)));

        doctorStaffWriteService.clearUserCache(userId);
        result.put("DoctorStaff", RespHelper.or500(doctorStaffReadService.findStaffByUserId(userId)));

        return result;
    }

    /**
     * 检查当前用户是否为运营人员, 若不是将抛出无权限异常
     * @return
     */
    private BaseUser checkUserTypeOperator(){
        BaseUser baseUser = UserUtil.getCurrentUser();
        if(!Objects.equals(UserType.ADMIN.value(), baseUser.getType())){
            throw new JsonResponseException("authorize.fail");
        }
        return baseUser;
    }

}
