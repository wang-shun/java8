package io.terminus.doctor.user.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.docor.user.manager.BaseManagerTest;
import io.terminus.parana.user.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xjn on 18/4/10.
 * email:xiaojiannan@terminus.io
 */
public class PrimaryUserReadServiceImplTest extends BaseManagerTest {

    @Autowired
    private PrimaryUserReadService primaryUserReadService;

    @Test
    public void pagingOpenDoctorServiceUserTest() {
        Response<Paging<User>> pagingResponse = primaryUserReadService.pagingOpenDoctorServiceUser(null, null, null, "18439892837", null, null, null, null);
        Assert.assertTrue(pagingResponse.isSuccess());

        Assert.assertEquals(pagingResponse.getResult().getTotal().longValue(), 1);
    }
}
