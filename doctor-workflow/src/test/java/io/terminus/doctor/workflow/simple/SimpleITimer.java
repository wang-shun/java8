package io.terminus.doctor.workflow.simple;

import io.terminus.doctor.workflow.base.BaseServiceTest;
import io.terminus.doctor.workflow.core.TimerExecution;
import io.terminus.doctor.workflow.event.ITimer;
import io.terminus.doctor.workflow.model.FlowTimer;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

/**
 * Created by xiao on 16/8/11.
 */
public class SimpleITimer extends BaseServiceTest{

    private Long businessId = 215l;
    private String flowDefinitionKey = "simpleFlowTimer";
    @Test
    @Rollback(value = false)
    public void executeTest(){
        defService().deploy("simple/simple_timer.xml");
        processService().startFlowInstance(flowDefinitionKey, businessId);
        processService().getExecutor(flowDefinitionKey, businessId).execute();
        //processService().getExecutor(flowDefinitionKey, businessId).execute();


    }

    @Test
    @Rollback(value = false)
    public void timerTest(){
        workFlowService.doTimerSchedule();
    }
}
