package io.terminus.doctor.event.handler.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.DoctorWeanGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorWeanGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 12:13 17/3/11
 */

@Slf4j
@Component
public class DoctorWeanGroupEventHandler extends DoctorAbstractGroupEventHandler{

    private final DoctorGroupEventDao doctorGroupEventDao;

    @Autowired
    public DoctorWeanGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao, DoctorGroupTrackDao doctorGroupTrackDao, DoctorGroupEventDao doctorGroupEventDao, DoctorBarnDao doctorBarnDao) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, doctorGroupEventDao, doctorBarnDao);
        this.doctorGroupEventDao = doctorGroupEventDao;
    }

    @Override
    public <I extends BaseGroupInput> DoctorGroupEvent buildGroupEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.WEAN.getValue());
        DoctorWeanGroupInput weanInput = (DoctorWeanGroupInput) input;

        //1.转换转入猪群事件
        DoctorWeanGroupEvent weanGroupEvent = BeanMapper.map(weanInput, DoctorWeanGroupEvent.class);

        //2.创建猪群断奶事件
        DoctorGroupEvent<DoctorWeanGroupEvent> event = dozerGroupEvent(group, GroupEventType.WEAN, weanInput);
        event.setQuantity(weanInput.getPartWeanPigletsCount());
        event.setAvgWeight(weanInput.getPartWeanAvgWeight());


        event.setExtraMap(weanGroupEvent);
        return event;
    }

    @Override
    public DoctorGroupTrack updateTrackOtherInfo(DoctorGroupEvent event, DoctorGroupTrack track) {
        DoctorWeanGroupEvent weanGroupEvent = JSON_MAPPER.fromJson(event.getExtra(), DoctorWeanGroupEvent.class);
        track.setUnqQty(EventUtil.plusInt(track.getUnqQty(), weanGroupEvent.getNotQualifiedCount()));
        track.setQuaQty(EventUtil.minusQuantity(track.getQuantity(), track.getUnqQty()));
        track.setWeanQty(EventUtil.plusInt(track.getWeanQty(), weanGroupEvent.getPartWeanPigletsCount()));
        track.setUnweanQty(EventUtil.minusQuantity(track.getQuantity(), track.getWeanQty()));
        track.setWeanWeight(EventUtil.plusDouble(track.getWeanWeight(), weanGroupEvent.getPartWeanAvgWeight() * weanGroupEvent.getPartWeanPigletsCount()));
        return track;
    }

    @Override
    protected <I extends BaseGroupInput> void handleEvent(List<DoctorEventInfo> eventInfoList, DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.WEAN.getValue());
        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorWeanGroupInput weanInput = (DoctorWeanGroupInput) input;

        //1.转换转入猪群事件
        DoctorWeanGroupEvent weanGroupEvent = BeanMapper.map(weanInput, DoctorWeanGroupEvent.class);

        //2.创建猪群断奶事件
        DoctorGroupEvent<DoctorWeanGroupEvent> event = dozerGroupEvent(group, GroupEventType.WEAN, weanInput);
        event.setQuantity(weanInput.getPartWeanPigletsCount());
        event.setAvgWeight(weanInput.getPartWeanAvgWeight());


        event.setExtraMap(weanGroupEvent);
        doctorGroupEventDao.create(event);

        //创建关联关系
        createEventRelation(event);

        //3.更新猪群跟踪
        groupTrack.setUnqQty(EventUtil.plusInt(groupTrack.getUnqQty(), weanInput.getNotQualifiedCount()));
        groupTrack.setQuaQty(EventUtil.minusQuantity(groupTrack.getQuantity(), groupTrack.getUnqQty()));
        groupTrack.setWeanQty(EventUtil.plusInt(groupTrack.getWeanQty(), weanInput.getPartWeanPigletsCount()));
        groupTrack.setUnweanQty(EventUtil.minusQuantity(groupTrack.getQuantity(), groupTrack.getWeanQty()));
        groupTrack.setWeanWeight(EventUtil.plusDouble(groupTrack.getWeanWeight(), weanInput.getPartWeanAvgWeight() * weanInput.getPartWeanPigletsCount()));

        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, groupTrack), GroupEventType.MOVE_IN);

    }


}
