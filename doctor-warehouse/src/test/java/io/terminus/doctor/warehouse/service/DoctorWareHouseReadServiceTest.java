package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseTrackDao;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseDto;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import io.terminus.doctor.warehouse.model.DoctorWareHouseTrack;
import org.junit.Assert;
import org.junit.Before;
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

    @Autowired
    private DoctorWareHouseDao doctorWareHouseDao;

    @Autowired
    private DoctorWareHouseTrackDao doctorWareHouseTrackDao;

    @Before
    public void initWareHouseTrackInfo(){
        List<DoctorWareHouse> wareHouses = doctorWareHouseDao.findByFarmId(12345l);
        wareHouses.forEach(houses->{
            doctorWareHouseTrackDao.create(DoctorWareHouseTrack.builder()
                    .wareHouseId(houses.getId()).farmId(houses.getFarmId()).farmName(houses.getFarmName())
                    .managerId(houses.getManagerId()).managerName(houses.getManagerName())
                    .materialLotNumber(null).lotNumber(1000l).isDefault(1).extraMap(null)
                    .build());
        });
    }


    @Test
    public void testFarmWareHouseTypeQuery(){
        Response<List<DoctorFarmWareHouseType>> response = doctorWareHouseReadService.queryDoctorFarmWareHouseType(12345l);
        Assert.assertTrue(response.isSuccess());

        List<DoctorFarmWareHouseType> types = response.getResult();
        Assert.assertThat(types.size(), is(5));

        // validate each item
        types.stream().forEach(t->{
            Assert.assertEquals(t.getFarmId(), new Long(12345l));
            Assert.assertEquals(t.getLogNumber(), new Long(200));
        });
        Assert.assertThat(123, is(123));
    }

    /**
     * 测试对应的分页信息
     */
    @Test
    public void queryDoctorWarehouseDtoTest(){
        Response<Paging<DoctorWareHouseDto>> response = doctorWareHouseReadService.queryDoctorWarehouseDto(12345l,1,2);
        Assert.assertTrue(response.isSuccess());

        Paging<DoctorWareHouseDto> dtoPaging = response.getResult();
        Assert.assertEquals(dtoPaging.getTotal(),new Long(5l));

        List<DoctorWareHouseDto> doctorWareHouseDtos = dtoPaging.getData();
        Assert.assertEquals(2, doctorWareHouseDtos.size());
        doctorWareHouseDtos.forEach(dto->{
            Assert.assertEquals(dto.getRemainder(), new Long(1000l));
        });
    }
}
