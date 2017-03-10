package io.terminus.doctor.event.editHandler.group;

import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.editHandler.DoctorEditGroupEventHandler;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 21:05 17/3/8
 */

@Slf4j
public abstract class DoctorAbstractEditGroupEventHandler implements DoctorEditGroupEventHandler{

    protected static final JsonMapper JSON_MAPPER = JsonMapper.nonDefaultMapper();

    @Autowired
    private DoctorGroupEventDao doctorGroupEventDao;
    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired
    private DoctorGroupSnapshotDao doctorGroupSnapshotDao;
    @Autowired
    private DoctorGroupDao doctorGroupDao;

    @Override
    public DoctorGroupTrack handle(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent){
        DoctorGroupEvent preDoctorGroupEvent = doctorGroupEventDao.findById(doctorGroupTrack.getRelEventId());
        //根据event推演track
        handlerGroupEvent(doctorGroupTrack, doctorGroupEvent, preDoctorGroupEvent);
        if(!checkDoctorGroupTrack(doctorGroupTrack)){
            return null;
        }
        //创建猪群事件
        doctorGroupEventDao.create(doctorGroupEvent);

        //创建猪群track
        doctorGroupTrack.setRelEventId(doctorGroupEvent.getId());    //关联此次的事件id
        doctorGroupTrackDao.update(doctorGroupTrack);

        //创建snapshot
        DoctorGroupSnapshot doctorGroupSnapshot = new DoctorGroupSnapshot();
        doctorGroupSnapshot.setGroupId(doctorGroupEvent.getGroupId());
        doctorGroupSnapshot.setFromEventId(preDoctorGroupEvent.getId());
        doctorGroupSnapshot.setToEventId(doctorGroupEvent.getId());

        DoctorGroupSnapShotInfo doctorGroupSnapShotInfo = new DoctorGroupSnapShotInfo();
        DoctorGroup doctorGroup = doctorGroupDao.findById(doctorGroupEvent.getGroupId());
        doctorGroupSnapShotInfo.setGroupEvent(doctorGroupEvent);
        doctorGroupSnapShotInfo.setGroupTrack(doctorGroupTrack);
        doctorGroupSnapShotInfo.setGroup(doctorGroup);
        doctorGroupSnapshot.setToInfo(JSON_MAPPER.toJson(doctorGroupSnapShotInfo));
        doctorGroupSnapshotDao.create(doctorGroupSnapshot);

        return doctorGroupTrack;
    }

    /**
     * 推演doctorGroupTrack
     * @param doctorGroupTrack
     * @param doctorGroupEvent
     */
    protected abstract void handlerGroupEvent(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent, DoctorGroupEvent preDoctorGroupEvent);

    /**
     * 检查推演之后的doctorGroupTrack是否正确
     * @param doctorGroupTrack
     * @return
     */
    private Boolean checkDoctorGroupTrack(DoctorGroupTrack doctorGroupTrack){
        if(doctorGroupTrack.getAvgDayAge() <= 0 ||
                doctorGroupTrack.getQuantity() <0 ||
                doctorGroupTrack.getBoarQty() < 0 ||
                doctorGroupTrack.getSowQty() < 0 ||
                doctorGroupTrack.getLiveQty() < 0 ||
                doctorGroupTrack.getHealthyQty() < 0 ||
                doctorGroupTrack.getWeakQty() < 0 ||
                doctorGroupTrack.getQuaQty() < 0 ||
                doctorGroupTrack.getUnqQty() < 0 ||
                doctorGroupTrack.getWeanQty() < 0 ||
                doctorGroupTrack.getUnweanQty() < 0){
            return false;
        }
        return true;
    }

}
