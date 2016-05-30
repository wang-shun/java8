package io.terminus.doctor.workflow.core;

/**
 * Desc: 任务调度类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/30
 */
public interface Scheduler {

    /**
     * 任务轮询方法
     */
    void doSchedule();

}
