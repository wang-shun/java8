package io.terminus.doctor.event.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.workflow.core.Execution;
import io.terminus.doctor.workflow.event.HandlerAware;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public abstract class DoctorAbstractEventCreate extends HandlerAware {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    private final DoctorPigDao doctorPigDao;

    private final DoctorPigEventDao doctorPigEventDao;

    private final DoctorPigTrackDao doctorPigTrackDao;

    private final DoctorPigSnapshotDao doctorPigSnapshotDao;

    private final DoctorRevertLogDao doctorRevertLogDao;

    @Autowired
    public DoctorAbstractEventCreate(DoctorPigDao doctorPigDao,
                                 DoctorPigEventDao doctorPigEventDao,
                                 DoctorPigTrackDao doctorPigTrackDao,
                                 DoctorPigSnapshotDao doctorPigSnapshotDao,
                                 DoctorRevertLogDao doctorRevertLogDao){
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorPigDao = doctorPigDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorPigSnapshotDao = doctorPigSnapshotDao;
        this.doctorRevertLogDao = doctorRevertLogDao;
    }

    @Override
    public void handle(Execution execution){
        // get data
//        Map<String,String> dataBussiness = OBJECT_MAPPER.readValue(execution.getBusinessData(), JacksonType.MAP_OF_STRING);
    }

    public abstract DoctorPigTrack updateDoctorPigTrackInfo(DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic);
}
