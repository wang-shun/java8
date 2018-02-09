package io.terminus.doctor.user.service;

import com.google.common.collect.Maps;
import io.terminus.docor.user.manager.BaseManagerTest;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.user.dao.UserDaoExt;
import io.terminus.doctor.user.manager.DoctorUserManager;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserWriteService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Created by xjn on 18/2/9.
 * email:xiaojiannan@terminus.io
 */
public class DoctorUserServiceTest extends BaseManagerTest {
    @Autowired
    private UserWriteService userWriteService;
    @Autowired
    private UserDaoExt userDaoExt;
    @Autowired
    private DoctorUserManager userManager;

    @Test
    public void update() throws Exception {
        User user = userDaoExt.findById(10);
        user.setType(UserType.FARM_SUB.value());
        user.setRolesJson("[\"SUB\",\"SUB(SUB(7081))\"]");
        Map<String, String> extra = Maps.newHashMap();
        extra.put("pid", "1");
        user.setExtra(extra);
        userWriteService.update(user);

    }

    @Test
    public void checkExist() {
        userManager.checkExist("15838037509", "adc");
        System.out.println("===");
//        userManager.checkExist("15838037500", "syzz2");
        System.out.println("===");
        userManager.checkExist("15838037509", "syzz2");
    }
}
