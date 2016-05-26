package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.enums.DataRange;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
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
public class DoctorPigReadServiceTest extends BaseServiceTest{

    @Autowired
    private DoctorPigReadService doctorPigReadService;

    @Test
    public void testBasicQuery(){
        Response<Long> response =
                doctorPigReadService.queryPigCount(DataRange.FARM.getKey(), 12345l, DoctorPig.PIG_TYPE.SOW.getKey());
        Assert.assertTrue(response.isSuccess());
        Assert.assertEquals(response.getResult(), new Long(5));

        Response<Paging<DoctorPigInfoDto>> pagingResponse = doctorPigReadService.pagingDoctorInfoDtoByPig(DoctorPig.builder().farmId(12345l).build(), 1, 2);
        Assert.assertTrue(pagingResponse.isSuccess());
        List<DoctorPigInfoDto> list = pagingResponse.getResult().getData();
        Assert.assertEquals(list.size(),  2);

        Response<Paging<DoctorPigInfoDto>> trackResponse = doctorPigReadService.pagingDoctorInfoDtoByPigTrack(DoctorPigTrack.builder().farmId(12345l).build(), 1, 2);
        Assert.assertTrue(trackResponse.isSuccess());
        List<DoctorPigInfoDto> doctorPigInfoDtos = trackResponse.getResult().getData();
        Assert.assertEquals(doctorPigInfoDtos.size(),2);
    }



    private DoctorPig buildDoctorPig(){
        DoctorPig doctorPig = new DoctorPig();
        doctorPig.setFarmId(12345l);
        return doctorPig;
    }
}
