package io.terminus.doctor.event.handler.rollback;

import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xiao on 16/9/22.
 */
public class RollbackPigEventHandler extends BaseServiceTest {
    @Autowired
    private DoctorRollbackHandlerChain doctorRollbackHandlerChain;

//    @Autowired
//    private

    @Test
    public void rollback(){
        System.out.println(doctorRollbackHandlerChain);

    }

}
