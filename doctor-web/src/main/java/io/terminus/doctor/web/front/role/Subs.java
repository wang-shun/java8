package io.terminus.doctor.web.front.role;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.web.core.component.MobilePattern;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.terminus.common.utils.Arguments.isNull;

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

    private final SubService subService;
    private final DoctorUserReadService doctorUserReadService;
    private final MobilePattern mobilePattern;
    @RpcConsumer
    private DoctorUserDataPermissionReadService permissionReadService;

    @Autowired
    public Subs(SubService subService,
                DoctorUserReadService doctorUserReadService,
                MobilePattern mobilePattern) {
        this.subService = subService;
        this.doctorUserReadService = doctorUserReadService;
        this.mobilePattern = mobilePattern;
    }


    /**
     * 通过用户id查询到账号信息
     * @param userId
     * @return
     */
    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    public Sub info(@PathVariable Long userId){
        checkAuth();
        return RespHelper.or500(subService.findSubByUserId(UserUtil.getCurrentUser(), userId));
    }

    /**
     * 创建子账号
     *
     * @param sub 子账号信息
     * @return 子账号 ID
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Long createSub(@RequestBody Sub sub) {
        if(sub.getFarmIds() == null || sub.getFarmIds().isEmpty()){
            throw new JsonResponseException(500, "need.at.least.one.farm");
        }
        if(sub.getContact() == null || !mobilePattern.getPattern().matcher(sub.getContact()).matches()){
            throw new JsonResponseException(500, "mobile.format.error");
        }
        checkAuth();
        return RespHelper.or500(subService.createSub(UserUtil.getCurrentUser(), sub));
    }

    /**
     * 更新子账号
     * @param sub
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public Boolean updateSub(@RequestBody Sub sub) {
        if(sub.getFarmIds() == null || sub.getFarmIds().isEmpty()){
            throw new JsonResponseException(500, "need.at.least.one.farm");
        }
        if(sub.getContact() == null || !mobilePattern.getPattern().matcher(sub.getContact()).matches()){
            throw new JsonResponseException(500, "mobile.format.error");
        }
        checkAuth();
        return RespHelper.or500(subService.updateSub(UserUtil.getCurrentUser(), sub));
    }

    /**
     * 分页查询子用户
     * @param roleId
     * @param realName 员工真实姓名, 匹配 user_profile 表中的 real_name 字段
     * @param pageNo
     * @param pageSize
     * @param status 子账号状态
     * @see io.terminus.doctor.user.model.Sub.Status
     * @return
     */
    @RequestMapping(value = "/pagination", method = RequestMethod.GET)
    public Paging<Sub> pagingSubs(@RequestParam Long farmId,
                                  @RequestParam(required = false) Long roleId,
                                  @RequestParam(required = false) String roleName,
                                  @RequestParam(required = false) String username,
                                  @RequestParam(required = false) String realName,
                                  @RequestParam(required = false) Integer status,
                                  @RequestParam(required = false) Integer pageNo,
                                  @RequestParam(required = false) Integer pageSize) {
        checkAuth();
        //校验猪场id是否为空
        if (isNull(farmId)) {
            throw new JsonResponseException("farm.id.not.null");
        }
        //校验是否拥有猪场权限
        DoctorUserDataPermission permission = RespHelper.or500(permissionReadService.findDataPermissionByUserId(UserUtil.getUserId()));
        if (isNull(permission) || Arguments.isNullOrEmpty(permission.getFarmIdsList()) || !permission.getFarmIdsList().contains(farmId)) {
//            throw new JsonResponseException(403, "user.no.permission");
            log.info("Subs.user.no.farm.permission");
            throw new JsonResponseException(403, "user.no.farm.permission");
        }

        return RespHelper.or500(subService.pagingSubs(farmId, UserUtil.getCurrentUser(), roleId, roleName, username, realName, status, pageNo, pageSize));
    }

    /**
     * 多条件筛选, 相当于分页查询去掉了分页参数, 所有参数都可以为空
     * @param status 子账号状态
     * @see io.terminus.doctor.user.model.Sub.Status
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<Sub> findByConditions(@RequestParam(required = false) Long roleId,
                                  @RequestParam(required = false) String roleName,
                                  @RequestParam(required = false) String username,
                                  @RequestParam(required = false) String realName,
                                  @RequestParam(required = false) Integer status,
                                  @RequestParam(required = false) Integer size) {
        checkAuth();
        return RespHelper.or500(subService.findByConditions(UserUtil.getCurrentUser(), roleId, roleName, username, realName, status, size));
    }

    /**
     * 重置员工密码
     * @param userId 员工用户ID
     * @param resetPassword 重置的密码
     * @return
     */
    @RequestMapping(value = "/reset/{userId}", method = RequestMethod.POST)
    public Boolean resetPassword(@PathVariable Long userId, @RequestParam String resetPassword){
        checkAuth();
        return RespHelper.or500(subService.resetPassword(UserUtil.getCurrentUser(), userId, resetPassword));
    }

    /**
     * 检查用户权限
     */
    private void checkAuth(){

        if (UserUtil.getCurrentUser() == null) {
            throw new JsonResponseException(401, "user.not.login");
        }
    }

    /**
     * 子账号查询其主账号的用户信息
     * @return
     */
    @RequestMapping(value = "/getPrimaryUser", method = RequestMethod.GET)
    public User getPrimaryUser(){
        Long primaryUserId = subService.getPrimaryUserId(UserUtil.getCurrentUser());
        User primaryUser = RespHelper.orServEx(doctorUserReadService.findById(primaryUserId));
        primaryUser.setPassword(null);
        return primaryUser;
    }

}
