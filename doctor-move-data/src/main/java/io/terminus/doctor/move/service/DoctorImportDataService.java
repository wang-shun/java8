package io.terminus.doctor.move.service;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Joiners;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.dao.DoctorBasicMaterialDao;
import io.terminus.doctor.basic.dao.DoctorChangeReasonDao;
import io.terminus.doctor.basic.dao.DoctorFarmBasicDao;
import io.terminus.doctor.basic.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorFarmBasic;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.service.DoctorMaterialInWareHouseWriteService;
import io.terminus.doctor.basic.service.DoctorWareHouseTypeWriteService;
import io.terminus.doctor.basic.service.DoctorWareHouseWriteService;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.constants.DoctorFarmEntryConstants;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.dto.event.sow.DoctorWeanDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.enums.BoarEntryType;
import io.terminus.doctor.event.enums.DoctorMatingType;
import io.terminus.doctor.event.enums.FarrowingType;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorMessageRuleWriteService;
import io.terminus.doctor.event.service.DoctorPigTypeStatisticWriteService;
import io.terminus.doctor.move.dto.DoctorImportSheet;
import io.terminus.doctor.move.dto.DoctorImportSow;
import io.terminus.doctor.move.util.ImportExcelUtils;
import io.terminus.doctor.user.dao.DoctorAddressDao;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.dao.DoctorServiceReviewDao;
import io.terminus.doctor.user.dao.DoctorServiceStatusDao;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.dao.PrimaryUserDao;
import io.terminus.doctor.user.dao.SubDao;
import io.terminus.doctor.user.dao.SubRoleDao;
import io.terminus.doctor.user.dao.UserDaoExt;
import io.terminus.doctor.user.interfaces.event.DoctorSystemCode;
import io.terminus.doctor.user.interfaces.event.EventType;
import io.terminus.doctor.user.interfaces.model.UserDto;
import io.terminus.doctor.user.manager.UserInterfaceManager;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.model.SubRole;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.user.service.SubRoleWriteService;
import io.terminus.parana.user.address.model.Address;
import io.terminus.parana.user.impl.dao.UserProfileDao;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.model.UserProfile;
import io.terminus.parana.user.service.UserWriteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isEmpty;
import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/10/19
 */
@Slf4j
@Service
public class DoctorImportDataService {
    private static final JsonMapper MAPPER = JsonMapper.nonEmptyMapper();

    //拥有所有权限的用户id
    @Value("${xrnm.auth.user.id: 0}")
    private Long xrnmId;

    @Autowired
    private DoctorBarnDao doctorBarnDao;
    @Autowired
    private DoctorBasicDao doctorBasicDao;
    @Autowired
    private DoctorPigDao doctorPigDao;
    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorGroupDao doctorGroupDao;
    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired
    private DoctorMoveBasicService doctorMoveBasicService;
    @Autowired
    private DoctorOrgDao doctorOrgDao;
    @Autowired
    private DoctorFarmDao doctorFarmDao;
    @Autowired
    private DoctorUserReadService doctorUserReadService;
    @Autowired
    private UserWriteService<User> userWriteService;
    @Autowired
    private UserProfileDao userProfileDao;
    @Autowired
    private UserInitService userInitService;
    @Autowired
    private DoctorStaffDao doctorStaffDao;
    @Autowired
    private DoctorUserDataPermissionDao doctorUserDataPermissionDao;
    @Autowired
    private DoctorMessageRuleWriteService doctorMessageRuleWriteService;
    @Autowired
    private SubRoleWriteService subRoleWriteService;
    @Autowired
    private SubRoleDao subRoleDao;
    @Autowired
    private SubDao subDao;
    @Autowired
    private PrimaryUserDao primaryUserDao;
    @Autowired
    private DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService;
    @Autowired
    private DoctorWareHouseTypeWriteService doctorWareHouseTypeWriteService;
    @Autowired
    private DoctorFarmBasicDao doctorFarmBasicDao;
    @Autowired
    private DoctorChangeReasonDao doctorChangeReasonDao;
    @Autowired
    private DoctorBasicMaterialDao basicMaterialDao;
    @Autowired
    private DoctorMaterialInWareHouseWriteService doctorMaterialInWareHouseWriteService;
    @Autowired
    private DoctorWareHouseWriteService doctorWareHouseWriteService;
    @Autowired
    private UserInterfaceManager userInterfaceManager;
    @Autowired
    private DoctorServiceStatusDao doctorServiceStatusDao;
    @Autowired
    private DoctorServiceReviewDao doctorServiceReviewDao;
    @Autowired
    private DoctorGroupEventDao doctorGroupEventDao;
    @Autowired
    private DoctorAddressDao addressDao;
    @Autowired
    private UserDaoExt userDaoExt;
    @Autowired
    private DoctorBasicMaterialDao doctorBasicMaterialDao;
    @Autowired
    private DoctorMoveDataService doctorMoveDataService;

    /**
     * 根据shit导入所有的猪场数据
     */
    @Transactional
    public DoctorFarm importAll(DoctorImportSheet shit) {
        DoctorFarm farm = null;
        try{
            // 猪场和员工
            Object[] result = this.importOrgFarmUser(shit.getFarm(), shit.getStaff());
            User primaryUser = (User) result[0];
            farm = (DoctorFarm) result[1];
            Map<String, Long> userMap = doctorMoveBasicService.getSubMap(farm.getOrgId());

            importBarn(farm, userMap, shit.getBarn());
            //把所有猪舍添加到所有用户的权限里去
            userInitService.updatePermissionBarn(primaryUser.getMobile());

            importBreed(shit.getBreed());

            Map<String, DoctorBarn> barnMap = doctorMoveBasicService.getBarnMap2(farm.getId());
            Map<String, Long> breedMap = doctorMoveBasicService.getBreedMap();

            importBoar(farm, barnMap, breedMap, shit.getBoar());
            importGroup(farm, barnMap, shit.getGroup());
            importSow(farm, barnMap, breedMap, shit.getSow());

            //刷新npd
            doctorMoveDataService.flushNpd(farm.getId());

            //首页统计数据
            movePigTypeStatistic(farm);

            //最后仓库数据
            importWarehouse(farm, shit, primaryUser, userMap);

            //猪场基础数据
            importFarmBasics(farm.getId());
            return farm;
        } catch(Exception e) {
            // 导入猪场失败，需要删除一些数据
            deleteUser(farm);
            throw e;
        }
    }

    //猪场基础数据权限 默认全部
    public void importFarmBasics(Long farmId) {
        List<Long> basicIds = doctorBasicDao.list(Maps.newHashMap()).stream().map(DoctorBasic::getId).collect(Collectors.toList());
        List<Long> reasonIds = doctorChangeReasonDao.list(Maps.newHashMap()).stream().map(DoctorChangeReason::getId).collect(Collectors.toList());
        List<Long> materialIds = doctorBasicMaterialDao.list(MapBuilder.<String, Integer>of().put("isValid", 1).map()).stream().map(DoctorBasicMaterial::getId).collect(Collectors.toList());
        DoctorFarmBasic farmBasic = new DoctorFarmBasic();
        farmBasic.setFarmId(farmId);
        farmBasic.setBasicIds(Joiners.COMMA.join(basicIds));
        farmBasic.setReasonIds(Joiners.COMMA.join(reasonIds));
        farmBasic.setMaterialIds(Joiners.COMMA.join(materialIds));
        doctorFarmBasicDao.create(farmBasic);
    }

    //统计下首页数据
    private void movePigTypeStatistic(DoctorFarm farm) {
        doctorPigTypeStatisticWriteService.statisticGroup(farm.getOrgId(), farm.getId());
        doctorPigTypeStatisticWriteService.statisticPig(farm.getOrgId(), farm.getId(), DoctorPig.PigSex.BOAR.getKey());
        doctorPigTypeStatisticWriteService.statisticPig(farm.getOrgId(), farm.getId(), DoctorPig.PigSex.SOW.getKey());
    }

    private void deleteUser(DoctorFarm farm){
        if(farm != null && farm.getOrgId() != null){
            List<DoctorUserDataPermission> permissions = doctorUserDataPermissionDao.findByOrgId(farm.getOrgId());
            permissions.forEach(permission -> {
                // 向用户中心专用 zk 发送删除用户的消息
                try {
                    UserDto dto = new UserDto(permission.getUserId());
                    userInterfaceManager.pulishZkEvent(dto, EventType.DELETE, DoctorSystemCode.PIG_DOCTOR);
                } catch (Exception e) {
                    log.error("导入猪场失败，且无法向用户中心专用 zk 发送删除用户的消息，请务必手动处理，farm={}, userId={}", farm, permission.getUserId());
                }
            });
        }
    }

    @Transactional
    private Object[] importOrgFarmUser(Sheet farmShit, Sheet staffShit) {
        Object[] result = this.importOrgFarm(farmShit);
        User primaryUser = (User) result[0];
        DoctorFarm farm = (DoctorFarm) result[1];
        this.importStaff(staffShit, primaryUser, farm);
        return result;
    }

    private void importStaff(Sheet staffShit, User primaryUser, DoctorFarm farm) {
        final String appKey = "MOBILE";
        List<SubRole> existRoles = subRoleDao.findByUserIdAndStatus(appKey, primaryUser.getId(), 1);
        if(existRoles.isEmpty()){
            RespHelper.or500(subRoleWriteService.initDefaultRoles(appKey, primaryUser.getId()));
            existRoles = subRoleDao.findByUserIdAndStatus(appKey, primaryUser.getId(), 1);
        }
        // key = roleName, value = roleId
        Map<String, Long> existRole = existRoles.stream().collect(Collectors.toMap(SubRole::getName, SubRole::getId));

        List<String> existSubName = subDao.findByParentUserId(primaryUser.getId()).stream().map(Sub::getRealName).collect(Collectors.toList());

        // 主账号的 staff 信息
        String farmIds = Joiner.on(",").join(doctorFarmDao.findByOrgId(farm.getOrgId()).stream().map(DoctorFarm::getId).collect(Collectors.toList()));

        for(Row row : staffShit){
            if(canImport(row)){
                String realName = ImportExcelUtils.getStringOrThrow(row, 0);
                String loginName = ImportExcelUtils.getStringOrThrow(row, 1);
                String contact = ImportExcelUtils.getStringOrThrow(row, 2);
                String roleName = ImportExcelUtils.getStringOrThrow(row, 3);

                // 用 realName 判断下这个员工是否已存在
                if(existSubName.contains(realName)){
                    log.warn("staff {} has existed", realName);
                    continue;
                }

                User subUser = new User();
                subUser.setName(loginName + "@" + farm.getFarmCode());
                subUser.setMobile(contact);
                subUser.setPassword("123456");
                subUser.setType(UserType.FARM_SUB.value());
                subUser.setStatus(UserStatus.NORMAL.value());

                if(existRole.get(roleName) == null){
                    SubRole subRole = new SubRole();
                    subRole.setName(roleName);
                    subRole.setUserId(primaryUser.getId());
                    subRole.setFarmId(farm.getId());
                    subRole.setAppKey(appKey);
                    subRole.setStatus(1);
                    subRole.setAllowJson("[]");
                    subRole.setExtraJson("{}");
                    subRoleDao.create(subRole);
                    existRole.put(roleName, subRole.getId());
                }
                List<String> roles = Lists.newArrayList("SUB", "SUB(SUB(" + existRole.get(roleName) + "))");
                subUser.setRoles(roles);

                subUser.setExtra(MapBuilder.<String, String>of()
                        .put("pid", primaryUser.getId().toString())
                        .put("contact", contact)
                        .put("realName", realName)
                        .map());
                Long subUserId = RespHelper.or500(userWriteService.create(subUser));
                //设置子账号关联猪场
                io.terminus.doctor.user.model.Sub sub = subDao.findByUserId(subUserId);
                io.terminus.doctor.user.model.Sub updateSub = new io.terminus.doctor.user.model.Sub();
                updateSub.setId(sub.getId());
                updateSub.setFarmId(farm.getId());
                subDao.update(updateSub);

                // 创建子账号员工
                this.createStaff(subUser, farm);

                //现在是数据权限
                DoctorUserDataPermission permission = new DoctorUserDataPermission();
                permission.setUserId(subUserId);
                permission.setFarmIds(farmIds);
                permission.setOrgIds(farm.getOrgId().toString());
                doctorUserDataPermissionDao.create(permission);
            }
        }
    }

    private Integer getAddressId(String name, Integer pid) {
        Address address = addressDao.findByNameAndPid(name, pid);
        if (address == null) {
            throw new JsonResponseException("猪场地址错误（" + name + "）");
        }
        return address.getId();
    }

    /**
     * 公司、猪场、主账号
     * @return 主账号的 user
     */
    @Transactional
    private Object[] importOrgFarm(Sheet farmShit){
        Row row1 = farmShit.getRow(1);
        String orgName = ImportExcelUtils.getStringOrThrow(row1, 0);
        String farmName = ImportExcelUtils.getStringOrThrow(row1, 1);
        String loginName = ImportExcelUtils.getStringOrThrow(row1, 2);
        String mobile = ImportExcelUtils.getStringOrThrow(row1, 3);
        String realName = ImportExcelUtils.getStringOrThrow(row1, 4);
        String province = ImportExcelUtils.getStringOrThrow(row1, 5);
        String city = ImportExcelUtils.getStringOrThrow(row1, 6);
        String district = ImportExcelUtils.getStringOrThrow(row1, 7);
        String detail = ImportExcelUtils.getStringOrThrow(row1, 8);
        String companyMobile = ImportExcelUtils.getString(row1, 9); //集团账号手机号

        // 公司
        DoctorOrg org = doctorOrgDao.findByName(orgName);
        if(org == null){
            org = new DoctorOrg();
            org.setName(orgName);
            org.setMobile(mobile);
            doctorOrgDao.create(org);
        }else{
            log.warn("org {} has existed, id = {}", orgName, org.getId());
        }

        // 猪场
        DoctorFarm farm = doctorFarmDao.findByOrgId(org.getId()).stream().filter(f -> farmName.equals(f.getName())).findFirst().orElse(null);
        if(farm == null){
            farm = new DoctorFarm();
            farm.setOrgId(org.getId());
            farm.setOrgName(org.getName());
            farm.setName(farmName);
            farm.setFarmCode(loginName);
            farm.setProvinceId(getAddressId(province, 1));
            farm.setProvinceName(province);
            farm.setCityId(getAddressId(city, farm.getProvinceId()));
            farm.setCityName(city);
            farm.setDistrictId(getAddressId(district, farm.getCityId()));
            farm.setDistrictName(district);
            farm.setDetailAddress(detail);
            doctorFarmDao.create(farm);
            RespHelper.or500(doctorMessageRuleWriteService.initTemplate(farm.getId()));
        }else{
            log.warn("farm {} has existed, id = {}", farmName, farm.getId());
            throw new JsonResponseException("farm.has.been.existed");
        }

        // 主账号
        User user;
        Long userId;
        Response<User> result = doctorUserReadService.findBy(mobile, LoginType.MOBILE);
        if(result.isSuccess() && result.getResult() != null){
            log.warn("primary user has existed, mobile={}", mobile);
            user = result.getResult();
            userId = user.getId();
        }else{
            user = new User();
            user.setMobile(mobile);
            user.setPassword("123456");
            user.setName(loginName);
            user.setStatus(UserStatus.NORMAL.value());
            user.setType(UserType.FARM_ADMIN_PRIMARY.value());
            user.setRoles(Lists.newArrayList("PRIMARY", "PRIMARY(OWNER)"));
            user.getExtra().put("realName", realName);
            userId = RespHelper.or500(userWriteService.create(user));
            user.setId(userId);

            //主账户关联猪场id
            PrimaryUser primaryUser = primaryUserDao.findByUserId(userId);
            PrimaryUser updatePrimary = new PrimaryUser();
            updatePrimary.setId(primaryUser.getId());
            updatePrimary.setRelFarmId(farm.getId());
            primaryUserDao.update(updatePrimary);

            // 把真实姓名存进 user profile
            UserProfile userProfile = userProfileDao.findByUserId(userId);
            userProfile.setRealName(realName);
            userProfileDao.update(userProfile);
        }

        DoctorUserDataPermission permission = doctorUserDataPermissionDao.findByUserId(userId);
        if(permission == null){
            //创建数据权限
            permission = new DoctorUserDataPermission();
            permission.setUserId(userId);
            permission.setFarmIds(farm.getId().toString());
            permission.setOrgIdsList(Lists.newArrayList(org.getId()));
            doctorUserDataPermissionDao.create(permission);
        }else if(permission.getFarmIdsList() == null || !permission.getFarmIdsList().contains(farm.getId())){
            permission.setFarmIds(permission.getFarmIds() + "," + farm.getId());
            doctorUserDataPermissionDao.update(permission);
        }

        //admin的数据权限
        createOrUpdateAdminPermission();

        //集团账号的数据权限
        createOrUpdateMultiPermission(companyMobile, org.getId(), farm.getId());

        if(doctorStaffDao.findByFarmIdAndUserId(farm.getId(), userId) == null){
            // 主账号的staff
            this.createStaff(user, farm);
        }

        DoctorServiceStatus serviceStatus = doctorServiceStatusDao.findByUserId(userId);
        if(serviceStatus == null){
            //初始化服务状态
            userInitService.initDefaultServiceStatus(userId);
        }else{
            serviceStatus.setPigdoctorStatus(1);
            doctorServiceStatusDao.update(serviceStatus);
        }

        DoctorServiceReview review = doctorServiceReviewDao.findByUserIdAndType(userId, DoctorServiceReview.Type.PIG_DOCTOR);
        if(review == null){
            //初始化服务的申请审批状态
            userInitService.initServiceReview(userId, mobile, user.getName());
        }else{
            review.setStatus(DoctorServiceReview.Status.OK.getValue());
            doctorServiceReviewDao.update(review);
        }

        return new Object[]{user, farm};
    }

    //admin的数据权限
    public void createOrUpdateAdminPermission() {
        User user = userDaoExt.findById(xrnmId);
        if (user == null) {
            return;
        }

        String orgIds = Joiners.COMMA.join(doctorOrgDao.findAll().stream().map(DoctorOrg::getId).collect(Collectors.toList()));
        String farmIds = Joiners.COMMA.join(doctorFarmDao.findAll().stream().map(DoctorFarm::getId).collect(Collectors.toList()));
        DoctorUserDataPermission permission = doctorUserDataPermissionDao.findByUserId(user.getId());
        if (permission == null) {
            permission = new DoctorUserDataPermission();
            permission.setUserId(user.getId());
            permission.setOrgIds(orgIds);
            permission.setFarmIds(farmIds);
            doctorUserDataPermissionDao.create(permission);
        } else {
            permission.setOrgIds(orgIds);
            permission.setFarmIds(farmIds);
            doctorUserDataPermissionDao.update(permission);
        }
    }

    //集团账号数据权限
    private void createOrUpdateMultiPermission(String mobile, Long orgId, Long farmId) {
        if (isEmpty(mobile)) {
            return;
        }
        User user = userDaoExt.findByMobile(mobile);
        if (user == null) {
            log.error("createOrUpdateMultiPermission error, mobile({}) not found", mobile);
            throw new JsonResponseException("集团账号手机号(" + mobile + ")未找到，请检查");
        }
        DoctorUserDataPermission permission = doctorUserDataPermissionDao.findByUserId(user.getId());
        if (permission == null) {
            log.error("createOrUpdateMultiPermission error, data permission not found, user:{}", user);
            throw new JsonResponseException("集团账号手机号(" + mobile + ")没有关联猪场，请检查");
        }
        permission.setOrgIds(permission.getOrgIds() + "," + orgId);
        permission.setFarmIds(permission.getFarmIds() + "," + farmId);
        doctorUserDataPermissionDao.update(permission);
    }


    private void createStaff(User user, DoctorFarm farm) {
        DoctorStaff staff = new DoctorStaff();
        staff.setFarmId(farm.getId());
        staff.setUserId(user.getId());
        staff.setStatus(DoctorStaff.Status.PRESENT.value());
        doctorStaffDao.create(staff);
    }

    /**
     * 导入猪舍
     */
    @Transactional
    private void importBarn(DoctorFarm farm, Map<String, Long> userMap, Sheet shit) {
        List<DoctorBarn> barns = Lists.newArrayList();
        shit.forEach(row -> {
            //第一行是表头，跳过
            if (canImport(row)) {
                DoctorBarn barn = new DoctorBarn();
                barn.setName(ImportExcelUtils.getString(row, 0));
                barn.setOrgId(farm.getOrgId());
                barn.setOrgName(farm.getOrgName());
                barn.setFarmId(farm.getId());
                barn.setFarmName(farm.getName());

                String barnTypeXls = ImportExcelUtils.getString(row, 1);
                PigType pigType = PigType.from(barnTypeXls);
                if (pigType != null) {
                    barn.setPigType(pigType.getValue());
                } else if ("后备母猪".equals(barnTypeXls) || "后备公猪".equals(barnTypeXls)) {
                    barn.setPigType(PigType.RESERVE.getValue());
                } else {
                    throw new JsonResponseException("猪舍类型错误：" + barnTypeXls + "，row " + (row.getRowNum() + 1) + "column " + 2);
                }

                barn.setCanOpenGroup(DoctorBarn.CanOpenGroup.YES.getValue());
                barn.setStatus(DoctorBarn.Status.USING.getValue());
                barn.setCapacity(1000);
                barn.setStaffName(ImportExcelUtils.getString(row, 3));
                barn.setStaffId(userMap.get(barn.getStaffName()));
                barn.setExtra(ImportExcelUtils.getString(row, 4));
                barns.add(barn);
            }
        });
        doctorBarnDao.creates(barns);
    }

    @Transactional
    private void importBreed(Sheet shit) {
        List<String> breeds = doctorBasicDao.findByType(DoctorBasic.Type.BREED.getValue()).stream()
                .map(DoctorBasic::getName).collect(Collectors.toList());
        shit.forEach(row -> {
            if (canImport(row)) {
                String breedName = ImportExcelUtils.getString(row, 0);
                if (!breeds.contains(breedName)) {
                    DoctorBasic basic = new DoctorBasic();
                    basic.setName(breedName);
                    basic.setType(DoctorBasic.Type.BREED.getValue());
                    basic.setTypeName(DoctorBasic.Type.BREED.getDesc());
                    basic.setIsValid(1);
                    basic.setSrm(ImportExcelUtils.getString(row, 1));
                    doctorBasicDao.create(basic);
                }
            }
        });
    }

    /**
     * 导入公猪
     */
    @Transactional
    private void importBoar(DoctorFarm farm, Map<String, DoctorBarn> barnMap, Map<String, Long> breedMap, Sheet shit) {
        for (Row row : shit) {
            if (!canImport(row)) {
                continue;
            }

            //公猪
            DoctorPig boar = new DoctorPig();
            boar.setOrgId(farm.getOrgId());
            boar.setOrgName(farm.getOrgName());
            boar.setFarmId(farm.getId());
            boar.setFarmName(farm.getName());
            boar.setPigCode(ImportExcelUtils.getString(row, 1));
            boar.setPigType(DoctorPig.PigSex.BOAR.getKey());
            boar.setIsRemoval(IsOrNot.NO.getValue());
            boar.setPigFatherCode(ImportExcelUtils.getString(row, 4));
            boar.setPigMotherCode(ImportExcelUtils.getString(row, 5));
            PigSource source = PigSource.from(ImportExcelUtils.getString(row, 7));
            if (source != null) {
                boar.setSource(source.getKey());
            }
            boar.setBirthDate(ImportExcelUtils.getDate(row, 3));
            boar.setInFarmDate(ImportExcelUtils.getDate(row, 2));

            if (boar.getBirthDate() == null || boar.getInFarmDate() == null) {
                throw new JsonResponseException("公猪号：" + boar.getPigCode() + " 的日期不能为空，行数：" + (row.getRowNum() + 1));
            }

            boar.setInitBarnName(ImportExcelUtils.getString(row, 0));
            DoctorBarn barn = barnMap.get(boar.getInitBarnName());
            if (barn != null) {
                boar.setInitBarnId(barn.getId());
            }
            boar.setBreedName(ImportExcelUtils.getString(row, 6));
            if(!StringUtils.isBlank(boar.getBreedName())){
                boar.setBreedId(breedMap.get(boar.getBreedName()));
                if(boar.getBreedId() == null){
                    throw new JsonResponseException("公猪品种错误：" + boar.getBreedName() + "，row " + (row.getRowNum() + 1) + "column" + 7);
                }
            }

            //进场默认活公猪
            BoarEntryType entryType = BoarEntryType.from(ImportExcelUtils.getString(row, 8));
            boar.setBoarType(entryType == null ? BoarEntryType.HGZ.getKey() : entryType.getKey());
            doctorPigDao.create(boar);

            //公猪跟踪
            DoctorPigTrack boarTrack = new DoctorPigTrack();
            boarTrack.setFarmId(boar.getFarmId());
            boarTrack.setPigId(boar.getId());
            boarTrack.setPigType(boar.getPigType());
            boarTrack.setStatus(PigStatus.BOAR_ENTRY.getKey());
            boarTrack.setIsRemoval(boar.getIsRemoval());
            boarTrack.setCurrentBarnId(boar.getInitBarnId());
            boarTrack.setCurrentBarnName(boar.getInitBarnName());
            boarTrack.setCurrentBarnType(barn.getPigType());
            boarTrack.setCurrentParity(1);      //配种次数置成1
            doctorPigTrackDao.create(boarTrack);

            //公猪进场事件
            DoctorPigEvent boarEntryEvent = DoctorPigEvent.builder()
                    .orgId(boar.getOrgId())
                    .orgName(boar.getOrgName())
                    .farmId(boar.getFarmId())
                    .farmName(boar.getFarmName())
                    .pigId(boar.getId())
                    .pigCode(boar.getPigCode())
                    .type(PigEvent.ENTRY.getKey())
                    .name(PigEvent.ENTRY.getName())
                    .kind(DoctorPig.PigSex.BOAR.getKey())
                    .isAuto(IsOrNot.YES.getValue())
                    .eventAt(boar.getInFarmDate())
                    .barnId(boar.getInitBarnId())
                    .barnName(boar.getInitBarnName())
                    .creatorId(boar.getCreatorId())
                    .creatorName(boar.getCreatorName())
                    .operatorId(boar.getCreatorId())
                    .operatorName(boar.getCreatorName())
                    .npd(0)
                    .dpnpd(0)
                    .pfnpd(0)
                    .plnpd(0)
                    .psnpd(0)
                    .pynpd(0)
                    .ptnpd(0)
                    .jpnpd(0)
                    .build();
            Map<String, String> fieldMap = Maps.newHashMap();
            if (!Strings.isNullOrEmpty(boar.getBreedName())) {
                fieldMap.put("品种", boar.getBreedName());
            }
            String desc = Joiner.on("#").withKeyValueSeparator("：").join(fieldMap);
            boarEntryEvent.setDesc(desc);
            doctorPigEventDao.create(boarEntryEvent);
        }
    }

    //校验猪群号是否重复
    private void checkGroupCodeExist(Long farmId, String groupCode) {
        List<DoctorGroup> groups = doctorGroupDao.findByFarmId(farmId);
        if (groups.stream().map(DoctorGroup::getGroupCode).collect(Collectors.toList()).contains(groupCode)) {
            throw new ServiceException("猪群号重复（" + groupCode + "）");
        }
    }

    /**
     * 导入猪群
     */
    @Transactional
    private void importGroup(DoctorFarm farm, Map<String, DoctorBarn> barnMap, Sheet shit) {
        List<String> existGroupCode = new ArrayList<>();
        for (Row row : shit) {
            if (!canImport(row)) {
                continue;
            }

            //猪群
            DoctorGroup group = new DoctorGroup();
            group.setOrgId(farm.getOrgId());
            group.setOrgName(farm.getOrgName());
            group.setFarmId(farm.getId());
            group.setFarmName(farm.getName());
            String code = ImportExcelUtils.getString(row, 0);
            group.setGroupCode(code);
            if(existGroupCode.contains(code)){
                throw new JsonResponseException("猪群号（" + code + "）重复");
            }else{
                existGroupCode.add(code);
            }

            Date openAt = ImportExcelUtils.getDate(row, 6);
            if (openAt == null) {
                throw new JsonResponseException("猪群号（" + code + "）建群日期不能为空");
            }

            group.setOpenAt(openAt);  //建群时间
            group.setStatus(DoctorGroup.Status.CREATED.getValue());
            group.setInitBarnName(ImportExcelUtils.getString(row, 1));

            DoctorBarn barn = barnMap.get(group.getInitBarnName());
            if (barn != null) {
                group.setInitBarnId(barn.getId());
                group.setPigType(barn.getPigType());
                group.setStaffId(barn.getStaffId());
                group.setStaffName(barn.getStaffName());
            }else{
                throw new JsonResponseException("找不到猪群（" + group.getGroupCode() + "）所属的猪舍（" + group.getInitBarnName() + "）");
            }
            group.setCurrentBarnId(group.getInitBarnId());
            group.setCurrentBarnName(group.getInitBarnName());
            doctorGroupDao.create(group);

            //猪群跟踪
            DoctorGroupTrack groupTrack = new DoctorGroupTrack();
            groupTrack.setGroupId(group.getId());

            DoctorGroupTrack.Sex sex = DoctorGroupTrack.Sex.from(ImportExcelUtils.getString(row, 2));
            if (sex != null) {
                groupTrack.setSex(sex.getValue());
            }
            groupTrack.setQuantity(ImportExcelUtils.getInt(row, 3));
            groupTrack.setBoarQty(0);
            groupTrack.setSowQty(groupTrack.getQuantity() - groupTrack.getBoarQty());

            //excel日龄是转入时的日龄，所以要重新算一发
            Integer dayAge = MoreObjects.firstNonNull(ImportExcelUtils.getInt(row, 4), 1);
            groupTrack.setAvgDayAge(dayAge + DateUtil.getDeltaDaysAbs(openAt, new Date()));
            groupTrack.setBirthDate(DateTime.now().minusDays(groupTrack.getAvgDayAge()).toDate());

            Double avgWeight = ImportExcelUtils.getDoubleOrDefault(row, 5, 0);

            groupTrack.setBirthWeight(avgWeight * groupTrack.getQuantity());

            //产房仔猪的批次总结字段
            if (PigType.FARROW_TYPES.contains(group.getPigType())) {
                groupTrack.setWeanWeight(groupTrack.getBirthWeight());
                groupTrack.setNest(0);
                groupTrack.setLiveQty(groupTrack.getQuantity());
                groupTrack.setHealthyQty(groupTrack.getQuantity());
                groupTrack.setWeakQty(0);
                groupTrack.setUnweanQty(0);
                groupTrack.setUnqQty(0);
                groupTrack.setWeanQty(groupTrack.getQuantity());    //默认全部断奶
                groupTrack.setQuaQty(groupTrack.getQuantity());
            }
            doctorGroupTrackDao.create(groupTrack);
            createMoveInGroupEvent(group, groupTrack, dayAge, avgWeight);
        }
    }

    /**
     * 创建默认的转入事件
     */
    private void createMoveInGroupEvent(DoctorGroup group, DoctorGroupTrack groupTrack, Integer dayAge, Double avgWeight) {
        DoctorGroupEvent event = new DoctorGroupEvent();
        event.setOrgId(group.getOrgId());
        event.setOrgName(group.getOrgName());
        event.setFarmId(group.getFarmId());
        event.setFarmName(group.getFarmName());
        event.setGroupId(group.getId());
        event.setGroupCode(group.getGroupCode());
        event.setEventAt(group.getOpenAt());
        event.setType(GroupEventType.MOVE_IN.getValue());
        event.setName(GroupEventType.MOVE_IN.getDesc());
        event.setBarnId(group.getInitBarnId());
        event.setBarnName(group.getInitBarnName());
        event.setPigType(group.getPigType());
        event.setQuantity(groupTrack.getQuantity());
        event.setAvgWeight(avgWeight);
        event.setWeight(event.getQuantity() * avgWeight);
        event.setDesc("转移类型：仔猪转入#猪只数：" + groupTrack.getQuantity() + "#平均日龄：" + groupTrack.getAvgDayAge() + "#均重：" + avgWeight);
        event.setAvgDayAge(dayAge);
        event.setIsAuto(IsOrNot.YES.getValue());
        event.setInType(DoctorMoveInGroupEvent.InType.PIGLET.getValue());
        doctorGroupEventDao.create(event);
    }

    /**
     * 导入产房仔猪群
     * @param feedSowLast 所有哺乳母猪各自的最后一条数据
     */
    private void importFarrowPiglet(List<DoctorPigTrack> feedSowTrack, List<DoctorImportSow> feedSowLast, Map<String, DoctorBarn> barnMap, DoctorFarm farm){
        Map<Long, List<DoctorPigTrack>> feedMap = feedSowTrack.stream().collect(Collectors.groupingBy(DoctorPigTrack::getCurrentBarnId));

        // 先按所在猪舍分组
        Map<String, List<DoctorImportSow>> sowMap = feedSowLast.stream().collect(Collectors.groupingBy(DoctorImportSow::getBarnName));
        sowMap.entrySet().forEach(entry -> {
            DoctorBarn barn = barnMap.get(entry.getKey());
            List<DoctorImportSow> sows = entry.getValue();
            Date openAt = sows.stream().map(sow -> sow.getPregDate() == null ? new Date() : sow.getPregDate()).min(Date::compareTo).orElse(new Date()); // 分娩日期的最小值，作为建群日期
            Integer pigletCount = sows.stream().map(DoctorImportSow::getLiveCount).reduce((a, b) -> a + b).orElse(0); // 活仔数
            Integer weak = sows.stream().map(DoctorImportSow::getWeakCount).reduce((a, b) -> a + b).orElse(0); // 弱仔数

            DoctorGroup group = new DoctorGroup();
            group.setOrgId(farm.getOrgId());
            group.setOrgName(farm.getOrgName());
            group.setFarmId(farm.getId());
            group.setFarmName(farm.getName());
            group.setGroupCode(barn.getName() + "(" + DateUtil.toDateString(openAt) + ")");
            // 校验猪群号是否重复
            checkGroupCodeExist(farm.getId(), group.getGroupCode());

            group.setOpenAt(openAt);  //建群时间
            group.setStatus(DoctorGroup.Status.CREATED.getValue());
            group.setInitBarnName(barn.getName());
            group.setInitBarnId(barn.getId());
            group.setPigType(barn.getPigType());
            group.setStaffId(barn.getStaffId());
            group.setStaffName(barn.getStaffName());
            group.setCurrentBarnId(group.getInitBarnId());
            group.setCurrentBarnName(group.getInitBarnName());
            doctorGroupDao.create(group);

            //final double baseWeight = 1.5D;
            DoctorGroupTrack groupTrack = new DoctorGroupTrack();
            groupTrack.setGroupId(group.getId());
            groupTrack.setSex(DoctorGroupTrack.Sex.MIX.getValue());
            groupTrack.setQuantity(pigletCount);
            groupTrack.setBoarQty(0);
            groupTrack.setSowQty(groupTrack.getQuantity() - groupTrack.getBoarQty());
            groupTrack.setAvgDayAge(DateUtil.getDeltaDaysAbs(new DateTime(openAt).withTimeAtStartOfDay().toDate(), DateTime.now().withTimeAtStartOfDay().toDate()));
            groupTrack.setBirthDate(openAt);
            groupTrack.setWeakQty(weak);
            groupTrack.setUnweanQty(pigletCount);

            //批次总结数据
            groupTrack.setNest(sows.size());
            groupTrack.setWeanWeight(groupTrack.getBirthWeight());
            groupTrack.setLiveQty(groupTrack.getQuantity());
            groupTrack.setHealthyQty(groupTrack.getQuantity());
            groupTrack.setUnqQty(0);
            groupTrack.setWeanQty(0);    //默认全部未断奶
            groupTrack.setQuaQty(0);

            if (groupTrack.getQuantity() == null) {
                throw new JsonResponseException("分娩转入猪群数量不能为空，猪群号：" + group.getGroupCode());
            }

            doctorGroupTrackDao.create(groupTrack);
            createMoveInGroupEvent(group, groupTrack, 1, MoreObjects.firstNonNull(groupTrack.getBirthWeight(), 7D) / groupTrack.getQuantity());

            // 把 产房仔猪群 的groupId 存入相应猪舍的所有母猪
            List<DoctorPigTrack> feedTracks = feedMap.get(group.getInitBarnId());
            if (notEmpty(feedTracks)) {
                feedTracks.forEach(feedTrack -> {
                    DoctorPigTrack newTrack = new DoctorPigTrack();
                    newTrack.setId(feedTrack.getId());
                    newTrack.setGroupId(group.getId());
                    doctorPigTrackDao.update(newTrack);

                    //在事件上设置groupId
                    setFarrowGroupId(feedTrack.getPigId(), group.getId());
                });
            }
        });
    }

    //找到最新的两个事件，如果是分娩或断奶，设置groupId
    private void setFarrowGroupId(Long pigId, Long groupId) {
        List<DoctorPigEvent> pigEvents = doctorPigEventDao.limitPigEventOrderByEventAt(pigId, 2);
        pigEvents.stream()
                .filter(event -> Objects.equals(event.getType(), PigEvent.FARROWING.getKey())
                        || Objects.equals(event.getType(), PigEvent.WEAN.getKey()))
                .forEach(event -> {
                    DoctorPigEvent updateEvent = new DoctorPigEvent();
                    updateEvent.setId(event.getId());
                    updateEvent.setGroupId(groupId);
                    doctorPigEventDao.update(updateEvent);
                });
    }

    /**
     * 导入母猪
     */
    @Transactional
    private void importSow(DoctorFarm farm, Map<String, DoctorBarn> barnMap, Map<String, Long> breedMap, Sheet shit) {
        List<DoctorImportSow> feedSowLast = new ArrayList<>(); // 所有哺乳母猪各自的最后一条数据
        List<DoctorPigTrack> feedSowTrack = new ArrayList<>();
        Map<String, List<DoctorImportSow>> sowMap = getImportSows(shit).stream().collect(Collectors.groupingBy(DoctorImportSow::getSowCode));
        sowMap.entrySet().forEach(map -> {
            List<DoctorImportSow> importSows = map.getValue().stream().sorted(Comparator.comparing(DoctorImportSow::getParity)).collect(Collectors.toList());
            int size = importSows.size();

            DoctorImportSow first = importSows.get(0);
            DoctorImportSow last = importSows.get(size - 1);
            checkStatus(last);
            DoctorPig sow = getSow(farm, barnMap, breedMap, first, last);

            //先创建进场事件
            DoctorPigEvent entryEvent = createEntryEvent(first, sow);
            Map<Integer, List<Long>> parityMap = MapBuilder.<Integer, List<Long>>of()
                    .put(first.getParity(), Lists.newArrayList(entryEvent.getId())).map();

//            int delta = 0;
//            boolean isPreg = true;
            for (int i = 0; i < size; i++) {
                DoctorImportSow is = importSows.get(i);

                //这个判断是为了保证胎次连续的
//                if (i == 0) {
//                    delta = is.getParity();
//                } else {
//                    if ((is.getParity() - i) != delta) {
//                        if (!isPreg) {
//                            throw new JsonResponseException("母猪号(" + sow.getPigCode() + ")的胎次( " + is.getParity() + " )不连续, 请检查");
//                        }
//                    }
//                    isPreg = true;
//                    if (notEmpty(is.getRemark()) && is.getRemark().contains("检查：")) {
//                        isPreg = false;
//                        delta -= 1;
//                    }
//                }

                //如果是最后一个元素，要根据当前状态生成事件
                if (i == size - 1) {
                    if (Objects.equals(is.getStatus(), PigStatus.Entry.getKey())) {
                        continue;
                    }
                    if (Objects.equals(is.getStatus(), PigStatus.Mate.getKey())) {
                        DoctorPigEvent mateEvent = createMateEvent(is, sow, entryEvent, DoctorMatingType.DP, null, null);
                        putParityMap(parityMap, is.getParity(), Lists.newArrayList(mateEvent.getId()));
                        continue;
                    }
                    if (Objects.equals(is.getStatus(), PigStatus.Pregnancy.getKey())) {
                        DoctorPigEvent mateEvent = createMateEvent(is, sow, entryEvent, DoctorMatingType.DP, 1, null);
                        DoctorPigEvent pregYang = createPregCheckEvent(is, sow, mateEvent, PregCheckResult.YANG, null);
                        putParityMap(parityMap, is.getParity(), Lists.newArrayList(mateEvent.getId(), pregYang.getId()));
                        continue;
                    }
                    if (Objects.equals(is.getStatus(), PigStatus.KongHuai.getKey())) {
                        DoctorPigEvent mateEvent = createMateEvent(is, sow, entryEvent, DoctorMatingType.DP, null, null);
                        DoctorPigEvent pregYang = createPregCheckEvent(is, sow, mateEvent, getCheckResultByRemark(is.getRemark()), getCheckDateByRemark(is.getRemark()));
                        putParityMap(parityMap, is.getParity(), Lists.newArrayList(mateEvent.getId(), pregYang.getId()));
                        continue;
                    }
                    if (Objects.equals(is.getStatus(), PigStatus.FEED.getKey())) {
                        DoctorPigEvent mateEvent = createMateEvent(is, sow, entryEvent, DoctorMatingType.DP, 1, 1);
                        DoctorPigEvent pregYang = createPregCheckEvent(is, sow, mateEvent, PregCheckResult.YANG, null);
                        DoctorPigEvent farrowEvent = createFarrowEvent(is, sow, pregYang);
                        putParityMap(parityMap, is.getParity(), Lists.newArrayList(mateEvent.getId(), pregYang.getId(), farrowEvent.getId()));
                        feedSowLast.add(last);
                        continue;
                    }
                    //断奶的情况走下面的方法
                }

                //上一胎次生成：配种 -> 妊检 -> 分娩 -> 断奶
                DoctorPigEvent mateEvent = createMateEvent(is, sow, entryEvent, DoctorMatingType.DP, 1, 1);
                putParityMap(parityMap, is.getParity(), Lists.newArrayList(mateEvent.getId()));

                //如果妊检是不是阳性，只生成到妊检事件
                if (notEmpty(is.getRemark()) && is.getRemark().contains("检查：")) {
                    DoctorPigEvent pregNotYang = createPregCheckEvent(is, sow, mateEvent, getCheckResultByRemark(is.getRemark()), getCheckDateByRemark(is.getRemark()));
                    putParityMap(parityMap, is.getParity(), Lists.newArrayList(pregNotYang.getId()));
                } else {
                    DoctorPigEvent pregYang = createPregCheckEvent(is, sow, mateEvent, PregCheckResult.YANG, null);

                    // 如果妊娠检查为阳性并且猪舍为产房， 则转入分娩舍
                    DoctorPigEvent toFarrowEvent = new DoctorPigEvent();
                    if(last.getStatus().equals(PigStatus.Pregnancy.getKey()) && barnMap.get(last.getBarnName()).getPigType().equals(PigType.DELIVER_SOW.getValue())){
                        toFarrowEvent = createToFarrowEvent(is, sow, pregYang);
                    }

                    DoctorPigEvent farrowEvent = createFarrowEvent(is, sow, pregYang);
                    DoctorPigEvent weanEvent = createWeanEvent(is, sow, farrowEvent);
                    putParityMap(parityMap, is.getParity(), Lists.newArrayList(pregYang.getId(), toFarrowEvent.getId(), farrowEvent.getId(), weanEvent.getId()));
                }
            }

            DoctorPigTrack track = getSowTrack(sow, last, parityMap, barnMap.get(last.getBarnName()));
            if (Objects.equals(track.getStatus(), PigStatus.FEED.getKey())) {
                feedSowTrack.add(track);
            }
        });

        // 智能生成产房仔猪群，holy shit
        this.importFarrowPiglet(feedSowTrack, feedSowLast, barnMap, farm);
    }

    private static PregCheckResult getCheckResultByRemark(String remark) {
        if (notEmpty(remark) && remark.contains("返情")) {
            return PregCheckResult.FANQING;
        }
        if (notEmpty(remark) && remark.contains("流产")) {
            return PregCheckResult.LIUCHAN;
        }
        return PregCheckResult.YING;
    }

    private static Date getCheckDateByRemark(String remark) {
        try {
            if (isEmpty(remark)) {
                return null;
            }
            return DateUtil.toDate(remark.substring(remark.length() - 10, remark.length()));
        } catch (Exception e) {
            log.error("get check date by remark failed, remark:{}, cause:{}", remark, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("获取妊娠检查日期失败，请检查：" + remark);
        }
    }

    //同一胎次的事件id，放到同一个list里
    private void putParityMap(Map<Integer, List<Long>> parityMap, Integer parity, List<Long> eventIds) {
        List<Long> ids = MoreObjects.firstNonNull(parityMap.get(parity), Lists.<Long>newArrayList());
        ids.addAll(eventIds);
        parityMap.put(parity, ids);
    }

    private void checkStatus(DoctorImportSow is) {
        if (is == null || is.getStatus() == null) {
            throw new ServiceException("sow.status.error");
        }
    }

    //导入母猪
    private DoctorPig getSow(DoctorFarm farm, Map<String, DoctorBarn> barnMap, Map<String, Long> breedMap, DoctorImportSow first, DoctorImportSow last) {
        DoctorPig sow = new DoctorPig();
        sow.setOrgId(farm.getOrgId());
        sow.setOrgName(farm.getOrgName());
        sow.setFarmId(farm.getId());
        sow.setFarmName(farm.getName());
        sow.setPigCode(last.getSowCode());
        sow.setIsRemoval(IsOrNot.NO.getValue());
        sow.setPigFatherCode(last.getFatherCode());
        sow.setPigMotherCode(last.getMotherCode());
        sow.setSource(PigSource.LOCAL.getKey());
        sow.setBirthWeight(0D);

        //设置进场日期
        if (first.getMateDate() != null) {
            sow.setInFarmDate(new DateTime(first.getMateDate()).plusDays(-1).toDate()); //进场时间取第一次配种时间减一天
        } else {
            sow.setInFarmDate(DateTime.now().plusDays(-7).toDate()); //进场时间取当前日期减7天
        }
        
        sow.setInitBarnName(last.getBarnName());
        sow.setPigType(DoctorPig.PigSex.SOW.getKey());   //猪类
        if(last.getBirthDate() != null){
            sow.setBirthDate(last.getBirthDate());
            sow.setInFarmDayAge(DateUtil.getDeltaDaysAbs(sow.getInFarmDate(), sow.getBirthDate()));
        }

        DoctorBarn barn = barnMap.get(last.getBarnName());
        if (barn != null) {
            sow.setInitBarnId(barn.getId());
        }
        if(StringUtils.isNotBlank(last.getBreed())){
            sow.setBreedName(last.getBreed());
            sow.setBreedId(breedMap.get(last.getBreed()));
            if(sow.getBreedId() == null){
                throw new JsonResponseException("母猪号:"+ sow.getPigCode() +" 母猪品种错误:" + sow.getBreedName());
            }
        }
        doctorPigDao.create(sow);
        return sow;
    }

    //导入母猪跟踪
    private DoctorPigTrack getSowTrack(DoctorPig sow, DoctorImportSow last, Map<Integer, List<Long>> parityMap, DoctorBarn barn) {
        DoctorPigTrack sowTrack = new DoctorPigTrack();
        sowTrack.setFarmId(sow.getFarmId());
        sowTrack.setPigId(sow.getId());
        sowTrack.setPigType(sow.getPigType());

        // 如果妊娠检查为阳性并且猪舍为产房， 则置状态为 待分娩
        if(last.getStatus().equals(PigStatus.Pregnancy.getKey()) && barn.getPigType().equals(PigType.DELIVER_SOW.getValue())){
            sowTrack.setStatus(PigStatus.Farrow.getKey());
        }else{
            sowTrack.setStatus(last.getStatus());
        }
        sowTrack.setIsRemoval(sow.getIsRemoval());
        sowTrack.setCurrentBarnId(sow.getInitBarnId());
        sowTrack.setCurrentBarnName(sow.getInitBarnName());
        sowTrack.setCurrentBarnType(barn.getPigType());
        sowTrack.setWeight(sow.getBirthWeight());
        sowTrack.setCurrentParity(last.getParity());

        Map<Integer, String> relMap = Maps.newHashMap();
        parityMap.entrySet().forEach(map -> relMap.put(map.getKey(), Joiners.COMMA.join(map.getValue())));

        sowTrack.setRelEventIds(MAPPER.toJson(relMap));
        sowTrack.setCurrentMatingCount(0);       //当前配种次数0 查询的时候自动加1
        sowTrack.setFarrowQty(0);
        sowTrack.setUnweanQty(0);
        sowTrack.setWeanQty(0);
        sowTrack.setFarrowAvgWeight(0D);
        sowTrack.setWeanAvgWeight(0D);

        if (Objects.equals(sowTrack.getStatus(), PigStatus.FEED.getKey())) {
            sowTrack.setGroupId(getGroup(sowTrack.getCurrentBarnId()).getId());
            sowTrack.setFarrowQty(last.getLiveCount());
            sowTrack.setUnweanQty(last.getLiveCount());
            sowTrack.setFarrowAvgWeight(MoreObjects.firstNonNull(last.getNestWeight(), 0D)
                    / (last.getLiveCount() == 0 ? 1 : last.getLiveCount()));
        }

        Map<String, Object> extra = new HashMap<>();
        if(last.getPrePregDate() != null){
            extra.put("judgePregDate", last.getPrePregDate());
        }
        if(!extra.isEmpty()){
            sowTrack.setExtraMap(extra);
        }

        doctorPigTrackDao.create(sowTrack);
        return sowTrack;
    }

    private DoctorGroup getGroup(Long barnId) {
        return doctorGroupDao.findByCurrentBarnId(barnId).stream()
                .filter(group -> group.getStatus() == DoctorGroup.Status.CREATED.getValue())
                .findFirst()
                .orElse(new DoctorGroup());
    }

    private DoctorPigEvent createSowEvent(DoctorImportSow info, DoctorPig sow) {
        DoctorPigEvent event = new DoctorPigEvent();
        event.setOrgId(sow.getOrgId());
        event.setOrgName(sow.getOrgName());
        event.setFarmId(sow.getFarmId());
        event.setFarmName(sow.getFarmName());
        event.setPigId(sow.getId());
        event.setPigCode(sow.getPigCode());
        event.setIsAuto(IsOrNot.NO.getValue());
        event.setKind(DoctorPig.PigSex.SOW.getKey());
        event.setBarnId(sow.getInitBarnId());
        event.setBarnName(sow.getInitBarnName());
        event.setRemark(info.getRemark());
        return event;
    }

    //创建进场事件
    private DoctorPigEvent createEntryEvent(DoctorImportSow info, DoctorPig sow) {
        DoctorPigEvent event = createSowEvent(info, sow);
        event.setEventAt(new DateTime(info.getMateDate()).plusDays(-1).toDate());
        event.setType(PigEvent.ENTRY.getKey());
        event.setName(PigEvent.ENTRY.getName());
        event.setPigStatusAfter(PigStatus.Entry.getKey());
        event.setParity(info.getParity() - 1 <= 0 ? 0 : info.getParity() - 1);

        //进场extra
        DoctorFarmEntryDto entry = new DoctorFarmEntryDto();
        entry.setPigType(sow.getPigType());
        entry.setPigCode(sow.getPigCode());
        entry.setBirthday(sow.getBirthDate());
        entry.setInFarmDate(sow.getInFarmDate());
        entry.setBarnId(event.getBarnId());
        entry.setBarnName(event.getBarnName());
        entry.setSource(PigSource.LOCAL.getKey());
        entry.setBreed(sow.getBreedId());
        entry.setBreedName(sow.getBreedName());
        entry.setFatherCode(sow.getPigFatherCode());
        entry.setMotherCode(sow.getPigMotherCode());
        entry.setEntryMark(event.getRemark());
        entry.setParity(event.getParity());

        //描述
        event.setDesc(getEventDesc(entry.descMap()));
        event.setExtra(MAPPER.toJson(entry));
        if(event.getEventAt() == null){
            throw new JsonResponseException("猪号：" + event.getPigCode() + "，无法获取进场事件时间，请检查数据");
        }
        if(event.getEventAt().after(new Date())){
            event.setEventAt(DateTime.now().minusDays(1).toDate());
        }
        doctorPigEventDao.create(event);
        return event;
    }

    // 转入分娩舍事件
    private DoctorPigEvent createToFarrowEvent(DoctorImportSow info, DoctorPig sow, DoctorPigEvent beforeEvent){
        DoctorPigEvent event = createSowEvent(info, sow);
        event.setEventAt(new DateTime(info.getPrePregDate()).minusDays(7).toDate());
        event.setType(PigEvent.TO_FARROWING.getKey());
        event.setName(PigEvent.TO_FARROWING.getName());
        event.setRelEventId(beforeEvent.getId());
        event.setPigStatusBefore(PigStatus.Pregnancy.getKey());
        event.setPigStatusAfter(PigStatus.Farrow.getKey());
        event.setParity(info.getParity());
        event.setBoarCode(info.getBoarCode());

        DoctorChgLocationDto extra = new DoctorChgLocationDto();
        extra.setChgLocationToBarnName(info.getBarnName());
        event.setExtra(MAPPER.toJson(extra));
        event.setDesc(getEventDesc(extra.descMap()));
        if(event.getEventAt() == null){
            throw new JsonResponseException("猪号：" + event.getPigCode() + "，无法获取去分娩舍事件时间，请检查数据");
        }
        if(event.getEventAt().after(new Date())){
            event.setEventAt(DateTime.now().minusDays(1).toDate());
        }
        doctorPigEventDao.create(event);
        return event;
    }

    //创建配种事件
    private DoctorPigEvent createMateEvent(DoctorImportSow info, DoctorPig sow, DoctorPigEvent beforeEvent, DoctorMatingType mateType, Integer isPreg, Integer isDevelivery) {
        DoctorPigEvent event = createSowEvent(info, sow);
        event.setEventAt(info.getMateDate());
        event.setType(PigEvent.MATING.getKey());
        event.setName(PigEvent.MATING.getName());
        event.setRelEventId(beforeEvent.getId());
        event.setPigStatusBefore(PigStatus.Entry.getKey());
        event.setPigStatusAfter(PigStatus.Mate.getKey());
        event.setParity(info.getParity());
        event.setCurrentMatingCount(1);     //默认初配
        event.setMattingDate(event.getEventAt());
        event.setDoctorMateType(mateType.getKey());
        event.setBoarCode(info.getBoarCode());
        event.setIsImpregnation(isPreg);        //是否受胎
        event.setIsDelivery(isDevelivery);      //是否分娩

        DoctorMatingDto mate = new DoctorMatingDto();
        mate.setMatingBoarPigCode(info.getBoarCode());
        mate.setJudgePregDate(info.getPrePregDate());
        event.setDesc(getEventDesc(mate.descMap()));
        event.setExtra(MAPPER.toJson(mate));
        if(event.getEventAt() == null){
            throw new JsonResponseException("猪号：" + event.getPigCode() + "，无法获取配种事件时间，请检查数据");
        }
        if(event.getEventAt().after(new Date())){
            event.setEventAt(DateTime.now().minusDays(1).toDate());
        }
        doctorPigEventDao.create(event);
        return event;
    }

    //创建妊检事件
    private DoctorPigEvent createPregCheckEvent(DoctorImportSow info, DoctorPig sow, DoctorPigEvent beforeEvent, PregCheckResult checkResult, Date pregDate) {
        DoctorPigEvent event = createSowEvent(info, sow);
        event.setEventAt(MoreObjects.firstNonNull(pregDate, new DateTime(beforeEvent.getEventAt()).plusWeeks(3).toDate()));  //妊检事件事件 = 配种时间 + 3周
        event.setType(PigEvent.PREG_CHECK.getKey());
        event.setName(PigEvent.PREG_CHECK.getName());
        event.setRelEventId(beforeEvent.getId());
        event.setPigStatusBefore(PigStatus.Mate.getKey());

        //妊娠检查
        if (checkResult == PregCheckResult.YANG) {
            event.setPigStatusAfter(PigStatus.Pregnancy.getKey());
        } else {
            event.setPigStatusAfter(PigStatus.KongHuai.getKey());
        }
        event.setParity(info.getParity());
        event.setPregCheckResult(checkResult.getKey());
        event.setCheckDate(event.getEventAt());
        event.setRemark(info.getRemark());

        //妊检extra
        DoctorPregChkResultDto result = new DoctorPregChkResultDto();
        result.setCheckDate(event.getCheckDate());
        result.setCheckResult(checkResult.getKey());
        result.setCheckMark(info.getRemark());

        event.setDesc(getEventDesc(result.descMap()));
        event.setExtra(MAPPER.toJson(result));
        if(event.getEventAt() == null){
            throw new JsonResponseException("猪号：" + event.getPigCode() + "，无法获取妊娠检查事件时间，请检查数据");
        }
        if(event.getEventAt().after(new Date())){
            event.setEventAt(DateTime.now().minusDays(1).toDate());
        }
        doctorPigEventDao.create(event);
        return event;
    }

    //创建分娩事件
    private DoctorPigEvent createFarrowEvent(DoctorImportSow info, DoctorPig sow, DoctorPigEvent beforeEvent) {
        DoctorPigEvent event = createSowEvent(info, sow);
        event.setEventAt(info.getPregDate());
        event.setType(PigEvent.FARROWING.getKey());
        event.setName(PigEvent.FARROWING.getName());
        event.setRelEventId(beforeEvent.getId());
        event.setPigStatusBefore(PigStatus.Farrow.getKey());
        event.setPigStatusAfter(PigStatus.FEED.getKey());
        event.setParity(info.getParity());
        event.setPregDays(DateUtil.getDeltaDaysAbs(info.getPrePregDate(), info.getPregDate())); //日期差
        event.setFarrowWeight(info.getNestWeight());
        event.setLiveCount(info.getLiveCount());
        event.setHealthCount(info.getLiveCount() + info.getWeakCount());
        event.setWeakCount(info.getWeakCount());
        event.setMnyCount(info.getMummyCount());
        event.setJxCount(info.getJixingCount());
        event.setDeadCount(info.getDeadCount());
        event.setBlackCount(info.getBlackCount());
        event.setFarrowingDate(event.getEventAt());

        //分娩extra
        DoctorFarrowingDto farrow = new DoctorFarrowingDto();
        farrow.setFarrowingDate(event.getEventAt());
        farrow.setBarnId(sow.getInitBarnId());
        farrow.setBarnName(sow.getInitBarnName());
        farrow.setBedCode(info.getBed());
        farrow.setFarrowingType(FarrowingType.USUAL.getKey());
        farrow.setGroupCode(getGroup(event.getBarnId()).getGroupCode());
        farrow.setBirthNestAvg(info.getNestWeight());
        farrow.setFarrowingLiveCount(event.getLiveCount());
        farrow.setHealthCount(event.getHealthCount());
        farrow.setWeakCount(event.getWeakCount());
        farrow.setMnyCount(event.getMnyCount());
        farrow.setJxCount(event.getJxCount());
        farrow.setDeadCount(event.getDeadCount());
        farrow.setBlackCount(event.getDeadCount());
        farrow.setToBarnId(event.getBarnId());
        farrow.setToBarnName(event.getBarnName());
        farrow.setFarrowIsSingleManager(IsOrNot.NO.getValue());
        farrow.setFarrowStaff1(info.getStaff1());
        farrow.setFarrowStaff2(info.getStaff2());
        farrow.setFarrowRemark(info.getRemark());

        event.setDesc("分娩");
        event.setExtra(MAPPER.toJson(farrow));
        if(event.getEventAt() == null){
            throw new JsonResponseException("猪号：" + event.getPigCode() + "，无法获取分娩事件时间，请检查数据");
        }
        if(event.getEventAt().after(new Date())){
            event.setEventAt(DateTime.now().minusDays(1).toDate());
        }
        doctorPigEventDao.create(event);
        return event;
    }

    //创建断奶事件
    private DoctorPigEvent createWeanEvent(DoctorImportSow info, DoctorPig sow, DoctorPigEvent beforeEvent) {
        DoctorPigEvent event = createSowEvent(info, sow);
        event.setEventAt(info.getWeanDate());
        event.setType(PigEvent.WEAN.getKey());
        event.setName(PigEvent.WEAN.getName());
        event.setRelEventId(beforeEvent.getId());
        event.setPigStatusBefore(PigStatus.FEED.getKey());
        event.setPigStatusAfter(PigStatus.Wean.getKey());
        event.setParity(info.getParity());
        event.setFeedDays(DateUtil.getDeltaDaysAbs(beforeEvent.getEventAt(), event.getEventAt()));
        event.setWeanCount(info.getLiveCount());
        event.setWeanAvgWeight(MoreObjects.firstNonNull(info.getWeanWeight(), 0D) / (event.getWeanCount() == 0 ? 1 : event.getWeanCount()));
        event.setPartweanDate(event.getEventAt());
        event.setBoarCode(info.getBoarCode());

        //断奶extra
        DoctorWeanDto wean = new DoctorWeanDto();
        wean.setPartWeanDate(event.getEventAt());
        wean.setPartWeanPigletsCount(event.getWeanCount());
        wean.setPartWeanAvgWeight(event.getWeanAvgWeight());
        wean.setPartWeanRemark(info.getRemark());
        wean.setQualifiedCount(event.getWeanCount());
        wean.setNotQualifiedCount(0);
        wean.setFarrowingLiveCount(event.getWeakCount());
        wean.setWeanPigletsCount(0);

        event.setDesc(getEventDesc(wean.descMap()));
        event.setExtra(MAPPER.toJson(wean));
        if(event.getEventAt() == null){
            throw new JsonResponseException("猪号：" + event.getPigCode() + "，无法获取断奶事件时间，请检查数据");
        }
        if(event.getEventAt().after(new Date())){
            event.setEventAt(DateTime.now().minusDays(1).toDate());
        }
        doctorPigEventDao.create(event);
        return event;
    }

    private static String getEventDesc(Map<String, String> map) {
        return Joiner.on("#").withKeyValueSeparator("：").join(map);
    }

    //把excel的每一个cell转换成bean
    private List<DoctorImportSow> getImportSows(Sheet shit) {
        List<DoctorImportSow> sows = Lists.newArrayList();
        for (Row row : shit) {
            if (canImport(row)) {
                DoctorImportSow sow = new DoctorImportSow();
                sow.setBarnName(ImportExcelUtils.getString(row, 0));       //猪舍
                sow.setSowCode(ImportExcelUtils.getString(row, 1));        //母猪耳号

                //获取母猪状态
                PigStatus status = getPigStatus(ImportExcelUtils.getString(row, 2));
                if (status == null) {
                    throw new JsonResponseException("母猪状态错误，猪号：" + sow.getSowCode() + "，row " + (row.getRowNum() + 1));
                } else {
                    sow.setStatus(status.getKey());         //当前状态
                }
                sow.setParity(MoreObjects.firstNonNull(ImportExcelUtils.getInt(row, 3), 1));            //胎次
                sow.setMateDate(ImportExcelUtils.getDate(row, 4));        //配种日期
                sow.setBoarCode(ImportExcelUtils.getString(row, 5));                                    //公猪耳号
                sow.setMateStaffName(ImportExcelUtils.getString(row, 6));                               //配种员
                sow.setPrePregDate(ImportExcelUtils.getDate(row, 7));     //预产日期
                if(sow.getPrePregDate() == null){
                    sow.setPrePregDate(new DateTime(sow.getMateDate()).plusDays(114).toDate());
                }
                sow.setPregDate(ImportExcelUtils.getDate(row, 8));        //实产日期
                sow.setFarrowBarnName(ImportExcelUtils.getString(row, 9));                              //分娩猪舍
                sow.setBed(ImportExcelUtils.getString(row, 10));                                        //床号
                sow.setWeanDate(ImportExcelUtils.getDate(row, 11));       //断奶日期
                sow.setLiveCount(ImportExcelUtils.getIntOrDefault(row, 12, 0) + ImportExcelUtils.getIntOrDefault(row, 14, 0)); //活仔数
                sow.setJixingCount(ImportExcelUtils.getIntOrDefault(row, 13, 0));                       //畸形
                sow.setWeakCount(ImportExcelUtils.getIntOrDefault(row, 14, 0));                         //弱仔数
                sow.setDeadCount(ImportExcelUtils.getIntOrDefault(row, 15, 0));                         //死仔
                sow.setMummyCount(ImportExcelUtils.getIntOrDefault(row, 16, 0));                        //木乃伊
                sow.setBlackCount(ImportExcelUtils.getIntOrDefault(row, 17, 0));                        //黑胎
                sow.setNestWeight(ImportExcelUtils.getDoubleOrDefault(row, 18, 0D));                    //窝重
                sow.setStaff1(ImportExcelUtils.getString(row, 19));                                     //接生员1
                sow.setStaff2(ImportExcelUtils.getString(row, 20));                                     //接生员2
                sow.setSowEarCode(ImportExcelUtils.getString(row, 21));                                 //母猪耳号
                sow.setBirthDate(ImportExcelUtils.getDate(row, 22));      //出生日期
                sow.setRemark(ImportExcelUtils.getString(row, 23));                                     //备注
                sow.setBreed(ImportExcelUtils.getString(row, 24));                                      //品种

                if (isEmpty(sow.getBreed())) {
                    throw new JsonResponseException("母猪品种不能为空，猪号：" + sow.getSowCode() + "，行号：" + (row.getRowNum() + 1));
                }

                sow.setWeanWeight(ImportExcelUtils.getDoubleOrDefault(row, 25, 0D));                    //断奶重
                sow.setWeanCount(ImportExcelUtils.getIntOrDefault(row, 26, 0));                         //断奶数
                sow.setFatherCode(ImportExcelUtils.getString(row, 27));                                 //父号
                sow.setMotherCode(ImportExcelUtils.getString(row, 28));                                 //母号
                sows.add(sow);
            }
        }
        return sows;
    }
    
    private static PigStatus getPigStatus(String status) {
        if (Strings.isNullOrEmpty(status)) {
            log.error("what the fuck!!! the status is fucking null!");
            return PigStatus.Wean;
        }
        if (status.contains("进场")) {
            return PigStatus.Entry;
        }
        if (status.contains("已配种")) {
            return PigStatus.Mate;
        }
        if (status.contains("阳性")) {
            return PigStatus.Pregnancy;
        }
        if (status.contains("哺乳")) {
            return PigStatus.FEED;
        }
        if (status.contains("断奶")) {
            return PigStatus.Wean;
        }
        if (status.contains("返情") || status.contains("流产") || status.contains("阴性")) {
            return PigStatus.KongHuai;
        }
        return null;
    }

    private void importWarehouse(DoctorFarm farm, DoctorImportSheet shit, User user, Map<String, Long> userMap) {
        // 主账号的 profile
        UserProfile userProfile = userProfileDao.findByUserId(user.getId());

        // 初始化仓库大类，数据都是0
        RespHelper.or500(doctorWareHouseTypeWriteService.initDoctorWareHouseType(farm.getId(), farm.getName(), user.getId(), userProfile.getRealName()));


        // 创建仓库
        Map<String, Long> warehouseMap = new HashMap<>(); // key = 仓库名称, value = 仓库id
        for (Row row : shit.getWarehouse()) {
            if (canImport(row)) {
                int line = row.getRowNum() + 1;
                String warehouseName = ImportExcelUtils.getStringOrThrow(row, 0);
                WareHouseType wareHouseType = WareHouseType.from(ImportExcelUtils.getStringOrThrow(row, 1));
                if(wareHouseType == null){
                    throw new JsonResponseException("仓库类型错误，仓库名称：" + warehouseName + "，row " + line + "column" + 2);
                }
                DoctorWareHouse wareHouse = DoctorWareHouse.builder()
                        .wareHouseName(warehouseName).type(wareHouseType.getKey())
                        .farmId(farm.getId()).farmName(farm.getName())
                        .creatorId(user.getId()).creatorName(userProfile.getRealName())
                        .build();
                String manager = ImportExcelUtils.getString(row, 2);
                if(StringUtils.isNotBlank(manager) && userMap.get(manager) != null){
                    wareHouse.setManagerId(userMap.get(manager));
                    wareHouse.setManagerName(manager);
                }else{
                    wareHouse.setManagerId(user.getId());
                    wareHouse.setManagerName(userProfile.getRealName());
                }
                if(warehouseMap.containsKey(warehouseName)){
                    throw new JsonResponseException("仓库名称重复：" + warehouseName + "，row " + line + "column" + 1);
                }else{
                    warehouseMap.put(warehouseName, RespHelper.or500(doctorWareHouseWriteService.createWareHouse(wareHouse)));
                }
            }
        }

        // 导入库存
        this.importStock(shit.getFeed(), WareHouseType.FEED, warehouseMap, farm, user.getId(), userProfile.getRealName());
        this.importStock(shit.getMaterial(), WareHouseType.MATERIAL, warehouseMap, farm, user.getId(), userProfile.getRealName());
        this.importStock(shit.getConsume(), WareHouseType.CONSUME, warehouseMap, farm, user.getId(), userProfile.getRealName());
        this.importStock(shit.getMedicine(), WareHouseType.MEDICINE, warehouseMap, farm, user.getId(), userProfile.getRealName());
        this.importStock(shit.getVacc(), WareHouseType.VACCINATION, warehouseMap, farm, user.getId(), userProfile.getRealName());
    }

    private void importStock(Sheet shit, WareHouseType materialType, Map<String, Long> warehouseMap, DoctorFarm farm, Long userId, String userName) {
        String shitName = materialType.getDesc();
        for (Row row : shit) {
            if (canImport(row)) {
                int line = row.getRowNum() + 1;
                String warehouseName = ImportExcelUtils.getStringOrThrow(row, 0);
                String materialName = ImportExcelUtils.getStringOrThrow(row, 1);
                Double stock = ImportExcelUtils.getDouble(row, 2);
                if(stock == null || stock < 0D){
                    throw new JsonResponseException("库存数量错误，sheet:" + shitName + "，row " + line + "column " + 3);
                }
                String unitName;
                Double unitPrice;
                if(materialType == WareHouseType.FEED || materialType == WareHouseType.MATERIAL){
                    unitPrice = ImportExcelUtils.getDouble(row, 3);
                    unitName = "千克";
                }else{
                    unitName = ImportExcelUtils.getStringOrThrow(row, 3);
                    if(StringUtils.isBlank(unitName)){
                        throw new JsonResponseException("单位错误，sheet:" + shitName + "，row " + line);
                    }
                    unitPrice = ImportExcelUtils.getDouble(row, 4);
                }
                if(unitPrice == null || unitPrice <= 0){
                    throw new JsonResponseException("单价错误，sheet:" + shitName + "，row " + line);
                }else{
                    unitPrice = unitPrice * 100D;
                }
                Long wareHouseId = warehouseMap.get(warehouseName);
                if(wareHouseId == null){
                    throw new JsonResponseException("仓库名称错误，sheet:" + shitName + "，row " + line);
                }
                DoctorBasicMaterial basicMaterial = basicMaterialDao.findByTypeAndName(materialType, materialName);
                if(basicMaterial == null){
                    throw new JsonResponseException("基础物料不存在：" + materialName + "，sheet:" + shitName + "，row " + line);
                }

                // 来一炮入库逻辑，所有应该关联的表就都有数据了
                RespHelper.or500(doctorMaterialInWareHouseWriteService.providerMaterialInfo(
                        DoctorMaterialConsumeProviderDto.builder()
                                .actionType(DoctorMaterialConsumeProvider.EVENT_TYPE.PROVIDER.getValue())
                                .type(materialType.getKey()).farmId(farm.getId()).farmName(farm.getName())
                                .materialTypeId(basicMaterial.getId()).materialName(materialName)
                                .wareHouseId(wareHouseId).wareHouseName(warehouseName)
                                .staffId(userId).staffName(userName)
                                .count(stock).unitPrice(unitPrice.longValue())
                                .unitName(unitName).eventTime(new Date())
                                .build()
                ));
            }
        }

    }

    //第一行是表头，跳过  第一列不能为空
    private static boolean canImport(Row row) {
        return row.getRowNum() > 0 && notEmpty(ImportExcelUtils.getString(row, 0));
    }

    /**
     * 修复配种率的各种统计
     */
    @Transactional
    public void updateMateRate(Long farmId) {
        //取出所有的配种，妊检，分娩事件
        List<DoctorPigEvent> events = doctorPigEventDao.findByFarmIdAndKindAndEventTypes(farmId, DoctorPig.PigSex.SOW.getKey(),
                Lists.newArrayList(PigEvent.MATING.getKey(), PigEvent.PREG_CHECK.getKey(), PigEvent.FARROWING.getKey()));
        events.stream()
                .filter(event -> {
                    if (Objects.equals(event.getType(), PigEvent.FARROWING.getKey())) {
                        return true;
                    }
                    if (Objects.equals(event.getType(), PigEvent.PREG_CHECK.getKey())) {
                        return Objects.equals(event.getPregCheckResult(), PregCheckResult.YANG.getKey());
                    }
                    return Objects.equals(event.getType(), PigEvent.MATING.getKey()) && event.getCurrentMatingCount() == 1;
                })
                .collect(Collectors.groupingBy(DoctorPigEvent::getPigId))
                .entrySet()
                .forEach(map -> map.getValue().stream()
                        .collect(Collectors.groupingBy(DoctorPigEvent::getParity))
                        .values()
                        .forEach(this::updateMate));
    }

    //根据妊检和分娩 更新配种事件标记
    private void updateMate(List<DoctorPigEvent> events) {
        Map<Integer, DoctorPigEvent> eventMap = Maps.newHashMap();
        events.forEach(event -> eventMap.put(event.getType(), event));

        if (!eventMap.containsKey(PigEvent.MATING.getKey())) {
            return;
        }
        DoctorPigEvent mateEvent = eventMap.get(PigEvent.MATING.getKey());
        boolean needUpdate = false;
        if (eventMap.containsKey(PigEvent.PREG_CHECK.getKey())) {
            if (mateEvent.getIsImpregnation() == null || mateEvent.getIsImpregnation() != 1) {
                needUpdate = true;
                mateEvent.setIsImpregnation(1);
            }
        }
        if (eventMap.containsKey(PigEvent.FARROWING.getKey())) {
            if (mateEvent.getIsDelivery() == null || mateEvent.getIsDelivery() != 1) {
                needUpdate = true;
                mateEvent.setIsDelivery(1);
            }
        }
        if (needUpdate) {
            DoctorPigEvent updateEvent = new DoctorPigEvent();
            updateEvent.setId(mateEvent.getId());
            updateEvent.setIsImpregnation(mateEvent.getIsImpregnation());
            updateEvent.setIsDelivery(mateEvent.getIsDelivery());
            doctorPigEventDao.update(updateEvent);
        }
    }


    /**
     * 修复分娩后的母猪事件，增加group_id 方便以后统计
     */
    @Transactional
    public void flushFarrowGroupId(Long farmId) {
        //取出所有的分娩, 断奶事件(过滤掉已经有group_id的)
        List<DoctorPigEvent> events = doctorPigEventDao.findByFarmIdAndKindAndEventTypes(farmId, DoctorPig.PigSex.SOW.getKey(),
                Lists.newArrayList(PigEvent.WEAN.getKey(), PigEvent.FARROWING.getKey()))
                .stream()
                .filter(e -> e.getGroupId() == null)
                .collect(Collectors.toList());

        // Map<Long, Map<Integer, List<DoctorPigEvent>>> 按照猪id，胎次分组
        events.stream()
                .collect(Collectors.groupingBy(DoctorPigEvent::getPigId))
                .entrySet()
                .forEach(map -> map.getValue().stream()
                        .filter(e -> e.getParity() != null)
                        .collect(Collectors.groupingBy(DoctorPigEvent::getParity))
                        .values()
                        .forEach(this::updateFarrowGroupId)
                );

    }

    //更新历史的group_id
    private void updateFarrowGroupId(List<DoctorPigEvent> events) {
        //排下序，让分娩在前面
        events = events.stream().sorted(Comparator.comparing(DoctorPigEvent::getType)).collect(Collectors.toList());
        DoctorGroup group = null;

        for (DoctorPigEvent event : events) {
            if (Objects.equals(event.getType(), PigEvent.FARROWING.getKey())) {
                group = getHistoryFarrowGroup(event);
            }
            else if (Objects.equals(event.getType(), PigEvent.WEAN.getKey())) {
                group = group == null ? getHistoryWeanGroup(event) : group;
            }
            updateEventGroupId(event.getId(), group);
        }
    }

    /**
     * 分娩事件的猪群
     */
    private DoctorGroup getHistoryFarrowGroup(DoctorPigEvent farrowEvent) {
        DoctorFarrowingDto farrow = MAPPER.fromJson(farrowEvent.getExtra(), DoctorFarrowingDto.class);
        if (notEmpty(farrow.getGroupCode())) {
            DoctorGroup group = doctorGroupDao.findByFarmIdAndGroupCode(farrowEvent.getFarmId(), farrow.getGroupCode());
            if (group != null) {
                return group;
            }
            log.warn("dirty data, farrow event group not found, sowId:{}, parity:{}, groupCode:{}", farrowEvent.getPigId(), farrowEvent.getParity(), farrow.getGroupCode());
        }
        log.warn("dirty data, farrow event groupCode empty, sowId:{}, parity:{}, farrow:{}", farrowEvent.getPigId(), farrowEvent.getParity(), farrow);
        return doctorGroupDao.findByFarmIdAndBarnIdAndDate(farrowEvent.getFarmId(), farrowEvent.getBarnId(), farrowEvent.getEventAt());
    }

    /**
     * 断奶事件的猪群
     */
    private DoctorGroup getHistoryWeanGroup(DoctorPigEvent weanEvent) {
        DoctorGroup group = doctorGroupDao.findByFarmIdAndBarnIdAndDate(weanEvent.getFarmId(), weanEvent.getBarnId(), weanEvent.getEventAt());
        return MoreObjects.firstNonNull(group, new DoctorGroup());
    }

    private void updateEventGroupId(Long eventId, DoctorGroup group) {
        if (group != null && group.getId() != null) {
            DoctorPigEvent updateEvent = new DoctorPigEvent();
            updateEvent.setId(eventId);
            updateEvent.setGroupId(group.getId());
            doctorPigEventDao.update(updateEvent);
            log.warn("update event group id success! eventId:{}, groupId:{}", eventId, group.getId());
        } else {
            log.warn("update event group id error, group not found! eventId:{}", eventId);
        }
    }

    /**
     * 刷新公猪类型
     * @see BoarEntryType
     */
    @Transactional
    public void flushBoarType(Long farmId) {
        List<DoctorPig> boars = doctorPigDao.findPigsByFarmIdAndPigType(farmId, DoctorPig.PigSex.BOAR.getKey());
        if (!notEmpty(boars)) {
            log.info("this farm do not have any boars, farmId:{}", farmId);
            return;
        }
        boars.forEach(boar -> {
            Map<String, Object> map = boar.getExtraMap();
            if (map == null || map.get(DoctorFarmEntryConstants.BOAR_TYPE_ID) == null) {
                updateBoarType(boar.getId(), BoarEntryType.HGZ.getKey());
            } else {
                Integer boarType = Integer.valueOf(String.valueOf(map.get(DoctorFarmEntryConstants.BOAR_TYPE_ID)));
                updateBoarType(boar.getId(), boarType);
            }
        });
    }

    private void updateBoarType(Long boarId, Integer boarType) {
        DoctorPig pig = new DoctorPig();
        pig.setId(boarId);
        pig.setBoarType(MoreObjects.firstNonNull(boarType, BoarEntryType.HGZ.getKey()));
        doctorPigDao.update(pig);
    }
}
