package io.terminus.doctor.user.dao;

import io.terminus.doctor.user.enums.TargetSystem;
import io.terminus.doctor.user.model.UserBind;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class UserBindDaoTest extends BaseDaoTest{
    @Autowired
    private UserBindDao userBindDao;

    @Test
    public void testFindById(){
        UserBind bind = userBindDao.findById(1L);
        Assert.assertNotNull(bind);
        Assert.assertEquals(bind.getId().longValue(), 1L);
    }

    @Test
    public void testFindByUserId(){
        List<UserBind> bind = userBindDao.findByUserId(1L);
        Assert.assertNotNull(bind);
        Assert.assertEquals(bind.size(), 1L);
    }

    @Test
    public void testCreate(){
        UserBind bind = new UserBind();
        bind.setUserId(111L);
        bind.setTargetUserEmail("E");
        bind.setTargetSystem(1);
        bind.setTargetUserMobile("m");
        bind.setTargetUserName("n");
        bind.setUuid("uid");
        Boolean res = userBindDao.create(bind);
        Assert.assertTrue(res);
    }

    @Test
    public void testDelete(){
        Boolean res = userBindDao.delete(1L);
        Assert.assertTrue(res);
    }

    @Test
    public void testUpdate(){
        UserBind bind = userBindDao.findById(1L);
        bind.setTargetUserEmail("eee");
        Boolean res = userBindDao.update(bind);
        Assert.assertTrue(res);
    }

    @Test
    public void findByUserIdAndTargetSystem(){
        userBindDao.findByUserIdAndTargetSystem(1L, TargetSystem.PIGMALL);
    }
}
