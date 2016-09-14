package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
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

    @Test
    public void testPagingPigEvent(){

        Response<Paging<DoctorPigEvent>> response = doctorPigEventReadService.queryPigDoctorEvents(12345l, 1l, 1, 10, null, null);
        Assert.assertTrue(response.isSuccess());
        List<DoctorPigEvent> doctorPigEvents = response.getResult().getData();
        Assert.assertEquals(doctorPigEvents.size(), 1);
    }

    @Test
    public void testQueryPigEventsByCriteria(){
        try {
//            DoctorPigEventSearchDto doctorPigEventSearchDto = new DoctorPigEventSearchDto();
//            //doctorPigEventSearchDto.setOperatorName("");
//            doctorPigEventSearchDto.setBeginDate(new SimpleDateFormat("yyyy-MM-dd").parse("2016-05-07"));
//            doctorPigEventSearchDto.setEndDate(new Date());
//            doctorPigEventSearchDto.setType(PigEvent.MATING.getKey());
//
//            Paging<DoctorPigEvent> pigEventPaging = RespHelper.orServEx(doctorPigEventReadService.queryPigEventsByCriteria(doctorPigEventSearchDto, 0, 5));
//            System.out.println(pigEventPaging.getTotal());
        } catch (Exception e) {

        }


    }
}
