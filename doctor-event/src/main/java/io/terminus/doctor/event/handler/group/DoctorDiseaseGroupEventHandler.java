package io.terminus.doctor.event.handler.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.event.group.DoctorDiseaseGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Desc: 疾病事件处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
@Component
public class DoctorDiseaseGroupEventHandler extends DoctorAbstractGroupEventHandler {

    private final DoctorGroupEventDao doctorGroupEventDao;

    @Autowired
    public DoctorDiseaseGroupEventHandler(DoctorGroupDao doctorGroupDao,
                                          DoctorGroupEventDao doctorGroupEventDao,
                                          DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                          DoctorGroupTrackDao doctorGroupTrackDao,
                                          DoctorGroupReadService doctorGroupReadService,
                                          CoreEventDispatcher coreEventDispatcher,
                                          DoctorGroupWriteService doctorGroupWriteService) {
        super(doctorGroupDao, doctorGroupEventDao, doctorGroupSnapshotDao, doctorGroupTrackDao,
                doctorGroupReadService, coreEventDispatcher, doctorGroupWriteService);
        this.doctorGroupEventDao = doctorGroupEventDao;
    }

    @Override
    protected <I extends BaseGroupInput> void handleEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        DoctorDiseaseGroupInput disease = (DoctorDiseaseGroupInput) input;
        checkQuantity(groupTrack.getQuantity(), disease.getQuantity());

        //1.转换下疾病信息
        DoctorDiseaseGroupEvent diseaseEvent = BeanMapper.map(disease, DoctorDiseaseGroupEvent.class);

        //2.创建疾病事件
        DoctorGroupEvent<DoctorDiseaseGroupEvent> event = dozerGroupEvent(group, GroupEventType.DISEASE, disease);
        event.setQuantity(disease.getQuantity());
        event.setExtraMap(diseaseEvent);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(group, event, groupTrack, GroupEventType.DISEASE);
    }
}
