package io.terminus.doctor.user.manager;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import io.terminus.common.utils.Joiners;
import io.terminus.doctor.common.enums.IsOrNot;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.dao.PrimaryUserDao;
import io.terminus.doctor.user.dao.SubDao;
import io.terminus.doctor.user.dao.UserDaoExt;
import io.terminus.doctor.user.dto.DoctorUserUnfreezeDto;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionWriteService;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.address.service.AddressReadService;
import io.terminus.parana.user.impl.dao.UserProfileDao;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.model.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notNull;

/**
 * Created by chenzenghui on 16/7/15.
 */

@Slf4j
@Component
public class DoctorFarmManager {
    private final DoctorFarmDao doctorFarmDao;
    private final DoctorUserDataPermissionDao doctorUserDataPermissionDao;
    private final DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService;
    private final AddressReadService addressReadService;
    private final DoctorOrgReadService doctorOrgReadService;
    private final UserDaoExt userDaoExt;
    private final PrimaryUserDao primaryUserDao;
    private final SubDao subDao;
    private final UserProfileDao userProfileDao;

    @Autowired
    public DoctorFarmManager(DoctorFarmDao doctorFarmDao,
                             DoctorUserDataPermissionDao doctorUserDataPermissionDao,
                             DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService,
                             AddressReadService addressReadService,
                             DoctorOrgReadService doctorOrgReadService, UserDaoExt userDaoExt, PrimaryUserDao primaryUserDao, SubDao subDao, UserProfileDao userProfileDao){
        this.doctorFarmDao = doctorFarmDao;
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
        this.doctorUserDataPermissionWriteService = doctorUserDataPermissionWriteService;
        this.addressReadService = addressReadService;
        this.doctorOrgReadService = doctorOrgReadService;
        this.userDaoExt = userDaoExt;
        this.primaryUserDao = primaryUserDao;
        this.subDao = subDao;
        this.userProfileDao = userProfileDao;
    }

    /**
     * 给已开通猪场软件的用户添加猪场
     */
    @Transactional
    public List<DoctorFarm> addFarms4PrimaryUser(Long userId, Long orgId, List<DoctorFarm> farms){
        DoctorOrg org = RespHelper.orServEx(doctorOrgReadService.findOrgById(orgId));
        List<DoctorFarm> newFarms = Lists.newArrayList(); //将被保存下来的新猪场
        farms.forEach(farm -> {
            farm.setOrgName(org.getName());
            farm.setOrgId(org.getId());
            if (farm.getProvinceId() != null) {
                farm.setProvinceName(RespHelper.orServEx(addressReadService.findById(farm.getProvinceId())).getName());
            }
            if (farm.getCityId() != null) {
                farm.setCityName(RespHelper.orServEx(addressReadService.findById(farm.getCityId())).getName());
            }
            if (farm.getDistrictId() != null) {
                farm.setDistrictName(RespHelper.orServEx(addressReadService.findById(farm.getDistrictId())).getName());
            }
            doctorFarmDao.create(farm);
            newFarms.add(farm);
        });

        //更新下数据权限
        DoctorUserDataPermission permission = doctorUserDataPermissionDao.findByUserId(userId);
        permission.setFarmIds(permission.getFarmIds() + "," + Joiner.on(",").join(newFarms.stream().map(DoctorFarm::getId).collect(Collectors.toList())));
        RespHelper.orServEx(doctorUserDataPermissionWriteService.updateDataPermission(permission));
        return newFarms;
    }

    /**
     * 冻结猪场
     *
     * @param farmId 猪场id
     */
    @Transactional
    public void freezeFarm(Long farmId){
        //冻结猪场
        doctorFarmDao.freeze(farmId);

//        //冻结猪场下的主用户
//        PrimaryUser primaryUser = primaryUserDao.findPrimaryByFarmId(farmId);
//        if (notNull(primaryUser)) {
//            freezeUser(primaryUser.getUserId());
//            primaryUserDao.freeze(primaryUser.getId());
//            doctorUserDataPermissionDao.freeze(primaryUser.getUserId());
//        }
//
//        //冻结猪场下子账户
//        List<Sub> subList = subDao.findSubsByFarmId(farmId);
//        subList.forEach(sub -> {
//            freezeUser(sub.getUserId());
//            subDao.freeze(sub.getId());
//            doctorUserDataPermissionDao.freeze(sub.getUserId());
//        });

        List<DoctorUserDataPermission> permissions = doctorUserDataPermissionDao.findByFarmId(farmId);
        permissions.forEach(permission -> {
            permission.getFarmIdsList().remove(farmId);
            if (permission.getFarmIdsList().isEmpty()) {
                doctorUserDataPermissionDao.delete(permission.getId());
                User user = userDaoExt.findById(permission.getUserId());
                freezeUser(user);
                if (Objects.equals(user.getType(), UserType.FARM_ADMIN_PRIMARY.value())) {
                    primaryUserDao.freezeByUser(user.getId());
                } else {
                    subDao.freezeByUser(user.getId());
                }
            } else {
                permission.setFarmIds(Joiners.COMMA.join(permission.getFarmIdsList()));
                doctorUserDataPermissionDao.update(permission);
            }
        });

    }

    @Transactional
    public void unfreezeUser(DoctorUserUnfreezeDto doctorUserUnfreezeDto) {
        //解冻user表
        User user = userDaoExt.findById(doctorUserUnfreezeDto.getUserId());
        if (notNull(user)) {
            Map<String, String> extraMap = user.getExtra();
            if (isNull(extraMap)) {
                extraMap = new HashMap<>();
            }
            extraMap.put("frozen", IsOrNot.NO.getKey().toString());
            user.setExtra(user.getExtra());
            userDaoExt.update(user);
        }

        //解冻猪场主/子用户
        if (Objects.equals(doctorUserUnfreezeDto.getUserType(), UserType.FARM_ADMIN_PRIMARY.value())) {
            PrimaryUser primaryUser = doctorUserUnfreezeDto.getPrimaryUser();
            if (isNull(primaryUser.getId())) {
                primaryUserDao.create(primaryUser);
            } else {
                primaryUser.setFrozen(IsOrNot.NO.getKey());
                primaryUserDao.update(primaryUser);
            }
        } else {
            Sub sub = doctorUserUnfreezeDto.getSub();
            if (isNull(sub.getId())) {
                subDao.create(sub);
            } else {
                sub.setFrozen(IsOrNot.NO.getKey());
                subDao.update(sub);
            }
        }

        //更新用户个人
        UserProfile userProfile = doctorUserUnfreezeDto.getUserProfile();
        if (isNull(userProfile.getId())) {
            userProfileDao.create(userProfile);
        } else {
            userProfileDao.update(userProfile);
        }
    }

    //(解冻时直接删除除user表以外的其他信息)
    @Transactional
    public void unfreezeUser(Long userId) {
        //解冻user表
        User user = userDaoExt.findById(userId);
        if (notNull(user)) {
            Map<String, String> extraMap = user.getExtra();
            if (isNull(extraMap)) {
                extraMap = new HashMap<>();
            }
            extraMap.put("frozen", IsOrNot.NO.getKey().toString());
            user.setExtra(user.getExtra());
            userDaoExt.update(user);
        }

        // TODO: 18/2/9 暂时没有解冻操作 
    }

    private void freezeUser(User user) {
        if (notNull(user)) {
            Map<String, String> extraMap = user.getExtra();
            if (isNull(extraMap)) {
                extraMap = new HashMap<>();
            }
            extraMap.put("frozen", IsOrNot.YES.getKey().toString());
            user.setExtra(user.getExtra());
            userDaoExt.update(user);
        }
    }
}
