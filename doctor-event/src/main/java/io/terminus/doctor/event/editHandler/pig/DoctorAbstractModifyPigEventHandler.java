package io.terminus.doctor.event.editHandler.pig;

import com.google.common.collect.Lists;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dao.DoctorDailyReportDao;
import io.terminus.doctor.event.dao.DoctorEventModifyLogDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.editHandler.DoctorModifyPigEventHandler;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorEventModifyLog;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectNotNull;
import static io.terminus.doctor.event.dto.DoctorBasicInputInfoDto.generateEventDescFromExtra;
import static io.terminus.doctor.event.editHandler.group.DoctorAbstractModifyGroupEventHandler.validEventAt;
import static io.terminus.doctor.event.handler.DoctorAbstractEventHandler.IGNORE_EVENT;


/**
 * Created by xjn on 17/4/13.
 * 猪事件编辑抽象实现
 */
@Slf4j
public abstract class DoctorAbstractModifyPigEventHandler implements DoctorModifyPigEventHandler{
    @Autowired
    protected DoctorPigEventDao doctorPigEventDao;
    @Autowired
    protected DoctorPigTrackDao doctorPigTrackDao;
    @Autowired
    protected DoctorPigDao doctorPigDao;
    @Autowired
    protected DoctorDailyReportDao doctorDailyPigDao;
    @Autowired
    protected DoctorGroupEventDao doctorGroupEventDao;
    @Autowired
    private DoctorEventModifyLogDao doctorEventModifyLogDao;

    protected final JsonMapperUtil JSON_MAPPER = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER;

    protected final ToJsonMapper TO_JSON_MAPPER = ToJsonMapper.JSON_NON_DEFAULT_MAPPER;

    /**
     * 影响猪信息的事件类型
     */
    public static final List<Integer> EFFECT_PIG_EVENTS = Lists.newArrayList(PigEvent.ENTRY.getKey(),
            PigEvent.CHG_FARM.getKey(), PigEvent.CHG_FARM_IN.getKey(), PigEvent.REMOVAL.getKey());


    @Override
    public final Boolean canModify(DoctorPigEvent oldPigEvent) {
        return Objects.equals(oldPigEvent.getIsAuto(), IsOrNot.NO.getValue())
                && !Objects.equals(oldPigEvent.getEventSource(), SourceType.TRANS_FARM.getValue());
    }

    @Override
    public void modifyHandle(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        log.info("modify pig event handler starting, oldPigEvent:{}", oldPigEvent);
        log.info("inputDto:{}", inputDto);
        //1.校验
        modifyHandleCheck(oldPigEvent, inputDto);

        //2.构建变化量
        DoctorEventChangeDto changeDto = buildEventChange(oldPigEvent, inputDto);

        //3.更新事件
        DoctorPigEvent newEvent = buildNewEvent(oldPigEvent, inputDto);
        doctorPigEventDao.updateIncludeNull(newEvent);

        //4.创建事件完成后创建编辑记录
        createModifyLog(oldPigEvent, newEvent);

        //5.更新猪信息
        if (isUpdatePig(changeDto)) {
            DoctorPig oldPig = doctorPigDao.findById(oldPigEvent.getPigId());
            DoctorPig newPig = buildNewPig(oldPig, inputDto);
            doctorPigDao.update(newPig);
        }

        //6.更新track
        if (isUpdateTrack(changeDto)) {
            DoctorPigTrack oldPigTrack = doctorPigTrackDao.findByPigId(oldPigEvent.getPigId());
            DoctorPigTrack newTrack = buildNewTrack(oldPigTrack, changeDto);
            doctorPigTrackDao.update(newTrack);
        }

        //7.更新每日数据记录
        updateDailyForModify(oldPigEvent, inputDto, changeDto);

        //8.调用触发事件的编辑
        triggerEventModifyHandle(newEvent);

        log.info("modify pig event handler ending");
    }

    @Override
    public Boolean canRollback(DoctorPigEvent deletePigEvent) {
        return  !Objects.equals(deletePigEvent.getEventSource(), SourceType.TRANS_FARM.getValue())
                && isLastManualEvent(deletePigEvent)
                && rollbackHandleCheck(deletePigEvent);
    }

    @Override
    public void rollbackHandle(DoctorPigEvent deletePigEvent, Long operatorId, String operatorName) {
       log.info("rollback handle starting, deletePigEvent:{}", deletePigEvent);

        //1.删除触发事件
        triggerEventRollbackHandle(deletePigEvent, operatorId, operatorName);

        //2.删除事件
        doctorPigEventDao.delete(deletePigEvent.getId());

        //3.删除记录
        createModifyLog(deletePigEvent);

        //4.更新猪
        if (isUpdatePig(deletePigEvent.getType())) {
            DoctorPig oldPig = doctorPigDao.findById(deletePigEvent.getPigId());
            if (Objects.equals(deletePigEvent.getType(), PigEvent.ENTRY.getKey())) {
                doctorPigDao.delete(oldPig.getId());
            } else {
                DoctorPig newPig = buildNewPigForRollback(deletePigEvent, oldPig);
                doctorPigDao.update(newPig);
            }
        }

        //5.更新track
        if (isUpdateTrack(deletePigEvent.getType())) {
            DoctorPigTrack oldTrack = doctorPigTrackDao.findByPigId(deletePigEvent.getPigId());
            if (Objects.equals(deletePigEvent.getType(), PigEvent.ENTRY.getKey())) {
                doctorPigTrackDao.delete(oldTrack.getId());
            } else {
                DoctorPigTrack newTrack = buildNewTrackForRollback(deletePigEvent, oldTrack);
                doctorPigTrackDao.update(newTrack);
            }
        }

        //6.更新报表
        updateDailyForDelete(deletePigEvent);

        log.info("rollback handle ending");
    }

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        return null;
    }

    /**
     * 默认实现
     * @param oldPigEvent 原事件
     * @param inputDto 新输入
     */
    protected void modifyHandleCheck(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto) {
        //编辑的事件的时间校验
        DoctorPigEvent downEvent;
        DoctorPigEvent upEvent = null;
        if (IGNORE_EVENT.contains(oldPigEvent.getType())) {
            downEvent = doctorPigEventDao.queryLastEnter(oldPigEvent.getPigId());
        } else if (Objects.equals(oldPigEvent.getType(), PigEvent.REMOVAL.getKey())) {
            downEvent = doctorPigEventDao.getLastEventBeforeRemove(oldPigEvent.getPigId(), oldPigEvent.getId());
        } else {
            downEvent = doctorPigEventDao.getLastStatusEventBeforeEventAtExcludeId(
                    oldPigEvent.getPigId(), oldPigEvent.getEventAt(), oldPigEvent.getId());
            upEvent = doctorPigEventDao.getLastStatusEventAfterEventAtExcludeId(
                    oldPigEvent.getPigId(), oldPigEvent.getEventAt(), oldPigEvent.getId());
        }
        validEventAt(inputDto.eventAt(), notNull(downEvent) ? downEvent.getEventAt() : null
                , notNull(upEvent) ? upEvent.getEventAt() : new Date());
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
    public DoctorPig buildNewPig(DoctorPig oldPig, BasePigEventInputDto inputDto) {
        return oldPig;
    }

    @Override
    public DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoctorEventChangeDto changeDto) {
        return oldPigTrack;
    }

    /**
     * 更新日记录表(编辑)
     * @param oldPigEvent 原事件
     * @param inputDto 新输入
     * @param changeDto 变化
     */
    protected void updateDailyForModify(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto, DoctorEventChangeDto changeDto){}

    /**
     * 构建日记录
     * @param oldDailyPig 原记录
     * @param changeDto 变化量
     * @return 新记录
     */
    protected DoctorDailyReport buildDailyPig(DoctorDailyReport oldDailyPig, DoctorEventChangeDto changeDto){
        return expectNotNull(oldDailyPig, "daily.pig.not.null");
    }

    /**
     * 触发事件的处理(编辑)
     * @param newPigEvent 猪事件
     */
    protected void triggerEventModifyHandle(DoctorPigEvent newPigEvent){}

    /**
     * 子类的具体实现(删除)
     * @param deletePigEvent 删除事件
     */
    protected boolean rollbackHandleCheck(DoctorPigEvent deletePigEvent) {
        return true;
    }

    /**
     * 触发事件处理(删除)
     * @param deletePigEvent 删除事件
     */
    protected void triggerEventRollbackHandle(DoctorPigEvent deletePigEvent, Long operatorId, String operatorName ){}

    /**
     * 构建新猪(删除)
     * @param deletePigEvent 删除事件
     * @param oldPig 原猪
     * @return 新猪
     */
    protected DoctorPig buildNewPigForRollback(DoctorPigEvent deletePigEvent, DoctorPig oldPig) {
        return oldPig;
    }

    /**
     * 构建删除后新track(删除)
     * @param deletePigEvent 删除事件
     * @param oldPigTrack 原track
     * @return 新track
     */
    protected DoctorPigTrack buildNewTrackForRollback(DoctorPigEvent deletePigEvent, DoctorPigTrack oldPigTrack){
        return null;
    }

    /**
     * 更新日记录(删除)
     * @param deletePigEvent 删除时间
     */
    protected void updateDailyForDelete(DoctorPigEvent deletePigEvent){}

    /**
     * 删除事件更新日记录
     * @param oldPigEvent 被删除事件
     */
    public void updateDailyOfDelete(DoctorPigEvent oldPigEvent) {}

    /**
     * 新建事件更新日记录
     * @param newPigEvent 新建事件
     * @param inputDto 新输入
     */
    public void updateDailyOfNew(DoctorPigEvent newPigEvent, BasePigEventInputDto inputDto) {}

        /**
         * 是否需要更新猪(编辑)
         * @param changeDto 变化记录
         * @return 是否需要更新猪
         */
    private boolean isUpdatePig(DoctorEventChangeDto changeDto) {
        return notNull(changeDto)
                && (notNull(changeDto.getSource())
                || notNull(changeDto.getBirthDate())
                || notNullAndNotZero(changeDto.getBirthWeightChange())
                || notNull(changeDto.getNewEventAt())
                || notNull(changeDto.getPigBreedId())
                || notNull(changeDto.getPigBreedTypeId())
                || notNull(changeDto.getBoarType()));
    }

    /**
     * 是否需要更新猪(删除)
     * @param eventType 事件类型
     * @return  是否需要更新猪
     */
    private boolean isUpdatePig(Integer eventType) {
        return EFFECT_PIG_EVENTS.contains(eventType);
    }

    /**
     * 是否需要更新track(编辑)
     * @param changeDto 变化记录
     * @return 是否需要更新track
     */
    private boolean isUpdateTrack(DoctorEventChangeDto changeDto) {
        return true;
    }

    /**
     * 是否需要更新track(删除)
     * @param eventType 事件类型
     * @return 是否需要更新track
     */
    private boolean isUpdateTrack(Integer eventType) {
        return !IGNORE_EVENT.contains(eventType);
    }

    /**
     * 创建编辑记录
     * @param oldEvent 原事件
     * @param newEvent 新事件
     */
    private void createModifyLog(DoctorPigEvent oldEvent, DoctorPigEvent newEvent) {
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
     * 创建删除记录
     * @param deleteEvent 删除事件
     */
    private void createModifyLog(DoctorPigEvent deleteEvent) {
        DoctorEventModifyLog modifyLog = DoctorEventModifyLog.builder()
                .businessId(deleteEvent.getPigId())
                .businessCode(deleteEvent.getPigCode())
                .farmId(deleteEvent.getFarmId())
                .deleteEvent(ToJsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(deleteEvent))
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

    /**
     * 是否是最新事件
     * @param pigEvent 猪事件
     */
    private boolean isLastManualEvent(DoctorPigEvent pigEvent) {
        if (IGNORE_EVENT.contains(pigEvent.getType())) {
            return true;
        }
        DoctorPigEvent lastEvent = doctorPigEventDao.findLastManualEventExcludeTypes(pigEvent.getPigId(), IGNORE_EVENT);
        return notNull(lastEvent) && Objects.equals(pigEvent.getId(), lastEvent.getId());
    }
}
