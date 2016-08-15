package io.terminus.doctor.workflow.updateData;

import io.terminus.doctor.workflow.base.BaseServiceTest;
import io.terminus.doctor.workflow.service.FlowProcessService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xiao on 16/8/9.
 */
public class Rollback extends BaseServiceTest {
    @Autowired
    private FlowProcessService flowProcessService;
    @Test
    @org.springframework.test.annotation.Rollback(value = false)
    public void rollBack(){
        flowProcessService.rollBack("sow",16l,2);
    }


}
