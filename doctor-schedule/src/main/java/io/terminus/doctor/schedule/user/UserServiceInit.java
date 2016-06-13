package io.terminus.doctor.schedule.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 陈增辉 on 16/6/13.用户服务状态初始化job
 * 用户注册时有一个事件分发,在事件监听器中已经初始化了用户的服务状态,此job只是再做一次数据检查和处理,理论上不应该真的会有数据需要处理
 */
@Slf4j
@Configuration
@EnableScheduling
@Component
public class UserServiceInit {

    @Scheduled(cron = "0 */15 * * * ?")
    public void userServiceInit() {

    }
}
