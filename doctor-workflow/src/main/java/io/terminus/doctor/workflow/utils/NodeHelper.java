package io.terminus.doctor.workflow.utils;

import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.node.DecisionNode;
import io.terminus.doctor.workflow.node.EndNode;
import io.terminus.doctor.workflow.node.Node;
import io.terminus.doctor.workflow.node.StartNode;
import io.terminus.doctor.workflow.node.TaskNode;

/**
 * Desc: 流转节点帮助类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/12
 */
public class NodeHelper {

    /**
     * 根据节点类型, 返回节点实现类
     * @param nodeType  节点类型枚举
     *                  @see io.terminus.doctor.workflow.model.FlowDefinitionNode.Type
     * @return
     */
    public static Node buildNode(FlowDefinitionNode.Type nodeType) {
        Node node = null;
        switch (nodeType) {
            case START:
                node = buildStartNode();
                break;
            case END:
                node = buildEndNode();
                break;
            case TASK:
                node = buildTaskNode();
                break;
            case DECISION:
                node = buildDecisionNode();
                break;
            default:
                break;
        }
        return node;
    }

    public static Node buildStartNode() {
        return new StartNode();
    }

    public static Node buildEndNode() {
        return new EndNode();
    }

    public static Node buildTaskNode() {
        return new TaskNode();
    }

    public static Node buildDecisionNode() {
        return new DecisionNode();
    }
}
