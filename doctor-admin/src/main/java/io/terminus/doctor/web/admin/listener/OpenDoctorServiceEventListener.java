package io.terminus.doctor.web.admin.listener;


import com.google.common.eventbus.Subscribe;
import io.terminus.doctor.common.event.EventListener;
import io.terminus.doctor.msg.service.DoctorMessageRuleWriteService;
import io.terminus.doctor.user.event.OpenDoctorServiceEvent;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/16
 */
@Component
@Slf4j
public class OpenDoctorServiceEventListener implements EventListener {

    private final DoctorMessageRuleWriteService doctorMessageRuleWriteService;

    @Autowired
    public OpenDoctorServiceEventListener(DoctorMessageRuleWriteService doctorMessageRuleWriteService) {
        this.doctorMessageRuleWriteService = doctorMessageRuleWriteService;
    }

    /**
     * 管理员审批允许给用户开通猪场软件服务 事件监听
     * 将猪场和消息规则进行绑定
     */
    @Subscribe
    public void onFarmOpen(OpenDoctorServiceEvent event) {
        List<Long> farms = event.getFarmIds();
        for (int i = 0; farms != null && i < farms.size(); i++) {
            doctorMessageRuleWriteService.initTemplate(farms.get(i));
        }
    }
}
