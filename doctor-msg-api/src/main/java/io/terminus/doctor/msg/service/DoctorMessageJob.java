package io.terminus.doctor.msg.service;

/**
 * Desc: 消息的job执行
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/1
 */
public interface DoctorMessageJob {

    /**
     * 产生消息方法
     */
    void produce();

}
