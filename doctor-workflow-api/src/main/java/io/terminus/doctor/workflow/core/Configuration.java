package io.terminus.doctor.workflow.core;

import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;

import java.util.List;

/**
 * Desc: 流程定义xml文件的解析类接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/26
 */
public interface Configuration {

    /**
     * 获取流程定义的对象
     * @return
     */
    public FlowDefinition getFlowDefinition();

    /**
     * 获取流程定义的所有任务节点对象
     * @return
     */
    public List<FlowDefinitionNode> getFlowDefinitionNodes();

    /**
     * 获取流程定义中任务节点对象的所有连接事件
     * @return
     */
    public List<FlowDefinitionNodeEvent> getFlowDefinitionNodeEvents();
}
