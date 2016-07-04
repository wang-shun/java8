package io.terminus.doctor.event.handler.group;

import com.google.common.base.MoreObjects;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.edit.BaseGroupEdit;
import io.terminus.doctor.event.dto.event.group.edit.DoctorMoveInGroupEdit;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * Desc: 转入猪群事件处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class DoctorMoveInGroupEventHandler extends DoctorAbstractGroupEventHandler {

    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorGroupTrackDao doctorGroupTrackDao;

    @Autowired
    public DoctorMoveInGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                         DoctorGroupTrackDao doctorGroupTrackDao,
                                         CoreEventDispatcher coreEventDispatcher,
                                         DoctorGroupEventDao doctorGroupEventDao) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, coreEventDispatcher, doctorGroupEventDao);
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
    }


    @Override
    protected <I extends BaseGroupInput> void handleEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorMoveInGroupInput moveIn = (DoctorMoveInGroupInput) input;

        checkQuantityEqual(moveIn.getQuantity(), moveIn.getBoarQty(), moveIn.getSowQty());

        //1.转换转入猪群事件
        DoctorMoveInGroupEvent moveInEvent = BeanMapper.map(moveIn, DoctorMoveInGroupEvent.class);
        checkBreed(group.getBreedId(), moveInEvent.getBreedId());

        //2.创建转入猪群事件
        DoctorGroupEvent<DoctorMoveInGroupEvent> event = dozerGroupEvent(group, GroupEventType.MOVE_IN, moveIn);
        event.setQuantity(moveIn.getQuantity());
        event.setAvgDayAge(moveIn.getAvgDayAge());
        event.setAvgWeight(moveIn.getAvgWeight());
        event.setWeight(EventUtil.getWeight(event.getAvgWeight(), event.getQuantity()));
        event.setExtraMap(moveInEvent);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        groupTrack.setQuantity(EventUtil.plusQuantity(groupTrack.getQuantity(), moveIn.getQuantity()));
        groupTrack.setBoarQty(EventUtil.plusQuantity(groupTrack.getBoarQty(), moveIn.getBoarQty()));
        groupTrack.setSowQty(EventUtil.plusQuantity(groupTrack.getSowQty(), moveIn.getSowQty()));

        //重新计算日龄
        groupTrack.setAvgDayAge(EventUtil.getAvgDayAge(groupTrack.getAvgDayAge(), groupTrack.getQuantity(), moveIn.getAvgDayAge(), moveIn.getQuantity()));
        groupTrack.setBirthDate(EventUtil.getBirthDate(new Date(), groupTrack.getAvgDayAge()));

        //重新计算重量
        groupTrack.setAvgWeight(EventUtil.getAvgWeight(groupTrack.getWeight(), EventUtil.getWeight(moveIn.getAvgWeight(), moveIn.getQuantity()), groupTrack.getQuantity()));
        groupTrack.setWeight(EventUtil.getWeight(groupTrack.getAvgWeight(), groupTrack.getQuantity()));

        //重新计算金额
        groupTrack.setAmount(groupTrack.getAmount() + MoreObjects.firstNonNull(moveIn.getAmount(), 0L));
        groupTrack.setPrice(EventUtil.getPrice(groupTrack.getAmount(), groupTrack.getQuantity()));
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, event, groupTrack), GroupEventType.MOVE_IN);

        //发布统计事件
        publishCountGroupEvent(group.getOrgId(), group.getFarmId());
        publistGroupAndBarn(group.getId(), group.getCurrentBarnId());
    }

    @Override
    protected <E extends BaseGroupEdit> void editEvent(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorGroupEvent event, E edit) {
        DoctorMoveInGroupEdit moveInEdit = (DoctorMoveInGroupEdit) edit;
        DoctorMoveInGroupEvent moveInEvent = JSON_MAPPER.fromJson(event.getExtra(), DoctorMoveInGroupEvent.class);

        //更新track(更新均重和金额)
        if (!Objects.equals(event.getAvgWeight(), moveInEdit.getAvgWeight())) {
            groupTrack.setAvgWeight(editAvgWeight(groupTrack, event, moveInEdit));
            groupTrack.setWeight(EventUtil.getWeight(groupTrack.getAvgWeight(), groupTrack.getQuantity()));
        }
        if (moveInEdit.getAmount() != null) {
            groupTrack.setAmount(groupTrack.getAmount() - MoreObjects.firstNonNull(moveInEvent.getAmount(), 0) + moveInEdit.getAmount());
            groupTrack.setPrice(EventUtil.getPrice(groupTrack.getAmount(), groupTrack.getQuantity()));
        }
        doctorGroupTrackDao.update(groupTrack);

        //更新事件字段
        moveInEvent.setSource(moveInEdit.getSource());
        moveInEvent.setBreedId(moveInEdit.getBreedId());
        moveInEvent.setBreedName(moveInEdit.getBreedName());
        event.setExtraMap(moveInEvent);

        if (!Objects.equals(event.getAvgWeight(), moveInEdit.getAvgWeight())) {
            event.setAvgWeight(moveInEdit.getAvgWeight());
            event.setWeight(EventUtil.getWeight(event.getAvgWeight(), event.getQuantity()));
        }
        editGroupEvent(event, edit);

        //更新猪群镜像
        editGroupSnapShot(group, groupTrack, event);
    }

    //重新计算下均重
    private Double editAvgWeight(DoctorGroupTrack groupTrack, DoctorGroupEvent event, DoctorMoveInGroupEdit moveInEdit) {
        Double allWeight = groupTrack.getAvgWeight() * groupTrack.getQuantity() -
                event.getAvgWeight() * event.getQuantity() + moveInEdit.getAvgWeight() * event.getQuantity();
        return allWeight / groupTrack.getQuantity();
    }
}
