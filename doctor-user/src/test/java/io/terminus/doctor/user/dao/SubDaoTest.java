package io.terminus.doctor.user.dao;

import io.terminus.doctor.user.model.Sub;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SubDaoTest extends BaseDaoTest{
    @Autowired
    private SubDao subDao;

    @Test
    public void testFindById(){
        Sub sub = subDao.findById(1L);
        Assert.assertNotNull(sub);
        Assert.assertEquals(sub.getId().longValue(), 1L);
    }

    @Test
    public void testFindByUserId(){
        Sub sub = subDao.findByUserId(1L);
        Assert.assertNotNull(sub);
        Assert.assertEquals(sub.getUserId().longValue(), 1L);
    }

    @Test
    public void testCreate(){
        Sub sub = new Sub();
        sub.setUserId(111L);
        sub.setStatus(1);
        sub.setUserName("name");
        sub.setParentUserId(1L);
        Boolean res = subDao.create(sub);
        Assert.assertTrue(res);
    }

    @Test
    public void testDelete(){
        Boolean res = subDao.delete(1L);
        Assert.assertTrue(res);
    }

    @Test
    public void testUpdate(){
        Sub sub = subDao.findById(1L);
        sub.setStatus(1);
        Boolean res = subDao.update(sub);
        Assert.assertTrue(res);
    }
    @Test
    public void findByParentUserIdAndUserId(){
        Sub sub = subDao.findByParentUserIdAndUserId(1l, 1L);
        Assert.assertNotNull(sub);
    }

    @Test
    public void findAllActiveSubs(){
        List<Sub> list = subDao.findAllActiveSubs();
        Assert.assertNotNull(list);
    }
}
