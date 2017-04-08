package io.terminus.doctor.event.handler.group;

import com.google.common.base.MoreObjects;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;

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

    @Autowired
    public DoctorMoveInGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                         DoctorGroupTrackDao doctorGroupTrackDao,
                                         DoctorGroupEventDao doctorGroupEventDao,
                                         DoctorBarnDao doctorBarnDao) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, doctorGroupEventDao, doctorBarnDao);
        this.doctorGroupEventDao = doctorGroupEventDao;
    }


    @Override
    protected <I extends BaseGroupInput> void handleEvent(List<DoctorEventInfo> eventInfoList, DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        DoctorGroupEvent event = buildGroupEvent(group, groupTrack, input);
        doctorGroupEventDao.create(event);

        //创建关联关系
        createEventRelation(event);

        input.setEventType(GroupEventType.MOVE_IN.getValue());
        DoctorMoveInGroupInput moveIn = (DoctorMoveInGroupInput) input;

        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        //3.更新猪群跟踪
        Integer oldQty = groupTrack.getQuantity();
        groupTrack.setQuantity(EventUtil.plusInt(groupTrack.getQuantity(), moveIn.getQuantity()));
        groupTrack.setBoarQty(EventUtil.plusInt(groupTrack.getBoarQty(), moveIn.getBoarQty()));
        groupTrack.setSowQty(EventUtil.plusInt(groupTrack.getSowQty(), moveIn.getSowQty()));

        //重新计算日龄, 按照事件录入日期计算
        int deltaDays = DateUtil.getDeltaDaysAbs(event.getEventAt(), new Date());
        groupTrack.setAvgDayAge(EventUtil.getAvgDayAge(getGroupEventAge(groupTrack.getAvgDayAge(), deltaDays), oldQty, moveIn.getAvgDayAge(), moveIn.getQuantity()) + deltaDays);

        //如果是母猪分娩转入或母猪转舍转入，窝数，分娩统计字段需要累加
        if (moveIn.isSowEvent()) {
            groupTrack.setNest(EventUtil.plusInt(groupTrack.getNest(), 1));  //窝数加 1
            groupTrack.setLiveQty(EventUtil.plusInt(groupTrack.getLiveQty(), moveIn.getQuantity()));
            groupTrack.setWeakQty(EventUtil.plusInt(groupTrack.getWeakQty(), moveIn.getWeakQty()));
            groupTrack.setHealthyQty(groupTrack.getLiveQty() - groupTrack.getWeakQty());    //健仔数 = 活仔数 - 弱仔数
            groupTrack.setUnweanQty(EventUtil.plusInt(groupTrack.getUnweanQty(), moveIn.getQuantity()));    //分娩时，未断奶数累加
            groupTrack.setBirthWeight(EventUtil.plusDouble(groupTrack.getBirthWeight(), moveIn.getAvgWeight() * moveIn.getQuantity()));
        }
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, groupTrack), GroupEventType.MOVE_IN);

        //发布统计事件
        //publistGroupAndBarn(event);
    }

    @Override
    public <I extends BaseGroupInput> DoctorGroupEvent buildGroupEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.MOVE_IN.getValue());
        DoctorMoveInGroupInput moveIn = (DoctorMoveInGroupInput) input;

        checkQuantityEqual(moveIn.getQuantity(), moveIn.getBoarQty(), moveIn.getSowQty());

        //1.转换转入猪群事件
        DoctorMoveInGroupEvent moveInEvent = BeanMapper.map(moveIn, DoctorMoveInGroupEvent.class);
        checkBreed(group.getBreedId(), moveInEvent.getBreedId());

        //2.创建转入猪群事件
        DoctorGroupEvent<DoctorMoveInGroupEvent> event = dozerGroupEvent(group, GroupEventType.MOVE_IN, moveIn);
        event.setSowId(moveIn.getSowId());
        event.setSowCode(moveIn.getSowCode());
        event.setQuantity(moveIn.getQuantity());
        event.setAvgDayAge(moveIn.getAvgDayAge());
        event.setAvgWeight(moveIn.getAvgWeight());
        event.setWeight(EventUtil.getWeight(event.getAvgWeight(), event.getQuantity()));
        event.setInType(moveIn.getInType());

        if (moveIn.getFromBarnId() != null) {
            DoctorBarn fromBarn = getBarnById(moveIn.getFromBarnId());
            moveInEvent.setFromBarnType(fromBarn.getPigType());
            event.setTransGroupType(getTransType(moveIn.getInType(), group.getPigType(), fromBarn).getValue());   //区别内转还是外转
            event.setOtherBarnId(moveIn.getFromBarnId());  //来源猪舍id
            event.setOtherBarnType(fromBarn.getPigType());   //来源猪舍类型
        }

        //空降产房仔猪，断奶统计要重新计算
        if (moveIn.getFromBarnId() == null && Objects.equals(group.getPigType(), PigType.DELIVER_SOW.getValue())) {
            groupTrack.setQuaQty(EventUtil.plusInt(groupTrack.getQuaQty(), event.getQuantity()));
            groupTrack.setWeanQty(EventUtil.plusInt(groupTrack.getWeanQty(), event.getQuantity()));
            groupTrack.setWeanWeight(EventUtil.plusDouble(groupTrack.getWeanWeight(), event.getAvgWeight() * event.getQuantity()));
        }

        event.setExtraMap(moveInEvent);
        event.setEventSource(SourceType.INPUT.getValue());
        return event;
    }

    @Override
    protected void updateAvgDayAge(DoctorGroupEvent event, DoctorGroupTrack track){
        int deltaDays = DateUtil.getDeltaDaysAbs(Dates.startOfDay(event.getEventAt()),Dates.startOfDay(MoreObjects.firstNonNull(track.getBirthDate(), event.getEventAt())));
        int avgDayAge = EventUtil.getAvgDayAge(deltaDays, MoreObjects.firstNonNull(track.getQuantity(), 0), event.getAvgDayAge(), event.getQuantity());
        track.setBirthDate(new DateTime(Dates.startOfDay(event.getEventAt())).minusDays(avgDayAge).toDate());
        track.setAvgDayAge(avgDayAge);
    }

    @Override
    public DoctorGroupTrack updateTrackOtherInfo(DoctorGroupEvent event, DoctorGroupTrack track) {
        DoctorMoveInGroupEvent doctorMoveInGroupEvent = JSON_MAPPER.fromJson(event.getExtra(), DoctorMoveInGroupEvent.class);
        if(Arguments.isNull(doctorMoveInGroupEvent)) {
            log.info("parse doctorMoveInGroupEvent faild, doctorGroupEvent = {}", event);
            //throw new InvalidException("movein.group.event.info.broken", event.getId());
            doctorMoveInGroupEvent = new DoctorMoveInGroupEvent();
        }
        //1.更新猪群跟踪
        track.setQuantity(EventUtil.plusInt(track.getQuantity(), event.getQuantity()));
        if(!Arguments.isNull(track.getBoarQty()) || !Arguments.isNull(doctorMoveInGroupEvent.getBoarQty())){
            track.setBoarQty(EventUtil.plusInt(track.getBoarQty(), doctorMoveInGroupEvent.getBoarQty()));
        }
        if(!Arguments.isNull(track.getSowQty()) || !Arguments.isNull(doctorMoveInGroupEvent.getSowQty())){
            track.setSowQty(EventUtil.plusInt(track.getSowQty(),  doctorMoveInGroupEvent.getSowQty()));
        }

        //空降产房仔猪，断奶统计要重新计算
        if (doctorMoveInGroupEvent.getFromBarnId() == null && Objects.equals(event.getPigType(), PigType.DELIVER_SOW.getValue())) {
            track.setQuaQty(EventUtil.plusInt(track.getQuaQty(), event.getQuantity()));
            track.setWeanQty(EventUtil.plusInt(track.getWeanQty(), event.getQuantity()));
            track.setWeanWeight(EventUtil.plusDouble(track.getWeanWeight(), event.getAvgWeight() * event.getQuantity()));
        }

        //如果是母猪分娩转入或母猪转舍转入，窝数，分娩统计字段需要累加
        if (notNull(event.getRelPigEventId()) || checkFarrowFirstMoveIn(event)) {
            track.setNest(EventUtil.plusInt(track.getNest(), 1));  //窝数加 1
            track.setLiveQty(EventUtil.plusInt(track.getLiveQty(), event.getQuantity()));
            track.setWeakQty(EventUtil.plusInt(track.getWeakQty(), doctorMoveInGroupEvent.getWeakQty()));
            track.setHealthyQty(track.getLiveQty() - track.getWeakQty());    //健仔数 = 活仔数 - 弱仔数
            track.setUnweanQty(EventUtil.plusInt(track.getUnweanQty(), event.getQuantity()));    //分娩时，未断奶数累加
            track.setBirthWeight(EventUtil.plusDouble(track.getBirthWeight(), event.getWeight()));
        }

        track.setSex(DoctorGroupTrack.Sex.MIX.getValue());
        return track;
    }

}
