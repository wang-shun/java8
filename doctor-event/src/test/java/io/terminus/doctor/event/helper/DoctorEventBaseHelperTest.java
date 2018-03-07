package io.terminus.doctor.event.helper;

import io.terminus.doctor.event.dao.BaseDaoTest;
import io.terminus.doctor.event.enums.PigStatus;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xjn on 18/3/6.
 * email:xiaojiannan@terminus.io
 */
public class DoctorEventBaseHelperTest extends BaseDaoTest {
    @Autowired
    private DoctorEventBaseHelper doctorEventBaseHelper;

    @Test
    public void getCurrentStatus() {
        Integer status =  doctorEventBaseHelper.getCurrentStatus(705035L);
        Assert.assertEquals(PigStatus.Farrow.getKey(), status);
    }

    @Test
    public void getCurrentParity() {
        Integer status = doctorEventBaseHelper.getCurrentParity(705033L);
        Assert.assertEquals(2, status.longValue());
    }
}
