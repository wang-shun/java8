package io.terminus.doctor.front.basic;

import configuration.front.FrontWebConfiguration;
import io.terminus.doctor.front.BaseFrontWebTest;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * Desc: 基础数据controller测试类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/7
 */
@SpringApplicationConfiguration(FrontWebConfiguration.class)
public class DoctorBasicsTest extends BaseFrontWebTest {

    private static final String URL = "/api/doctor/basic";

    /**
     * 查询所有品种测试
     */
    @Test
    public void finaAllBreedTest() {
        String url = gateway + URL + "/breed/all";
        ResponseEntity<List> result= restTemplate.getForEntity(url, List.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), not(0));
    }

    /**
     * 查询所有品种测试
     */
    @Test
    public void finaAllGeneticTest() {

    }

    /**
     * 查询疾病详情测试
     */
    @Test
    public void findDiseaseByIdTest() {

    }

    /**
     * 根据farmId查询疾病列表测试
     */
    @Test
    public void findDiseaseByfarmIdTest() {

    }

    /**
     * 创建或更新疾病表测试
     */
    @Test
    public void createOrUpdateDiseaseTest() {

    }

    /**
     * 根据主键id删除DoctorDisease测试
     */
    @Test
    public void deleteDiseaseTest() {

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
}
