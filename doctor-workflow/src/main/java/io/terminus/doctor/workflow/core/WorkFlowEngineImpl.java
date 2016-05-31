package io.terminus.doctor.workflow.core;

import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.event.Interceptor;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.service.FlowDefinitionService;
import io.terminus.doctor.workflow.service.FlowProcessService;
import io.terminus.doctor.workflow.service.FlowQueryService;
import io.terminus.doctor.workflow.utils.AssertHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Desc: 流程引擎实现类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/26
 */
@Slf4j
@Component
public class WorkFlowEngineImpl implements WorkFlowEngine {

    @Autowired
    private Context context;

    @Autowired
    private JdbcAccess jdbcAccess;

    @Autowired
    private WorkFlowService workFlowService;

    @Autowired
    private FlowDefinitionService flowDefinitionService;

    @Autowired
    private FlowProcessService flowProcessService;

    @Autowired
    private FlowQueryService flowQueryService;

    @Autowired
    private Scheduler scheduler;

    @Override
    public JdbcAccess buildJdbcAccess() {
        if (jdbcAccess == null) {
            log.error("[engine build] -> jdbc access build error");
            AssertHelper.throwException("jdbc access 获取失败");
        }
        return jdbcAccess;
    }

    @Override
    public Configuration buildConfiguration(InputStream inputStream) throws Exception {
        return new ConfigManager(inputStream);
    }

    @Override
    public Context buildContext() {
        if (context == null) {
            log.error("[engine build] -> workflow context build error");
            AssertHelper.throwException("workflow context 获取失败");
        }
        return context;
    }

    @Override
    public List<Interceptor> buildInterceptors() {
        return buildContext().getList(Interceptor.class);
    }

    @Override
    public Executor buildExecutor(String flowDefinitionKey, Long businessId, String assignee) {
        return new ExecutorImpl(this, flowDefinitionKey, businessId, assignee);
    }

    @Override
    public Execution buildExecution(FlowProcess flowProcess, Map expression, String flowData, Long operatorId, String operatorName) {
        return new ExecutionImpl(this, flowProcess, expression, flowData, operatorId, operatorName);
    }

    @Override
    public WorkFlowService buildWorkFlowService() {
        if (workFlowService == null) {
            log.error("[engine build] -> WorkFlowService build error");
            AssertHelper.throwException("WorkFlowService 获取失败");
        }
        return workFlowService;
    }

    @Override
    public FlowDefinitionService buildFlowDefinitionService() {
        if (flowDefinitionService == null) {
            log.error("[engine build] -> FlowDefinitionService build error");
            AssertHelper.throwException("FlowDefinitionService 获取失败");
        }
        return flowDefinitionService;
    }

    @Override
    public FlowProcessService buildFlowProcessService() {
        if (flowProcessService == null) {
            log.error("[engine build] -> FlowProcessService build error");
            AssertHelper.throwException("FlowProcessService 获取失败");
        }
        return flowProcessService;
    }

    @Override
    public FlowQueryService buildFlowQueryService() {
        if (flowQueryService == null) {
            log.error("[engine build] -> FlowQueryService build error");
            AssertHelper.throwException("FlowQueryService 获取失败");
        }
        return flowQueryService;
    }

    @Override
    public Scheduler buildScheduler() {
        return scheduler;
    }

}
