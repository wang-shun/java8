package io.terminus.doctor.event.dao;

import io.terminus.doctor.event.model.DoctorDemo;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xjn on 18/3/29.
 * email:xiaojiannan@terminus.io
 */
public class DoctorDemoDaoTest extends BaseDaoTest {
    @Autowired
    DoctorDemoDao doctorDemoDao;
    @Test
    public void findByNameTest() {
        DoctorDemo doctorDemo =  doctorDemoDao.findByName("xiao");
        Assert.assertNotNull(doctorDemo);
    }
}
