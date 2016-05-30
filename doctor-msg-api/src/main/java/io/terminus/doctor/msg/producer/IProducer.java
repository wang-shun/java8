package io.terminus.doctor.msg.producer;

/**
 * Desc: 预警消息产生接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/30
 */
public interface IProducer {

    /**
     * 消息产生者处理方法
     */
    void produce();

}
