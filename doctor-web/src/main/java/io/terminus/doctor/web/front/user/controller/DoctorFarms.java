package io.terminus.doctor.web.front.user.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.service.*;
import io.terminus.doctor.web.core.dto.DoctorBasicDto;
import io.terminus.doctor.web.core.dto.FarmStaff;
import io.terminus.doctor.web.core.service.DoctorStatisticReadService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.model.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.common.utils.Arguments.notNull;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/29
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/farm")
public class DoctorFarms {

    private final DoctorFarmReadService doctorFarmReadService;
    private final DoctorOrgReadService doctorOrgReadService;
    private final DoctorStaffReadService doctorStaffReadService;
    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;
    private final DoctorUserReadService doctorUserReadService;
    private final DoctorStatisticReadService doctorStatisticReadService;
    @RpcConsumer
    private DoctorUserProfileReadService doctorUserProfileReadService;
    @RpcConsumer
    private PrimaryUserReadService primaryUserReadService;

    @Autowired
    public DoctorFarms(DoctorFarmReadService doctorFarmReadService,
                       DoctorOrgReadService doctorOrgReadService,
                       DoctorStaffReadService doctorStaffReadService,
                       DoctorUserDataPermissionReadService doctorUserDataPermissionReadService,
                       DoctorUserReadService doctorUserReadService,
                       DoctorStatisticReadService doctorStatisticReadService) {
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorOrgReadService = doctorOrgReadService;
        this.doctorStaffReadService = doctorStaffReadService;
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
        this.doctorUserReadService = doctorUserReadService;
        this.doctorStatisticReadService = doctorStatisticReadService;
    }

    /**
     * 根据用户id查询所拥有权限的公司信息
     *
     * @return 公司信息
     */
    @RequestMapping(value = "/orgInfo", method = RequestMethod.GET)
    public DoctorBasicDto getCompanyInfo() {
        return RespHelper.or500(doctorStatisticReadService.getOrgStatistic(UserUtil.getUserId()));
    }

    /**
     * 根据用户id查询所拥有权限的公司信息
     *
     * @return 公司信息
     */
    @RequestMapping(value = "/companyInfo", method = RequestMethod.GET)
    public DoctorBasicDto getCompanyInfo(@RequestParam(value = "orgId", required = false) Long orgId,HttpServletRequest request) {
        String isshow = null;
        Cookie[] cookie = request.getCookies();
        for (int i = 0; i < cookie.length; i++) {
            Cookie cook = cookie[i];
            if(cook.getName().equalsIgnoreCase("isshow")){ //获取键
                isshow = cook.getValue().toString();    //获取值
            }
        }
        Integer userType = doctorOrgReadService.getUserType(UserUtil.getUserId());
        log.error("=====ishwo="+isshow+"=userType="+userType);
        if(userType != null) {
            if (userType == 1 && isshow == null) {
                return null;
            }
        }
        return RespHelper.or500(doctorStatisticReadService.getOrgStatisticByOrg(UserUtil.getUserId(), orgId));
    }

    /**
     * 根据当前登录用户公司,查询猪场(非数据权限, 查询当前用户所属公司下的所有猪场!)
     *
     * @return 猪场list
     */
    @RequestMapping(value = "/loginUser", method = RequestMethod.GET)
    public List<DoctorFarm> findFarmsByLoginUser(@RequestParam(value = "farmIds", required = false) String farmIds,
                                                 @RequestParam(value = "excludeFarmIds", required = false) String excludeFarmIds) {
        if (UserUtil.getUserId() == null) {
            throw new JsonResponseException("user.not.login");
        }
        return filterFarm(RespHelper.or500(doctorFarmReadService.findFarmsByUserId(UserUtil.getUserId())), farmIds, excludeFarmIds);
    }

    /**
     * @return 猪场list
     */
    @RequestMapping(value = "/toFarms", method = RequestMethod.GET)
    public List<DoctorFarm> findToFarmsByFarm(@RequestParam(value = "farmId") Long farmId) {
        DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
        if (Arguments.isNull(farm)) {
            log.error("no farm find, farmId: {}", farmId);
            throw new JsonResponseException("no.farm.find");
        }
        return filterFarm(RespHelper.or500(doctorFarmReadService.findFarmsByOrgId(farm.getOrgId())), null, String.valueOf(farmId));
    }

    private List<DoctorFarm> filterFarm(List<DoctorFarm> farms, String farmIds, String excludeFarmIds) {
        //保留的字段
        if (notEmpty(farmIds)) {
            List<Long> requiredFarmIds = Splitters.splitToLong(farmIds, Splitters.COMMA);
            farms = farms.stream().filter(farm -> requiredFarmIds.contains(farm.getId())).collect(Collectors.toList());
        }

        //排除的字段
        if (notEmpty(excludeFarmIds)) {
            List<Long> exFarmIds = Splitters.splitToLong(excludeFarmIds, Splitters.COMMA);
            farms = farms.stream().filter(farm -> !exFarmIds.contains(farm.getId())).collect(Collectors.toList());
        }
        return farms;
    }

    @RequestMapping(value = "/find/{farmId}", method = RequestMethod.GET)
    public List<FarmStaff> findByFarmId(@PathVariable Long farmId) {
        return transformStaffs(farmId);
    }

    /**
     * 查询猪场所有的staff信息
     *
     * @param farmId 猪场id
     * @return staff信息
     */
    @RequestMapping(value = "/staff/{farmId}", method = RequestMethod.GET)
    public List<FarmStaff> findStaffByFarmId(@PathVariable Long farmId) {
        return transformStaffs(farmId);
    }

    /**
     * 获取猪场员工
     *
     * @param farmId 猪场id
     * @return 员工列表
     */
    // 软件登陆人员是谁，仓库单据操作人就默认是谁，并支持修改 （陈娟 2018-09-13）
    private List<FarmStaff> transformStaffs(Long farmId) {
        List<FarmStaff> staffList = Lists.newArrayList();
        BaseUser currentUser = UserUtil.getCurrentUser();
        // 得到当前登陆人的信息，置顶
        Sub subUser = RespHelper.or500(primaryUserReadService.findSubsByFarmIdAndStatusAndUserId(farmId, Sub.Status.ACTIVE.value(), currentUser.getId()));
        if(subUser!=null&&notNull(subUser)){
            FarmStaff farmStaff = new FarmStaff();
            farmStaff.setUserId(subUser.getUserId());
            farmStaff.setRealName(subUser.getRealName());
            farmStaff.setStatus(subUser.getStatus());
            farmStaff.setFarmId(subUser.getFarmId());
            staffList.add(farmStaff);
        }else{
            PrimaryUser primaryUser = RespHelper.or500(primaryUserReadService.findPrimaryByFarmIdAndStatus(farmId, UserStatus.NORMAL.value()));
            if (primaryUser != null) {
                FarmStaff farmStaff = new FarmStaff();
                farmStaff.setFarmId(primaryUser.getRelFarmId());
                farmStaff.setUserId(primaryUser.getUserId());
                farmStaff.setStatus(primaryUser.getStatus());
                Response<UserProfile> userProfileResponse = doctorUserProfileReadService.findProfileByUserId(primaryUser.getUserId());
                if (userProfileResponse.isSuccess()
                        && notNull(userProfileResponse.getResult())
                        && !Strings.isNullOrEmpty(userProfileResponse.getResult().getRealName())) {
                    farmStaff.setRealName(userProfileResponse.getResult().getRealName());
                } else {
                    User user = RespHelper.or500(doctorUserReadService.findById(primaryUser.getUserId()));
                    farmStaff.setRealName(user.getName());
                }
                staffList.add(farmStaff);
            }
        }

        List<Sub> subList = RespHelper.or500(primaryUserReadService.findSubsByFarmIdAndStatus(farmId, Sub.Status.ACTIVE.value(),currentUser.getId()));
        if (!Arguments.isNullOrEmpty(subList)) {
            staffList.addAll(subList.stream().map(sub -> {
                FarmStaff farmStaff = new FarmStaff();
                farmStaff.setUserId(sub.getUserId());
                farmStaff.setRealName(sub.getRealName());
                farmStaff.setStatus(sub.getStatus());
                farmStaff.setFarmId(sub.getFarmId());
                return farmStaff;
            }).collect(Collectors.toList()));
        }
        return staffList;
    }

    /**
     * 查询当前用户有权限的猪场
     *
     * @return
     */
    @RequestMapping(value = "/permissionFarm", method = RequestMethod.GET)
    public List<DoctorFarm> permissionFarm() {
        return RespHelper.or500(doctorFarmReadService.findFarmsByUserId(UserUtil.getUserId()));
    }

    /**
     * 根据orgId及用户权限查询猪场
     *
     * @return
     */
    @RequestMapping(value = "/org/farm-list", method = RequestMethod.GET)
    public List<DoctorFarm> findFarmByOrgId(@RequestParam Long orgId,
                                            @RequestParam(required = false) Integer isIntelligent) {
        DoctorUserDataPermission doctorUserDataPermission = RespHelper.or500(doctorUserDataPermissionReadService.findDataPermissionByUserId(UserUtil.getUserId()));
        List<Long> doctorFarmIds = doctorUserDataPermission.getFarmIdsList();
        List<DoctorFarm> doctorFarms = RespHelper.or500(doctorFarmReadService.findFarmsBy(orgId, isIntelligent));
        if (doctorFarms != null) {
            doctorFarms = doctorFarms.stream().filter(t -> doctorFarmIds.contains(t.getId())).collect(Collectors.toList());
        }
        return doctorFarms;
    }

    /**
     * 根据猪厂查询同公司的所有的猪厂
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/org/farm", method = RequestMethod.GET)
    public List<DoctorFarm> findOtherFarmByOrgId(Long farmId) {
        DoctorUserDataPermission doctorUserDataPermission = RespHelper.or500(doctorUserDataPermissionReadService.findDataPermissionByUserId(UserUtil.getUserId()));
        List<Long> doctorFarmIds = doctorUserDataPermission.getFarmIdsList();

        DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
        if (null == farm)
            throw new JsonResponseException("farm.not.found");
        Long orgId = farm.getOrgId();

        List<DoctorFarm> doctorFarms = RespHelper.or500(doctorFarmReadService.findFarmsByOrgId(orgId));
        if (doctorFarms != null) {
            doctorFarms = doctorFarms.stream().filter(t -> doctorFarmIds.contains(t.getId())).collect(Collectors.toList());
        }
        return doctorFarms;
    }

    /**
     * 根据猪场id查询猪场信息
     *
     * @param farmId 猪场id
     * @return 猪场信息
     */
    @RequestMapping(value = "/{farmId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorFarm findFarmById(@PathVariable Long farmId) {
        return RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
    }

    /**
     * 查询所有猪场
     *
     * @return 猪场列表
     */
    @RequestMapping(value = "/findAllFarm", method = RequestMethod.GET)
    public List<DoctorFarm> findAllFarm() {
        return RespHelper.or500(doctorFarmReadService.findAllFarms());
    }

}
