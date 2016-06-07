package io.terminus.doctor.front.user;

import com.google.common.collect.ImmutableMap;
import configuration.front.PrimaryFrontWebConfiguration;
import io.terminus.doctor.front.BaseFrontWebTest;
import io.terminus.doctor.user.dto.DoctorServiceApplyDto;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorServiceReviewWriteService;
import io.terminus.doctor.user.service.DoctorServiceStatusWriteService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SpringApplicationConfiguration(value = {PrimaryFrontWebConfiguration.class})
public class ServiceReviewTest extends BaseFrontWebTest {

    @Autowired
    private DoctorServiceReviewWriteService doctorServiceReviewWriteService;
    @Autowired
    private DoctorServiceStatusWriteService doctorServiceStatusWriteService;

    private static final String baseUrl = "http://localhost:{port}/api/user/service";

    @Test
    public void getUserServiceStatusTest(){
        String url = baseUrl + "/getUserServiceStatus";
        ResponseEntity<Object> result = restTemplate.getForEntity(url, Object.class, ImmutableMap.of("port", port));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void applyOpenServiceTest(){
        String url = baseUrl + "/applyOpenService";
        DoctorServiceApplyDto serviceApplyDto = new DoctorServiceApplyDto();
        serviceApplyDto.setType(1);
        DoctorOrg org = new DoctorOrg();
        org.setLicense("http://integration.test.com");
        org.setMobile("12345678901");
        org.setName("orgname");
        serviceApplyDto.setOrg(org);
        doctorServiceReviewWriteService.initServiceReview(4L, "44444444444");
        doctorServiceStatusWriteService.initDefaultServiceStatus(4L);
        Boolean result = restTemplate.postForObject(url, serviceApplyDto, Boolean.class, ImmutableMap.of("port", port));
        Assert.assertTrue(result);
    }
}
