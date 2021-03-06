package io.terminus.doctor.event.handler.group;

import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorWeanGroupInput;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupWeanEventHandler;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

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
    private DoctorModifyGroupWeanEventHandler modifyGroupWeanEventHandler;

    @Autowired
    public DoctorWeanGroupEventHandler(DoctorGroupTrackDao doctorGroupTrackDao, DoctorGroupEventDao doctorGroupEventDao, DoctorBarnDao doctorBarnDao) {
        super(doctorGroupTrackDao, doctorGroupEventDao, doctorBarnDao);
        this.doctorGroupEventDao = doctorGroupEventDao;
    }

    @Override
    public <I extends BaseGroupInput> DoctorGroupEvent buildGroupEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.WEAN.getValue());
        DoctorWeanGroupInput weanInput = (DoctorWeanGroupInput) input;

        //2.创建猪群断奶事件
        DoctorGroupEvent event = dozerGroupEvent(group, GroupEventType.WEAN, weanInput);
        event.setQuantity(weanInput.getPartWeanPigletsCount());
        event.setAvgWeight(weanInput.getPartWeanAvgWeight());
        event.setExtraMap(weanInput);
        return event;
    }

    @Override
    public DoctorGroupTrack updateTrackOtherInfo(DoctorGroupEvent event, DoctorGroupTrack track) {
        DoctorWeanGroupInput weanGroupEvent = JSON_MAPPER.fromJson(event.getExtra(), DoctorWeanGroupInput.class);
        if(Arguments.isNull(weanGroupEvent)){
            log.error("parse doctorTransGroupEvent faild, doctorGroupEvent = {}", event);
            throw new InvalidException("wean.group.event.info.broken", event.getId());
        }
        track.setUnqQty(EventUtil.plusInt(track.getUnqQty(), weanGroupEvent.getNotQualifiedCount()));
        track.setQuaQty(EventUtil.minusInt(track.getQuantity(), track.getUnqQty()));
        track.setWeanQty(EventUtil.plusInt(track.getWeanQty(), weanGroupEvent.getPartWeanPigletsCount()));
        track.setUnweanQty(EventUtil.minusQuantity(track.getUnweanQty(), weanGroupEvent.getPartWeanPigletsCount()));
        track.setWeanWeight(EventUtil.plusDouble(track.getWeanWeight(), weanGroupEvent.getPartWeanAvgWeight() * weanGroupEvent.getPartWeanPigletsCount()));
        return track;
    }

    @Override
    protected <I extends BaseGroupInput> void handleEvent(List<DoctorEventInfo> eventInfoList, DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.WEAN.getValue());
        DoctorWeanGroupInput weanInput = (DoctorWeanGroupInput) input;

        //1.转换转入猪群事件

        //2.创建猪群断奶事件
        DoctorGroupEvent event = dozerGroupEvent(group, GroupEventType.WEAN, weanInput);
        event.setQuantity(weanInput.getPartWeanPigletsCount());
        event.setAvgWeight(weanInput.getPartWeanAvgWeight());
        event.setSowId(weanInput.getSowId());
        event.setSowCode(weanInput.getSowCode());

        event.setExtraMap(weanInput);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        groupTrack.setUnqQty(EventUtil.plusInt(groupTrack.getUnqQty(), weanInput.getNotQualifiedCount()));
        groupTrack.setQuaQty(EventUtil.minusInt(groupTrack.getQuantity(), groupTrack.getUnqQty()));
        groupTrack.setWeanQty(EventUtil.plusInt(groupTrack.getWeanQty(), weanInput.getPartWeanPigletsCount()));
        groupTrack.setUnweanQty(EventUtil.minusQuantity(groupTrack.getUnweanQty(), weanInput.getPartWeanPigletsCount()));
        groupTrack.setWeanWeight(EventUtil.plusDouble(groupTrack.getWeanWeight(), weanInput.getPartWeanAvgWeight() * weanInput.getPartWeanPigletsCount()));

        updateGroupTrack(groupTrack, event);
        if (Objects.equals(event.getEventSource(), SourceType.INPUT.getValue())) {
            updateDailyForNew(event);
        }

    }

    @Override
    protected void updateDailyForNew(DoctorGroupEvent newGroupEvent) {
        BaseGroupInput newInput = JSON_MAPPER.fromJson(newGroupEvent.getExtra(), DoctorWeanGroupInput.class);
        modifyGroupWeanEventHandler.updateDailyOfNew(newGroupEvent, newInput);
    }
}
