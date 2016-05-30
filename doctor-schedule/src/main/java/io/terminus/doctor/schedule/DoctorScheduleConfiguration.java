package io.terminus.doctor.schedule;

import io.terminus.doctor.workflow.WorkFlowJobConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Desc: schedule 配置
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/30
 */
@Configuration
@Import({
        WorkFlowJobConfiguration.class
})
public class DoctorScheduleConfiguration {

}
