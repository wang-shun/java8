package io.terminus.doctor.user.dao;

import io.terminus.doctor.user.model.PrimaryUser;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PrimaryUserDaoTest extends BaseDaoTest {
    @Autowired
    private PrimaryUserDao primaryUserDao;

    @Test
    public void testFindById(){
        PrimaryUser primaryUser = primaryUserDao.findById(1L);
        Assert.assertNotNull(primaryUser);
        Assert.assertEquals(primaryUser.getId().longValue(), 1L);
    }

    @Test
    public void testFindByUserId(){
        PrimaryUser primaryUser = primaryUserDao.findByUserId(1L);
        Assert.assertNotNull(primaryUser);
        Assert.assertEquals(primaryUser.getUserId().longValue(), 1L);
    }

    @Test
    public void testCreate(){
        PrimaryUser primaryUser = new PrimaryUser();
        primaryUser.setUserId(111L);
        primaryUser.setStatus(1);
        primaryUser.setUserName("vvv");
        Boolean res = primaryUserDao.create(primaryUser);
        Assert.assertTrue(res);
    }

    @Test
    public void testDelete(){
        Boolean res = primaryUserDao.delete(1L);
        Assert.assertTrue(res);
    }

    @Test
    public void testUpdate(){
        PrimaryUser primaryUser = primaryUserDao.findById(1L);
        primaryUser.setStatus(2);
        Boolean res = primaryUserDao.update(primaryUser);
        Assert.assertTrue(res);
    }
}
