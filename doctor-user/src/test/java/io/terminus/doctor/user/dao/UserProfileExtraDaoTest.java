package io.terminus.doctor.user.dao;

import com.google.common.collect.Lists;
import io.terminus.parana.user.model.UserProfile;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class UserProfileExtraDaoTest extends BaseDaoTest{
    @Autowired
    private UserProfileExtraDao userProfileExtraDao;


    @Test
    public void testFindByUserId(){
        List<UserProfile> userProfile = userProfileExtraDao.findByUserIds(Lists.newArrayList(1L));
        Assert.assertNotNull(userProfile);
        Assert.assertEquals(userProfile.size(), 1L);
    }


}
