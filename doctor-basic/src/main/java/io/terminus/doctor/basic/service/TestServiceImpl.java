package io.terminus.doctor.basic.service;

import io.terminus.boot.rpc.common.annotation.RpcProvider;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/6
 */
@Service
@RpcProvider
public class TestServiceImpl implements TestService {
    @Override
    public void say(String name) {
        System.out.println("hello:" + name);
    }
}
