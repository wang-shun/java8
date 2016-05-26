package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-25
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public class DoctorMaterialInWareHouseReadServiceTest extends BasicServiceTest{

    @Autowired
    private DoctorMaterialInWareHouseReadService doctorMaterialInWareHouseReadService;

    @Test
    public void testListMaterialWareHouseReadInfo(){

        Response<List<DoctorMaterialInWareHouse>> listResponse = doctorMaterialInWareHouseReadService.queryDoctorMaterialInWareHouse(12345l, 1l);
        Assert.assertTrue(listResponse.isSuccess());
        Assert.assertEquals(listResponse.getResult().size(),1);
        Assert.assertEquals(listResponse.getResult().get(0).getLotNumber(),new Long(1000));
    }
}
