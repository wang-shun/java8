package io.terminus.doctor.event.handler.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorLiveStockGroupEvent;
import io.terminus.doctor.event.dto.event.group.edit.BaseGroupEdit;
import io.terminus.doctor.event.dto.event.group.edit.DoctorLiveStockGroupEdit;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Desc: 猪只存栏事件处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class DoctorLiveStockGroupEventHandler extends DoctorAbstractGroupEventHandler {

    private final DoctorGroupEventDao doctorGroupEventDao;

    @Autowired
    public DoctorLiveStockGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                            DoctorGroupTrackDao doctorGroupTrackDao,
                                            CoreEventDispatcher coreEventDispatcher,
                                            DoctorGroupEventDao doctorGroupEventDao) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, coreEventDispatcher, doctorGroupEventDao);
        this.doctorGroupEventDao = doctorGroupEventDao;
    }


    @Override
    protected <I extends BaseGroupInput> void handleEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorLiveStockGroupInput liveStock = (DoctorLiveStockGroupInput) input;

        //1.转换下猪只存栏信息
        DoctorLiveStockGroupEvent liveStockEvent = BeanMapper.map(liveStock, DoctorLiveStockGroupEvent.class);

        //2.创建猪只存栏事件
        DoctorGroupEvent<DoctorLiveStockGroupEvent> event = dozerGroupEvent(group, GroupEventType.LIVE_STOCK, liveStock);
        event.setQuantity(groupTrack.getQuantity());  //猪群存栏数量 = 猪群数量
        event.setAvgDayAge(groupTrack.getAvgDayAge());
        event.setAvgWeight(liveStock.getAvgWeight());
        event.setWeight(event.getQuantity() * event.getAvgWeight()); // 总活体重 = 数量 * 均重
        event.setExtraMap(liveStockEvent);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, event, groupTrack), GroupEventType.LIVE_STOCK);
    }

    @Override
    protected <E extends BaseGroupEdit> void editEvent(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorGroupEvent event, E edit) {
        DoctorLiveStockGroupEdit liveStockEdit = (DoctorLiveStockGroupEdit) edit;

        //更新字段
        DoctorLiveStockGroupEvent liveStockEvent = JSON_MAPPER.fromJson(event.getExtra(), DoctorLiveStockGroupEvent.class);

    }
}
