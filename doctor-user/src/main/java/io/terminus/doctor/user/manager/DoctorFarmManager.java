package io.terminus.doctor.user.manager;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import io.terminus.doctor.common.enums.IsOrNot;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.dao.PrimaryUserDao;
import io.terminus.doctor.user.dao.SubDao;
import io.terminus.doctor.user.dao.UserDaoExt;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionWriteService;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.address.service.AddressReadService;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    
    @Autowired
    public DoctorFarmManager(DoctorFarmDao doctorFarmDao,
                             DoctorUserDataPermissionDao doctorUserDataPermissionDao,
                             DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService,
                             AddressReadService addressReadService,
                             DoctorOrgReadService doctorOrgReadService, UserDaoExt userDaoExt, PrimaryUserDao primaryUserDao, SubDao subDao){
        this.doctorFarmDao = doctorFarmDao;
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
        this.doctorUserDataPermissionWriteService = doctorUserDataPermissionWriteService;
        this.addressReadService = addressReadService;
        this.doctorOrgReadService = doctorOrgReadService;
        this.userDaoExt = userDaoExt;
        this.primaryUserDao = primaryUserDao;
        this.subDao = subDao;
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
     * @param farmId 猪场id
     */
    @Transactional
    public void freezeFarm(Long farmId){
        //冻结猪场
        doctorFarmDao.freeze(farmId);

        //冻结猪场下的主用户
        PrimaryUser primaryUser = primaryUserDao.findPrimaryByFarmId(farmId);
        if (notNull(primaryUser)) {
            freezeUser(primaryUser.getUserId());
            primaryUserDao.freeze(primaryUser.getId());
            doctorUserDataPermissionDao.freeze(primaryUser.getUserId());
        }

        //冻结猪场下子账户
        List<Sub> subList = subDao.findSubsByFarmId(farmId);
        subList.forEach(sub -> {
            freezeUser(sub.getUserId());
            subDao.freeze(sub.getId());
            doctorUserDataPermissionDao.freeze(sub.getUserId());
        });
    }

    private void freezeUser(Long userId) {
        User user = userDaoExt.findById(userId);
        if (notNull(user)) {
            user.getExtra().put("frozen", IsOrNot.YES.getKey().toString());
            user.setExtra(user.getExtra());
            userDaoExt.update(user);
        }
    }
}
