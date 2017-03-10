package io.terminus.doctor.event.editHandler.group;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dao.*;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.editHandler.DoctorEditGroupEventHandler;
import io.terminus.doctor.event.model.*;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

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
    @Autowired
    private DoctorBarnDao doctorBarnDao;

    @Override
    public DoctorGroupTrack handle(List<DoctorGroupEvent> doctorGroupEventList, DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent){
        //校验基本的数量,看会不会失败
        if(!checkDoctorGroupEvent(doctorGroupTrack, doctorGroupEvent)){
            log.info("edit group event failed, doctorGroupEvent={}", doctorGroupEvent);
            throw new JsonResponseException("edit.group.event.failed");
        }

        DoctorGroupEvent preDoctorGroupEvent = doctorGroupEventDao.findById(doctorGroupTrack.getRelEventId());
        //根据event推演track
        handlerGroupEvent(doctorGroupTrack, doctorGroupEvent, preDoctorGroupEvent);
        //创建猪群事件
        doctorGroupEventDao.create(doctorGroupEvent);

        //新增的事件放入需要回滚的list
        doctorGroupEventList.add(doctorGroupEvent);

        //创建猪群track
        updateGroupTrack(doctorGroupTrack, doctorGroupEvent);

        //创建snapshot
        createGroupEventSnapshot(doctorGroupTrack, doctorGroupEvent, preDoctorGroupEvent);


        return doctorGroupTrack;
    }

    /**
     * 推演之前先校验
     * @param doctorGroupTrack
     * @param doctorGroupEvent
     */
    protected abstract boolean checkDoctorGroupEvent(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent);

    /**
     * 推演doctorGroupTrack
     * @param doctorGroupTrack
     * @param doctorGroupEvent
     */
    protected abstract void handlerGroupEvent(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent, DoctorGroupEvent preDoctorGroupEvent);


    //更新猪群跟踪
    public void updateGroupTrack(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent) {
        doctorGroupTrack.setRelEventId(doctorGroupEvent.getId());    //关联此次的事件id
        doctorGroupTrack.setUpdatorId(doctorGroupEvent.getCreatorId());
        doctorGroupTrack.setUpdatorName(doctorGroupEvent.getCreatorName());
        doctorGroupTrack.setBirthDate(new DateTime(doctorGroupEvent.getEventAt()).plusDays(1 - doctorGroupTrack.getAvgDayAge()).toDate());
        doctorGroupTrackDao.update(doctorGroupTrack);
    }

    private void createGroupEventSnapshot(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent, DoctorGroupEvent preDoctorGroupEvent) {
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
    }

    public DoctorBarn getBarnById(Long barnId) {
        return doctorBarnDao.findById(barnId);
    }

    //获取转种猪性别
    public static DoctorPig.PigSex getSex(Integer toBarnType) {
        if (PigType.MATING_TYPES.contains(toBarnType)) {
            return DoctorPig.PigSex.SOW;
        }
        return DoctorPig.PigSex.BOAR;
    }

    //如果是公猪并且数量大于0 就 -1
    public static int getBoarQty(DoctorPig.PigSex sex, Integer oldQty) {
        if (oldQty <= 0) {
            return 0;
        }
        if (sex.equals(DoctorPig.PigSex.BOAR)) {
            return oldQty - 1;
        }
        return oldQty;
    }

}
