package io.terminus.doctor.event.editHandler.group;

import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dao.DoctorEventModifyLogDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.editHandler.DoctorModifyGroupEventHandler;
import io.terminus.doctor.event.model.DoctorEventModifyLog;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xjn on 17/4/13.
 * 猪群编辑抽象实现
 */
public abstract class DoctorAbstractModifyGroupEventHandler implements DoctorModifyGroupEventHandler{
    @Autowired
    private DoctorGroupDao doctorGroupDao;
    @Autowired
    private DoctorGroupEventDao doctorGroupEventDao;
    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired
    private DoctorEventModifyLogDao doctorEventModifyLogDao;

    @Override
    public void handle(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        //1.构建变化记录
        DoctorEventChangeDto changeDto = buildEventChange(oldGroupEvent, input);

        //2.校验
        handleCheck(oldGroupEvent, changeDto);

        //3.更新事件
        DoctorGroupEvent newEvent = buildNewEvent(oldGroupEvent, input);
        doctorGroupEventDao.update(newEvent);

        //4.更新猪群
        if (isUpdateGroup(changeDto)) {
            DoctorGroup oldGroup = doctorGroupDao.findById(oldGroupEvent.getGroupId());
            DoctorGroup newGroup = buildNewGroup(oldGroup, changeDto);
            doctorGroupDao.update(newGroup);
        }

        //5.更新track
        if (isUpdateTrack(changeDto)) {
            DoctorGroupTrack oldTrack = doctorGroupTrackDao.findByGroupId(oldGroupEvent.getGroupId());
            DoctorGroupTrack newTrack = buildNewTrack(oldTrack, changeDto);
            doctorGroupTrackDao.update(newTrack);
        }

        //6.更新每日数据记录

        //7.调用触发事件的编辑

    }

    @Override
    public void handleCheck(DoctorGroupEvent oldGroupEvent, DoctorEventChangeDto changeDto) {

    }

    @Override
    public DoctorGroup buildNewGroup(DoctorGroup oldGroup, DoctorEventChangeDto changeDto) {
        return null;
    }

    @Override
    public DoctorGroupTrack buildNewTrack(DoctorGroupTrack oldGroupTrack, DoctorEventChangeDto changeDto) {
        return null;
    }

    /**
     * 是否需要更新猪群
     * @param changeDto 变化记录
     * @return
     */
    private boolean isUpdateGroup(DoctorEventChangeDto changeDto){
        //// TODO: 17/4/13 是否需要更新
        return true;
    }

    /**
     * 是否需要更新track
     * @param changeDto 变化记录
     * @return
     */
    private boolean isUpdateTrack(DoctorEventChangeDto changeDto){
        // TODO: 17/4/13 是否需要跟新
        return true;
    }


    /**
     * 创建编辑记录
     * @param oldEvent 原事件
     * @param newEvent 新事件
     */
    protected void createModifyLog(DoctorGroupEvent oldEvent, DoctorGroupEvent newEvent) {
        DoctorEventModifyLog modifyLog = DoctorEventModifyLog.builder()
                .businessId(newEvent.getGroupId())
                .businessCode(newEvent.getGroupCode())
                .farmId(newEvent.getFarmId())
                .fromEvent(ToJsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(oldEvent))
                .toEvent(ToJsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(newEvent))
                .type(DoctorEventModifyRequest.TYPE.PIG.getValue())
                .build();
        doctorEventModifyLogDao.create(modifyLog);
    }
}
