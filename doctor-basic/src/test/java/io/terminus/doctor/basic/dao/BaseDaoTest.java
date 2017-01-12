package io.terminus.doctor.basic.dao;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 12:41 17/1/11
 */

import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DaoConfiguration.class)
@Transactional
@Rollback
@ActiveProfiles("test")
public abstract class BaseDaoTest {
}
