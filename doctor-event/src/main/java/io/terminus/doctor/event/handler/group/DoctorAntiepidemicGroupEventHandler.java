package io.terminus.doctor.event.handler.group;

import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Desc: 防疫事件处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class DoctorAntiepidemicGroupEventHandler extends DoctorAbstractGroupEventHandler {

    private final DoctorGroupEventDao doctorGroupEventDao;

    @Autowired
    public DoctorAntiepidemicGroupEventHandler(DoctorGroupTrackDao doctorGroupTrackDao,
                                               DoctorGroupEventDao doctorGroupEventDao,
                                               DoctorBarnDao doctorBarnDao) {
        super(doctorGroupTrackDao, doctorGroupEventDao, doctorBarnDao);
        this.doctorGroupEventDao = doctorGroupEventDao;
    }

    @Override
    public <I extends BaseGroupInput> DoctorGroupEvent buildGroupEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.ANTIEPIDEMIC.getValue());
        DoctorAntiepidemicGroupInput antiepidemic = (DoctorAntiepidemicGroupInput) input;
        checkQuantity(groupTrack.getQuantity(), antiepidemic.getQuantity());

        //2.创建防疫事件
        DoctorGroupEvent<DoctorAntiepidemicGroupInput> event = dozerGroupEvent(group, GroupEventType.ANTIEPIDEMIC, antiepidemic);

        event.setBasicId(antiepidemic.getVaccinItemId());
        event.setBasicName(antiepidemic.getVaccinItemName());
        event.setVaccinationId(antiepidemic.getVaccinId());
        event.setVaccinationName(antiepidemic.getVaccinName());
        event.setVaccinResult(antiepidemic.getVaccinResult());
        event.setQuantity(antiepidemic.getQuantity());
        event.setExtraMap(antiepidemic);
        event.setOperatorId(antiepidemic.getVaccinStaffId());
        event.setOperatorName(antiepidemic.getVaccinStaffName());

        return event;
    }

    @Override
    public DoctorGroupTrack updateTrackOtherInfo(DoctorGroupEvent event, DoctorGroupTrack track) {
        return track;
    }


    @Override
    protected <I extends BaseGroupInput> void handleEvent(List<DoctorEventInfo> eventInfoList, DoctorGroup group, DoctorGroupTrack groupTrack, I input) {

        //1.构建event
        DoctorGroupEvent event = buildGroupEvent(group, groupTrack, input);

        //2.检验事件是否可执行


        doctorGroupEventDao.create(event);

        //3.推演track


        //4.创建snapshot


    }

}
