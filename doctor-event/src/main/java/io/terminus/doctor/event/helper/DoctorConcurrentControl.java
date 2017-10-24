package io.terminus.doctor.event.helper;

import io.terminus.common.redis.utils.JedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by xjn on 17/10/24.
 */
@Component
public class DoctorConcurrentControl {
    @Autowired
    private JedisTemplate jedisTemplate;

    private static final Integer EXPIRE = 600;

    public Boolean setKey(String key) {
        return jedisTemplate.execute(jedis -> {
            long result = jedis.setnx(key, "");
            if (result == 1) {
                jedis.expire(key, EXPIRE);
            }
            return result;
        }) == 1;
    }

    public void delKey(String key) {
        jedisTemplate.execute(jedis -> {
            return jedis.del(key);
        });
    }
}
