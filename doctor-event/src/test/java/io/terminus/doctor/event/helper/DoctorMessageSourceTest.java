package io.terminus.doctor.event.helper;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/3/14.
 */
@Component
public class DoctorMessageSourceTest extends BaseServiceTest{
    @Autowired
    private DoctorMessageSourceHelper messageSource;
    @Test
    public void testMessage() {
        messageSource.getMessage("get.pig.handler.failed", Lists.newArrayList(1));
    }
}
