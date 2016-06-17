package io.terminus.doctor.front.event;

import configuration.front.FrontWebConfiguration;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.RandomUtil;
import io.terminus.doctor.event.model.DoctorVaccinationPigWarn;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.web.front.event.controller.DoctorVaccinations;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpEntity;
import utils.HttpGetRequest;
import utils.HttpPostRequest;

import java.io.IOException;
import java.util.Date;

/**
 * Desc: 疫苗注射程序
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/17
 */
@Slf4j
@SpringApplicationConfiguration(FrontWebConfiguration.class)
public class DoctorVaccinationsTest extends BaseFrontWebTest {

    private static JsonMapper MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER;

    /**
     * 创建
     * @see DoctorVaccinations#createOrUpdateVaccWarns(DoctorVaccinationPigWarn)
     * @see DoctorVaccinations#findVaccById(Long)
     */
    @Test
    public void test_CREATE_VaccWarns() {
        // 创建
        String url = "http://localhost:" + port + "/api/doctor/vacc/warns";
        HttpEntity entity = HttpPostRequest.bodyRequest()
                .params(MAPPER.toJson(create()));
        this.restTemplate.postForObject(url, entity, Object.class);

        // 查询
        url = "http://localhost:" + port + "/api/doctor/vacc/warns/detail?id=1";
        Object object = this.restTemplate.getForObject(url, Object.class);
        Assert.assertNotNull(object);
    }

    /**
     * 删除
     * @see DoctorVaccinations#deleteVaccWarns(Long)
     */
    @Test
    public void test_DELETE_VaccWarns() {
        // 创建
        String url = "http://localhost:" + port + "/api/doctor/vacc/warns";
        HttpEntity entity = HttpPostRequest.bodyRequest()
                .params(MAPPER.toJson(create()));
        this.restTemplate.postForObject(url, entity, Object.class);

        // 删除
        url = "http://localhost:" + port + "/api/doctor/vacc/warns?id=1";
        this.restTemplate.delete(url);

        // 查询
        url = "http://localhost:" + port + "/api/doctor/vacc/warns/detail?id=1";
        Object object = this.restTemplate.getForObject(url, Object.class);
        Assert.assertNull(object);
    }

    /**
     * 分页查询
     * @see DoctorVaccinations#pagingVaccPigWarns(Integer, Integer, Long)
     */
    @Test
    public void test_PAGING_VaccWarns() throws IOException {
        // 创建
        String url = "http://localhost:" + port + "/api/doctor/vacc/warns";
        for (int i = 0; i < 8; i++) {
            HttpEntity entity = HttpPostRequest.bodyRequest()
                    .params(MAPPER.toJson(create()));
            this.restTemplate.postForObject(url, entity, Object.class);
        }

        // 查询
        url = HttpGetRequest.url(url)
                .params("pageNo", 1)
                .params("pageSize", 5)
                .params("farmId", 1L)
                .build();
        Object json = this.restTemplate.getForObject(url, Object.class);
        System.out.println(json); // Paging<DoctorVaccinationPigWarn>
    }


    private DoctorVaccinationPigWarn create() {
        Long random = (long) RandomUtil.random(1, 9);
        return DoctorVaccinationPigWarn.builder()
                .farmId(1L)
                .farmName("猪场" + random)
                .pigType(random.intValue())
                .startDate(new Date())
                .endDate(new Date())
                .materialId(random)
                .materialName("疫苗" + random)
                .inputValue(random.intValue() + 20)
                .dose(random + 10)
                .remark("测试测试测试")
                .build();
    }
}
