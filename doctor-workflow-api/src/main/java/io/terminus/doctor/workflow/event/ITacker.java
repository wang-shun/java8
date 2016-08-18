package io.terminus.doctor.workflow.event;

/**
 * Created by xiao on 16/8/17.
 */
public interface ITacker {
    /**
     * 母猪配种次数处理
     * @param flowData
     * @return
     */
    Boolean tacker(String flowData);
}
