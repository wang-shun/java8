package io.terminus.doctor.workflow.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Desc: 流程服务类,包括功能如下
 *      1. 流程部署
 *      2. 流程删除
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/25
 */
@Slf4j
@Service
public class FlowManagerServiceImpl implements FlowManagerService{

    @Override
    public void deploy(String sourceName) {
        deploy(getClass().getClassLoader().getResourceAsStream(sourceName));
    }

    @Override
    public void deploy(InputStream inputStream) {
        log.debug("流程部署开始");

    }
}
