package io.terminus.doctor.front.warehouse;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.basic.dao.DoctorWareHouseDao;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.web.front.warehouse.dto.DoctorWareHouseCreateDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-06-02
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@SpringApplicationConfiguration(FrontWebConfiguration.class)
public class DoctorWareHouseQueryTest extends BaseFrontWebTest{

    @Autowired
    private DoctorWareHouseDao doctorWareHouseDao;

    private Map<String,Object> urlParam = ImmutableMap.of("port", this.port);

    @Test
    public void testListWareHouseType(){
        String url = "http://localhost:{port}/api/doctor/warehouse/query/listWareHouseType?farmId={farmId}";
        ResponseEntity responseEntity = this.restTemplate.getForEntity(url, Object.class,
                ImmutableMap.of("port",this.port, "farmId", 12345));
        System.out.println(JsonMapper.JSON_NON_EMPTY_MAPPER.toJson(responseEntity.getBody()));
    }

    @Test
    public void testCreateWareHouseInfo(){
        DoctorWareHouseCreateDto doctorWareHouseCreateDto = DoctorWareHouseCreateDto.builder()
                .wareHouseName("createWareHouseName").farmId(12345l).address("createAddress")
                .managerId(1L).type(WareHouseType.FEED.getKey())
                .build();

        String url = "http://localhost:"+this.port+"/api/doctor/warehouse/query/createWareHouse";
        Long result = this.restTemplate.postForObject(url, doctorWareHouseCreateDto, Long.class);

        // 校验数据插入
        System.out.println(result);
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorWareHouseDao.findById(result)));
    }

    @Test
    public void testPagingWareHouse(){
        String url = "http://localhost:"+this.port+
                "/api/doctor/warehouse/query/pagingDoctorWareHouseDto?farmId={farmId}&type={type}&pageNo={pageNo}&pageSize={pageSize}";
        Object result = this.restTemplate.getForEntity(url, Object.class, ImmutableMap.of("farmId",12345l, "type", 1, "pageNo",1, "pageSize", 2)).getBody();
        System.out.println(JsonMapper.JSON_NON_EMPTY_MAPPER.toJson(result));
    }
}
