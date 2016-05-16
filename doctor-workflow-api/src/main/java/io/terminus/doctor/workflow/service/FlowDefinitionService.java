package io.terminus.doctor.workflow.service;

import java.io.InputStream;

/**
 * Desc: 流程服务类,包括功能如下
 *      1. 流程部署
 *      2. 流程删除
 *      3. 流程相关查询
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
public interface FlowDefinitionService {
    ///////////////////////////////////////////////////////////////
    ///// 流程定义 deploy 相关方法 //////////////////////////////////
    ///////////////////////////////////////////////////////////////
    /**
     * 根据文件名称部署流程定义
     * @param sourceName    文件名称
     */
    public void deploy(String sourceName);

    /**
     * 根据文件流部署流程定义
     * @param inputStream   文件输入流
     */
    public void deploy(InputStream inputStream);

    /**
     * 根据文件名称部署流程定义, 传入部署人
     * @param sourceName    文件名称
     * @param operatorId    部署人id
     * @param operatorName  部署人姓名
     */
    public void deploy(String sourceName, Long operatorId, String operatorName);

    /**
     * 根据文件流部署流程定义
     * @param inputStream   文件输入流
     * @param operatorId    部署人id
     * @param operatorName  部署人姓名
     */
    public void deploy(InputStream inputStream, Long operatorId, String operatorName);


    ///////////////////////////////////////////////////////////////
    ///// 流程定义 delete 相关方法 //////////////////////////////////
    ///////////////////////////////////////////////////////////////
    /**
     * 根据流程定义id删除流程定义, 不强制级联删除, 如果当前流程定义存在流程实例, 则抛出异常.
     *
     * @param flowDefinitionId  流程定义id
     */
    public void delete(Long flowDefinitionId);

    /**
     * 根据流程定义id删除流程定义
     *      1. 默认是不强制级联删除, 如果当前定义存在流程实例, 则抛出异常.
     *      2. 如果强制级联删除, 则删除流程实例, 以及所有执行的任务和任务追踪/历史
     *
     * @param flowDefinitionId  流程定义id
     * @param cascade           是否强制级联删除, 默认false
     */
    public void delete(Long flowDefinitionId, boolean cascade);

    /**
     * 根据流程定义id删除流程定义
     *      1. 默认是不强制级联删除, 如果当前定义存在流程实例, 则抛出异常.
     *      2. 如果强制级联删除, 则删除流程实例, 以及所有执行的任务和任务追踪/历史
     *
     * @param flowDefinitionId  流程定义id
     * @param cascade           是否强制级联删除, 默认false
     * @param operatorId        操作人id
     * @param operatorName      操作人姓名
     */
    public void delete(Long flowDefinitionId, boolean cascade, Long operatorId, String operatorName);

}
