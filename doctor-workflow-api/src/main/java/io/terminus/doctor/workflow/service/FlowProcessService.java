package io.terminus.doctor.workflow.service;

import io.terminus.doctor.workflow.core.Executor;

import java.util.Map;

/**
 * Desc: 流程流转相关的接口
 *      1. 启动流程实例
 *      2. 查询已存在的流程实例
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/28
 */
public interface FlowProcessService {

    ///////////////////////////////////////////////////////////////
    ///// 流程实例 启动 相关方法 /////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    /**
     * 根据key值, 启动一个流程实例, 根据version最新的流程定义启动.
     * 注意: 每个业务id只能启动一个类型的流程定义, 多次启动会抛出异常
     *
     * @param flowDefinitionKey 流程定义的key
     * @param businessId        业务id
     */
    void startFlowInstance(String flowDefinitionKey, Long businessId);

    /**
     * 根据key值, 启动一个流程实例, 根据version最新的流程定义启动.
     * 注意: 每个业务id只能启动一个类型的流程定义, 多次启动会抛出异常
     *
     * @param flowDefinitionKey 流程定义的key
     * @param businessId        业务id
     * @param businessData      业务全局数据(推荐json), 每个流程节点都能访问到
     */
    void startFlowInstance(String flowDefinitionKey, Long businessId, String businessData);

    /**
     * 根据key值, 启动一个流程实例, 根据version最新的流程定义启动.
     * 注意: 每个业务id只能启动一个类型的流程定义, 多次启动会抛出异常
     *
     * @param flowDefinitionKey 流程定义的key
     * @param businessId        业务id
     * @param businessData      业务全局数据(推荐json), 每个流程节点都能访问到
     * @param flowData          节点之间的流转数据, 当前节点之外的无法获取
     */
    void startFlowInstance(String flowDefinitionKey, Long businessId, String businessData, String flowData);

    /**
     * 根据key值, 启动一个流程实例, 根据version最新的流程定义启动.
     * 注意: 每个业务id只能启动一个类型的流程定义, 多次启动会抛出异常
     *
     * @param flowDefinitionKey 流程定义的key
     * @param businessId        业务id
     * @param businessData      业务全局数据(推荐json), 每个流程节点都能访问到
     * @param flowData          节点之间的流转数据, 当前节点之外的无法获取
     * @param expression        流转判断表达式(decision节点情形)
     */
    void startFlowInstance(String flowDefinitionKey, Long businessId, String businessData, String flowData, Map expression);

    /**
     * 根据key值, 启动一个流程实例, 根据version最新的流程定义启动.
     * 注意: 每个业务id只能启动一个类型的流程定义, 多次启动会抛出异常
     *
     * @param flowDefinitionKey 流程定义的key
     * @param businessId        业务id
     * @param businessData      业务全局数据(推荐json), 每个流程节点都能访问到
     * @param flowData          节点之间的流转数据, 当前节点之外的无法获取
     * @param expression        流转判断表达式(decision节点情形)
     * @param operatorId        操作人id
     * @param operatorName      操作人姓名
     */
    void startFlowInstance(String flowDefinitionKey, Long businessId, String businessData, String flowData, Map expression, Long operatorId, String operatorName);

    /**
     * 获取任务执行器
     * @param flowDefinitionKey 流程定义的key
     * @param businessId        业务id
     * @return
     */
    Executor getExecutor(String flowDefinitionKey, Long businessId);

    /**
     * 获取任务执行器
     * @param flowDefinitionKey 流程定义的key
     * @param businessId        业务id
     * @param assignee          任务处理人, 一般指fork-join情况
     * @return
     */
    Executor getExecutor(String flowDefinitionKey, Long businessId, String assignee);

    /**
     * 结束流程实例, 如果存在正在执行的流程, 抛出异常
     * @param flowDefinitionKey 流程定义的key
     * @param businessId        业务id
     */
    void endFlowInstance(String flowDefinitionKey, Long businessId);

    /**
     * 结束流程实例
     * @param flowDefinitionKey 流程定义的key
     * @param businessId        业务id
     * @param isForce           是否强制结束, 强制结束会移除所有的正在运行的流程.
     *                          isForce 默认为false, 如果存在正在执行的流程, 抛出异常
     * @param describe          删除的理由(描述信息)
     */
    void endFlowInstance(String flowDefinitionKey, Long businessId, boolean isForce, String describe);

    /**
     * 结束流程实例
     * @param flowDefinitionKey 流程定义的key
     * @param businessId        业务id
     * @param isForce           是否强制结束, 强制结束会移除所有的正在运行的流程.
     *                          isForce 默认为false, 如果存在正在执行的流程, 抛出异常
     * @param operatorId        操作人id
     * @param operatorName      操作人姓名
     * @param describe          删除的理由(描述信息)
     */
    void endFlowInstance(String flowDefinitionKey, Long businessId, boolean isForce, String describe, Long operatorId, String operatorName);
}
