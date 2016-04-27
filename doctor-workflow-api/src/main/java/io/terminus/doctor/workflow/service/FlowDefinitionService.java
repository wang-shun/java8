package io.terminus.doctor.workflow.service;

import io.terminus.doctor.workflow.model.FlowDefinition;

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
    ///// 流程定义 query 相关方法 ///////////////////////////////////
    ///////////////////////////////////////////////////////////////
    /**
     * 根据流程定义的key值获取当前最新版本的流程定义
     * @param key
     * @return
     */
    public FlowDefinition findLatestDefinitionByKey(String key);

}
