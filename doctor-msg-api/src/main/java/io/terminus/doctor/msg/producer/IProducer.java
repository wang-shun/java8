package io.terminus.doctor.msg.producer;

import io.terminus.doctor.msg.dto.SubUser;

import java.util.List;

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
    void produce(List<SubUser> subUsers);

}
