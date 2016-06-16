package io.terminus.doctor.msg;

import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Desc: msg base service test
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/30
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ServiceConfiguration.class)
@Transactional
@Rollback
@ActiveProfiles("test")
public class BaseServiceTest {

}
