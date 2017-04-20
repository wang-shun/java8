package io.terminus.doctor.event.editHandler.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dao.DoctorDailyGroupDao;
import io.terminus.doctor.event.dao.DoctorEventModifyLogDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.editHandler.DoctorModifyGroupEventHandler;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorEventModifyLog;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Objects;

/**
 * Created by xjn on 17/4/13.
 * 猪群编辑抽象实现
 */
@Slf4j
public abstract class DoctorAbstractModifyGroupEventHandler implements DoctorModifyGroupEventHandler{
    @Autowired
    protected DoctorGroupDao doctorGroupDao;
    @Autowired
    protected DoctorGroupEventDao doctorGroupEventDao;
    @Autowired
    protected DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired
    protected DoctorDailyGroupDao doctorDailyGroupDao;
    @Autowired
    private DoctorEventModifyLogDao doctorEventModifyLogDao;

    protected final JsonMapperUtil JSON_MAPPER = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER;

    protected final ToJsonMapper TO_JSON_MAPPER = ToJsonMapper.JSON_NON_DEFAULT_MAPPER;

    @Override
    public final Boolean canModify(DoctorGroupEvent oldGroupEvent) {
        return Objects.equals(oldGroupEvent.getIsAuto(), IsOrNot.NO.getValue())
                && Objects.equals(oldGroupEvent.getEventSource(), SourceType.INPUT.getValue());
    }

    @Override
    public void modifyHandle(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        log.info("modify pig event handler starting, oldGroupEvent:{}, input:{}", oldGroupEvent, input);

        //1.构建变化记录
        DoctorEventChangeDto changeDto = buildEventChange(oldGroupEvent, input);

        //2.校验
        modifyHandleCheck(oldGroupEvent, changeDto);

        //3.更新事件
        DoctorGroupEvent newEvent = buildNewEvent(oldGroupEvent, input);
        doctorGroupEventDao.update(newEvent);

        //4.创建事件完成后创建编辑记录
        createModifyLog(oldGroupEvent, newEvent);

        //5.更新猪群
        if (isUpdateGroup(changeDto)) {
            DoctorGroup oldGroup = doctorGroupDao.findById(oldGroupEvent.getGroupId());
            DoctorGroup newGroup = buildNewGroup(oldGroup, changeDto);
            doctorGroupDao.update(newGroup);
        }

        //6.更新track
        if (isUpdateTrack(changeDto)) {
            DoctorGroupTrack oldTrack = doctorGroupTrackDao.findByGroupId(oldGroupEvent.getGroupId());
            DoctorGroupTrack newTrack = buildNewTrack(oldTrack, changeDto);
            doctorGroupTrackDao.update(newTrack);
        }

        //7.更新每日数据记录
        updateDailyForModify(oldGroupEvent, input, changeDto);

        //8.调用触发事件的编辑
        triggerEventModifyHandle(newEvent);

        log.info("modify pig event handler ending");
    }
    
    @Override
    public Boolean canRollback(DoctorGroupEvent deleteGroupEvent) {
        return Objects.equals(deleteGroupEvent.getIsAuto(), IsOrNot.NO.getValue())
                && Objects.equals(deleteGroupEvent.getEventSource(), SourceType.INPUT.getValue());
    }
    
    @Override
    public void rollbackHandle(DoctorGroupEvent deleteGroupEvent, Long operatorId, String operatorName) {
        log.info("rollback handle starting, deleteGroupEvent:{}", deleteGroupEvent);

        //1.删除校验
        rollbackHandleCheck(deleteGroupEvent);

        //2.删除触发事件
        triggerEventRollbackHandle(deleteGroupEvent, operatorId, operatorName);

        //3.删除事件
        doctorGroupEventDao.delete(deleteGroupEvent.getId());

        //4.删除记录

        //5.更新猪
        if (isUpdateGroup(deleteGroupEvent.getType())) {
            DoctorGroup oldGroup = doctorGroupDao.findById(deleteGroupEvent.getGroupId());
            if (Objects.equals(deleteGroupEvent.getType(), GroupEventType.NEW.getValue())) {
                doctorGroupDao.delete(oldGroup.getId());
            } else {
                DoctorGroup newGroup = buildNewGroupForRollback(deleteGroupEvent, oldGroup);
                doctorGroupDao.update(newGroup);
            }
        }

        //6.更新track
        if (isUpdateTrack(deleteGroupEvent.getType())) {
            DoctorGroupTrack oldTrack = doctorGroupTrackDao.findByGroupId(deleteGroupEvent.getGroupId());
            if (Objects.equals(deleteGroupEvent.getType(), GroupEventType.NEW.getValue())) {
                doctorGroupTrackDao.delete(oldTrack.getId());
            } else {
                DoctorGroupTrack newTrack = buildNewTrackForRollback(deleteGroupEvent, oldTrack);
                doctorGroupTrackDao.update(newTrack);
            }
        }

        //7.更新报表
        updateDailyForDelete(deleteGroupEvent);

        log.info("rollback handle ending");
    }

    /**
     * 编辑校验的具体事件
     * @param oldGroupEvent 原事件
     * @param changeDto 变化
     */
    protected void modifyHandleCheck(DoctorGroupEvent oldGroupEvent, DoctorEventChangeDto changeDto) {

    }

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        return null;
    }


    protected DoctorEventChangeDto buildEventChange(BaseGroupInput oldInputDto, BaseGroupInput newInputDto) {
        return null;
    }

    @Override
    public DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorGroupEvent newEvent = new DoctorGroupEvent();
        BeanMapper.copy(oldGroupEvent, newEvent);
        newEvent.setDesc(input.generateEventDesc());
        newEvent.setExtra(TO_JSON_MAPPER.toJson(input));
        newEvent.setEventAt(DateUtil.toDate(input.getEventAt()));
        newEvent.setRemark(input.getRemark());
        return newEvent;
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
     * 更新日记录表
     * @param oldGroupEvent 原事件
     * @param input 新输入
     * @param changeDto 变化
     */
    protected void updateDailyForModify(DoctorGroupEvent oldGroupEvent, BaseGroupInput input, DoctorEventChangeDto changeDto){};

    /**
     * 触发事件的编辑处理(编辑)
     * @param newEvent 修改后事件
     */
    protected void triggerEventModifyHandle(DoctorGroupEvent newEvent) {}

    /**
     * 删除事件校验的具体实现(删除)
     * @param deleteGroupEvent 删除事件
     */
    protected void rollbackHandleCheck(DoctorGroupEvent deleteGroupEvent) {}

    /**
     * 触发事件的删除处理(删除)
     * @param deleteGroupEvent 删除事件
     */
    protected void triggerEventRollbackHandle(DoctorGroupEvent deleteGroupEvent, Long operatorId, String operatorName) {}

    /**
     * 构建猪群信息(删除)
     * @param deleteGroupEvent 删除事件
     * @param oldGroup 原猪群信息
     * @return 新猪群信息
     */
    protected DoctorGroup buildNewGroupForRollback(DoctorGroupEvent deleteGroupEvent, DoctorGroup oldGroup){return null;}

    /**
     * 构建track(删除)
     * @param deleteGroupEvent 删除事件
     * @param oldGroupTrack 原track
     * @return 新 track
     */
    protected DoctorGroupTrack buildNewTrackForRollback(DoctorGroupEvent deleteGroupEvent, DoctorGroupTrack oldGroupTrack) {return null;}
    /**
     * 更新日记录(删除)
     * @param deleteGroupEvent 删除事件
     */
    protected void updateDailyForDelete(DoctorGroupEvent deleteGroupEvent) {}

    /**
     * 是否需要更新猪群(编辑)
     * @param changeDto 变化记录
     * @return
     */
    private boolean isUpdateGroup(DoctorEventChangeDto changeDto){
        //// TODO: 17/4/13 是否需要更新
        return true;
    }

    /**
     * 是否需要更新猪群(删除)
     * @param eventType 事件类型
     * @return
     */
    private boolean isUpdateGroup(Integer eventType){
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
     * 是否需要更新track(删除)
     * @param eventType 事件类型
     * @return
     */
    private boolean isUpdateTrack(Integer eventType){
        // TODO: 17/4/13 是否需要跟新
        return true;
    }


    /**
     * 创建编辑记录
     * @param oldEvent 原事件
     * @param newEvent 新事件
     */
    private void createModifyLog(DoctorGroupEvent oldEvent, DoctorGroupEvent newEvent) {
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

    /**
     * 更新猪群某天及某天之后的存栏数量
     * @param groupId 猪群id
     * @param sumAt 统计时间
     * @param changeCount 存栏变化量
     */
    protected void updateDailyGroupLiveStock(Long groupId, Date sumAt, Integer changeCount) {
        doctorDailyGroupDao.updateDailyGroupLiveStock(groupId, sumAt, changeCount);
    }

    /**
     * 获取日期下一天
     * @param date 日期
     * @return 下一天
     */
    public static Date getAfterDay(Date date) {
        return new DateTime(date).plusDays(1).toDate();
    }
}
