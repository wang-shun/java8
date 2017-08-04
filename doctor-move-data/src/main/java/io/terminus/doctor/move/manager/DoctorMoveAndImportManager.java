package io.terminus.doctor.move.manager;

import io.terminus.doctor.event.manager.DoctorGroupEventManager;
import io.terminus.doctor.event.manager.DoctorGroupManager;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListSow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by xjn on 17/8/4.
 * 迁移与导入
 */
@Slf4j
@Component
public class DoctorMoveAndImportManager {
    @Autowired
    public DoctorPigEventManager pigEventManager;
    @Autowired
    public DoctorGroupEventManager groupEventManager;
    @Autowired
    public DoctorGroupManager groupManager;

    public void executePigEvent(DoctorMoveBasicData moveBasicData, List<View_EventListSow> rowEventList) {
        //1.构建事件所需数据
        // TODO: 17/8/4 策略模式

        //2.执行事件
        // TODO: 17/8/4 调用 pigEventManager
    }

    public void executeGroupEvent() {
        //1.构建事件所需数据

        //2.执行事件
    }
}
