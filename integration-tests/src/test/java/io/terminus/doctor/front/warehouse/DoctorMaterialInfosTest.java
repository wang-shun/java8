package io.terminus.doctor.front.warehouse;

import com.google.api.client.util.Lists;
import configuration.front.FrontWebConfiguration;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.warehouse.dao.DoctorFarmWareHouseTypeDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInfoDao;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseTrackDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialProductRatioDto;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import io.terminus.doctor.web.front.warehouse.dto.DoctorMaterialInfoCreateDto;
import io.terminus.zookeeper.pubsub.Subscriber;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpEntity;
import io.terminus.doctor.utils.HttpGetRequest;
import io.terminus.doctor.utils.HttpPostRequest;

import java.util.List;

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

    @Autowired
    private DoctorFarmWareHouseTypeDao doctorFarmWareHouseTypeDao;

    @Autowired
    private DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao;

    @Autowired
    private DoctorWareHouseTrackDao doctorWareHouseTrackDao;

    @Autowired(required = false)
    private Subscriber subscriber;

    @Test
    public void testCreateMaterial() throws Exception{

        subscriber.subscribe(data->{
            System.out.println(String.valueOf(data));
        });

        DoctorMaterialInfoCreateDto doctorMaterialInfoCreateDto = DoctorMaterialInfoCreateDto.builder()
                .farmId(12345l).type(WareHouseType.FEED.getKey()).materialName("createMaterialName").inputCode("inputCodeTestContent")
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

    @Test
    public void testCreateMaterialRules(){
        String url = "http://localhost:" + this.port + "/api/doctor/warehouse/materialInfo/rules";
        HttpEntity httpEntity = HttpPostRequest.bodyRequest().params(buildDoctorMaterialProductRatioDto());
        Boolean result = this.restTemplate.postForObject(url, httpEntity, Boolean.class);

        System.out.println(result);
        // 校验对应的结果信息内容
        DoctorMaterialInfo doctorMaterialInfo = doctorMaterialInfoDao.findById(1l);
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorMaterialInfo));
    }

    /**
     * 对应的生产方式（Material pre produce） TODO 测试Consumer Count
     */
    @Test
    public void testPreRealProduceMaterial(){
        // 录入对应的规则信息
        String url = "http://localhost:" + this.port + "/api/doctor/warehouse/materialInfo/rules";
        Assert.assertTrue(this.restTemplate.postForObject(url, buildDoctorMaterialProductRatioDto(), Boolean.class));

        // 原料信息的生产方式
        String url2 =
        HttpGetRequest.url("http://localhost:" + this.port + "/api/doctor/warehouse/materialInfo/preProduceMaterial")
                .params("materialId",1l).params("produceCount", 8000000l).build();
        Object objectResult = this.restTemplate.getForEntity(url2, Object.class).getBody();
//        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(objectResult));

        // 具体生产情况
        String url3 = "http://localhost:" + this.port + "/api/doctor/warehouse/materialInfo/realProduceMaterial";
        Object result = this.restTemplate.postForObject(url3,
                HttpPostRequest.formRequest()
                        .param("farmId", 12345l).param("wareHouseId", 1l).param("materialId",1l).param("materialProduce", objectResult)
                        .httpEntity(), Object.class);

        // 输出生产信息
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(result));
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorFarmWareHouseTypeDao.findByFarmId(12345l)));
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorWareHouseTrackDao.listAll()));
        System.out.println(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(doctorMaterialInWareHouseDao.listAll()));
    }

    private DoctorMaterialProductRatioDto buildDoctorMaterialProductRatioDto(){
        // type = 1 饲料生产方式， type =2 (原料) 4 （饲料信息）
        List<DoctorMaterialInfo.MaterialProduceEntry> materialProduceEntryList = Lists.newArrayList();
        materialProduceEntryList.add(DoctorMaterialInfo.MaterialProduceEntry.builder()
                .materialId(5l).materialName("materialName5").materialCount(500000D)
                .build());
        materialProduceEntryList.add(DoctorMaterialInfo.MaterialProduceEntry.builder()
                .materialId(6l).materialName("materialName6").materialCount(500000D)
                .build());

        List<DoctorMaterialInfo.MaterialProduceEntry> medicalProduceEntryList = Lists.newArrayList();
        medicalProduceEntryList.add(DoctorMaterialInfo.MaterialProduceEntry.builder()
                .materialId(14l).materialName("materialName14").materialCount(10D)
                .build());
        medicalProduceEntryList.add(DoctorMaterialInfo.MaterialProduceEntry.builder()
                .materialId(15l).materialName("material15").materialCount(10D)
                .build());

        DoctorMaterialProductRatioDto ratioDto = DoctorMaterialProductRatioDto.builder()
                .materialId(1l).produce(DoctorMaterialInfo.MaterialProduce.builder()
                        .total(1000000D)
                        .materialProduceEntries(materialProduceEntryList)
                        .medicalProduceEntries(medicalProduceEntryList)
                        .build())
                .build();
        return ratioDto;
    }
}
