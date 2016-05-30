package io.terminus.doctor.msg.producer;

/**
 * Desc: 消息产生者抽象类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/30
 */
public abstract class AbstractProducer implements IProducer {

    @Override
    public void produce() {
        // 1. 产生消息

        // 2. 存入消息
    }

    protected abstract boolean ifProduce();

    protected abstract String message();
}
