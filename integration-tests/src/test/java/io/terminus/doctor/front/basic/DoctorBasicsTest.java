package io.terminus.doctor.front.basic;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.doctor.basic.model.DoctorDisease;
import io.terminus.doctor.front.BaseFrontWebTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Desc: 基础数据controller测试类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/7
 */
@Slf4j
@SpringApplicationConfiguration(FrontWebConfiguration.class)
public class DoctorBasicsTest extends BaseFrontWebTest {

    /**
     * 查询所有品种测试
     */
    @Test
    public void finaAllBreedTest() {
        String url = "/api/doctor/basic/breed/all";
        ResponseEntity<List> result= getForEntity(url, null, List.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().size(), not(0));
        log.info("finaAllBreedTest result:{}", result.getBody());
    }

    /**
     * 查询所有品种测试
     */
    @Test
    public void finaAllGeneticTest() {
        String url = "/api/doctor/basic/genetic/all";
        ResponseEntity<List> result= getForEntity(url, null, List.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().size(), not(0));
        log.info("finaAllGeneticTest result:{}", result.getBody());
    }

    /**
     * 查询疾病详情测试
     */
    @Test
    public void findDiseaseByIdTest() {
        DoctorDisease disease = findDiseaseById(3L);
        assertNotNull(disease);
        log.info("findDiseaseByIdTest result:{}", disease);
    }

    /**
     * 根据farmId查询疾病列表测试
     */
    @Test
    public void findDiseaseByfarmIdTest() {
        String url = "/api/doctor/basic/disease/farmId";
        ResponseEntity<List> result = getForEntity(url, ImmutableMap.of("farmId", 0L), List.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().size(), not(0));
        log.info("findDiseaseByfarmIdTest result:{}", result.getBody());
    }

    /**
     * 创建或更新疾病表测试
     */
    @Test
    public void createOrUpdateDiseaseTest() {
        String url = "/api/doctor/basic/disease";
        ResponseEntity<Long> result = postForEntity(url, mockDisease(), Long.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));

        DoctorDisease disease = findDiseaseById(result.getBody());
        assertNotNull(disease);
        log.info("createOrUpdateDiseaseTest result:{}", disease);
    }

    /**
     * 根据主键id删除DoctorDisease测试
     */
    @Test
    public void deleteDiseaseTest() {
        Long deleteId = 1L;
        String url = "/api/doctor/basic/disease";
        deleteForEntity(url, ImmutableMap.of("diseaseId", deleteId));

        DoctorDisease disease = findDiseaseById(deleteId);
        assertNull(disease);
    }

    private DoctorDisease findDiseaseById(Long diseaseId) {
        String url = "/api/doctor/basic/disease/id";
        ResponseEntity<DoctorDisease> result = getForEntity(url, ImmutableMap.of("diseaseId", diseaseId), DoctorDisease.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        return result.getBody();
    }

    /**
     * 根据id查询变动类型表测试
     */
    @Test
    public void findChangeTypeByIdTest() {

    }

    /**
     * 根据farmId查询变动类型表测试
     */
    @Test
    public void findChangeTypesByfarmIdTest() {

    }

    /**
     * 创建或更新DoctorChangeType测试
     */
    @Test
    public void createOrUpdateChangeTypeTest() {

    }

    /**
     * 根据主键id删除DoctorChangeType测试
     */
    @Test
    public void deleteChangeTypeTest() {

    }

    /**
     * 根据id查询变动原因表测试
     */
    @Test
    public void findChangeReasonByIdTest() {

    }

    /**
     * 根据变动类型id查询变动原因表测试
     */
    @Test
    public void findChangeReasonByChangeTypeIdTest() {

    }

    /**
     * 创建或更新DoctorChangeReason测试
     */
    @Test
    public void createOrUpdateChangeReasonTest() {

    }

    /**
     * 根据主键id删除DoctorChangeReason测试
     */
    @Test
    public void deleteChangeReasonTest() {

    }

    /**
     * 根据id查询客户表测试
     */
    @Test
    public void findCustomerByIdTest() {

    }

    /**
     * 根据farmId查询客户表测试
     */
    @Test
    public void findCustomersByfarmIdTest() {

    }

    /**
     * 创建或更新DoctorCustomer测试
     */
    @Test
    public void createOrUpdateCustomerTest() {

    }

    /**
     * 根据主键id删除DoctorCustomer测试
     */
    @Test
    public void deleteCustomerTest() {

    }

    /**
     * 查询所有计量单位测试
     */
    @Test
    public void finaAllUnitsTest() {

    }

    private DoctorDisease mockDisease() {
        DoctorDisease disease = new DoctorDisease();
        disease.setName("病");
        disease.setFarmId(1L);
        disease.setFarmName("猪场猪场");
        return disease;
    }
}
