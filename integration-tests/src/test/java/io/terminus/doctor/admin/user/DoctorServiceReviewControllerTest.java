package io.terminus.doctor.admin.user;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import configuration.admin.OperatorAdminWebConfiguration;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.admin.BaseAdminWebTest;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.user.dto.DoctorServiceApplyDto;
import io.terminus.doctor.user.model.*;
import io.terminus.doctor.user.service.*;
import io.terminus.doctor.user.service.business.DoctorServiceReviewService;
import io.terminus.doctor.web.admin.dto.UserApplyServiceDetailDto;
import io.terminus.parana.common.model.ParanaUser;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import utils.HttpPostRequest;

import java.util.Map;

import static org.hamcrest.core.Is.is;

@SpringApplicationConfiguration({OperatorAdminWebConfiguration.class})
public class DoctorServiceReviewControllerTest  extends BaseAdminWebTest {

    @Autowired
    private DoctorServiceReviewWriteService doctorServiceReviewWriteService;
    @Autowired
    private DoctorFarmWriteService doctorFarmWriteService;
    @Autowired
    private DoctorOrgWriteService doctorOrgWriteService;
    @Autowired
    private DoctorStaffWriteService doctorStaffWriteService;
    @Autowired
    private DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService;
    @Autowired
    private DoctorServiceStatusWriteService doctorServiceStatusWriteService;
    @Autowired
    private DoctorServiceReviewService doctorServiceReviewService;

    private static final String baseUrl = "http://localhost:{port}/api/doctor/admin/service";
    @Test
    public void pageServiceAppliesTest(){
        DoctorServiceReview review = new DoctorServiceReview();
        review.setType(1);
        review.setStatus(2);
        review.setUserId(4L);
        doctorServiceReviewWriteService.createReview(review);
        String url = baseUrl + "/apply/page";
        ResponseEntity<Paging> result = restTemplate.getForEntity(url, Paging.class, ImmutableMap.of("port", port));
        Assert.assertThat(result.getStatusCode(), is(HttpStatus.OK));
        Assert.assertEquals(result.getBody().getTotal().longValue(), 1L);
    }

    @Test
    public void findUserApplyDetailTest(){
        Long userId = 4L;
        DoctorOrg org = new DoctorOrg();
        org.setMobile("12345678901");
        org.setName("orgname");
        org.setLicense("http://a.b.c/e.jpg");
        doctorOrgWriteService.createOrg(org);

        DoctorStaff staff = new DoctorStaff();
        staff.setOrgName(org.getName());
        staff.setOrgId(org.getId());
        staff.setUserId(userId);
        doctorStaffWriteService.createDoctorStaff(staff);

        DoctorFarm farm = new DoctorFarm();
        farm.setOrgId(org.getId());
        farm.setOrgName(org.getName());
        farm.setName("farmname");
        doctorFarmWriteService.createFarm(farm);

        DoctorUserDataPermission permission = new DoctorUserDataPermission();
        permission.setFarmIds(farm.getId().toString());
        permission.setUserId(userId);
        doctorUserDataPermissionWriteService.createDataPermission(permission);

        String url = baseUrl + "/pigdoctor/detail/{userId}";
        ResponseEntity<UserApplyServiceDetailDto> result = restTemplate.getForEntity(url, UserApplyServiceDetailDto.class, ImmutableMap.of("port", port, "userId", userId));
        Assert.assertThat(result.getStatusCode(), is(HttpStatus.OK));
        Assert.assertTrue(result.getBody().getOrg() != null);
        Assert.assertTrue(!result.getBody().getFarms().isEmpty());
    }

    @Test
    public void openDoctorServiceTest(){
        //先申请
        DoctorOrg org = new DoctorOrg();
        org.setLicense("http://integration.test.com");
        org.setMobile("12345678901");
        org.setName("orgname");
        ParanaUser applyUser = this.applyService(DoctorServiceReview.Type.PIG_DOCTOR, org);

        //审批数据dto
        UserApplyServiceDetailDto auditDto = new UserApplyServiceDetailDto();
        auditDto.setUserId(applyUser.getId());
        auditDto.setOrg(org);
        DoctorFarm farm = new DoctorFarm();
        farm.setOrgId(org.getId());
        farm.setOrgName(org.getName());
        farm.setName("farmname");
        farm.setProvinceId(330000);
        farm.setCityId(330100);
        farm.setDistrictId(330102);
        farm.setDetailAddress("黄泉街道办事处");
        auditDto.setFarms(Lists.newArrayList(farm));

        String url = baseUrl + "/pigdoctor/open";
        Boolean result = restTemplate.postForObject(url, auditDto, Boolean.class, ImmutableMap.of("port", port));
        Assert.assertTrue(result);
    }

    @Test
    public void openPigmallServiceTest(){
        //先申请
        ParanaUser applyUser = this.applyService(DoctorServiceReview.Type.PIGMALL, null);

        String url = baseUrl + "/pigmall/open/{userId}";
        Boolean result = restTemplate.getForObject(url, Boolean.class, ImmutableMap.of("port", port, "userId", applyUser.getId()));
        Assert.assertTrue(result);
    }

    @Test
    public void openNeverestServiceTest(){
        //先申请
        ParanaUser applyUser = this.applyService(DoctorServiceReview.Type.NEVEREST, null);

        String url = baseUrl + "/neverest/open/{userId}";
        Boolean result = restTemplate.getForObject(url, Boolean.class, ImmutableMap.of("port", port, "userId", applyUser.getId()));
        Assert.assertTrue(result);
    }

    @Test
    public void notOpenServiceTest(){
        //先申请
        ParanaUser applyUser = this.applyService(DoctorServiceReview.Type.NEVEREST, null);

        String url = baseUrl + "/notopen";
        HttpEntity param = HttpPostRequest.formRequest()
                .param("userId", applyUser.getId())
                .param("type", DoctorServiceReview.Type.NEVEREST.getValue())
                .param("reason", "reason")
                .httpEntity();
        ResponseEntity<Object> entity = restTemplate.postForEntity(url, param, Object.class, ImmutableMap.of("port", port));
        Assert.assertThat(entity.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void frozeApplyTest(){
        //先申请
        ParanaUser applyUser = this.applyService(DoctorServiceReview.Type.NEVEREST, null);

        String url = baseUrl + "/froze";
        HttpEntity param = HttpPostRequest.formRequest()
                .param("userId", applyUser.getId())
                .param("type", DoctorServiceReview.Type.NEVEREST.getValue())
                .param("reason", "reason")
                .httpEntity();
        ResponseEntity<Object> entity = restTemplate.postForEntity(url, param, Object.class, ImmutableMap.of("port", port));
        Assert.assertThat(entity.getStatusCode(), is(HttpStatus.OK));
    }

    private ParanaUser applyService(DoctorServiceReview.Type type, DoctorOrg org){
        //申请服务的用户model
        ParanaUser applyUser = new ParanaUser();
        applyUser.setId(4L);
        applyUser.setType(UserType.FARM_ADMIN_PRIMARY.value());
        //初始化数据
        doctorServiceReviewWriteService.initServiceReview(applyUser.getId(), "4444444444");
        doctorServiceStatusWriteService.initDefaultServiceStatus(applyUser.getId());
        //申请
        DoctorServiceApplyDto serviceApplyDto = new DoctorServiceApplyDto();
        serviceApplyDto.setType(type.getValue());
        serviceApplyDto.setOrg(org);
        doctorServiceReviewService.applyOpenService(applyUser, serviceApplyDto);
        return applyUser;
    }
}
