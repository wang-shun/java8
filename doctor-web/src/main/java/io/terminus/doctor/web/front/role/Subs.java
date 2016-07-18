package io.terminus.doctor.web.front.role;

import com.google.common.collect.Lists;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorStaffReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.web.core.component.MobilePattern;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.common.utils.RespHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    private final MobilePattern mobilePattern;

    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;

    private final DoctorFarmReadService doctorFarmReadService;
    
    private final DoctorStaffReadService doctorStaffReadService;

    @Autowired
    public Subs(SubService subService,
                MobilePattern mobilePattern,
                DoctorUserDataPermissionReadService doctorUserDataPermissionReadService,
                DoctorFarmReadService doctorFarmReadService,
                DoctorStaffReadService doctorStaffReadService) {
        this.subService = subService;
        this.mobilePattern = mobilePattern;
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorStaffReadService = doctorStaffReadService;
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
     * @return
     */
    @RequestMapping(value = "/pagination", method = RequestMethod.GET)
    public Paging<Sub> pagingSubs(@RequestParam(required = false) Long roleId,
                                  @RequestParam(required = false) String roleName,
                                  @RequestParam(required = false) String username,
                                  @RequestParam(required = false) String realName,
                                  @RequestParam(required = false) Integer pageNo,
                                  @RequestParam(required = false) Integer pageSize) {
        checkAuth();
        return RespHelper.or500(subService.pagingSubs(UserUtil.getCurrentUser(), roleId, roleName, username, realName, pageNo, pageSize));
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<Sub> findByConditions(@RequestParam(required = false) Long roleId,
                                  @RequestParam(required = false) String roleName,
                                  @RequestParam(required = false) String username,
                                  @RequestParam(required = false) String realName,
                                  @RequestParam(required = false) Integer size) {
        checkAuth();
        return RespHelper.or500(subService.findByConditions(UserUtil.getCurrentUser(), roleId, roleName, username, realName, size));
    }

    @RequestMapping(value = "/find/{farmId}", method = RequestMethod.GET)
    public List<Sub> findByFarmId(@PathVariable Long farmId){
        DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
        Map<Long, Long> userIdJoinStaffId = new HashMap<>(); // key =userId, value = staffId
        RespHelper.or500(doctorStaffReadService.findStaffByOrgId(farm.getOrgId()))
                .forEach(staff -> userIdJoinStaffId.put(staff.getUserId(), staff.getId()));
        List<Long> matchUserIds = Lists.newArrayList();
        RespHelper.or500(doctorUserDataPermissionReadService.findDataPermissionByUserIds(userIdJoinStaffId.keySet().stream().collect(Collectors.toList())))
                .forEach(permission -> {
                    if (permission.getFarmIdsList().contains(farmId)) {
                        matchUserIds.add(permission.getUserId());
                    }
                });
        List<Sub> subs = RespHelper.or500(subService.findByUserIds(matchUserIds));
        subs.forEach(sub -> sub.setStaffId(userIdJoinStaffId.get(sub.getId())));
        return subs;
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

        if(!Objects.equals(UserUtil.getCurrentUser().getType(), UserType.FARM_ADMIN_PRIMARY.value())){
            throw new JsonResponseException(403, "user.no.permission");
        }
    }

}
