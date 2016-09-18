package io.terminus.doctor.front.event;

import com.google.common.collect.ImmutableMap;
import io.terminus.doctor.front.BaseFrontWebTest;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by xiao on 16/9/13.
 */
@Component
public class DoctorPigEventsTest extends BaseFrontWebTest {
    @Test
    public void test_pigEvents(){
        String url = "/api/doctor/events/pig/pigEvents";
        ResponseEntity<List> result = getForEntity(url, ImmutableMap.of("types", "0_1"), List.class);
        System.out.println(result);
    }

}
