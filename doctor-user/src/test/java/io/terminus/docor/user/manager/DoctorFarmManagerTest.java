package io.terminus.docor.user.manager;

import io.terminus.doctor.user.manager.DoctorFarmManager;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xjn on 18/2/7.
 * email:xiaojiannan@terminus.io
 */
public class DoctorFarmManagerTest extends BaseManagerTest{
    @Autowired
    private DoctorFarmManager doctorFarmManager;

    @Test
    public void freezeTest() {
        doctorFarmManager.freezeFarm(855L);
    }
}
