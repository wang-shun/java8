package io.terminus.doctor.event.handler.group;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.event.ZkGroupPublishDto;
import io.terminus.doctor.common.event.ZkListenedGroupEvent;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.DoctorCloseGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Desc: 关闭猪群事件处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class DoctorCloseGroupEventHandler extends DoctorAbstractGroupEventHandler {

    private final DoctorGroupDao doctorGroupDao;
    private final DoctorGroupEventDao doctorGroupEventDao;
    @Autowired
    private Publisher publisher;

    @Autowired
    public DoctorCloseGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                        DoctorGroupTrackDao doctorGroupTrackDao,
                                        DoctorGroupDao doctorGroupDao,
                                        DoctorGroupEventDao doctorGroupEventDao,
                                        DoctorBarnDao doctorBarnDao) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, doctorGroupEventDao, doctorBarnDao);
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
    }


    @Override
    protected <I extends BaseGroupInput> void handleEvent(List<DoctorEventInfo> eventInfoList, DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        //校验能否关闭
        checkCanClose(groupTrack);

        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorCloseGroupInput close = (DoctorCloseGroupInput) input;

        //1.转换下信息
        DoctorCloseGroupEvent closeEvent = BeanMapper.map(close, DoctorCloseGroupEvent.class);

        //2.创建关闭猪群事件
        DoctorGroupEvent<DoctorCloseGroupEvent> event = dozerGroupEvent(group, GroupEventType.CLOSE, close);

        int deltaDays = DateUtil.getDeltaDaysAbs(event.getEventAt(), new Date());
        int dayAge = getGroupEventAge(groupTrack.getAvgDayAge(), deltaDays);
        event.setAvgDayAge(dayAge);  //重算日龄
        event.setExtraMap(closeEvent);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪, 日龄是事件发生时的日龄
        groupTrack.setAvgDayAge(dayAge);
        updateGroupTrack(groupTrack, event);

        //4.猪群状态改为关闭
        group.setStatus(DoctorGroup.Status.CLOSED.getValue());
        group.setCloseAt(event.getEventAt());
        doctorGroupDao.update(group);

        //5.创建镜像
        createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, event, groupTrack), GroupEventType.CLOSE);

        //发布统计事件
        //publistGroupAndBarn(event);

        //发布zk事件
        try{
            // 向zk发送刷新消息的事件
            ZkGroupPublishDto zkGroupPublishDto = new ZkGroupPublishDto(group.getId(), event.getId(), event.getEventAt(), event.getType());
            publisher.publish(DataEvent.toBytes(DataEventType.GroupEventClose.getKey(), new ZkListenedGroupEvent(group.getOrgId(), group.getFarmId(), Lists.newArrayList(zkGroupPublishDto))));
        }catch(Exception e){
            log.error(Throwables.getStackTraceAsString(e));
        }
    }

    //猪群里还有猪不可关闭!
    private void checkCanClose(DoctorGroupTrack groupTrack) {
        if (groupTrack.getQuantity() > 0) {
            throw new ServiceException("group.not.empty.cannot.close");
        }
    }
}
