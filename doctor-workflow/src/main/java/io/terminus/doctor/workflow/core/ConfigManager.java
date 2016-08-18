package io.terminus.doctor.workflow.core;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.doctor.workflow.model.FlowDefinition;
import io.terminus.doctor.workflow.model.FlowDefinitionNode;
import io.terminus.doctor.workflow.model.FlowDefinitionNodeEvent;
import io.terminus.doctor.workflow.utils.AssertHelper;
import io.terminus.doctor.workflow.utils.StringHelper;
import io.terminus.doctor.workflow.utils.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static io.terminus.doctor.workflow.node.Node.*;

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
        // 初始化简单任务节点
        initTaskNodes();
        // 初始化结束节点
        initEndNode();
        // 初始化选择节点(唯一网关)
        initDecisionNode();
        // 初始化fork节点(并行网关)和join节点
        int forkNodeCount = initForkNode();
        initJoinNode(forkNodeCount);
        // 初始化子流程开始节点和子流程结束节点
        initSubStartNode();
        initSubEndNode();
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
        AssertHelper.isBlank(XmlHelper.getAttrValue(transitionNode, ATTR_TARGET),"开始节点事件连线缺少目标节点");
        List<Node> transitionNodes = Lists.newArrayList(transitionNode);
        transitionMap.put(nodeAttrName, transitionNodes);
    }

    /**
     * 初始化 子流程开始 节点
     */
    private void initSubStartNode() throws Exception {
        String expression = "/" + NODE_ROOT + "/" + NODE_SUB_START;
        NodeList nodeList = XmlHelper.getNodeList(expression, document);
        for (int i = 0; nodeList != null && i < nodeList.getLength(); i++) {
            // 1. 解析出 subStart 节点
            Node subStartNode = nodeList.item(i);
            String nodeAttrName = XmlHelper.getAttrValue(subStartNode, ATTR_NAME);
            AssertHelper.mapContainsKey(nodeMap, nodeAttrName,
                    "子流程开始节点的name属性必须唯一, 当前节点名称为: {}, name属性值为: {}", subStartNode.getNodeName(), nodeAttrName);
            nodeMap.put(nodeAttrName, subStartNode);

            // 2. 解析subStart节点下的事件连线transition
            Node transitionNode = XmlHelper.getChildrenSingleNode(subStartNode, NODE_TRANSITION);
            AssertHelper.isNull(transitionNode, "子流程开始节点下缺少事件连线");
            AssertHelper.isBlank(XmlHelper.getAttrValue(transitionNode, ATTR_TARGET),"子流程开始节点事件连线缺少目标节点");
            List<Node> transitionNodes = Lists.newArrayList(transitionNode);
            transitionMap.put(nodeAttrName, transitionNodes);
        }
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
        Node endNode = nodeList.item(0);
        String nodeAttrName = XmlHelper.getAttrValue(endNode, ATTR_NAME);
        AssertHelper.mapContainsKey(nodeMap, nodeAttrName,
                "任务节点的name属性必须唯一, 当前节点名称为: {}, name属性值为: {}", endNode.getNodeName(), nodeAttrName);
        nodeMap.put(nodeAttrName, endNode);

        // 3. 不存在transition节点, 以下忽视
    }

    /**
     * 初始化 子流程结束 节点
     * @throws Exception
     */
    private void initSubEndNode() throws Exception {
        // 1. 校验是否存在end节点
        String expression = "/" + NODE_ROOT + "/" + NODE_SUB_END;
        NodeList nodeList = XmlHelper.getNodeList(expression, document);
        for (int i = 0; nodeList != null && i < nodeList.getLength(); i++) {
            // 2. 解析出 end 节点
            Node subEndNode = nodeList.item(i);
            String nodeAttrName = XmlHelper.getAttrValue(subEndNode, ATTR_NAME);
            AssertHelper.mapContainsKey(nodeMap, nodeAttrName,
                    "子流程结束节点的name属性必须唯一, 当前节点名称为: {}, name属性值为: {}", subEndNode.getNodeName(), nodeAttrName);
            nodeMap.put(nodeAttrName, subEndNode);

            // 3. 不存在transition节点, 以下忽视
        }
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
            // 解析timer
            String timer = XmlHelper.getAttrValue(taskNode, ATTR_TIMER);
            if (StringHelper.isNotBlank(timer)) {
                if (!timer.trim().matches("[\\s\\d]+")) {
                    AssertHelper.throwException(
                            "任务节点的timer属性必须只能包含空格和数字, 当前节点名称为: {}, name属性值为: {}", taskNode.getNodeName(), nodeAttrName);
                }
            }
            nodeMap.put(nodeAttrName, taskNode);

            // 3. transition 节点
            Node transitionNode = XmlHelper.getChildrenSingleNode(taskNode, NODE_TRANSITION);
            AssertHelper.isNull(transitionNode,
                    "任务节点下缺少事件连线, 当前节点名称为: {}, name属性值为: {}", taskNode.getNodeName(), nodeAttrName);
            AssertHelper.isBlank(XmlHelper.getAttrValue(transitionNode, ATTR_TARGET),
                    "任务节点事件连线缺少目标节点, 当前节点名称为: {}, name属性值为: {}", taskNode.getNodeName(), nodeAttrName);
            List<Node> transitionNodes = Lists.newArrayList(transitionNode);

            // 4. subflow 节点
            List<Node> subTransNodes = XmlHelper.getChildrenNodes(taskNode, NODE_SUBFLOW);
            if (subTransNodes != null && subTransNodes.size() > 0) {
                if (subTransNodes.size() > 1) {
                    AssertHelper.throwException(
                            "任务节点子流程连线数量不能大于1, 当前节点名称为: {}, name属性值为: {}", taskNode.getNodeName(), nodeAttrName);
                }
                Node subTransNode = subTransNodes.get(0);
                AssertHelper.isBlank(XmlHelper.getAttrValue(subTransNode, ATTR_TARGET),
                        "任务节点子流程连线缺少目标节点, 当前节点名称为: {}, name属性值为: {}", taskNode.getNodeName(), nodeAttrName);
                transitionNodes.add(subTransNode);
            }

            transitionMap.put(nodeAttrName, transitionNodes);
        }
    }

    /**
     * 初始化选择节点(唯一网关)
     */
    private void initDecisionNode() throws Exception {
        // 1. 获取所有的decision节点
        String expression = "/" + NODE_ROOT + "/" + NODE_DECISION;
        NodeList nodeList = XmlHelper.getNodeList(expression, document);
        if(nodeList == null || nodeList.getLength() == 0) {
            return;
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            // 2. 解析decision节点
            Node decisionNode = nodeList.item(i);
            String nodeAttrName = XmlHelper.getAttrValue(decisionNode, ATTR_NAME);
            AssertHelper.mapContainsKey(nodeMap, nodeAttrName,
                    "选择节点的name属性必须唯一, 当前节点名称为: {}, name属性值为: {}", decisionNode.getNodeName(), nodeAttrName);
            nodeMap.put(nodeAttrName, decisionNode);

            // 3. transition 节点
            List<Node> transitionNodes = XmlHelper.getChildrenNodes(decisionNode, NODE_TRANSITION);
            AssertHelper.isEquals(transitionNodes.size(), 0,
                    "选择节点下缺少事件连线, 当前节点名称为: {}, name属性值为: {}", decisionNode.getNodeName(), nodeAttrName);
            AssertHelper.isEquals(transitionNodes.size(), 1,
                    "选择节点下的事件连线数量必须大于1, 当前节点名称为: {}, name属性值为: {}", decisionNode.getNodeName(), nodeAttrName);
            // 校验每个事件连线
            int blankExpressionNum = 0;
            for(Node transitionNode : transitionNodes) {
                AssertHelper.isBlank(XmlHelper.getAttrValue(transitionNode, ATTR_TARGET),
                        "选择节点事件连线缺少目标节点, 当前节点名称为: {}, name属性值为: {}", decisionNode.getNodeName(), nodeAttrName);
                if(StringHelper.isBlank(XmlHelper.getAttrValue(transitionNode, ATTR_EXPRESSION))) {
                    blankExpressionNum ++;
                    if(blankExpressionNum > 1) {
                        AssertHelper.throwException("选择节点事件连线只能存在一个默认的表达式, 当前节点名称为: {}, name属性值为: {}", decisionNode.getNodeName(), nodeAttrName);
                    }
                }
            }
            transitionMap.put(nodeAttrName, transitionNodes);
        }
    }

    /**
     * 初始化Fork节点
     * @throws Exception
     */
    private int initForkNode() throws Exception {
        // 1. 获取所有的fork节点
        String expression = "/" + NODE_ROOT + "/" + NODE_FORK;
        NodeList nodeList = XmlHelper.getNodeList(expression, document);
        if(nodeList == null || nodeList.getLength() == 0) {
            return 0;
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            // 2. 解析fork节点
            Node forkNode = nodeList.item(i);
            String nodeAttrName = XmlHelper.getAttrValue(forkNode, ATTR_NAME);
            AssertHelper.mapContainsKey(nodeMap, nodeAttrName,
                    "fork节点的name属性必须唯一, 当前节点名称为: {}, name属性值为: {}", forkNode.getNodeName(), nodeAttrName);
            nodeMap.put(nodeAttrName, forkNode);

            // 3. transition 节点
            List<Node> transitionNodes = XmlHelper.getChildrenNodes(forkNode, NODE_TRANSITION);
            AssertHelper.isEquals(transitionNodes.size(), 0,
                    "fork节点下缺少事件连线, 当前节点名称为: {}, name属性值为: {}", forkNode.getNodeName(), nodeAttrName);
            AssertHelper.isEquals(transitionNodes.size(), 1,
                    "fork节点下的事件连线数量必须大于1, 当前节点名称为: {}, name属性值为: {}", forkNode.getNodeName(), nodeAttrName);

            // 校验每个事件连线
            for(Node transitionNode : transitionNodes) {
                AssertHelper.isBlank(XmlHelper.getAttrValue(transitionNode, ATTR_TARGET),
                        "fork节点事件连线缺少目标节点, 当前节点名称为: {}, name属性值为: {}", forkNode.getNodeName(), nodeAttrName);
            }
            transitionMap.put(nodeAttrName, transitionNodes);
        }
        return nodeList.getLength();
    }

    /**
     * 初始化Join节点
     * @throws Exception
     */
    private void initJoinNode(int forkNodeCount) throws Exception {
        // 1. 获取所有的Join节点
        String expression = "/" + NODE_ROOT + "/" + NODE_JOIN;
        NodeList nodeList = XmlHelper.getNodeList(expression, document);
        if(nodeList == null || nodeList.getLength() == 0) {
            AssertHelper.notEquals(0, forkNodeCount, "fork节点数量与join节点数量不匹配");
            return;
        }
        AssertHelper.notEquals(nodeList.getLength(), forkNodeCount, "fork节点数量与join节点数量不匹配");

        for (int i = 0; i < nodeList.getLength(); i++) {
            // 2. 解析join节点
            Node JoinNode = nodeList.item(i);
            String nodeAttrName = XmlHelper.getAttrValue(JoinNode, ATTR_NAME);
            AssertHelper.mapContainsKey(nodeMap, nodeAttrName,
                    "join节点的name属性必须唯一, 当前节点名称为: {}, name属性值为: {}", JoinNode.getNodeName(), nodeAttrName);
            nodeMap.put(nodeAttrName, JoinNode);

            // 3. transition 节点
            Node transitionNode = XmlHelper.getChildrenSingleNode(JoinNode, NODE_TRANSITION);
            AssertHelper.isNull(transitionNode,
                    "join节点下缺少事件连线, 当前节点名称为: {}, name属性值为: {}", JoinNode.getNodeName(), nodeAttrName);
            AssertHelper.isBlank(XmlHelper.getAttrValue(transitionNode, ATTR_TARGET),
                    "join节点事件连线缺少目标节点, 当前节点名称为: {}, name属性值为: {}", JoinNode.getNodeName(), nodeAttrName);
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
                    .value(XmlHelper.getAttrValue(node, ATTR_VALUE))
                    .nodeName(node.getNodeName())
                    .type(FlowDefinitionNode.Type.vlaue(node.getNodeName()))
                    .assignee(XmlHelper.getAttrValue(node, ATTR_ASSIGNEE))
                    .timer(XmlHelper.getAttrValue(node, ATTR_TIMER))
                    .iTimer(XmlHelper.getAttrValue(node, ATTR_ITIMER))
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
                            .value(XmlHelper.getAttrValue(node, ATTR_VALUE))
                            .flowDefinitionId(flowDefinition.getId())
                            .sourceNodeId(definitionNodeMap.get(taskNodeAttrName).getId())
                            .targetNodeId(target.getId())
                            .handler(XmlHelper.getAttrValue(node, ATTR_HANDLER))
                            .tacker(XmlHelper.getAttrValue(node, ATTR_TACKER))
                            .expression(XmlHelper.getAttrValue(node, ATTR_EXPRESSION))
                            .describe(XmlHelper.getAttrValue(node, ATTR_DESCRIBE))
                            .build();
                    events.add(event);
                }));
        return events;
    }
}