package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseDao;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by yaoqijun.
 * Date:2016-05-25
 * Email:yaoqj@terminus.io
 * Descirbe: 测试WareHouse Writer 功能
 */
public class DoctorWareHouseWriteServiceTest extends BasicServiceTest{

    @Autowired
    private DoctorWareHouseWriteService doctorWareHouseWriteService;

    @Autowired
    private DoctorWareHouseDao doctorWareHouseDao;

    @Test
    public void testCreateWareHouse(){

        DoctorWareHouse doctorWareHouse = buildWareHouse();
        Response<Long> response = doctorWareHouseWriteService.createWareHouse(doctorWareHouse);
        Assert.assertTrue(response.isSuccess());
        Assert.assertNotNull(response.getResult());
        System.out.println("create id is "+response.getResult());

        Long id = response.getResult();
        DoctorWareHouse doctorWareHouse1 = doctorWareHouseDao.findById(id);
        Assert.assertNotNull(doctorWareHouse1);
        Assert.assertEquals(doctorWareHouse1.getAddress(), "farm address");
    }

    private DoctorWareHouse buildWareHouse(){
        DoctorWareHouse doctorWareHouse = new DoctorWareHouse();
        doctorWareHouse.setWareHouseName("warehouseName");
        doctorWareHouse.setFarmId(1l);
        doctorWareHouse.setFarmName("farmName");
        doctorWareHouse.setManagerId(1l);
        doctorWareHouse.setManagerName("managerName");
        doctorWareHouse.setAddress("farm address");
        doctorWareHouse.setType(1);
        doctorWareHouse.setExtraMap(null);
        doctorWareHouse.setCreatorId(1l);
        doctorWareHouse.setCreatorName("creatorName");
        doctorWareHouse.setUpdatorId(1l);
        doctorWareHouse.setUpdatorName("updatorName");
        return doctorWareHouse;
    }
}
