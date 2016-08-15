package io.terminus.doctor.workflow.core;

import io.terminus.common.model.Response;

/**
 * Created by xiao on 16/8/11.
 */
public interface SynchronizedData {
    /**
     *更新数据库有关流程的表数据包含(workflow_process_instances,workflow_process_tracks,workflow_processes)三张表
     * @param flowDefinitionKey
     * @param businessId
     * @return
     */
    Response<Boolean> updateData(String flowDefinitionKey, Long businessId);
}
