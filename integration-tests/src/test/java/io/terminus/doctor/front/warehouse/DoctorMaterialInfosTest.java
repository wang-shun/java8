package io.terminus.doctor.front.warehouse;

import configuration.front.FrontWebConfiguration;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInfoDao;
import io.terminus.doctor.warehouse.enums.WareHouseType;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import io.terminus.doctor.web.front.warehouse.dto.DoctorMaterialInfoCreateDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import utils.HttpGetRequest;

/**
 * Created by yaoqijun.
 * Date:2016-06-02
 * Email:yaoqj@terminus.io
 * Descirbe: 原料信息内容测试
 */
@SpringApplicationConfiguration(FrontWebConfiguration.class)
public class DoctorMaterialInfosTest extends BaseFrontWebTest{

    @Autowired
    private DoctorMaterialInfoDao doctorMaterialInfoDao;

    @Test
    public void testCreateMaterial(){
        DoctorMaterialInfoCreateDto doctorMaterialInfoCreateDto = DoctorMaterialInfoCreateDto.builder()
                .farmId(12345l).type(WareHouseType.FEED.getKey()).materialName("createMaterialName")
                .mark("materialCreateMark").unitId(1l).unitGroupId(1l).defaultConsumeCount(1000l).price(1000l)
                .build();

        String url = "http://localhost:" + this.port + "/api/doctor/warehouse/materialInfo/create";
        Long result = this.restTemplate.postForObject(url, doctorMaterialInfoCreateDto, Long.class);
        System.out.println(result);
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorMaterialInfoDao.findById(result)));

        url = "http://localhost:" + this.port + "/api/doctor/warehouse/materialInfo/queryMaterialById?id="+result;
        DoctorMaterialInfo doctorMaterialInfo = this.restTemplate.getForObject(url, DoctorMaterialInfo.class);
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorMaterialInfo));
    }

    @Test
    public void testPagingMaterialInfo(){
        String url = "http://localhost:" + this.port + "/api/doctor/warehouse/materialInfo/pagingMaterialInfo";
        Object result = this.restTemplate.getForObject((HttpGetRequest.url(url).
                        params("farmId", 12345l)).params("type", 1).params("pageNo", 1).params("pageSize", 10).build(),
                Object.class);
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(result));
    }
}
