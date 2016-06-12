package io.terminus.doctor.front.warehouse;

import configuration.front.FrontWebConfiguration;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.web.front.warehouse.dto.DoctorConsumeProviderInputDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import utils.HttpGetRequest;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-06-03
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@SpringApplicationConfiguration(FrontWebConfiguration.class)
public class DoctorWareHouseEventsTest extends BaseFrontWebTest{

    @Autowired
    private DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao;

    @Test
    public void testListMaterialInWareHouse(){
        String url = "http://localhost:"+this.port+"/api/doctor/warehouse/event/list";
        String urlWithParam = HttpGetRequest.url(url).params("farmId",12345l).params("wareHouseId",8l).build();
        Object o  = this.restTemplate.getForObject(urlWithParam, Object.class);
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(o));
    }

    @Test
    public void testPagingMaterialInWareHouse(){
        String url = "http://localhost:"+this.port+"/api/doctor/warehouse/event/paging";
        String urlWithParam = HttpGetRequest.url(url).params("farmId", 12345l).params("wareHouseId",6).params("pageNo",1).params("pageSize",3).build();
        Object o  = this.restTemplate.getForObject(urlWithParam, Object.class);
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(o));
    }

    @Test
    public void testProviderMaterialInWareHouse(){
        DoctorConsumeProviderInputDto dto = DoctorConsumeProviderInputDto.builder()
                .farmId(12345l).wareHouseId(2l).materialId(5l).barnId(5l).feederId(1l).count(1000l).consumeDays(10)
                .build();
        String url = "http://localhost:"+this.port+"/api/doctor/warehouse/event/provider";
        Long result = this.restTemplate.postForObject(url, dto, Long.class);
        System.out.println(result);

        List<DoctorMaterialInWareHouse> doctorMaterialInWareHouseList =
                doctorMaterialInWareHouseDao.queryByFarmAndWareHouseId(12345l, 2l);

        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorMaterialInWareHouseList));
    }

    /**
     * 测试消耗 原料信息
     */
    @Test
    public void testConsumeMaterialInWareHouse(){

        DoctorConsumeProviderInputDto dto = DoctorConsumeProviderInputDto.builder()
                .farmId(12345l).wareHouseId(2l).materialId(5l).barnId(5l).feederId(1l).count(50000l).consumeDays(10)
                .build();

        String url = "http://localhost:"+this.port+"/api/doctor/warehouse/event/consume";
        Long result = this.restTemplate.postForObject(url, dto, Long.class);
        System.out.println(result);

        List<DoctorMaterialInWareHouse> doctorMaterialInWareHouseList =
                doctorMaterialInWareHouseDao.queryByFarmAndWareHouseId(12345l, 2l);

        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorMaterialInWareHouseList));
    }
}
