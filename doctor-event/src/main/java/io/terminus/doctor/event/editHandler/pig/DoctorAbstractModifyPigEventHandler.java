package io.terminus.doctor.event.editHandler.pig;

import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dao.DoctorEventModifyLogDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.editHandler.DoctorModifyPigEventHandler;
import io.terminus.doctor.event.model.DoctorEventModifyLog;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xjn on 17/4/13.
 * 猪事件编辑抽象实现
 */
public abstract class DoctorAbstractModifyPigEventHandler implements DoctorModifyPigEventHandler{
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;
    @Autowired
    private DoctorPigDao doctorPigDao;
    @Autowired
    private DoctorEventModifyLogDao doctorEventModifyLogDao;

    protected final JsonMapperUtil JSON_MAPPER = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER;
    @Override
    public void handleCheck(DoctorPigEvent oldPigEvent, DoctorEventChangeDto changeDto) {

    }

    @Override
    public void handle(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        //1.构建变化量
        DoctorEventChangeDto changeDto = buildEventChange(oldPigEvent, inputDto);

        //2.校验
        handleCheck(oldPigEvent, changeDto);

        //3.更新事件
        DoctorPigEvent newEvent = buildNewEvent(oldPigEvent, inputDto);
        doctorPigEventDao.update(newEvent);

        //4.更新猪信息
        if (isUpdatePig(changeDto)) {
            DoctorPig oldPig = doctorPigDao.findById(oldPigEvent.getId());
            DoctorPig newPig = buildNewPig(oldPig, changeDto);
            doctorPigDao.update(newPig);
        }

        //5.更新track
        if (isUpdateTrack(changeDto)) {
            DoctorPigTrack oldPigTrack = doctorPigTrackDao.findByPigId(oldPigEvent.getPigId());
            DoctorPigTrack newTrack = buildNewTrack(oldPigTrack, changeDto);
            doctorPigTrackDao.update(newTrack);
        }

        //6.更新每日数据记录

        //7.调用触发事件的编辑

    }

    @Override
    public DoctorPig buildNewPig(DoctorPig oldPig, DoctorEventChangeDto changeDto) {
        return null;
    }

    @Override
    public DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoctorEventChangeDto changeDto) {
        return null;
    }

    /**
     * 是否需要更新猪
     * @param changeDto 变化记录
     * @return
     */
    private boolean isUpdatePig(DoctorEventChangeDto changeDto){
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
    protected void createModifyLog(DoctorPigEvent oldEvent, DoctorPigEvent newEvent) {
        DoctorEventModifyLog modifyLog = DoctorEventModifyLog.builder()
                .businessId(newEvent.getPigId())
                .businessCode(newEvent.getPigCode())
                .farmId(newEvent.getFarmId())
                .fromEvent(ToJsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(oldEvent))
                .toEvent(ToJsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(newEvent))
                .type(DoctorEventModifyRequest.TYPE.PIG.getValue())
                .build();
        doctorEventModifyLogDao.create(modifyLog);
    }
}
