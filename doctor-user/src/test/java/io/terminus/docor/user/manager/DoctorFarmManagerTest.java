package io.terminus.docor.user.manager;

import io.terminus.doctor.common.enums.IsOrNot;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.dao.PrimaryUserDao;
import io.terminus.doctor.user.dao.SubDao;
import io.terminus.doctor.user.dao.UserDaoExt;
import io.terminus.doctor.user.dto.DoctorUserUnfreezeDto;
import io.terminus.doctor.user.manager.DoctorFarmManager;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.parana.user.impl.dao.UserProfileDao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by xjn on 18/2/7.
 * email:xiaojiannan@terminus.io
 */
public class DoctorFarmManagerTest extends BaseManagerTest{
    @Autowired
    private DoctorFarmManager doctorFarmManager;
    @Autowired
    private  UserDaoExt userDaoExt;
    @Autowired
    private PrimaryUserDao primaryUserDao;
    @Autowired
    private SubDao subDao;
    @Autowired
    private UserProfileDao userProfileDao;
    @Autowired
    private DoctorUserDataPermissionDao doctorUserDataPermissionDao;
    @Autowired
    private DoctorFarmDao doctorFarmDao;

    @Test
    public void freezeTest() {
        doctorFarmManager.freezeFarm(855L);
    }

    @Test
    public void unfreezeUserToPrimaryTest() {
        long userId = 10L;
        DoctorUserUnfreezeDto doctorUserUnfreezeDto = new DoctorUserUnfreezeDto();
        doctorUserUnfreezeDto.setUserId(userId);
        doctorUserUnfreezeDto.setUserType(UserType.FARM_ADMIN_PRIMARY.value());
        doctorUserUnfreezeDto.setPrimaryUser(primaryUserDao.findIncludeFrozenByUserId(userId));
        doctorUserUnfreezeDto.setPermission(doctorUserDataPermissionDao.findFrozenByUserId(userId));
        doctorUserUnfreezeDto.setUserProfile(userProfileDao.findByUserId(userId));
        doctorFarmManager.unfreezeUser(doctorUserUnfreezeDto);
        Assert.assertEquals(userDaoExt.findById(userId).getExtra().get("frozen").toString(), IsOrNot.NO.getKey().toString());
        Assert.assertNotNull(primaryUserDao.findByUserId(userId));
        Assert.assertNotNull(doctorUserDataPermissionDao.findByUserId(userId));
    }

    @Test
    public void unfreezeUserToSubTest() {
        long userId = 13L;
        DoctorUserUnfreezeDto doctorUserUnfreezeDto = new DoctorUserUnfreezeDto();
        doctorUserUnfreezeDto.setUserId(userId);
        doctorUserUnfreezeDto.setUserType(UserType.FARM_SUB.value());
        doctorUserUnfreezeDto.setSub(subDao.findIncludeFrozenByUserId(userId));
        doctorUserUnfreezeDto.setPermission(doctorUserDataPermissionDao.findFrozenByUserId(userId));
        doctorUserUnfreezeDto.setUserProfile(userProfileDao.findByUserId(userId));
        doctorFarmManager.unfreezeUser(doctorUserUnfreezeDto);
        Assert.assertEquals(userDaoExt.findById(userId).getExtra().get("frozen").toString(), IsOrNot.NO.getKey().toString());
        Assert.assertNotNull(subDao.findByUserId(userId));
        Assert.assertNotNull(doctorUserDataPermissionDao.findByUserId(userId));
    }

    @Test
    public void findPrimaryByUserIdTest() {
        System.out.println(primaryUserDao.findIncludeFrozenByUserId(10L));
    }

    @Test
    public void findByUserIdTest() {

        System.out.println(doctorUserDataPermissionDao.findFrozenByUserId(10L));
    }

    @Test
    public void findFrozenByUserId() {
        System.out.println(subDao.findIncludeFrozenByUserId(12L));
    }

    @Test
    public void freezeFarmTest() {
        doctorFarmManager.freezeFarm(1L);
    }

    @Test
    public void updateFarmNameTest() {
        doctorFarmManager.updateFarmName(1L, "小");
    }

    @Test
    public void updateFarmOptionsTest() {
        doctorFarmManager.updateFarmOptions(577L, "内蒙古雏鹰第一采精", "11", 1, 1);
        DoctorFarm doctorFarm = doctorFarmDao.findById(1L);

        Assert.assertEquals(doctorFarm.getName(), "内蒙古雏鹰第一采精站");
        Assert.assertEquals(doctorFarm.getNumber(), "11");
        Assert.assertEquals(doctorFarm.getIsWeak().toString(), "1");
        Assert.assertEquals(doctorFarm.getIsIntelligent().toString(), "1");

    }

    @Test
    public void findByNameTest() {
        List<DoctorFarm> doctorFarmList = doctorFarmDao.findByName("小");
        Assert.assertEquals(doctorFarmList.size(), 1);
    }
}
