package io.terminus.doctor.workflow.node;

/**
 * Desc: 流程节点的顶层接口
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
    String NODE_END = "end";

    String NODE_TRANSITION = "transition";

    ///////////////////////////////////////////////////////////////
    ///// 流程定义 属性 相关 ////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    String ATTR_KEY = "key";
    String ATTR_NAME = "name";

    String ATTR_ASSIGNEE = "assignee";
    String ATTR_POINT_X = "pointx";
    String ATTR_POINT_Y = "pointy";
    String ATTR_TARGET = "target";
    String ATTR_HANDLER = "handler";
    String ATTR_EXPRESSION = "expression";
    String ATTR_DESCRIBE = "describe";

}
