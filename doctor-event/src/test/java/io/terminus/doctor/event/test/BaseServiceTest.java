package io.terminus.doctor.event.test;

import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Desc: 工作流基础测试类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ServiceConfiguration.class)
@Transactional
@Rollback
@ActiveProfiles("test")
public class BaseServiceTest {

}
