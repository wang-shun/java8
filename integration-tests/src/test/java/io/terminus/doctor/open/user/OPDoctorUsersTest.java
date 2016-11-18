package io.terminus.doctor.open.user;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import configuration.open.OpenWebConfiguration;
import io.terminus.doctor.open.BaseOpenWebTest;
import io.terminus.doctor.web.core.login.Sessions;
import io.terminus.doctor.user.dto.DoctorServiceApplyDto;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.service.DoctorServiceReviewWriteService;
import io.terminus.doctor.user.service.DoctorServiceStatusWriteService;
import io.terminus.doctor.web.core.service.ServiceBetaStatusHandler;
import io.terminus.session.AFSessionManager;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.TreeMap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * 陈增辉 on 16/6/12.
 */
@SpringApplicationConfiguration({OpenWebConfiguration.class})
public class OPDoctorUsersTest extends BaseOpenWebTest {
    @Autowired
    private AFSessionManager sessionManager;
    @Autowired
    private DoctorServiceReviewWriteService doctorServiceReviewWriteService;
    @Autowired
    private DoctorServiceStatusWriteService doctorServiceStatusWriteService;
    @Autowired
    private ServiceBetaStatusHandler serviceBetaStatusHandler;

    private final String sid = "testSessionId";
    private final String appKey = "pigDoctorAndroid";
    private final String appSecret = "pigDoctorAndroidSecret";
    private final String deviceId = "deviceId";
    private final Long userId = 1L;

    @Test
    public void getUserServiceStatusTest(){
        this.login(sid, userId, deviceId);
        doctorServiceReviewWriteService.initServiceReview(1L, "44444444444");
        serviceBetaStatusHandler.initDefaultServiceStatus(1L);
        String pampasCall = "get.user.service.status";
        ResponseEntity<Object> result = restTemplate.getForEntity("http://localhost:{port}/api/gateway" +
                        "?pampasCall=" + pampasCall +
                        "&sid=" + sid +
                        "&appKey=" + appKey +
                        "&sign=" + this.generateSign(ImmutableMap.of("pampasCall", pampasCall)),
                Object.class, ImmutableMap.of("port", port));
        System.out.println(result.getBody());
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void applyOpenServiceTest(){
        this.login(sid, userId, deviceId);
        doctorServiceReviewWriteService.initServiceReview(1L, "44444444444");
        serviceBetaStatusHandler.initDefaultServiceStatus(1L);
        String pampasCall = "apply.open.service";
        DoctorServiceApplyDto dto = new DoctorServiceApplyDto();
        dto.setType(DoctorServiceReview.Type.PIG_DOCTOR.getValue());
        DoctorOrg org = new DoctorOrg();
        org.setLicense("http://we.we.we");
        org.setName("ceshi");
        org.setMobile("11111111");
        dto.setOrg(org);
        Object result = restTemplate.postForObject("http://localhost:{port}/api/gateway" +
                        "?pampasCall=" + pampasCall +
                        "&sid=" + sid +
                        "&appKey=" + appKey +
                        "&sign=" + this.generateSign(ImmutableMap.of("pampasCall", pampasCall)),
                dto, Object.class, ImmutableMap.of("port", port));
        System.out.println(result);
//        assertTrue(result);
    }

    @Test
    public void getUserRoleTypeTest(){
        this.login(sid, userId, deviceId);
        String pampasCall = "get.user.role.type";
        ResponseEntity<Object> result = restTemplate.getForEntity("http://localhost:{port}/api/gateway" +
                        "?pampasCall=" + pampasCall +
                        "&sid=" + sid +
                        "&appKey=" + appKey +
                        "&sign=" + this.generateSign(ImmutableMap.of("pampasCall", pampasCall)),
                Object.class, ImmutableMap.of("port", port));
        System.out.println(result.getBody());
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void getUserBasicInfoTest(){
        this.login(sid, userId, deviceId);
        String pampasCall = "get.user.basic.info";
        ResponseEntity<Object> result = restTemplate.getForEntity("http://localhost:{port}/api/gateway" +
                        "?pampasCall=" + pampasCall +
                        "&sid=" + sid +
                        "&appKey=" + appKey +
                        "&sign=" + this.generateSign(ImmutableMap.of("pampasCall", pampasCall)),
                Object.class, ImmutableMap.of("port", port));
        System.out.println(result.getBody());
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void getOrgInfoTest(){
        this.login(sid, userId, deviceId);
        String pampasCall = "get.org.info";
        ResponseEntity<Object> result = restTemplate.getForEntity("http://localhost:{port}/api/gateway" +
                        "?pampasCall=" + pampasCall +
                        "&sid=" + sid +
                        "&appKey=" + appKey +
                        "&sign=" + this.generateSign(ImmutableMap.of("pampasCall", pampasCall)),
                Object.class, ImmutableMap.of("port", port));
        System.out.println(result.getBody());
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void getUserLevelOneMenuTest(){
        this.login(sid, userId, deviceId);
        String pampasCall = "get.user.level.one.menu";
        ResponseEntity<Object> result = restTemplate.getForEntity("http://localhost:{port}/api/gateway" +
                        "?pampasCall=" + pampasCall +
                        "&sid=" + sid +
                        "&appKey=" + appKey +
                        "&sign=" + this.generateSign(ImmutableMap.of("pampasCall", pampasCall)),
                Object.class, ImmutableMap.of("port", port));
        System.out.println(result.getBody());
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }

    private String generateSign(Map<String, Object> params) {
        Map<String, Object> treeMap = new TreeMap<>();
        treeMap.putAll(params);
        treeMap.put("appKey", appKey);
        treeMap.put("sid", sid);
        String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(treeMap);
        return Hashing.md5().newHasher().putString(toVerify, Charsets.UTF_8).putString(appSecret, Charsets.UTF_8).hash().toString();
    }
    private void login(String sid, Long userId, String deviceId){
        sessionManager.save(Sessions.TOKEN_PREFIX, sid,
                ImmutableMap.of(Sessions.USER_ID, userId, Sessions.DEVICE_ID, deviceId),
                Sessions.LONG_INACTIVE_INTERVAL);
    }
}
