package io.terminus.doctor.event.dao;

import io.terminus.doctor.event.model.DoctorGroup;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by xjn on 17/12/19.
 * email:xiaojiannan@terminus.io
 */
public class DoctorGroupDaoTest extends BaseDaoTest {
    @Autowired
    private DoctorGroupDao doctorGroupDao;

    @Test
    public void listOpenGroupsByTest() {
        List<DoctorGroup> groupList = doctorGroupDao.listOpenGroupsBy("2017-01-01");
        Assert.assertEquals(groupList.size(), 1620);
    }
}
