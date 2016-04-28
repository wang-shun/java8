package io.terminus.doctor.workflow.core;

import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.event.Interceptor;
import io.terminus.doctor.workflow.service.FlowDefinitionService;
import io.terminus.doctor.workflow.service.FlowProcessService;

import java.io.InputStream;
import java.util.List;

/**
 * Desc: 流程引擎接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/26
 */
public interface WorkFlowEngine {

    /**
     * 获取去DAO层统一接口
     * @return
     */
    public JdbcAccess buildJdbcAccess();

    /**
     * 创建配置解析类
     * @param inputStream   文件流
     * @return
     * @throws Exception
     */
    public Configuration buildConfiguration(InputStream inputStream) throws Exception;

    /**
     * 获取全局上下文对象
     * @return
     */
    public Context buildContext();

    /**
     * 获取所有拦截器对象
     * @return
     */
    public List<Interceptor> buildInterceptors();

    /**
     * 构造流程定义服务类
     * @return
     */
    public FlowDefinitionService buildFlowDefinitionService();

    /**
     * 构造流程节点流转服务类
     * @return
     */
    public FlowProcessService buildFlowProcessService();
}
