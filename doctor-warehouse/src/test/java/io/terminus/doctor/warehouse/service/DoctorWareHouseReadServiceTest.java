package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;

/**
 * Created by yaoqijun.
 * Date:2016-05-25
 * Email:yaoqj@terminus.io
 * Descirbe: 测试
 */
public class DoctorWareHouseReadServiceTest extends BasicServiceTest{

    @Autowired
    private DoctorWareHouseReadService doctorWareHouseReadService;

    @Test
    public void testFarmWareHouseTypeQuery(){
        Response<List<DoctorFarmWareHouseType>> response = doctorWareHouseReadService.queryDoctorFarmWareHouseType(12345l);
        Assert.assertTrue(response.isSuccess());
        List<DoctorFarmWareHouseType> types = response.getResult();
        Assert.assertThat(types.size(), is(5));
        Assert.assertThat(123, is(123));
    }

}
