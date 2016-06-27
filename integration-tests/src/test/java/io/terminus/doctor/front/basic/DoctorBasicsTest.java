package io.terminus.doctor.front.basic;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
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
     * 根据id查询基础数据表测试
     * @see DoctorBasics#findBasicById(java.lang.Long)
     */
    @Test
    public void findBasicByIdTest() {
        DoctorBasic basic = findBasicById(20L);
        assertNotNull(basic);
        log.info("findBasicByIdTest result:{}", basic);
    }

    /**
     * 根据基础数据类型和输入码查询基础数据测试
     * @see DoctorBasics#findBasicByTypeAndSrmWithCache(java.lang.Integer, java.lang.String)
     */
    @Test
    public void findBasicByTypeAndSrm() {
        String url = "/api/doctor/basic/type";
        ResponseEntity<List> result = getForEntity(url, ImmutableMap.of("type", 4, "srm", "xm"), List.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().size(), not(0));
        log.info("findBasicByTypeAndSrm result:{}", result.getBody());
    }

    private DoctorBasic findBasicById(Long basicId) {
        String url = "/api/doctor/basic/id";
        ResponseEntity<DoctorBasic> result = getForEntity(url, ImmutableMap.of("basicId", basicId), DoctorBasic.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        return result.getBody();
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
