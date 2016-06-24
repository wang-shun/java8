package io.terminus.doctor.open.rest.user;

import com.google.common.eventbus.Subscribe;
import io.terminus.common.redis.utils.JedisTemplate;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.open.common.Sessions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/24
 */
@Slf4j
@Component
public class DoctorLoginEventListener implements EventListener {
    @Autowired
    private JedisTemplate jedisTemplate;

    @Subscribe
    public void testSession(DoctorLoginEvent event) {
        String sessionId = event.getSessionId();
        log.info("get doctor login event:{}", event);
        jedisTemplate.execute(jedis -> {
            jedis.expire(Sessions.TOKEN_PREFIX + ":" + sessionId, 888);
            log.info("expire session:{}", sessionId);
        });

    }
}
