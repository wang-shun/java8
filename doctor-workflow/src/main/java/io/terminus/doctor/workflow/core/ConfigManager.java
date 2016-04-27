package io.terminus.doctor.workflow.core;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import io.terminus.doctor.workflow.utils.AssertHelper;
import io.terminus.doctor.workflow.utils.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static io.terminus.doctor.workflow.node.Node.ATTR_ASSIGNEE;
import static io.terminus.doctor.workflow.node.Node.ATTR_DESCRIBE;
import static io.terminus.doctor.workflow.node.Node.ATTR_EXPRESSION;
import static io.terminus.doctor.workflow.node.Node.ATTR_HANDLER;
import static io.terminus.doctor.workflow.node.Node.ATTR_KEY;
import static io.terminus.doctor.workflow.node.Node.ATTR_NAME;
import static io.terminus.doctor.workflow.node.Node.ATTR_POINT_X;
import static io.terminus.doctor.workflow.node.Node.ATTR_POINT_Y;
import static io.terminus.doctor.workflow.node.Node.ATTR_TARGET;
import static io.terminus.doctor.workflow.node.Node.NODE_END;
import static io.terminus.doctor.workflow.node.Node.NODE_ROOT;
import static io.terminus.doctor.workflow.node.Node.NODE_START;
import static io.terminus.doctor.workflow.node.Node.NODE_TASK;
import static io.terminus.doctor.workflow.node.Node.NODE_TRANSITION;

/**
 * Desc: 流程定义xml文件的解析类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/26
 */
public class ConfigManager implements Configuration {
    /**
     *  流程定义文件的文档
     */
    private Document document;

    /**
     * 流程定义解析出的类
     */
    private FlowDefinition flowDefinition;

    /**
     * 流程定义所有任务节点的Node对象和FlowDefinitionNode对象
     * Map 的 key表示每个节点name属性的值
     */
    private Map<String, Node> nodeMap = Maps.newHashMap();
    private Map<String, FlowDefinitionNode> definitionNodeMap = Maps.newHashMap();

    /**
     * 流程定义所有连线事件的节点Node对象
     * key 表示任务节点的name属性值, 用来方便获取源节点对象和目标节点对象
     */
    private Map<String, List<Node>> transitionMap = Maps.newHashMap();

    public ConfigManager(InputStream inputStream) throws Exception {
        this.document = XmlHelper.toDocument(inputStream);
        // 初始化流程定义
        initFlowDefinition();
        // 初始化开始节点
        initStartNode();
        initTaskNodes();
        // 初始化结束节点
        initEndNode();
    }

    /**
     * 初始化 流程定义 对象
     */
    private void initFlowDefinition() throws Exception {
        // 1. 检查是否是workflow流程文件
        String expression = "/" + NODE_ROOT;
        Node root = XmlHelper.getNode(expression, document);
        AssertHelper.isNull(root, "未能找到流程定义根节点, 节点表达式为: {}", expression);

        // 2. 解析流程定义对象
        flowDefinition = FlowDefinition.builder()
                .key(AssertHelper.isBlank(XmlHelper.getAttrValue(root, ATTR_KEY)))
                .name(XmlHelper.getAttrValue(root, ATTR_NAME))
                .resource(XmlHelper.toXml(document))
                .build();
    }

    /**
     * 初始化 开始 节点
     */
    private void initStartNode() throws Exception {
        // 1. 校验是否存在start节点
        String expression = "/" + NODE_ROOT + "/" + NODE_START;
        NodeList nodeList = XmlHelper.getNodeList(expression, document);
        AssertHelper.isNull(nodeList, "流程定义的开始节点数至少存在一个");
        AssertHelper.notEquals(nodeList.getLength(), 1, "流程定义的开始节点数量只能为: 1, 当前数量为: {}", nodeList.getLength());

        // 2. 解析出 start 节点
        Node startNode = nodeList.item(0);
        String nodeAttrName = XmlHelper.getAttrValue(startNode, ATTR_NAME);
        AssertHelper.mapContainsKey(nodeMap, nodeAttrName,
                "任务节点的name属性必须唯一, 当前节点名称为: {}, name属性值为: {}", startNode.getNodeName(), nodeAttrName);
        nodeMap.put(nodeAttrName, startNode);

        // 3. 解析start节点下的事件连线transition
        Node transitionNode = XmlHelper.getChildrenSingleNode(startNode, NODE_TRANSITION);
        AssertHelper.isNull(transitionNode, "开始节点下缺少事件连线");
        AssertHelper.isBlank(XmlHelper.getAttrValue(transitionNode, ATTR_TARGET),"开始节点缺少事件连线");
        List<Node> transitionNodes = Lists.newArrayList(transitionNode);
        transitionMap.put(nodeAttrName, transitionNodes);
    }

    /**
     * 初始化结束节点
     */
    private void initEndNode() throws Exception {
        // 1. 校验是否存在end节点
        String expression = "/" + NODE_ROOT + "/" + NODE_END;
        NodeList nodeList = XmlHelper.getNodeList(expression, document);
        AssertHelper.isNull(nodeList, "流程定义的结束节点数至少存在一个");
        AssertHelper.notEquals(nodeList.getLength(), 1, "流程定义的结束节点数量只能为: 1, 当前数量为: {}", nodeList.getLength());

        // 2. 解析出 end 节点
        Node startNode = nodeList.item(0);
        String nodeAttrName = XmlHelper.getAttrValue(startNode, ATTR_NAME);
        AssertHelper.mapContainsKey(nodeMap, nodeAttrName,
                "任务节点的name属性必须唯一, 当前节点名称为: {}, name属性值为: {}", startNode.getNodeName(), nodeAttrName);
        nodeMap.put(nodeAttrName, startNode);

        // 3. 不存在transition节点, 以下忽视
    }

    /**
     * 初始化简单任务节点
     */
    private void initTaskNodes() throws Exception {
        // 1. 获取所有 task 节点
        String expression = "/" + NODE_ROOT + "/" + NODE_TASK;
        NodeList nodeList = XmlHelper.getNodeList(expression, document);
        if(nodeList == null || nodeList.getLength() == 0) {
            return;
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            // 2. 解析 task 节点
            Node taskNode = nodeList.item(i);
            String nodeAttrName = XmlHelper.getAttrValue(taskNode, ATTR_NAME);
            AssertHelper.mapContainsKey(nodeMap, nodeAttrName,
                    "任务节点的name属性必须唯一, 当前节点名称为: {}, name属性值为: {}", taskNode.getNodeName(), nodeAttrName);
            nodeMap.put(nodeAttrName, taskNode);

            // 3. transition 节点
            Node transitionNode = XmlHelper.getChildrenSingleNode(taskNode, NODE_TRANSITION);
            AssertHelper.isNull(transitionNode, "开始节点下缺少事件连线");
            AssertHelper.isBlank(XmlHelper.getAttrValue(transitionNode, ATTR_TARGET),"开始节点缺少事件连线");
            List<Node> transitionNodes = Lists.newArrayList(transitionNode);
            transitionMap.put(nodeAttrName, transitionNodes);
        }
    }

    @Override
    public FlowDefinition getFlowDefinition() {
        return flowDefinition;
    }

    @Override
    public List<FlowDefinitionNode> getFlowDefinitionNodes() {
        final List<FlowDefinitionNode> list = Lists.newArrayList();
        nodeMap.forEach((nodeAttrName, node) -> {
            // 获取 FlowDefinitionNode 对象
            FlowDefinitionNode definitionNode = FlowDefinitionNode.builder()
                    .flowDefinitionId(flowDefinition.getId())
                    .name(XmlHelper.getAttrValue(node, ATTR_NAME))
                    .nodeName(node.getNodeName())
                    .type(FlowDefinitionNode.Type.vlaue(node.getNodeName()))
                    .assignee(XmlHelper.getAttrValue(node, ATTR_ASSIGNEE))
                    .pointX(XmlHelper.getAttrDoubleValue(node, ATTR_POINT_X))
                    .pointY(XmlHelper.getAttrDoubleValue(node, ATTR_POINT_Y))
                    .build();
            list.add(definitionNode);
            definitionNodeMap.put(nodeAttrName, definitionNode);
        });
        return list;
    }

    @Override
    public List<FlowDefinitionNodeEvent> getFlowDefinitionNodeEvents() {
        final List<FlowDefinitionNodeEvent> events = Lists.newArrayList();
        transitionMap.forEach((taskNodeAttrName, nodeList) ->
                nodeList.forEach(node -> {
                    // 校验一些值
                    FlowDefinitionNode target = definitionNodeMap.get(XmlHelper.getAttrValue(node, ATTR_TARGET));
                    AssertHelper.isNull(target, "错误的目标节点, 当前节点为: {}, 目标节点为: {}",
                            target.getName(), XmlHelper.getAttrValue(node, ATTR_TARGET));
                    // 获取 FlowDefinitionNode 对象
                    FlowDefinitionNodeEvent event = FlowDefinitionNodeEvent.builder()
                            .name(XmlHelper.getAttrValue(node, ATTR_NAME))
                            .flowDefinitionId(flowDefinition.getId())
                            .sourceNodeId(definitionNodeMap.get(taskNodeAttrName).getId())
                            .targetNodeId(target.getId())
                            .handler(XmlHelper.getAttrValue(node, ATTR_HANDLER))
                            .expression(XmlHelper.getAttrValue(node, ATTR_EXPRESSION))
                            .describe(XmlHelper.getAttrValue(node, ATTR_DESCRIBE))
                            .build();
                    events.add(event);
                }));
        return events;
    }
}