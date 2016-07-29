package io.terminus.doctor.move.controller;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.move.handler.DoctorMoveTableEnum;
import io.terminus.doctor.move.model.RoleTemplate;
import io.terminus.doctor.move.model.View_FarmInfo;
import io.terminus.doctor.move.model.View_FarmMember;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.dao.SubRoleDao;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.model.SubRole;
import io.terminus.doctor.user.service.DoctorServiceReviewWriteService;
import io.terminus.doctor.user.service.DoctorServiceStatusWriteService;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sun.awt.ModalExclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Created by chenzenghui on 16/7/27.
 */

@Slf4j
@RestController
@RequestMapping("/api/data/user")
public class UserInit {

    @Autowired
    private DoctorUserReadService doctorUserReadService;
    @Autowired
    private UserWriteService<User> userWriteService;
    @Autowired
    private DoctorServiceReviewWriteService doctorServiceReviewWriteService;
    @Autowired
    private DoctorServiceStatusWriteService doctorServiceStatusWriteService;
    @Autowired
    private DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;
    @Autowired
    private DoctorOrgDao doctorOrgDao;
    @Autowired
    private DoctorStaffDao doctorStaffDao;
    @Autowired
    private DoctorFarmDao doctorFarmDao;
    @Autowired
    private DoctorUserDataPermissionDao doctorUserDataPermissionDao;
    @Autowired
    private SubRoleDao subRoleDao;


    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public String userInit(@RequestParam String mobile, @RequestParam Long dataSourceId) {
        log.warn("start to init user and farm data, mobile = {}, dataSourceId = {}", mobile, dataSourceId);
        try{
            this.init(mobile, dataSourceId);
            log.warn("init user and farm data succeed, mobile = {}, dataSourceId = {}", mobile, dataSourceId);
            return "ok";
        }catch(Exception e){
            log.error("init user data, mobile={}, dataSourceId={}, error:{}", mobile, dataSourceId, Throwables.getStackTraceAsString(e));
            return Throwables.getStackTraceAsString(e);
        }
    }

    private void init(String mobile, Long dataSourceId){
        List<View_FarmMember> list = doctorMoveDatasourceHandler.findAllData(dataSourceId, View_FarmMember.class, DoctorMoveTableEnum.view_FarmMember).getResult();
        List<DoctorFarm> farms = new ArrayList<>();
        doctorMoveDatasourceHandler.findAllData(dataSourceId, View_FarmInfo.class, DoctorMoveTableEnum.view_FarmInfo).getResult().forEach(farmInfo -> {
            if(farmInfo.getLevels() == 1){
                farms.add(makeFarm(farmInfo));
            }
        });

        User primaryUser = null;
        List<Long> farmIds = Lists.newArrayList();
        for(View_FarmMember member : list){
            if(member.getLevels() == 0){
                // 主账号注册,内含事务
                primaryUser= this.registerByMobile(mobile, "123456", null);
                Long userId = primaryUser.getId();
                //初始化服务状态
                this.initDefaultServiceStatus(userId);
                //初始化服务的申请审批状态
                RespHelper.or500(doctorServiceReviewWriteService.initServiceReview(userId, mobile));
                //创建org
                DoctorOrg org = this.createOrg(member.getFarmName(), mobile, null, member.getFarmOID());
                //创建staff
                this.createStaff(member, primaryUser, org, member.getOID());
                //创建猪场
                for(DoctorFarm farm : farms){
                    farm.setOrgId(org.getId());
                    farm.setOrgName(org.getName());
                    doctorFarmDao.create(farm);
                    farmIds.add(farm.getId());
                }
                //创建数据权限
                DoctorUserDataPermission permission = new DoctorUserDataPermission();
                permission.setUserId(userId);
                permission.setFarmIds(Joiner.on(",").join(farmIds));
                doctorUserDataPermissionDao.create(permission);
            }
        }

        //创建子账号角色,后面创建子账号需要用到
        Map<String, Long> roleId = this.createSubRole(primaryUser.getId(), dataSourceId);

        //现在轮到子账号了
        for(View_FarmMember member : list) {
            if(member.getLevels() == 1){
                this.createSubUser(member, roleId, primaryUser.getId(), mobile, farmIds, member.getOID());
            }
        }
    }

    /**
     * 手机注册
     *
     * @param mobile 手机号
     * @param password 密码
     * @param userName 用户名
     * @return 注册成功之后的用户
     */
    private User registerByMobile(String mobile, String password, String userName) {
        Response<User> result = doctorUserReadService.findBy(mobile, LoginType.MOBILE);
        // 检测手机号是否已存在
        if(result.isSuccess() && result.getResult() != null){
            throw new JsonResponseException("user.register.mobile.has.been.used");
        }
        // 设置用户信息
        User user = new User();
        user.setMobile(mobile);
        user.setPassword(password);
        user.setName(userName);

        // 用户状态 0: 未激活, 1: 正常, -1: 锁定, -2: 冻结, -3: 删除
        user.setStatus(UserStatus.NORMAL.value());

        user.setType(UserType.FARM_ADMIN_PRIMARY.value());

        // 注册用户默认成为猪场管理员
        user.setRoles(Lists.newArrayList("PRIMARY", "PRIMARY(OWNER)"));

        Response<Long> resp = userWriteService.create(user);
        if(!resp.isSuccess()){
            throw new JsonResponseException(resp.getError());
        }
        user.setId(resp.getResult());
        return user;
    }

    public Long initDefaultServiceStatus(Long userId){
        DoctorServiceStatus status = new DoctorServiceStatus();
        status.setUserId(userId);

        status.setPigdoctorStatus(DoctorServiceStatus.Status.OPENED.value());
        status.setPigdoctorReviewStatus(DoctorServiceReview.Status.INIT.getValue());

        //电商初始状态
        status.setPigmallStatus(DoctorServiceStatus.Status.BETA.value());
        status.setPigmallReason("敬请期待");
        status.setPigmallReviewStatus(DoctorServiceReview.Status.INIT.getValue());

        //大数据初始状态
        status.setNeverestStatus(DoctorServiceStatus.Status.BETA.value());
        status.setNeverestReason("敬请期待");
        status.setNeverestReviewStatus(DoctorServiceReview.Status.INIT.getValue());

        //猪场软件初始状态
        status.setPigtradeStatus(DoctorServiceStatus.Status.BETA.value());
        status.setPigtradeReason("敬请期待");
        status.setPigtradeReviewStatus(DoctorServiceReview.Status.INIT.getValue());

        return RespHelper.or500(doctorServiceStatusWriteService.createServiceStatus(status));
    }

    private DoctorOrg createOrg(String orgName, String orgMobile, String license, String outId){
        DoctorOrg org = new DoctorOrg();
        org.setName(orgName);
        org.setMobile(orgMobile);
        org.setLicense(license);
        org.setOutId(outId);
        doctorOrgDao.create(org);
        return org;
    }

    private void createStaff(View_FarmMember member, User user, DoctorOrg org, String outId){
        DoctorStaff staff = new DoctorStaff();
        staff.setOrgName(org.getName());
        staff.setOrgId(org.getId());
        staff.setUserId(user.getId());
        staff.setOutId(outId);
        staff.setCreatorId(user.getId());
        staff.setCreatorName(user.getName());
        staff.setUpdatorId(user.getId());
        staff.setUpdatorName(user.getName());
        if(Objects.equals(member.getSex(), "男")){
            staff.setSex(DoctorStaff.Sex.MALE.value());
        }else{
            staff.setSex(DoctorStaff.Sex.FEFMALE.value());
        }
        if(Objects.equals(member.getStatus(), "在职")){
            staff.setStatus(DoctorStaff.Status.PRESENT.value());
        }else{
            staff.setStatus(DoctorStaff.Status.ABSENT.value());
        }
        doctorStaffDao.create(staff);
    }

    private DoctorFarm makeFarm(View_FarmInfo farmInfo){
        DoctorFarm farm = new DoctorFarm();
        farm.setName(farmInfo.getFarmName());
//        farm.setProvinceId();
//        farm.setProvinceName();
//        farm.setCityId();
//        farm.setCityName();
//        farm.setDistrictId();
//        farm.setDistrictName();
//        farm.setDetailAddress();
        farm.setOutId(farmInfo.getFarmOID());
        return farm;
    }

    private Map<String, Long> createSubRole(Long primaryUserId, Long dataSourceId){
        Map<String, Long> roleId = new HashMap<>(); // key = roleName, value = roleId
        List<RoleTemplate> roleTemplates = doctorMoveDatasourceHandler.findAllData(dataSourceId, RoleTemplate.class, DoctorMoveTableEnum.RoleTemplate).getResult();

        SubRole role = new SubRole();
        role.setAppKey("MOBILE");
        role.setStatus(1);
        role.setUserId(primaryUserId);
        role.setAllowJson("[\"message_user_center\",\"manage_user_info\",\"manage_user_changepwd\"]");
        for(RoleTemplate roleTemplate : roleTemplates){
            role.setName(roleTemplate.getRoleName());
            subRoleDao.create(role);
            roleId.put(roleTemplate.getRoleName(), role.getId());
        }

        return roleId;
    }

    private void createSubUser(View_FarmMember member, Map<String, Long> roleIdMap, Long primaryUserId, String primaryUserMobile, List<Long> farmIds, String staffoutId){
        User subUser = new User();
        subUser.setName(member.getLoginName() + "@" + primaryUserMobile);
        subUser.setPassword("123456");
        subUser.setType(UserType.FARM_SUB.value());
        subUser.setStatus(UserStatus.NORMAL.value());

        List<String> roles = Lists.newArrayList("SUB", "SUB(SUB(" + roleIdMap.get(member.getRoleName()) + "))");
        subUser.setRoles(roles);

        subUser.setExtra(MapBuilder.<String, String>of()
                .put("pid", primaryUserId.toString())
                .put("contact", "")
                .put("realName", member.getOrganizeName())
                .map());
        Long subUserId = RespHelper.or500(userWriteService.create(subUser));

        // 给staff 设置下outId, fuck...
        DoctorStaff staff = doctorStaffDao.findByUserId(subUserId);
        staff.setOutId(staffoutId);
        doctorStaffDao.update(staff);

        //现在是数据权限
        DoctorUserDataPermission permission = new DoctorUserDataPermission();
        permission.setUserId(subUserId);
        permission.setFarmIds(Joiner.on(",").join(farmIds));
        doctorUserDataPermissionDao.create(permission);
    }
}
