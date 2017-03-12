package io.terminus.doctor.event.handler.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.DoctorLiveStockGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
                                            DoctorGroupEventDao doctorGroupEventDao,
                                            DoctorBarnDao doctorBarnDao) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, doctorGroupEventDao, doctorBarnDao);
        this.doctorGroupEventDao = doctorGroupEventDao;
    }

    @Override
    public <I extends BaseGroupInput> DoctorGroupEvent buildGroupEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.LIVE_STOCK.getValue());

        DoctorLiveStockGroupInput liveStock = (DoctorLiveStockGroupInput) input;

        //1.转换下猪只存栏信息
        DoctorLiveStockGroupEvent liveStockEvent = BeanMapper.map(liveStock, DoctorLiveStockGroupEvent.class);

        //2.创建猪只存栏事件
        DoctorGroupEvent<DoctorLiveStockGroupEvent> event = dozerGroupEvent(group, GroupEventType.LIVE_STOCK, liveStock);
        event.setQuantity(groupTrack.getQuantity());  //猪群存栏数量 = 猪群数量



        event.setAvgWeight(liveStock.getAvgWeight());
        event.setWeight(event.getQuantity() * event.getAvgWeight()); // 总活体重 = 数量 * 均重
        event.setExtraMap(liveStockEvent);
        return event;
    }

    @Override
    public DoctorGroupTrack elicitGroupTrack(DoctorGroupEvent preEvent, DoctorGroupEvent event, DoctorGroupTrack groupTrack) {
        return null;
    }


    @Override
    protected <I extends BaseGroupInput> void handleEvent(List<DoctorEventInfo> eventInfoList, DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.LIVE_STOCK.getValue());

        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorLiveStockGroupInput liveStock = (DoctorLiveStockGroupInput) input;

        //1.转换下猪只存栏信息
        DoctorLiveStockGroupEvent liveStockEvent = BeanMapper.map(liveStock, DoctorLiveStockGroupEvent.class);

        //2.创建猪只存栏事件
        DoctorGroupEvent<DoctorLiveStockGroupEvent> event = dozerGroupEvent(group, GroupEventType.LIVE_STOCK, liveStock);
        event.setQuantity(groupTrack.getQuantity());  //猪群存栏数量 = 猪群数量



        event.setAvgWeight(liveStock.getAvgWeight());
        event.setWeight(event.getQuantity() * event.getAvgWeight()); // 总活体重 = 数量 * 均重
        event.setExtraMap(liveStockEvent);
        doctorGroupEventDao.create(event);

        //猪只存栏不更新track,不增加snapshot
//        //3.更新猪群跟踪
//        updateGroupTrack(groupTrack, event);
//
//        //4.创建镜像
//        createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, event, groupTrack), GroupEventType.LIVE_STOCK);
    }
}
