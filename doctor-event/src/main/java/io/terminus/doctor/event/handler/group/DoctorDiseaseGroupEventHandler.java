package io.terminus.doctor.event.handler.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.DoctorDiseaseGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Desc: 疾病事件处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class DoctorDiseaseGroupEventHandler extends DoctorAbstractGroupEventHandler {

    private final DoctorGroupEventDao doctorGroupEventDao;

    @Autowired
    public DoctorDiseaseGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                          DoctorGroupTrackDao doctorGroupTrackDao,
                                          DoctorGroupEventDao doctorGroupEventDao,
                                          DoctorBarnDao doctorBarnDao) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, doctorGroupEventDao, doctorBarnDao);
        this.doctorGroupEventDao = doctorGroupEventDao;
    }


    @Override
    protected <I extends BaseGroupInput> void handleEvent(List<DoctorEventInfo> eventInfoList, DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.DISEASE.getValue());

        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorDiseaseGroupInput disease = (DoctorDiseaseGroupInput) input;
        checkQuantity(groupTrack.getQuantity(), disease.getQuantity());

        //1.转换下疾病信息
        DoctorDiseaseGroupEvent diseaseEvent = BeanMapper.map(disease, DoctorDiseaseGroupEvent.class);

        //2.创建疾病事件
        DoctorGroupEvent<DoctorDiseaseGroupEvent> event = dozerGroupEvent(group, GroupEventType.DISEASE, disease);

        int deltaDays = DateUtil.getDeltaDaysAbs(event.getEventAt(), new Date());
        event.setAvgDayAge(getGroupEventAge(groupTrack.getAvgDayAge(), deltaDays));  //重算日龄

        event.setQuantity(disease.getQuantity());
        event.setExtraMap(diseaseEvent);
        doctorGroupEventDao.create(event);

        //疾病不影响track

//        //3.更新猪群跟踪
//        updateGroupTrack(groupTrack, event);

        //4.创建镜像
//        createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, event, groupTrack), GroupEventType.DISEASE);
    }
}
