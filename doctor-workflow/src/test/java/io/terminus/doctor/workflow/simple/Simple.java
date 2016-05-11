package io.terminus.doctor.workflow.simple;

import io.terminus.doctor.workflow.base.BaseServiceTest;
import io.terminus.doctor.workflow.core.WorkFlowEngine;
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
    private WorkFlowEngine workFlowEngine;


    @Test
    public void test01(){
        workFlowEngine.buildFlowDefinitionService().deploy("simple/simple.xml");
    }

    @Test
    public void testStartFlowInstance(){
        workFlowEngine.buildFlowProcessService().startFlowInstance("simpleFlow",1314L,"businessData","FlowData",2333L,"IceMimosa");
    }

}
