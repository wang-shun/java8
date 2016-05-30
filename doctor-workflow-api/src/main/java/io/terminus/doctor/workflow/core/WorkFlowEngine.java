package io.terminus.doctor.workflow.core;

import io.terminus.doctor.workflow.access.JdbcAccess;
import io.terminus.doctor.workflow.event.Interceptor;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.service.FlowDefinitionService;
import io.terminus.doctor.workflow.service.FlowProcessService;
import io.terminus.doctor.workflow.service.FlowQueryService;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

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
    JdbcAccess buildJdbcAccess();

    /**
     * 创建配置解析类
     * @param inputStream   文件流
     * @return
     * @throws Exception
     */
    Configuration buildConfiguration(InputStream inputStream) throws Exception;

    /**
     * 获取全局上下文对象
     * @return
     */
    Context buildContext();

    /**
     * 获取所有拦截器对象
     * @return
     */
    List<Interceptor> buildInterceptors();

    /**
     * 创建节点执行器
     * @param flowDefinitionKey 流程定义key
     * @param businessId        业务id
     * @param assignee          任务
     * @return
     */
    Executor buildExecutor(String flowDefinitionKey, Long businessId, String assignee);

    /**
     * 创建节点执行容器
     * @param flowProcess   当前活动的流程节点
     * @param expression    流转表达式, 一般是decision节点情况
     * @param flowData      节点间的流转数据
     * @param operatorId    操作人id
     * @param operatorName  操作人name
     * @return
     */
    Execution buildExecution(FlowProcess flowProcess, Map expression, String flowData, Long operatorId, String operatorName);

    /**
     * 构造公共服务类
     * @return
     */
    WorkFlowService buildWorkFlowService();

    /**
     * 构造流程定义服务类
     * @return
     */
    FlowDefinitionService buildFlowDefinitionService();

    /**
     * 构造流程节点流转服务类
     * @return
     */
    FlowProcessService buildFlowProcessService();

    /**
     * 构造流程查询服务类
     * @return
     */
    FlowQueryService buildFlowQueryService();

    /**
     * 构造一个Schedule对象
     * @return
     */
    Scheduler buildScheduler();
}
