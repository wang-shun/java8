package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.IotBarnWithStorage;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-26
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public class DoctorPigEventReadServiceTest extends BaseServiceTest{

    @Autowired
    private DoctorPigEventReadService doctorPigEventReadService;
    @Autowired
    private DoctorGroupReadService doctorGroupReadService;
    @Autowired
    private DoctorBarnReadService doctorBarnReadService;
    @Autowired
    private DoctorPigReadService doctorPigReadService;

    @Test
    public void testPagingPigEvent(){

        Response<Paging<DoctorPigEvent>> response = doctorPigEventReadService.queryPigDoctorEvents(12345l, 1l, 1, 10, null, null);
        Assert.assertTrue(response.isSuccess());
        List<DoctorPigEvent> doctorPigEvents = response.getResult().getData();
        Assert.assertEquals(doctorPigEvents.size(), 1);

    }

    @Test
    public void testSelectBarns() {
        List<DoctorBarn> list = RespHelper.orServEx(doctorBarnReadService.selectBarns(1L, 1L, "妊娠",10));
        System.out.println(list);
    }

    @Test
    public void testQueryFattenOut() {
        System.out.println(doctorGroupReadService.queryFattenOutBySumAt("2016-10-10"));
    }

    @Test
    public void testSuggestSowPig() {
        System.out.println(RespHelper.orServEx(doctorPigReadService.suggestSowPig(21L, "l", 10)));
    }

    @Test
    public void IotBarnWithStorageTest() {
        List<IotBarnWithStorage> barnWithStorageList = RespHelper.orServEx(doctorBarnReadService.findIotBarnWithStorage(1L));
        System.out.println(barnWithStorageList);
    }
}
