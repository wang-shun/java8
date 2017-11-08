package io.terminus.doctor.event.helper;

import com.google.common.collect.Maps;
import io.terminus.common.redis.utils.JedisTemplate;
import io.terminus.doctor.common.utils.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;

/**
 * Created by xjn on 17/10/24.
 */
@Slf4j
@Component
public class DoctorConcurrentControl {
    private final JedisTemplate jedisTemplate;
    private ThreadLocal<Map<String, DoctorConcurrentDto>>  threadLocal =
            ThreadLocal.withInitial(Maps::newHashMap);
//    private ThreadLocal<Map<String, InterProcessMutex>>  threadLocal =
//            ThreadLocal.withInitial(Maps::newHashMap);

//    private CuratorFramework curatorFramework;

    private static final Integer EXPIRE = 600;

    @Autowired
    public DoctorConcurrentControl(JedisTemplate jedisTemplate
//                                   ZKClientFactory zkClientFactory
                                   ) {
        this.jedisTemplate = jedisTemplate;
//        this.curatorFramework = zkClientFactory.getClient();
    }

    public Boolean setKey(String key) {
        Map<String, DoctorConcurrentDto> map = threadLocal.get();
        if (map.containsKey(key)) {
            map.get(key).setCount(map.get(key).getCount() + 1);
            return Boolean.TRUE;
        }
        String value = String.valueOf(RandomUtil.random(0, Integer.MAX_VALUE-1));
        Boolean result = Objects.equals(jedisTemplate.execute(jedis -> {
            return jedis.set(key, value, "nx", "ex", EXPIRE);
        }), "OK");
        if (result) {
            threadLocal.get().put(key, new DoctorConcurrentDto(value, 1));
        }
        return result;
    }

//    public Boolean setKey(String key) {
//        try {
//            if (threadLocal.get().containsKey(key)) {
//                InterProcessMutex mutex = threadLocal.get().get(key);
//                return mutex.acquire(EXPIRE, TimeUnit.SECONDS);
//            }
//            InterProcessMutex mutex = new InterProcessMutex(curatorFramework, "/" + key);
//            Boolean result = mutex.acquire(EXPIRE, TimeUnit.SECONDS);
//            if (result) {
//                threadLocal.get().put(key, mutex);
//            }
//            return result;
//        } catch (Exception e) {
//            log.error("acquire lock failed, cause:{}", Throwables.getStackTraceAsString(e));
//            return false;
//        }
//    }

    public void delKey(String key) {
        Map<String, DoctorConcurrentDto> map = threadLocal.get();
        DoctorConcurrentDto doctorConcurrentDto = map.get(key);
        if (isNull(doctorConcurrentDto)) {
            return;
        }
        if (doctorConcurrentDto.getCount() > 1) {
            doctorConcurrentDto.setCount(doctorConcurrentDto.getCount() -1);
            return;
        }
        String value = jedisTemplate.execute(jedis -> {
            return jedis.get(key);
        });
        if (threadLocal.get().containsKey(key)
                && threadLocal.get().get(key).getValue().equals(value)) {
            jedisTemplate.execute(jedis -> {
                return jedis.del(key);
            });
            threadLocal.get().remove(key);
        }
    }

//    public void delKey(String key) {
//        try {
//            InterProcessMutex mutex = threadLocal.get().get(key);
//            if (notNull(mutex) && mutex.isAcquiredInThisProcess()) {
//                mutex.release();
//            }
//        } catch (Exception e) {
//            log.error("release lock failed, cause:{}", Throwables.getStackTraceAsString(e));
//        }
//
//    }
}

