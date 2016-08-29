package io.terminus.doctor.workflow.event;

import io.terminus.doctor.workflow.core.TackerExecution;

/**
 * Created by xiao on 16/8/17.
 */
public interface ITacker {
    /**
     * tacker处理
     * @param tackerExecution
     * @return
     */
    Boolean tacker(TackerExecution tackerExecution);
}
