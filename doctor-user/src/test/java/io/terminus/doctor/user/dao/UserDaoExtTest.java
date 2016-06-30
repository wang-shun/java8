package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.parana.user.model.User;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class UserDaoExtTest extends BaseDaoTest{
    @Autowired
    private UserDaoExt userDaoExt;

    @Test
    public void listAll(){
        List<User> users = userDaoExt.listAll();
        Assert.assertEquals(users.size(), 1L);
    }

    @Test
    public void listByUser(){
        User user = new User();
        user.setName("admin");
        List<User> users =  userDaoExt.list(user);
        Assert.assertEquals(users.size(), 1L);
    }

    @Test
    public void listByMap(){
        List<User> users =  userDaoExt.list(ImmutableMap.of("name", "admin"));
        Assert.assertEquals(users.size(), 1L);
    }

    @Test
    public void maxId(){
        Long maxId =  userDaoExt.maxId();
        Assert.assertEquals(maxId.longValue(), 1L);
    }

    @Test
    public void minDate(){
        Date minDate =  userDaoExt.minDate();
        Assert.assertNotNull(minDate);
    }

    @Test
    public void listCreatedSince(){
        List<User> users = userDaoExt.listCreatedSince(DateTime.now().minusYears(1).toDate());
        Assert.assertEquals(users.size(), 1L);
    }
}
