package io.terminus.doctor.workflow.simple;

import io.terminus.doctor.workflow.base.BaseServiceTest;
import io.terminus.doctor.workflow.service.FlowManagerService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Desc:
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
public class Simple extends BaseServiceTest{

    @Autowired
    private FlowManagerService flowManagerService;

    @Test
    public void test01(){
        flowManagerService.deploy("simple/simple.xml");
    }
}
