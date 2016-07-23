package io.terminus.doctor.event.handler.group;

import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorCloseGroupEvent;
import io.terminus.doctor.event.dto.event.group.edit.BaseGroupEdit;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    public DoctorCloseGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                        DoctorGroupTrackDao doctorGroupTrackDao,
                                        CoreEventDispatcher coreEventDispatcher,
                                        DoctorGroupDao doctorGroupDao,
                                        DoctorGroupEventDao doctorGroupEventDao,
                                        DoctorBarnReadService doctorBarnReadService) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, coreEventDispatcher, doctorGroupEventDao, doctorBarnReadService);
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
    }


    @Override
    protected <I extends BaseGroupInput> void handleEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        //校验能否关闭
        checkCanClose(groupTrack);

        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorCloseGroupInput close = (DoctorCloseGroupInput) input;

        //1.转换下信息
        DoctorCloseGroupEvent closeEvent = BeanMapper.map(close, DoctorCloseGroupEvent.class);

        //2.创建关闭猪群事件
        DoctorGroupEvent<DoctorCloseGroupEvent> event = dozerGroupEvent(group, GroupEventType.CLOSE, close);
        event.setExtraMap(closeEvent);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        updateGroupTrack(groupTrack, event);

        //4.猪群状态改为关闭
        group.setStatus(DoctorGroup.Status.CLOSED.getValue());
        doctorGroupDao.update(group);

        //5.创建镜像
        createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, event, groupTrack), GroupEventType.CLOSE);

        //发布统计事件
        publistGroupAndBarn(group.getOrgId(), group.getFarmId(), group.getId(), group.getCurrentBarnId(), event.getId());
    }

    @Override
    protected <E extends BaseGroupEdit> void editEvent(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorGroupEvent event, E edit) {
        // 关闭猪群事件不可编辑, 因为已经关闭的猪群不可操作了
    }

    //猪群里还有猪不可关闭!
    private void checkCanClose(DoctorGroupTrack groupTrack) {
        if (groupTrack.getQuantity() > 0) {
            throw new ServiceException("group.not.empty.cannot.close");
        }
    }
}
