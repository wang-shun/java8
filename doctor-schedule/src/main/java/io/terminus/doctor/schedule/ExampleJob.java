package io.terminus.doctor.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Desc:
 * Mail: houly@terminus.io
 * author: Hou Luyao
 * Date: 16/4/20.
 */
@Slf4j
@Component
@Configurable
public class ExampleJob {
    @Scheduled(cron = "0/1  * * * * ?")
    public void demo(){
        // System.out.println("Hello World");
    }
}
