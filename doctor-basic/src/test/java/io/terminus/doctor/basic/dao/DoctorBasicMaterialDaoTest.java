package io.terminus.doctor.basic.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by sunbo@terminus.io on 2017/11/9.
 */
public class DoctorBasicMaterialDaoTest extends BaseDaoTest{

    @Autowired
    private DoctorBasicMaterialDao DoctorBasicMaterialDao;

    @Test
    public void testFindByType(){
        Assert.assertEquals(3,DoctorBasicMaterialDao.findByType(2).size());
    }
}
