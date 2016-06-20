package io.terminus.doctor.front.basic;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorChangeType;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.basic.model.DoctorDisease;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.web.front.basic.controller.DoctorBasics;
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
     * @see DoctorBasics#finaAllBreed()
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
     * @see DoctorBasics#finaAllGenetic()
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
     * @see DoctorBasics#findDiseaseById(java.lang.Long)
     */
    @Test
    public void findDiseaseByIdTest() {
        DoctorDisease disease = findDiseaseById(3L);
        assertNotNull(disease);
        log.info("findDiseaseByIdTest result:{}", disease);
    }

    /**
     * 根据farmId查询疾病列表测试
     * @see DoctorBasics#findDiseaseByfarmId(java.lang.Long)
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
     * @see @see DoctorBasics#createOrUpdateDisease(io.terminus.doctor.basic.model.DoctorDisease)
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
     * @see DoctorBasics#deleteDisease(java.lang.Long)
     */
    @Test
    public void deleteDiseaseTest() {
        Long deleteId = 1L;
        String url = "/api/doctor/basic/disease";
        delete(url, ImmutableMap.of("diseaseId", deleteId));

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
     * @see DoctorBasics#findChangeTypeById(java.lang.Long)
     */
    @Test
    public void findChangeTypeByIdTest() {
        DoctorChangeType changeType = findChangeTypeById(2L);
        assertNotNull(changeType);
        log.info("findChangeTypeByIdTest result:{}", changeType);
    }

    private DoctorChangeType findChangeTypeById(Long changeTypeId) {
        String url = "/api/doctor/basic/changeType/id";
        ResponseEntity<DoctorChangeType> result = getForEntity(url, ImmutableMap.of("changeTypeId", changeTypeId), DoctorChangeType.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        return result.getBody();
    }

    /**
     * 根据farmId查询变动类型表测试
     * @see DoctorBasics#findChangeTypesByfarmId(java.lang.Long)
     */
    @Test
    public void findChangeTypesByfarmIdTest() {
        String url = "/api/doctor/basic/changeType/farmId";
        ResponseEntity<List> result = getForEntity(url, ImmutableMap.of("farmId", 0L), List.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().size(), not(0));
        log.info("findChangeTypesByfarmIdTest result:{}", result.getBody());
    }

    /**
     * 创建或更新DoctorChangeType测试
     * @see DoctorBasics#createOrUpdateChangeType(io.terminus.doctor.basic.model.DoctorChangeType)
     */
    @Test
    public void createOrUpdateChangeTypeTest() {
        String url = "/api/doctor/basic/changeType";
        ResponseEntity<Long> result = postForEntity(url, mockChangeType(), Long.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));

        DoctorChangeType changeType = findChangeTypeById(result.getBody());
        assertNotNull(changeType);
        log.info("createOrUpdateChangeTypeTest result:{}", changeType);
    }

    /**
     * 根据主键id删除DoctorChangeType测试
     * @see DoctorBasics#deleteChangeType(java.lang.Long)
     */
    @Test
    public void deleteChangeTypeTest() {
        Long deleteId = 1L;
        String url = "/api/doctor/basic/changeType";
        delete(url, ImmutableMap.of("changeTypeId", deleteId));

        DoctorChangeType changeType = findChangeTypeById(deleteId);
        assertNull(changeType);
    }

    /**
     * 根据id查询变动原因表测试
     * @see DoctorBasics#findChangeReasonById(java.lang.Long)
     */
    @Test
    public void findChangeReasonByIdTest() {
        DoctorChangeReason changeReason = findChangeReasonById(2L);
        assertNotNull(changeReason);
        log.info("findChangeReasonByIdTest result:{}", changeReason);
    }

    private DoctorChangeReason findChangeReasonById(Long changeReasonId) {
        String url = "/api/doctor/basic/changeReason/id";
        ResponseEntity<DoctorChangeReason> result = getForEntity(url, ImmutableMap.of("changeReasonId", changeReasonId), DoctorChangeReason.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        return result.getBody();
    }

    /**
     * 根据变动类型id查询变动原因表测试
     * @see DoctorBasics#findChangeReasonByChangeTypeId(java.lang.Long)
     */
    @Test
    public void findChangeReasonByChangeTypeIdTest() {
        String url = "/api/doctor/basic/changeReason/typeId";
        ResponseEntity<List> result = getForEntity(url, ImmutableMap.of("changeTypeId", 3L), List.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().size(), not(0));
        log.info("findChangeReasonByChangeTypeIdTest result:{}", result.getBody());
    }

    /**
     * 创建或更新DoctorChangeReason测试
     * @see DoctorBasics#createOrUpdateChangeReason(java.lang.Long, java.lang.String)
     */
    @Test
    public void createOrUpdateChangeReasonTest() {
        String url = "/api/doctor/basic/changeReason";
        ResponseEntity<Long> result = postFormForEntity(url, ImmutableMap.of("changeTypeId", 3L, "reason", JsonMapper.nonEmptyMapper().toJson(mockChangeReason())), Long.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));

        DoctorChangeReason changeReason = findChangeReasonById(result.getBody());
        assertNotNull(changeReason);
        log.info("createOrUpdateChangeReasonTest result:{}", changeReason);
    }

    /**
     * 根据主键id删除DoctorChangeReason测试
     * @see DoctorBasics#deleteChangeReason(java.lang.Long)
     */
    @Test
    public void deleteChangeReasonTest() {
        Long deleteId = 1L;
        String url = "/api/doctor/basic/changeReason";
        delete(url, ImmutableMap.of("changeReasonId", deleteId));

        DoctorChangeReason changeReason = findChangeReasonById(deleteId);
        assertNull(changeReason);
    }

    /**
     * 根据id查询客户表测试
     * @see DoctorBasics#findCustomerById(java.lang.Long)
     */
    @Test
    public void findCustomerByIdTest() {
        DoctorCustomer customer = findCustomerById(2L);
        assertNotNull(customer);
        log.info("findCustomerByIdTest result:{}", customer);
    }

    private DoctorCustomer findCustomerById(Long customerId) {
        String url = "/api/doctor/basic/customer/id";
        ResponseEntity<DoctorCustomer> result = getForEntity(url, ImmutableMap.of("customerId", customerId), DoctorCustomer.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        return result.getBody();
    }

    /**
     * 根据farmId查询客户表测试
     * @see DoctorBasics#findCustomersByfarmId(java.lang.Long)
     */
    @Test
    public void findCustomersByfarmIdTest() {
        String url = "/api/doctor/basic/customer/farmId";
        ResponseEntity<List> result = getForEntity(url, ImmutableMap.of("farmId", 0L), List.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().size(), not(0));
        log.info("findCustomersByfarmIdTest result:{}", result.getBody());
    }

    /**
     * 创建或更新DoctorCustomer测试
     * @see DoctorBasics#createOrUpdateCustomer(io.terminus.doctor.basic.model.DoctorCustomer)
     */
    @Test
    public void createOrUpdateCustomerTest() {
        String url = "/api/doctor/basic/customer";
        ResponseEntity<Long> result = postForEntity(url, mockCustomer(), Long.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));

        DoctorCustomer customer = findCustomerById(result.getBody());
        assertNotNull(customer);
        log.info("createOrUpdateCustomerTest result:{}", customer);
    }

    /**
     * 根据主键id删除DoctorCustomer测试
     * @see DoctorBasics#deleteCustomer(java.lang.Long)
     */
    @Test
    public void deleteCustomerTest() {
        Long deleteId = 1L;
        String url = "/api/doctor/basic/customer";
        delete(url, ImmutableMap.of("customerId", deleteId));

        DoctorCustomer customer = findCustomerById(deleteId);
        assertNull(customer);
    }

    /**
     * 查询所有计量单位测试
     * @see DoctorBasics#finaAllUnits()
     */
    @Test
    public void finaAllUnitsTest() {
        String url = "/api/doctor/basic/unit/all";
        ResponseEntity<List> result= getForEntity(url, null, List.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().size(), not(0));
        log.info("finaAllUnitsTest result:{}", result.getBody());
    }

    private DoctorDisease mockDisease() {
        DoctorDisease disease = new DoctorDisease();
        disease.setName("病");
        disease.setFarmId(1L);
        disease.setFarmName("猪场猪场");
        return disease;
    }

    private DoctorChangeType mockChangeType() {
        DoctorChangeType changeType = new DoctorChangeType();
        changeType.setName("变动类型啊");
        changeType.setIsCountOut(1);
        changeType.setFarmId(0L);
        changeType.setFarmName("测试猪场");
        return changeType;
    }

    private DoctorChangeReason mockChangeReason() {
        DoctorChangeReason changeReason = new DoctorChangeReason();
        changeReason.setChangeTypeId(3L);
        changeReason.setReason("没啥原因!");
        return changeReason;
    }

    private DoctorCustomer mockCustomer() {
        DoctorCustomer customer = new DoctorCustomer();
        customer.setMobile("12111111111");
        customer.setFarmId(0L);
        customer.setFarmName("测试猪场");
        customer.setName("客户客户客户");
        return customer;
    }
}
