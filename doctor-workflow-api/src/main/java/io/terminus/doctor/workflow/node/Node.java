package io.terminus.doctor.workflow.node;

import io.terminus.doctor.workflow.core.Execution;

/**
 * Desc: 流程节点的顶层接口, 一般继承BaseNode类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/26
 */
public interface Node {
    ///////////////////////////////////////////////////////////////
    ///// 流程定义 节点 相关 ////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    String NODE_ROOT = "workflow";
    String NODE_START = "start";
    String NODE_TASK = "task";
    String NODE_DECISION = "decision";
    String NODE_FORK = "fork";
    String NODE_JOIN = "join";
    String NODE_SUB_START = "substart";
    String NODE_SUB_END = "subend";
    String NODE_END = "end";

    String NODE_TRANSITION = "transition";
    String NODE_SUBFLOW = "subflow";

    ///////////////////////////////////////////////////////////////
    ///// 流程定义 属性 相关 ////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    String ATTR_KEY = "key";
    String ATTR_NAME = "name";
    String ATTR_VALUE = "value";

    String ATTR_ASSIGNEE = "assignee";
    String ATTR_TIMER = "timer";
    String ATTR_ITIMER = "itimer";
    String ATTR_POINT_X = "pointx";
    String ATTR_POINT_Y = "pointy";
    String ATTR_TARGET = "target";
    String ATTR_HANDLER = "handler";
    String ATTR_TACKER = "tacker";
    String ATTR_EXPRESSION = "expression";
    String ATTR_DESCRIBE = "describe";

    /**
     * 节点执行方法入口
     * @param execution
     */
    void execute(Execution execution);
}
