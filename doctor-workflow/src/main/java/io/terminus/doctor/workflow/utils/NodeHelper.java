package io.terminus.doctor.workflow.utils;

import io.terminus.doctor.workflow.node.Node;
import io.terminus.doctor.workflow.node.StartNode;

/**
 * Desc: 流转节点帮助类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/12
 */
public class NodeHelper {

    public static Node buildNode(Integer nodeType) {
        if (nodeType == null) {
            return null;
        }
        Node node = null;
        switch (nodeType) {
            case 1:
                node = buildStartNode();
                break;
            case -1:
                node = buildEndNode();
                break;
            case 2:
                node = buildTaskNode();
                break;
        }
        return node;
    }

    // TODO
    public static Node buildStartNode() {
        return new StartNode();
    }

    public static Node buildEndNode() {
        return null;
    }

    public static Node buildTaskNode() {
        return null;
    }
}
