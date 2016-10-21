package io.terminus.doctor.move.service;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.UserStatus;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.move.dto.DoctorImportSheet;
import io.terminus.doctor.move.dto.DoctorImportSow;
import io.terminus.doctor.move.util.ImportExcelUtils;
import io.terminus.doctor.msg.service.DoctorMessageRuleWriteService;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.dao.SubDao;
import io.terminus.doctor.user.dao.SubRoleDao;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.model.SubRole;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.user.service.SubRoleWriteService;
import io.terminus.parana.user.impl.dao.UserProfileDao;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.model.UserProfile;
import io.terminus.parana.user.service.UserWriteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy/MM/dd");

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

    /**
     * 根据shit导入所有的猪场数据
     */
    public void importAll(DoctorImportSheet shit) {
        // 猪场和员工
        Object[] result = this.importOrgFarmUser(shit.getFarm(), shit.getStaff());
        User primaryUser = (User) result[0];
        DoctorFarm farm = (DoctorFarm) result[1];
        Map<String, Long> userMap = doctorMoveBasicService.getSubMap(farm.getOrgId());

        importBarn(farm, userMap, shit.getBarn());
        //把所有猪舍添加到所有用户的权限里去
        userInitService.updatePermissionBarn(primaryUser.getMobile());

        Map<String, DoctorBarn> barnMap = doctorMoveBasicService.getBarnMap2(farm.getId());
        Map<String, Long> breedMap = doctorMoveBasicService.getBreedMap();

        importBoar(farm, barnMap, breedMap, shit.getBoar());

    }

    public Object[] importOrgFarmUser(Sheet farmShit, Sheet staffShit) {
        Object[] result = this.importOrgFarm(farmShit);
        User primaryUser = (User) result[0];
        this.importStaff(staffShit, primaryUser);
        return result;
    }

    private void importStaff(Sheet staffShit, User primaryUser){
        final String appKey = "MOBILE";
        RespHelper.or500(subRoleWriteService.initDefaultRoles(appKey, primaryUser.getId()));
        // key = roleName, value = roleId
        Map<String, Long> existRole = subRoleDao.findByUserIdAndStatus(appKey, primaryUser.getId(), 1).stream().collect(Collectors.toMap(SubRole::getName, SubRole::getId));

        List<String> existSubName = subDao.findByParentUserId(primaryUser.getId()).stream().map(Sub::getRealName).collect(Collectors.toList());
        String farmIds = Joiner.on(",").join(doctorFarmDao.findByOrgId(doctorStaffDao.findByUserId(primaryUser.getId()).getOrgId()).stream().map(DoctorFarm::getId).collect(Collectors.toList()));

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
                subUser.setName(loginName + "@" + primaryUser.getName());
                subUser.setMobile(contact);
                subUser.setPassword("123456");
                subUser.setType(UserType.FARM_SUB.value());
                subUser.setStatus(UserStatus.NORMAL.value());

                if(existRole.get(roleName) == null){
                    log.error("role not exist, row {} column {}, sheet : staff", row.getRowNum(), 3);
                    throw new JsonResponseException("role not exist, row " + row.getRowNum() + " column 3, sheet : staff");
                }
                List<String> roles = Lists.newArrayList("SUB", "SUB(SUB(" + existRole.get(roleName) + "))");
                subUser.setRoles(roles);

                subUser.setExtra(MapBuilder.<String, String>of()
                        .put("pid", primaryUser.getId().toString())
                        .put("contact", contact)
                        .put("realName", realName)
                        .map());
                Long subUserId = RespHelper.or500(userWriteService.create(subUser));

                //现在是数据权限
                DoctorUserDataPermission permission = new DoctorUserDataPermission();
                permission.setUserId(subUserId);
                permission.setFarmIds(farmIds);
                doctorUserDataPermissionDao.create(permission);
            }
        }
    }

    /**
     * 公司、猪场、主账号
     * @param farmShit
     * @return 主账号的 user
     */
    private Object[] importOrgFarm(Sheet farmShit){
        Row row1 = farmShit.getRow(1);
        String orgName = ImportExcelUtils.getStringOrThrow(row1, 0);
        String farmName = ImportExcelUtils.getStringOrThrow(row1, 1);
        String loginName = ImportExcelUtils.getStringOrThrow(row1, 2);
        String mobile = ImportExcelUtils.getStringOrThrow(row1, 3);
        String realName = ImportExcelUtils.getStringOrThrow(row1, 4);

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
            doctorFarmDao.create(farm);
            RespHelper.or500(doctorMessageRuleWriteService.initTemplate(farm.getId()));
        }else{
            log.warn("farm {} has existed, id = {}", farmName, farm.getId());
        }

        // 主账号
        Response<User> result = doctorUserReadService.findBy(mobile, LoginType.MOBILE);
        if(result.isSuccess() && result.getResult() != null){
            log.warn("primary user has existed, mobile={}", mobile);
            Long userId = result.getResult().getId();
            DoctorUserDataPermission permission = doctorUserDataPermissionDao.findByUserId(userId);
            if(!permission.getFarmIdsList().contains(farm.getId())){
                permission.setFarmIds(permission.getFarmIds() + "," + farm.getId());
                doctorUserDataPermissionDao.update(permission);
            }
            return new Object[]{result.getResult(), farm};
        }else{
            User user = new User();
            user.setMobile(mobile);
            user.setPassword("123456");
            user.setName(loginName);
            user.setStatus(UserStatus.NORMAL.value());
            user.setType(UserType.FARM_ADMIN_PRIMARY.value());
            user.setRoles(Lists.newArrayList("PRIMARY", "PRIMARY(OWNER)"));
            Long userId = RespHelper.or500(userWriteService.create(user));
            user.setId(userId);

            // 把真实姓名存进 user profile
            UserProfile userProfile = userProfileDao.findByUserId(userId);
            userProfile.setRealName(realName);
            userProfileDao.update(userProfile);

            //初始化服务状态
            userInitService.initDefaultServiceStatus(userId);
            //初始化服务的申请审批状态
            userInitService.initServiceReview(userId, mobile);

            // 主账号的staff
            this.createStaff(user, org, DoctorStaff.Sex.MALE);

            //创建数据权限
            DoctorUserDataPermission permission = new DoctorUserDataPermission();
            permission.setUserId(userId);
            permission.setFarmIds(farm.getId().toString());
            doctorUserDataPermissionDao.create(permission);
            return new Object[]{user, farm};
        }
    }

    private void createStaff(User user, DoctorOrg org, DoctorStaff.Sex sex){
        DoctorStaff staff = new DoctorStaff();
        staff.setOrgName(org.getName());
        staff.setOrgId(org.getId());
        staff.setUserId(user.getId());
        staff.setCreatorId(user.getId());
        staff.setCreatorName(user.getName());
        staff.setUpdatorId(user.getId());
        staff.setUpdatorName(user.getName());
        staff.setSex(sex != null ? sex.value() : null);
        staff.setStatus(DoctorStaff.Status.PRESENT.value());
        doctorStaffDao.create(staff);
    }

    /**
     * 导入猪舍
     */
    public void importBarn(DoctorFarm farm, Map<String, Long> userMap, Sheet shit) {
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

                PigType pigType = PigType.from(ImportExcelUtils.getString(row, 1));
                if (pigType != null) {
                    barn.setPigType(pigType.getValue());
                } else {
                    log.error("farm:{}, barn:{} type is null, please check!", farm, barn.getName());
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

    public void importBreed(Sheet shit) {
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
    public void importBoar(DoctorFarm farm, Map<String, DoctorBarn> barnMap, Map<String, Long> breedMap, Sheet shit) {
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
            boar.setPigType(PigType.BOAR.getValue());
            boar.setIsRemoval(IsOrNot.NO.getValue());
            boar.setPigFatherCode(ImportExcelUtils.getString(row, 4));
            boar.setPigMotherCode(ImportExcelUtils.getString(row, 5));
            PigSource source = PigSource.from(ImportExcelUtils.getString(row, 7));
            if (source != null) {
                boar.setSource(source.getKey());
            }
            boar.setBirthDate(DateUtil.formatToDate(DTF, ImportExcelUtils.getString(row, 3)));
            boar.setInFarmDate(DateUtil.formatToDate(DTF, ImportExcelUtils.getString(row, 2)));
            boar.setInitBarnName(ImportExcelUtils.getString(row, 0));
            DoctorBarn barn = barnMap.get(boar.getInitBarnName());
            if (barn != null) {
                boar.setInitBarnId(barn.getId());
            }
            boar.setBreedName(ImportExcelUtils.getString(row, 6));
            boar.setBreedId(breedMap.get(boar.getBreedName()));
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
            boarTrack.setCurrentParity(1);      //配种次数置成1
            doctorPigTrackDao.create(boarTrack);
        }
    }

    /**
     * 导入猪群
     */
    public void importGroup(DoctorFarm farm, Map<String, DoctorBarn> barnMap, Sheet shit) {
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
            group.setGroupCode(ImportExcelUtils.getString(row, 0));
            group.setOpenAt(null);  // TODO: 2016/10/20 建群时间待定
            group.setStatus(DoctorGroup.Status.CREATED.getValue());
            group.setInitBarnName(ImportExcelUtils.getString(row, 1));

            DoctorBarn barn = barnMap.get(group.getInitBarnName());
            if (barn != null) {
                group.setInitBarnId(barn.getId());
                group.setPigType(barn.getPigType());
                group.setStaffId(barn.getStaffId());
                group.setStaffName(barn.getStaffName());
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
            groupTrack.setAvgDayAge(ImportExcelUtils.getInt(row, 4));
            groupTrack.setBirthDate(DateTime.now().minusDays(groupTrack.getAvgDayAge()).toDate());
            groupTrack.setWeight(ImportExcelUtils.getDouble(row, 5));
            groupTrack.setAvgWeight(MoreObjects.firstNonNull(groupTrack.getWeight(), 0D) / groupTrack.getQuantity());
            groupTrack.setWeanAvgWeight(0D);
            groupTrack.setBirthAvgWeight(0D);
            groupTrack.setWeakQty(0);
            groupTrack.setUnweanQty(0);
            groupTrack.setUnqQty(0);
            doctorGroupTrackDao.create(groupTrack);
        }
    }

    /**
     * 导入母猪
     */
    public void importSow(DoctorFarm farm, Map<String, DoctorBarn> barnMap, Map<String, Long> breedMap, Sheet shit) {
        Map<String, List<DoctorImportSow>> sowMap = getImportSows(shit).stream().collect(Collectors.groupingBy(DoctorImportSow::getSowCode));
        sowMap.entrySet().forEach(map -> {
            List<DoctorImportSow> importSows = map.getValue();
            DoctorPig sow = getSow(farm, barnMap, breedMap, importSows.get(0), importSows.get(importSows.size() - 1));




        });
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
        sow.setBirthDate(last.getBirthDate());
        sow.setBirthWeight(0D);
        sow.setInFarmDate(new DateTime(first.getMateDate()).plusDays(-1).toDate()); //进场时间取第一次配种时间减一天
        sow.setInFarmDayAge(null); // TODO: 2016/10/21 进场日龄
        sow.setInitBarnName(last.getBarnName());

        DoctorBarn barn = barnMap.get(last.getBarnName());
        if (barn != null) {
            sow.setPigType(barn.getPigType());   //猪类
            sow.setInitBarnId(barn.getId());
        }
        sow.setBreedName(last.getBreed());
        sow.setBreedId(breedMap.get(last.getBreed()));
        doctorPigDao.create(sow);
        return sow;
    }

    //导入母猪跟踪
    private DoctorPigTrack getSowTrack(DoctorPig sow, DoctorImportSow last, DoctorPigEvent lastEvent) {
        DoctorPigTrack sowTrack = new DoctorPigTrack();
        sowTrack.setFarmId(sow.getFarmId());
        sowTrack.setPigId(sow.getId());
        sowTrack.setPigType(sow.getPigType());
        sowTrack.setStatus(last.getStatus());
        sowTrack.setIsRemoval(sow.getIsRemoval());
        sowTrack.setCurrentBarnId(sow.getInitBarnId());
        sowTrack.setCurrentBarnName(sow.getInitBarnName());
        sowTrack.setWeight(sow.getBirthWeight());
        sowTrack.setCurrentParity(last.getParity());
        sowTrack.setGroupId(null); // TODO: 2016/10/21 哺乳母猪所在猪群
//        sowTrack.setFarrowQty();
//        sowTrack.setUnweanQty();
//        sowTrack.setWeanQty();
//        sowTrack.setFarrowAvgWeight();
//        sowTrack.setWeanAvgWeight();
//        sowTrack.setRelEventIds();
//        sowTrack.setCurrentMatingCount();       //当前配种次数
        doctorPigTrackDao.create(sowTrack);
        return sowTrack;
    }

    //把excel的每一个cell转换成bean
    private List<DoctorImportSow> getImportSows(Sheet shit) {
        List<DoctorImportSow> sows = Lists.newArrayList();
        for (Row row : shit) {
            if (canImport(row)) {
                DoctorImportSow sow = new DoctorImportSow();
                sow.setBarnName(ImportExcelUtils.getString(row, 0));       //猪舍
                sow.setSowCode(ImportExcelUtils.getString(row, 1));        //母猪耳号

                PigStatus status = PigStatus.from(ImportExcelUtils.getString(row, 2));
                if (status == null) {
                    log.error("WTF! The pig status is null! row:{}, pigCode:{}", row.getRowNum(), sow.getSowCode());
                } else {
                    sow.setStatus(status.getKey());         //当前状态
                }
                sow.setParity(MoreObjects.firstNonNull(ImportExcelUtils.getInt(row, 3), 1));            //胎次
                sow.setMateDate(DateUtil.formatToDate(DTF, ImportExcelUtils.getString(row, 4)));        //配种日期
                sow.setBoarCode(ImportExcelUtils.getString(row, 5));                                    //公猪耳号
                sow.setMateStaffName(ImportExcelUtils.getString(row, 6));                               //配种员
                sow.setPrePregDate(DateUtil.formatToDate(DTF, ImportExcelUtils.getString(row, 7)));     //预产日期
                sow.setPregDate(DateUtil.formatToDate(DTF, ImportExcelUtils.getString(row, 8)));        //实产日期
                sow.setFarrowBarnName(ImportExcelUtils.getString(row, 9));                              //分娩猪舍
                sow.setBed(ImportExcelUtils.getString(row, 10));                                        //床号
                sow.setWeanDate(DateUtil.formatToDate(DTF, ImportExcelUtils.getString(row, 11)));       //断奶日期
                sow.setLiveCount(ImportExcelUtils.getIntOrDefault(row, 12, 0));                         //活仔数
                sow.setJixingCount(ImportExcelUtils.getIntOrDefault(row, 13, 0));                       //畸形
                sow.setWeakCount(ImportExcelUtils.getIntOrDefault(row, 14, 0));                         //弱仔数
                sow.setDeadCount(ImportExcelUtils.getIntOrDefault(row, 15, 0));                         //死仔
                sow.setMummyCount(ImportExcelUtils.getIntOrDefault(row, 16, 0));                        //木乃伊
                sow.setBlackCount(ImportExcelUtils.getIntOrDefault(row, 17, 0));                        //黑胎
                sow.setNestWeight(ImportExcelUtils.getDoubleOrDefault(row, 18, 0D));                    //窝重
                sow.setStaff1(ImportExcelUtils.getString(row, 19));                                     //接生员1
                sow.setStaff2(ImportExcelUtils.getString(row, 20));                                     //接生员2
                sow.setSowEarCode(ImportExcelUtils.getString(row, 21));                                 //母猪耳号
                sow.setBirthDate(DateUtil.formatToDate(DTF, ImportExcelUtils.getString(row, 22)));      //出生日期
                sow.setRemark(ImportExcelUtils.getString(row, 23));                                     //备注
                sow.setBreed(ImportExcelUtils.getString(row, 24));                                      //品种
                sow.setWeanWeight(ImportExcelUtils.getDoubleOrDefault(row, 25, 0D));                    //断奶重
                sow.setWeanCount(ImportExcelUtils.getIntOrDefault(row, 26, 0));                         //断奶数
                sow.setFatherCode(ImportExcelUtils.getString(row, 27));                                 //父号
                sow.setMotherCode(ImportExcelUtils.getString(row, 28));                                 //母号
                sows.add(sow);
            }
        }
        return sows;
    }

    public void importWarehouse(DoctorFarm farm, Sheet shit) {

    }

    public void importMedicine(DoctorFarm farm, Sheet shit) {

    }

    public void importVacc(DoctorFarm farm, Sheet shit) {

    }

    public void importMaterial(DoctorFarm farm, Sheet shit) {

    }

    public void importFeed(DoctorFarm farm, Sheet shit) {

    }

    public void importConsume(DoctorFarm farm, Sheet shit) {

    }

    //第一行是表头，跳过  第一列不能为空
    private static boolean canImport(Row row) {
        return row.getRowNum() > 0 && notEmpty(ImportExcelUtils.getString(row, 0));
    }
}
