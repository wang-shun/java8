package io.terminus.doctor.front.user;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.doctor.front.BaseFrontWebTest;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SpringApplicationConfiguration(value = {FrontWebConfiguration.class})
public class ServiceReviewTest extends BaseFrontWebTest {
    private static final String baseUrl = "http://localhost:{port}";

    @Test
    public void getUserServiceStatusTest(){
        String url = baseUrl + "/api/user";
        ResponseEntity<Object> result = restTemplate.getForEntity(url, Object.class, ImmutableMap.of("port", port));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }
}
