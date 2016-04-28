package io.terminus.doctor.workflow.service;

import io.terminus.doctor.workflow.model.FlowInstance;

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
     * @param flowDefinitionKey 流程实例的key
     * @param businessId        业务id
     */
    public void startFlowInstance(String flowDefinitionKey, Long businessId);

    /**
     * 根据key值, 启动一个流程实例, 根据version最新的流程定义启动.
     * 注意: 每个业务id只能启动一个类型的流程定义, 多次启动会抛出异常
     *
     * @param flowDefinitionKey 流程实例的key
     * @param businessId        业务id
     * @param businessData      业务全局数据(推荐json), 每个流程节点都能访问到
     */
    public void startFlowInstance(String flowDefinitionKey, Long businessId, String businessData);

    /**
     * 根据key值, 启动一个流程实例, 根据version最新的流程定义启动.
     * 注意: 每个业务id只能启动一个类型的流程定义, 多次启动会抛出异常
     *
     * @param flowDefinitionKey 流程实例key
     * @param businessId        业务id
     * @param businessData      业务全局数据(推荐json), 每个流程节点都能访问到
     * @param flowData          节点之间的流转数据, 当前节点之外的无法获取
     */
    public void startFlowInstance(String flowDefinitionKey, Long businessId, String businessData, String flowData);

    /**
     * 根据key值, 启动一个流程实例, 根据version最新的流程定义启动.
     * 注意: 每个业务id只能启动一个类型的流程定义, 多次启动会抛出异常
     *
     * @param flowDefinitionKey 流程实例key
     * @param businessId        业务id
     * @param businessData      业务全局数据(推荐json), 每个流程节点都能访问到
     * @param flowData          节点之间的流转数据, 当前节点之外的无法获取
     * @param operatorId        操作人id
     * @param operatorName      操作人姓名
     */
    public void startFlowInstance(String flowDefinitionKey, Long businessId, String businessData, String flowData, Long operatorId, String operatorName);


    ///////////////////////////////////////////////////////////////
    ///// 流程实例 查询 相关方法 /////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    /**
     * 查询是否已经存在的流程实例, 一个业务id只能启动一种key类型的流程定义
     * @param flowDefinitionKey 流程实例的key
     * @param businessId        业务id
     * @return
     */
    public FlowInstance findExistFlowInstance(String flowDefinitionKey, Long businessId);
}
