package io.terminus.doctor.user.dao;

import io.terminus.doctor.user.model.SubRole;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SubRoleDaoTest extends BaseDaoTest{
    @Autowired
    private SubRoleDao subRoleDao;

    @Test
    public void testFindById(){
        SubRole subRole = subRoleDao.findById(1L);
        Assert.assertNotNull(subRole);
        Assert.assertEquals(subRole.getId().longValue(), 1L);
    }

    @Test
    public void testfindByUserIdAndStatus(){
        List<SubRole> subRole = subRoleDao.findByUserIdAndStatus("MOBILE", 1L, 1);
        Assert.assertNotNull(subRole);
        Assert.assertEquals(subRole.size(), 1L);
    }

    @Test
    public void testCreate(){
        SubRole subRole = new SubRole();
        subRole.setUserId(111L);
        subRole.setStatus(1);
        subRole.setAppKey("mobile");
        Boolean res = subRoleDao.create(subRole);
        Assert.assertTrue(res);
    }

    @Test
    public void testDelete(){
        Boolean res = subRoleDao.delete(1L);
        Assert.assertTrue(res);
    }

    @Test
    public void testUpdate(){
        SubRole subRole = subRoleDao.findById(1L);
        subRole.setStatus(2);
        Boolean res = subRoleDao.update(subRole);
        Assert.assertTrue(res);
    }
}
