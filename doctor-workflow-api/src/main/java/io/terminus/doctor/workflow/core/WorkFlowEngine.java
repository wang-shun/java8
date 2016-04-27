package io.terminus.doctor.workflow.core;

import io.terminus.doctor.workflow.access.JdbcAccess;

import java.io.InputStream;

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

}
