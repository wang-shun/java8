package io.terminus.doctor.workflow.core;

import io.terminus.doctor.workflow.access.JdbcAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Desc: 流程引擎实现类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/26
 */
@Component
public class WorkFlowEngineImpl implements WorkFlowEngine{

    @Autowired
    private JdbcAccess jdbcAccess;

    @Override
    public JdbcAccess buildJdbcAccess() {
        return jdbcAccess;
    }

    @Override
    public Configuration buildConfiguration(InputStream inputStream) throws Exception {
        return new ConfigManager(inputStream);
    }

}
