package io.terminus.doctor.workflow.core;

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
    public void execute();

    /**
     * 执行当前流程实例的任务
     * @param expression    执行表达式, 一般指decision节点
     */
    public void execute(Map expression);

    /**
     * 执行当前流程实例的任务
     * @param flowData      流转数据
     */
    public void execute(String flowData);

    /**
     * 执行当前流程实例的任务
     * @param expression    执行表达式, 一般指decision节点
     * @param flowData      流转数据
     */
    public void execute(Map expression, String flowData);

    /**
     * 执行当前流程实例的任务
     * @param operatorId    操作人id
     * @param operatorName  操作人姓名
     */
    public void execute(Long operatorId, String operatorName);

    /**
     * 执行当前流程实例的任务
     * @param expression    执行表达式, 一般指decision节点
     * @param operatorId    操作人id
     * @param operatorName  操作人姓名
     */
    public void execute(Map expression, Long operatorId, String operatorName);

    /**
     * 执行当前流程实例的任务
     * @param expression    执行表达式, 一般指decision节点
     * @param flowData      流转数据
     * @param operatorId    操作人id
     * @param operatorName  操作人姓名
     */
    public void execute(Map expression, String flowData, Long operatorId, String operatorName);

}
