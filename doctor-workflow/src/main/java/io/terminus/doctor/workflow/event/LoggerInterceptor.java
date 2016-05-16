package io.terminus.doctor.workflow.event;

import io.terminus.doctor.workflow.core.Execution;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Desc: 日志拦截器
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/27
 */
@Slf4j
@Component
public class LoggerInterceptor implements Interceptor {

    @Override
    public void before(Execution execution) {
        FlowDefinitionNode currNode = execution.getWorkFlowService().getFlowQueryService().getFlowDefinitionNodeQuery()
                .id(execution.getFlowProcess().getFlowDefinitionNodeId())
                .single();
        log.info("[workflow logger interceptor] -> {} 开始执行, name属性为: {}, 处理标识(人)为: {}, 操作人id: {}, 操作人姓名: {}",
                FlowDefinitionNode.Type.describe(currNode.getType()),
                currNode.getName(),
                currNode.getAssignee(),
                execution.getOperatorId(),
                execution.getOperatorName()
        );
    }

    @Override
    public void after(Execution execution) {
        FlowDefinitionNode currNode = execution.getWorkFlowService().getFlowQueryService().getFlowDefinitionNodeQuery()
                .id(execution.getFlowProcess().getFlowDefinitionNodeId())
                .single();
        log.info("[workflow logger interceptor] -> {} 结束执行, name属性为: {}",
                FlowDefinitionNode.Type.describe(currNode.getType()),
                currNode.getName()
        );
    }
}
