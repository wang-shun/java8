package io.terminus.doctor.workflow.core;

import io.terminus.doctor.workflow.model.FlowProcess;

import java.util.Map;

/**
 * Desc: 执行器, 用来执行节点的操作
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/13
 */
public interface Executor {

    /**
     * 执行当前流程实例的任务
     */
    void execute();

    /**
     * 执行当前流程实例的任务
     * @param expression    执行表达式, 一般指decision节点
     */
    void execute(Map expression);

    /**
     * 执行当前流程实例的任务
     * @param flowData      流转数据
     */
    void execute(String flowData);

    /**
     * 执行当前流程实例的任务
     * @param expression    执行表达式, 一般指decision节点
     * @param flowData      流转数据
     */
    void execute(Map expression, String flowData);

    /**
     * 执行当前流程实例的任务
     * @param operatorId    操作人id
     * @param operatorName  操作人姓名
     */
    void execute(Long operatorId, String operatorName);

    /**
     * 执行当前流程实例的任务
     * @param expression    执行表达式, 一般指decision节点
     * @param operatorId    操作人id
     * @param operatorName  操作人姓名
     */
    void execute(Map expression, Long operatorId, String operatorName);

    /**
     * 执行当前流程实例的任务
     * @param expression    执行表达式, 一般指decision节点
     * @param flowData      流转数据
     * @param operatorId    操作人id
     * @param operatorName  操作人姓名
     */
    void execute(Map expression, String flowData, Long operatorId, String operatorName);

    /**
     * 启动子流程实例
     */
    void startSubFlowInstance();

    /**
     * 启动子流程实例
     * @param flowData  流转数据
     */
    void startSubFlowInstance(String flowData);

    /**
     * 启动子流程实例
     * @param expression    执行判断表达式
     */
    void startSubFlowInstance(Map expression);

    /**
     * 启动子流程实例
     * @param flowData      流转数据
     * @param expression    执行判断表达式
     */
    void startSubFlowInstance(String flowData, Map expression);

    /**
     * 启动子流程实例
     * @param flowData      流转数据
     * @param expression    执行判断表达式
     * @param operatorId    操作人id
     * @param operatorName  操作人姓名
     */
    void startSubFlowInstance(String flowData, Map expression, Long operatorId, String operatorName);

    /**
     * 结束子流程实例
     * @param flowProcess   当前执行的流程任务
     */
    void endSubFlowInstance(FlowProcess flowProcess);

    /**
     * 结束子流程实例
     * @param flowProcess   当前执行的流程任务
     * @param operatorId    操作人id
     * @param operatorName  操作人姓名
     */
    void endSubFlowInstance(FlowProcess flowProcess, Long operatorId, String operatorName);
}
