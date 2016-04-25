package io.terminus.doctor.workflow.service;

import java.io.InputStream;

/**
 * Desc: 流程服务类,包括功能如下
 *      1. 流程部署
 *      2. 流程删除
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
public interface FlowManagerService {

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


}
