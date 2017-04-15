package io.terminus.doctor.move.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Joiners;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.service.DoctorMessageRuleWriteService;
import io.terminus.doctor.move.dto.DoctorFarmWithMobile;
import io.terminus.doctor.move.dto.DoctorMoveFarmInfo;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.move.handler.DoctorMoveTableEnum;
import io.terminus.doctor.move.model.RoleTemplate;
import io.terminus.doctor.move.model.View_FarmInfo;
import io.terminus.doctor.move.model.View_FarmMember;
import io.terminus.doctor.move.util.ImportExcelUtils;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.dao.PrimaryUserDao;
import io.terminus.doctor.user.dao.SubDao;
import io.terminus.doctor.user.dao.SubRoleDao;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.model.SubRole;
import io.terminus.doctor.user.service.DoctorServiceReviewReadService;
import io.terminus.doctor.user.service.DoctorServiceReviewWriteService;
import io.terminus.doctor.user.service.DoctorServiceStatusWriteService;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.user.service.SubRoleWriteService;
import io.terminus.parana.common.utils.EncryptUtil;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.address.model.Address;
import io.terminus.parana.user.impl.address.dao.AddressDao;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserWriteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.*;

/**
 * Created by chenzenghui on 16/8/3.
 */

@Slf4j
@Service
public class UserInitService {

    @Autowired
    private DoctorUserReadService doctorUserReadService;
    @Autowired
    private UserWriteService<User> userWriteService;
    @Autowired
    private DoctorServiceReviewWriteService doctorServiceReviewWriteService;
    @Autowired
    private DoctorServiceReviewReadService doctorServiceReviewReadService;
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
    @Autowired
    private SubDao subDao;
    @Autowired
    private AddressDao addressDao;
    @Autowired
    private PrimaryUserDao primaryUserDao;
    @Autowired
    private DoctorMessageRuleWriteService doctorMessageRuleWriteService;
    @Autowired
    private DoctorBarnDao doctorBarnDao;
    @Autowired
    private SubRoleWriteService subRoleWriteService;

    @Transactional
    public List<DoctorFarmWithMobile> init(String loginName, String mobile, Long dataSourceId, Sheet sheet){
        List<DoctorMoveFarmInfo> moveFarmInfoList = analyzeExcelForFarmInfo(sheet);
        log.info("===moveFarmInfoList:{}", moveFarmInfoList);

        List<String> includeFarmList = moveFarmInfoList.stream().map(DoctorMoveFarmInfo::getOldFarmName).collect(Collectors.toList());
        Map<String, String> farmNameMap = moveFarmInfoList.stream().collect(Collectors.toMap(k -> k.getOldFarmName(), v -> v.getNewFarmName()));

        List<View_FarmMember> allList = getFarmMember(dataSourceId);
        View_FarmMember admin = allList.stream().filter(farmMember -> farmMember.getLevels() == 0 && "admin".equals(farmMember.getLoginName())).collect(Collectors.toList()).get(0);
        log.info("===admin:{}", admin);
        List<View_FarmMember> list = allList.stream()
                .filter(view_farmMember -> includeFarmList.contains(view_farmMember.getFarmName()))
                .collect(Collectors.toList());
        log.info("===list{}", list);
        checkFarmNameRepeat(list);

        List<DoctorFarm> farms = new ArrayList<>();
        List<View_FarmInfo> allViewFarmInfoList = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, View_FarmInfo.class, DoctorMoveTableEnum.view_FarmInfo));
        log.info("===allViewFarmInfoList:{}", allViewFarmInfoList);
        allViewFarmInfoList.stream().filter(view_farmInfo -> includeFarmList.contains(view_farmInfo.getFarmName())).forEach(farmInfo -> {
            if (farmInfo.getLevels() == 1) {
                DoctorFarm doctorFarm = makeFarm(farmInfo);
                doctorFarm.setName(farmNameMap.get(doctorFarm.getName()));
                farms.add(doctorFarm);
            }
        });
        log.info("===farms:{}", farms);
        //猪场名称与猪场映射
        Map<String, DoctorFarm> nameFarmMap = farms.stream().collect(Collectors.toMap(k -> k.getName(), v ->v));
        log.info("===nameFarmMap:{}", nameFarmMap);

        List<DoctorFarmWithMobile> farmList = Lists.newArrayList();
        //创建org
        DoctorOrg org = doctorOrgDao.findByName(moveFarmInfoList.get(0).getOrgName());
        if (isNull(org)) {
            org = this.createOrg(moveFarmInfoList.get(0).getOrgName(), admin.getMobilPhone(), null, admin.getOID());
        }

        Address province = addressDao.findByName(moveFarmInfoList.get(0).getProvince());
        Address city = addressDao.findByName(moveFarmInfoList.get(0).getCity());;
        Address region = addressDao.findByName(moveFarmInfoList.get(0).getRegion());
        String detailAddress = moveFarmInfoList.get(0).getProvince();
        for (DoctorMoveFarmInfo farmInfo : moveFarmInfoList) {
            log.info("===farmInfo:{}", farmInfo);
            // 主账号注册,内含事务
            User primaryUser;
            Response<User> result = doctorUserReadService.findBy(farmInfo.getMobile(), LoginType.MOBILE);
            if(result.isSuccess() && result.getResult() != null){
                primaryUser = result.getResult();
                primaryUser.setName(farmInfo.getLoginName());
                primaryUser.setPassword(EncryptUtil.encrypt("123456"));
                primaryUser.setStatus(UserStatus.NORMAL.value());
                primaryUser.setType(UserType.FARM_ADMIN_PRIMARY.value());
                primaryUser.setRoles(Lists.newArrayList("PRIMARY", "PRIMARY(OWNER)"));
                Map<String, String> userExtraMap = Maps.newHashMap();
                userExtraMap.put("realName", farmInfo.getRealName());
                primaryUser.setExtra(userExtraMap);
                userWriteService.update(primaryUser);
            } else {
                primaryUser = this.registerByMobile(farmInfo.getMobile(), "123456", farmInfo.getLoginName(), farmInfo.getRealName());
                //初始化服务状态
                this.initDefaultServiceStatus(primaryUser.getId());
                //初始化服务的申请审批状态
                this.initServiceReview(primaryUser.getId(), primaryUser.getMobile(), primaryUser.getName());
            }
            Long userId = primaryUser.getId();
            DoctorFarm farm = nameFarmMap.get(farmInfo.getNewFarmName());
            log.info("===farm:{}", farm);
            //创建猪场
            farm.setFarmCode(farmInfo.getLoginName());
            farm.setOrgId(org.getId());
            farm.setOrgName(org.getName());
            farm.setProvinceId(province.getId());
            farm.setProvinceName(province.getName());
            farm.setCityId(city.getId());
            farm.setCityName(city.getName());
            farm.setDistrictId(region.getId());
            farm.setDistrictName(region.getName());
            farm.setDetailAddress(detailAddress);
            doctorFarmDao.create(farm);
            farmList.add(new DoctorFarmWithMobile(farm, primaryUser.getMobile()));

            //创建staff
            //this.createStaff(primaryUser, farm, farm.getOutId());

            RespHelper.or500(doctorMessageRuleWriteService.initTemplate(farm.getId()));

            //主账户关联猪场id
            PrimaryUser primary = primaryUserDao.findByUserId(userId);
            PrimaryUser updatePrimary = new PrimaryUser();
            updatePrimary.setId(primary.getId());
            updatePrimary.setRelFarmId(farm.getId());
            primaryUserDao.update(updatePrimary);

            //创建数据权限
            DoctorUserDataPermission permission = new DoctorUserDataPermission();
            permission.setUserId(userId);
            permission.setFarmIds(farm.getId().toString());
            permission.setOrgIds(org.getId().toString());
            doctorUserDataPermissionDao.create(permission);

            //创建子账号角色,后面创建子账号需要用到
            Map<String, Long> roleId = this.createSubRole(farm.getId(), primaryUser.getId(), dataSourceId);
            //现在轮到子账号了
            for (View_FarmMember member : list) {
                if(member.getLevels() == 1
                        && Objects.equals(member.getFarmName(), farmInfo.getOldFarmName())
                        && (!Objects.equals(member.getMobilPhone(), primaryUser.getMobile()))) {
                    this.createSubUser(member, roleId, primaryUser.getId(), primaryUser.getMobile(), farm.getId(), farm.getOutId());
                }
            }
        }
        log.info("===farmList:{}", farmList);
        return farmList;
    }

    public List<View_FarmMember> getFarmMember(Long datasourceId) {
        return doctorMoveDatasourceHandler.findAllData(datasourceId, View_FarmMember.class, DoctorMoveTableEnum.view_FarmMember).getResult();
    }

    /**
     * 解析Excel获取猪场信息
     * @param sheet
     * @return
     */
    private List<DoctorMoveFarmInfo> analyzeExcelForFarmInfo(Sheet sheet) {
        List<DoctorMoveFarmInfo> infoList = Lists.newArrayList();
        for (Row row : sheet) {
            if (row.getRowNum() > 0 && notEmpty(ImportExcelUtils.getString(row, 0))) {
                DoctorMoveFarmInfo moveFarmInfo = DoctorMoveFarmInfo.builder()
                        .oldFarmName(ImportExcelUtils.getString(row, 1))
                        .orgName(ImportExcelUtils.getString(row, 2))
                        .newFarmName(ImportExcelUtils.getString(row, 3))
                        .loginName(ImportExcelUtils.getString(row, 4))
                        .mobile(ImportExcelUtils.getString(row, 5))
                        .realName(ImportExcelUtils.getString(row, 6))
                        .province(ImportExcelUtils.getString(row, 7))
                        .city(ImportExcelUtils.getString(row, 8))
                        .region(ImportExcelUtils.getString(row, 9))
                        .address(ImportExcelUtils.getString(row, 10))
                        .build();
                infoList.add(moveFarmInfo);

            }
        }
        return infoList;
    }
    //校验猪场名称重复
    private void checkFarmNameRepeat(List<View_FarmMember> farmList) {
        List<String> farmNames = doctorFarmDao.findAll().stream().map(DoctorFarm::getName).collect(Collectors.toList());
        for (View_FarmMember member : farmList) {
            if (farmNames.contains(member.getFarmName())) {
                throw new ServiceException("farm.name.repeat");
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
    private User registerByMobile(String mobile, String password, String userName, String realName) {
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
        Map<String, String> userExtraMap = Maps.newHashMap();
        userExtraMap.put("realName", realName);
        user.setExtra(userExtraMap);

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
        status.setPigdoctorReviewStatus(DoctorServiceReview.Status.OK.getValue());

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

    private void createStaff(User user, DoctorFarm farm, String outId){
        DoctorStaff staff = new DoctorStaff();
        staff.setFarmId(farm.getId());
        staff.setUserId(user.getId());
        //if(Objects.equals(member.getStatus(), "在职")){
            staff.setStatus(DoctorStaff.Status.PRESENT.value());
//        }else{
//            staff.setStatus(DoctorStaff.Status.ABSENT.value());
//        }
        doctorStaffDao.create(staff);
    }

    private DoctorFarm makeFarm(View_FarmInfo farmInfo){
        DoctorFarm farm = new DoctorFarm();
        farm.setName(farmInfo.getFarmName());
        farm.setOutId(farmInfo.getFarmOID());
        return farm;
    }

    private Map<String, Long> createSubRole(Long farmId, Long primaryUserId, Long dataSourceId){
        final String appKey = "MOBILE";
        RespHelper.or500(subRoleWriteService.initDefaultRoles(appKey, primaryUserId, farmId));
        // key = roleName, value = roleId
        Map<String, Long> existRole = subRoleDao.findByFarmIdAndStatus(appKey, farmId, 1).stream().collect(Collectors.toMap(SubRole::getName, SubRole::getId));

        List<RoleTemplate> roleTemplates = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, RoleTemplate.class, DoctorMoveTableEnum.RoleTemplate));

        SubRole role = new SubRole();
        role.setAppKey(appKey);
        role.setStatus(1);
        role.setUserId(primaryUserId);
        role.setAllowJson("[\"message_user_center\",\"manage_user_info\",\"manage_user_changepwd\"]");
        for(RoleTemplate roleTemplate : roleTemplates){
            if(!existRole.containsKey(roleTemplate.getRoleName())){
                role.setName(roleTemplate.getRoleName());
                role.setFarmId(farmId);
                subRoleDao.create(role);
                existRole.put(roleTemplate.getRoleName(), role.getId());
            }
        }

        return existRole;
    }

    private void createSubUser(View_FarmMember member, Map<String, Long> roleIdMap, Long primaryUserId, String primaryUserMobile, Long farmId, String staffoutId){
        log.info("createSubUser:member:{}, primaryUserId:{}, farm:{}", member, primaryUserId, farmId);
        User subUser;
        DoctorFarm farm = doctorFarmDao.findById(farmId);
        String name = member.getLoginName() + "@" + farm.getFarmCode();

        Response<User> result = doctorUserReadService.findBy(member.getMobilPhone(), LoginType.MOBILE);
        Response<User> userResponse = doctorUserReadService.findBy(name, LoginType.NAME);
        if(result.isSuccess() && result.getResult() != null) {
            subUser = result.getResult();
        } else if(userResponse.isSuccess() && userResponse.getResult() != null) {
            subUser = userResponse.getResult();
        } else {
            subUser = new User();
        }
        subUser.setName(name);
        subUser.setMobile(member.getMobilPhone());
        subUser.setPassword(EncryptUtil.encrypt("123456"));
        subUser.setType(UserType.FARM_SUB.value());
        if (Objects.equals(member.getIsStopUse(), "true")) {
            subUser.setStatus(UserStatus.LOCKED.value());
        } else {
            subUser.setStatus(UserStatus.NORMAL.value());
        }

        List<String> roles = Lists.newArrayList("SUB", "SUB(SUB(" + roleIdMap.get(member.getRoleName()) + "))");
        subUser.setRoles(roles);

        subUser.setExtra(MapBuilder.<String, String>of()
                .put("pid", primaryUserId.toString())
                .put("contact", member.getMobilPhone())
                .put("realName", member.getOrganizeName())
                .map());
        log.info("subUser:{}", subUser);
        if(notNull(subUser.getId())) {
            userWriteService.update(subUser);
        } else {
            userWriteService.create(subUser);
        }
        Long subUserId = subUser.getId();
        // 设置下子账号的状态和关联猪场
        Sub sub = subDao.findByUserId(subUserId);
        sub.setFarmId(farmId);
        if(Objects.equals(member.getIsStopUse(), "true")){
            sub.setStatus(Sub.Status.ABSENT.value());
        }
        subDao.update(sub);

        //现在是数据权限
        DoctorUserDataPermission permission = new DoctorUserDataPermission();
        permission.setUserId(subUserId);
        permission.setFarmIds(farmId.toString());
        permission.setOrgIds(farm.getOrgId().toString());
        doctorUserDataPermissionDao.create(permission);
    }

    public void initServiceReview(Long userId, String mobile, String realName){
        RespHelper.or500(doctorServiceReviewWriteService.initServiceReview(userId, mobile, realName));
        DoctorServiceReview review = RespHelper.or500(doctorServiceReviewReadService.findServiceReviewByUserIdAndType(userId, DoctorServiceReview.Type.PIG_DOCTOR));
        review.setStatus(DoctorServiceReview.Status.OK.getValue());
        RespHelper.or500(doctorServiceReviewWriteService.updateReview(review));
    }

    /**
     * 把所有猪舍添加到所有用户的权限里去
     *
     * @param farmId 猪场id
     */
    @Transactional
    public void updatePermissionBarn(Long farmId) {
        List<Long> barnIdList = doctorBarnDao.findByFarmId(farmId).stream().map(DoctorBarn::getId).collect(Collectors.toList());
        List<DoctorUserDataPermission> permissionList = doctorUserDataPermissionDao.findByFarmId(farmId);

        permissionList.forEach(permission -> {
            List<Long> barnIds = permission.getBarnIdsList();
            if (isNull(barnIds)) {
                barnIds = Lists.newArrayList();
            }
            barnIds.addAll(barnIdList);
            permission.setBarnIds(Joiners.COMMA.join(Sets.newHashSet(barnIds)));
            doctorUserDataPermissionDao.update(permission);
        });
    }
}
