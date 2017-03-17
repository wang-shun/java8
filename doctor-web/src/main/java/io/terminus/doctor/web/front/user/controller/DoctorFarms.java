package io.terminus.doctor.web.front.user.controller;

import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorStaffReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.user.service.PrimaryUserReadService;
import io.terminus.doctor.web.core.dto.DoctorBasicDto;
import io.terminus.doctor.web.core.dto.FarmStaff;
import io.terminus.doctor.web.core.service.DoctorStatisticReadService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
    @RpcConsumer
    private PrimaryUserReadService primaryUserReadService;

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
    public DoctorBasicDto getCompanyInfo(@RequestParam(value = "orgId", required = false) Long orgId){
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
     * @param farmId 猪场id
     * @return 员工列表
     */
    private List<FarmStaff> transformStaffs(Long farmId) {
        List<Sub> subList = RespHelper.or500(primaryUserReadService.findSubsByFarmId(farmId));
        List<FarmStaff> staffList = Lists.newArrayList();
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
        PrimaryUser primaryUser = RespHelper.or500(primaryUserReadService.findPrimaryByFarmId(farmId));
        if(primaryUser !=null){
            FarmStaff farmStaff = new FarmStaff();
            farmStaff.setFarmId(primaryUser.getRelFarmId());
            farmStaff.setUserId(primaryUser.getUserId());
            farmStaff.setStatus(primaryUser.getStatus());
            farmStaff.setRealName(primaryUser.getRealName());
            staffList.add(farmStaff);
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
    public List<DoctorFarm> findFarmByOrgId(Long orgId) {
        DoctorUserDataPermission doctorUserDataPermission = RespHelper.or500(doctorUserDataPermissionReadService.findDataPermissionByUserId(UserUtil.getUserId()));
        List<Long> doctorFarmIds = doctorUserDataPermission.getFarmIdsList();
        List<DoctorFarm> doctorFarms = RespHelper.or500(doctorFarmReadService.findFarmsByOrgId(orgId));
        if (doctorFarms != null) {
            doctorFarms = doctorFarms.stream().filter(t -> doctorFarmIds.contains(t.getId())).collect(Collectors.toList());
        }
        return doctorFarms;
    }

    /**
     * 根据猪场id查询猪场信息
     * @param farmId 猪场id
     * @return 猪场信息
     */
    @RequestMapping(value = "/{farmId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorFarm findFarmById(@PathVariable Long farmId) {
        return RespHelper.or500(doctorFarmReadService.findFarmById(farmId));
    }

}
