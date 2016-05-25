package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by yaoqijun.
 * Date:2016-05-25
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public class DoctorMaterialInfoReadServiceTest extends BasicServiceTest {


    @Autowired
    private DoctorMaterialInfoReadService doctorMaterialInfoReadService;

    @Test
    public void testPagingMaterialInfo(){
        Response<Paging<DoctorMaterialInfo>> pagingResponse = doctorMaterialInfoReadService.pagingMaterialInfos(1l, null, 1, 2);
        Assert.assertTrue(pagingResponse.isSuccess());
        Assert.assertEquals(pagingResponse.getResult().getTotal(), new Long(5));
        Assert.assertEquals(pagingResponse.getResult().getData().size(), 2);

        // validate
        Response<Paging<DoctorMaterialInfo>> pagingResponse2 = doctorMaterialInfoReadService.pagingMaterialInfos(1l, 1 , null, null);
        Assert.assertTrue(pagingResponse2.isSuccess());
        Assert.assertEquals(pagingResponse2.getResult().getTotal(), new Long(1));
        Assert.assertEquals(pagingResponse2.getResult().getData().size(), 1);
    }


    @Test
    public void queryByIdTest(){
        Response<DoctorMaterialInfo> doctorMaterialInfoResponse = doctorMaterialInfoReadService.queryById(1l);
        Assert.assertTrue(doctorMaterialInfoResponse.isSuccess());

        DoctorMaterialInfo doctorMaterialInfo = doctorMaterialInfoResponse.getResult();
        Assert.assertEquals(doctorMaterialInfo.getFarmName(),"farmName");
    }
}
