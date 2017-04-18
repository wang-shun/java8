package io.terminus.doctor.event.editHandler.pig;

import io.terminus.common.utils.BeanMapper;
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
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.event.dto.DoctorBasicInputInfoDto.generateEventDescFromExtra;


/**
 * Created by xjn on 17/4/13.
 * 猪事件编辑抽象实现
 */
@Slf4j
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

    protected final ToJsonMapper TO_JSON_MAPPER = ToJsonMapper.JSON_NON_DEFAULT_MAPPER;

    @Override
    public void modifyHandleCheck(DoctorPigEvent oldPigEvent, DoctorEventChangeDto changeDto) {

    }

    @Override
    public void modifyHandle(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
       log.info("modify pig event handler starting, oldPigEvent:{}, inputDto:{}", oldPigEvent, inputDto);

        //1.构建变化量
        DoctorEventChangeDto changeDto = buildEventChange(oldPigEvent, inputDto);

        //2.校验
        modifyHandleCheck(oldPigEvent, changeDto);

        //3.更新事件
        DoctorPigEvent newEvent = buildNewEvent(oldPigEvent, inputDto);
        doctorPigEventDao.update(newEvent);

        //4.创建事件完成后创建编辑记录
        createModifyLog(oldPigEvent, newEvent);

        //5.更新猪信息
        if (isUpdatePig(changeDto)) {
            DoctorPig oldPig = doctorPigDao.findById(oldPigEvent.getId());
            DoctorPig newPig = buildNewPig(oldPig, changeDto);
            doctorPigDao.update(newPig);
        }

        //6.更新track
        if (isUpdateTrack(changeDto)) {
            DoctorPigTrack oldPigTrack = doctorPigTrackDao.findByPigId(oldPigEvent.getPigId());
            DoctorPigTrack newTrack = buildNewTrack(oldPigTrack, changeDto);
            doctorPigTrackDao.update(newTrack);
        }

        //7.更新每日数据记录

        //8.调用触发事件的编辑
        triggerEventModifyHandle(newEvent);

        log.info("modify pig event handler ending");
    }

    @Override
    public void rollbackHandle(DoctorGroupEvent groupEvent, Long operatorId, String operatorName) {

    }

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        return null;
    }

    @Override
    public DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        DoctorPigEvent newEvent = new DoctorPigEvent();
        BeanMapper.copy(oldPigEvent, newEvent);
        newEvent.setExtra(TO_JSON_MAPPER.toJson(inputDto));
        newEvent.setDesc(generateEventDescFromExtra(inputDto));
        newEvent.setRemark(inputDto.changeRemark());
        newEvent.setEventAt(inputDto.eventAt());
        return newEvent;
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
     * 触发事件的处理
     * @param newPigEvent 猪事件
     */
    protected void triggerEventModifyHandle(DoctorPigEvent newPigEvent){};

    /**
     * 是否需要更新猪
     * @param changeDto 变化记录
     * @return
     */
    private boolean isUpdatePig(DoctorEventChangeDto changeDto){
        return notNull(changeDto.getSource())
                || notNull(changeDto.getBirthDate())
                || notNullAndNotZero(changeDto.getBirthWeightChange())
                || notNull(changeDto.getNewEventAt())
                || notNull(changeDto.getPigBreedId())
                || notNull(changeDto.getPigBreedTypeId())
                || notNull(changeDto.getBoarType());
    }

    /**
     * 是否需要更新track
     * @param changeDto 变化记录
     * @return
     */
    private boolean isUpdateTrack(DoctorEventChangeDto changeDto) {
        return notNullAndNotZero(changeDto.getWeightChange())
                || notNullAndNotZero(changeDto.getLiveCountChange())
                || notNullAndNotZero(changeDto.getWeanCountChange());
        // TODO: 17/4/13 是否需要跟新
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

    /**
     * 不等于空且不等于零
     * @param d
     * @return
     */
    private boolean notNullAndNotZero(Double d) {
        return notNull(d) && d != 0D;
    }

    /**
     * 不等于空且不等于零
     * @param d
     * @return
     */
    private boolean notNullAndNotZero(Integer d) {
        return notNull(d) && d != 0;
    }
}
