package io.terminus.doctor.front.event;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.RandomUtil;
import io.terminus.doctor.event.model.DoctorVaccinationPigWarn;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.web.front.event.controller.DoctorVaccinations;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
        String url = "/api/doctor/vacc/warns";
        ResponseEntity<Boolean> response = postForEntity(url, create(), Boolean.class);
        Assert.assertThat(response.getStatusCode(), Is.is(HttpStatus.OK));

        // 查询
        url = "/api/doctor/vacc/warns/detail";
        DoctorVaccinationPigWarn warn = getForEntity(url, ImmutableMap.of("id", 1), DoctorVaccinationPigWarn.class).getBody();
        log.info("warn is {}", warn);
        Assert.assertNotNull(warn);
    }

    /**
     * 删除
     * @see DoctorVaccinations#deleteVaccWarns(Long)
     */
    @Test
    public void test_DELETE_VaccWarns() {
        // 创建
        String url = "/api/doctor/vacc/warns";
        ResponseEntity<Boolean> response = postForEntity(url, create(), Boolean.class);
        Assert.assertThat(response.getStatusCode(), Is.is(HttpStatus.OK));

        // 删除
        url = "/api/doctor/vacc/warns";
        delete(url, ImmutableMap.of("id", 1));

        // 查询
        url = "/api/doctor/vacc/warns/detail";
        DoctorVaccinationPigWarn warn = getForEntity(url, ImmutableMap.of("id", 1), DoctorVaccinationPigWarn.class).getBody();
        Assert.assertNull(warn);
    }

    /**
     * 分页查询
     * @see DoctorVaccinations#pagingVaccPigWarns(Integer, Integer, Long)
     */
    @Test
    public void test_PAGING_VaccWarns() throws IOException {
        // 创建
        String url = "/api/doctor/vacc/warns";
        for (int i = 0; i < 8; i++) {
            postForEntity(url, create(), Boolean.class);
        }

        // 查询
        Object json = getForEntity(url, ImmutableMap.of("pageNo", 1, "pageSize", 5, "farmId", 1L), Object.class).getBody();
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
