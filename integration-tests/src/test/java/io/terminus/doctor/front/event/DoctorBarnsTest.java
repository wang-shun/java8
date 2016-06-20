package io.terminus.doctor.front.event;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.web.front.event.controller.DoctorBarns;
import io.terminus.doctor.web.front.event.dto.DoctorBarnDetail;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Desc: 猪舍controller测试类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/7
 */
@Slf4j
@SpringApplicationConfiguration(FrontWebConfiguration.class)
public class DoctorBarnsTest extends BaseFrontWebTest {

    /**
     * 根据id查询猪舍表测试
     * @see DoctorBarns#findBarnById(java.lang.Long)            `
     */
    @Test
    public void findBarnByIdTest() {
        DoctorBarn barn = findBarnById(1L);
        assertNotNull(barn);
        log.info("findBarnByIdTest, result:{}", barn);
    }

    private DoctorBarn findBarnById(Long barnId) {
        String url = "/api/doctor/barn/id";
        ResponseEntity<DoctorBarn> result = getForEntity(url, ImmutableMap.of("barnId", barnId), DoctorBarn.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        return result.getBody();
    }

    /**
     * 根据farmId查询猪舍表测试
     * @see DoctorBarns#findBarnsByfarmId(java.lang.Long)
     */
    @Test
    public void findBarnsByfarmIdTest() {
        String url = "/api/doctor/barn/farmId";
        ResponseEntity<List> result = getForEntity(url, ImmutableMap.of("farmId", 0L), List.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().size(), not(0));
        log.info("findBarnsByfarmIdTest result:{}", result.getBody());
    }

    /**
     * 创建或更新DoctorBarn测试
     * @see DoctorBarns#createOrUpdateBarn(io.terminus.doctor.event.model.DoctorBarn)
     */
    @Test
    public void createOrUpdateBarnTest() {
        String url = "/api/doctor/barn";
        ResponseEntity<Long> result = postForEntity(url, mockBarn(), Long.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));

        DoctorBarn barn = findBarnById(result.getBody());
        assertNotNull(barn);
        log.info("createOrUpdateBarnTest result:{}", barn);
    }

    /**
     * 更新猪舍状态测试
     * @see DoctorBarns#updateBarnStatus(java.lang.Long, java.lang.Integer)
     */
    @Test
    public void updateBarnStatusTest() {
        String url = "/api/doctor/barn/status";
        ResponseEntity<Boolean> result = getForEntity(url, ImmutableMap.of("barnId", 4L, "status", -2), Boolean.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));

        DoctorBarn barn = findBarnById(4L);
        assertThat(barn.getStatus(), is(-2));
        log.info("updateBarnStatusTest, result:{}", barn);
    }

    /**
     * 查询猪舍详情测试
     * @see DoctorBarns#findBarnDetailByBarnId(java.lang.Long, java.lang.Integer, java.lang.Integer)
     */
    @Test
    public void findBarnDetailByBarnIdTest() {
        String url = "/api/doctor/barn/detail";
        ResponseEntity<DoctorBarnDetail> result = getForEntity(url, ImmutableMap.of("barnId", 1L), DoctorBarnDetail.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertNotNull(result.getBody());
        log.info("findBarnDetailByBarnIdTest result:{}", result.getBody());
    }

    private DoctorBarn mockBarn() {
        DoctorBarn barn = new DoctorBarn();
        barn.setName("测试猪场");
        barn.setOrgId(0L);
        barn.setOrgName("测试公司");
        barn.setFarmId(0L);
        barn.setFarmName("测试猪");
        barn.setPigType(2);
        barn.setCanOpenGroup(1);
        barn.setStatus(1);
        barn.setCapacity(50);
        barn.setStaffId(1L);
        barn.setStaffName("admin");
        return barn;
    }
}
