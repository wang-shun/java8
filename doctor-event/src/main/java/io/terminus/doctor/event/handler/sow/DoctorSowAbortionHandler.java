package io.terminus.doctor.event.handler.sow;

import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventFlowHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.workflow.core.Execution;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static io.terminus.common.utils.Arguments.notNull;

/**
 * Created by yaoqijun.
 * Date:2016-06-01
 * Email:yaoqj@terminus.io
 * Descirbe: 母猪流产事件处理
 */
@Component
public class DoctorSowAbortionHandler extends DoctorAbstractEventFlowHandler {

    @Autowired
    public DoctorSowAbortionHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao,
                                    DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao,
                                    DoctorRevertLogDao doctorRevertLogDao) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao);
    }

    @Override
    protected void eventCreatePreHandler(Execution execution, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto, Map<String, Object> extra, Map<String, Object> context) {
        //流产事件时间
        DateTime abortionDate = new DateTime(Long.valueOf(extra.get("abortionDate").toString()));
        doctorPigEvent.setAbortionDate(abortionDate.toDate());

        //查找最近一次配种事件
        DoctorPigEvent lastMate = doctorPigEventDao.queryLastFirstMate(doctorPigTrack.getPigId(), doctorPigTrack.getCurrentParity());
        if (notNull(lastMate)) {
            DateTime mattingDate = new DateTime(Long.valueOf(lastMate.getExtraMap().get("matingDate").toString()));

            int npd = Days.daysBetween(abortionDate, mattingDate).getDays();
            doctorPigEvent.setPlnpd(doctorPigEvent.getPlnpd() + npd);
            doctorPigEvent.setNpd(doctorPigEvent.getNpd() + npd);
        }
    }


    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(Execution execution, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) {
        doctorPigTrack.setStatus(PigStatus.Abortion.getKey());
        doctorPigTrack.addAllExtraMap(extra);
        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        doctorPigTrack.addAllExtraMap(MapBuilder.<String, Object>of().put("liuchanToMateLiuchan", true).map());
        return doctorPigTrack;
    }
}
