package io.terminus.doctor.web.front.user.controller;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorStaffReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.web.core.dto.DoctorBasicDto;
import io.terminus.doctor.web.core.dto.FarmStaff;
import io.terminus.doctor.web.core.service.DoctorStatisticReadService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

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
    private final DoctorStaffReadService doctorStaffReadService;
    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;
    private final DoctorUserReadService doctorUserReadService;
    private final DoctorStatisticReadService doctorStatisticReadService;

    @Autowired
    public DoctorFarms(DoctorFarmReadService doctorFarmReadService,
                       DoctorStaffReadService doctorStaffReadService,
                       DoctorUserDataPermissionReadService doctorUserDataPermissionReadService,
                       DoctorUserReadService doctorUserReadService,
                       DoctorStatisticReadService doctorStatisticReadService) {
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorStaffReadService = doctorStaffReadService;
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
        this.doctorUserReadService = doctorUserReadService;
        this.doctorStatisticReadService = doctorStatisticReadService;
    }

    /**
     * 根据用户id查询所拥有权限的公司信息
     * @return 公司信息
     */
    @RequestMapping(value = "/orgInfo", method = RequestMethod.GET)
    public DoctorBasicDto getCompanyInfo(){
        return RespHelper.or500(doctorStatisticReadService.getOrgStatistic(UserUtil.getUserId()));
    }

    /**
     * 根据用户id查询所拥有权限的公司信息
     * @return 公司信息
     */
    @RequestMapping(value = "/companyInfo", method = RequestMethod.GET)
    public DoctorBasicDto getCompanyInfo(@RequestParam(value = "orgId", required = false) Long orgId){
        return RespHelper.or500(doctorStatisticReadService.getOrgStatisticByOrg(UserUtil.getUserId(), orgId));
    }

    /**
     * 根据当前登录用户公司,查询猪场(非数据权限, 查询当前用户所属公司下的所有猪场!)
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
    public List<FarmStaff> findByFarmId(@PathVariable Long farmId){
        List<DoctorStaff> staffs = RespHelper.or500(doctorStaffReadService.findStaffByFarmIdAndStatus(farmId, DoctorStaff.Status.PRESENT.value()));
        return transformStaffs(staffs);
    }

    /**
     * 查询猪场所有的staff信息
     * @param farmId 猪场id
     * @return staff信息
     */
    @RequestMapping(value = "/staff/{farmId}", method = RequestMethod.GET)
    public List<FarmStaff> findStaffByFarmId(@PathVariable Long farmId){
        List<DoctorStaff> staffs = RespHelper.or500(doctorStaffReadService.findStaffByFarmIdAndStatus(farmId, null));
        return transformStaffs(staffs);
    }

    //拼接一发数据
    private List<FarmStaff> transformStaffs(List<DoctorStaff> staffs) {
        List<User> users = RespHelper.or500(doctorUserReadService.findByIds(staffs.stream()
                .map(DoctorStaff::getUserId).collect(Collectors.toList())));
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        return staffs.stream()
                .map(staff -> {
                    FarmStaff farmStaff = BeanMapper.map(staff, FarmStaff.class);

                    User user = userMap.get(staff.getUserId());
                    if (user != null) {
                        if(Objects.equals(user.getType(), UserType.FARM_ADMIN_PRIMARY.value())){
                            farmStaff.setRealName(user.getName() == null ? user.getMobile() : user.getName());
                        }else if(Objects.equals(user.getType(), UserType.FARM_SUB.value())){
                            farmStaff.setRealName(Params.get(user.getExtra(), "realName"));
                        }
                    }
                    return farmStaff;
                })
                .collect(Collectors.toList());
    }

    /**
     * 查询当前用户有权限的猪场
     * @return
     */
    @RequestMapping(value = "/permissionFarm", method = RequestMethod.GET)
    public List<DoctorFarm> permissionFarm(){
        return RespHelper.or500(doctorFarmReadService.findFarmsByUserId(UserUtil.getUserId()));
    }
    /**
     * 根据orgId及用户权限查询猪场
     * @return
     */
    @RequestMapping(value = "/org/farm-list", method = RequestMethod.GET)
    public List<DoctorFarm> findFarmByOrgId(Long orgId){
        DoctorUserDataPermission doctorUserDataPermission=RespHelper.or500( doctorUserDataPermissionReadService.findDataPermissionByUserId(UserUtil.getUserId()));
        List<Long> doctorFarmIds=doctorUserDataPermission.getFarmIdsList();
        List<DoctorFarm> doctorFarms=RespHelper.or500(doctorFarmReadService.findFarmsByOrgId(orgId));
        if (doctorFarms!=null){
            doctorFarms = doctorFarms.stream().filter(t->doctorFarmIds.contains(t.getId())).collect(Collectors.toList());
        }
        return doctorFarms;
    }

}
