package io.terminus.doctor.workflow.simple;

import io.terminus.doctor.workflow.base.BaseServiceTest;
import io.terminus.doctor.workflow.service.FlowDefinitionService;
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
    private FlowDefinitionService flowDefinitionService;

    @Test
    public void test01(){
        //flowDefinitionService.deploy("simple/simple.xml");
    }
}
