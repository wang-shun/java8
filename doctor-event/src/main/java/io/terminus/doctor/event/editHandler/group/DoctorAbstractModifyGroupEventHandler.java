package io.terminus.doctor.event.editHandler.group;

import com.google.common.collect.Lists;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorDailyGroupDao;
import io.terminus.doctor.event.dao.DoctorEventModifyLogDao;
import io.terminus.doctor.event.dao.DoctorGroupBatchSummaryDao;
import io.terminus.doctor.event.dao.DoctorGroupDailyDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.editHandler.DoctorModifyGroupEventHandler;
import io.terminus.doctor.event.enums.EventStatus;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.manager.DoctorDailyReportManager;
import io.terminus.doctor.event.manager.DoctorDailyReportV2Manager;
import io.terminus.doctor.event.helper.DoctorConcurrentControl;
import io.terminus.doctor.event.manager.DoctorDailyReportManager;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorDailyGroup;
import io.terminus.doctor.event.model.DoctorEventModifyLog;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.enums.SourceType.UN_MODIFY;
import static io.terminus.doctor.common.utils.Checks.expectNotNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

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
    protected DoctorGroupDailyDao doctorGroupDailyDao;
    @Autowired
    private DoctorEventModifyLogDao doctorEventModifyLogDao;
    @Autowired
    protected DoctorDailyReportV2Manager doctorDailyReportManager;
    @Autowired
    protected DoctorBarnDao doctorBarnDao;
    @Autowired
    protected DoctorDailyGroupDao oldDailyGroupDao;
    @Autowired
    protected DoctorDailyReportManager oldDailyReportManager;

    @Autowired
    protected DoctorConcurrentControl doctorConcurrentControl;

    @Autowired
    protected DoctorGroupBatchSummaryDao doctorGroupBatchSummaryDao;

    protected final JsonMapperUtil JSON_MAPPER = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER;

    protected final ToJsonMapper TO_JSON_MAPPER = ToJsonMapper.JSON_NON_DEFAULT_MAPPER;

    @Override
    public final Boolean canModify(DoctorGroupEvent oldGroupEvent) {
        return Objects.equals(oldGroupEvent.getIsAuto(), IsOrNot.NO.getValue())
                && !UN_MODIFY.contains(oldGroupEvent.getEventSource());

    }

    @Override
    public void modifyHandle(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        log.info("modify group event handler starting, oldGroupEvent:{}", oldGroupEvent);
        log.info("input:{}", input);
        String key = "group" + oldGroupEvent.getGroupId().toString();
        expectTrue(doctorConcurrentControl.setKey(key), "event.concurrent.error", oldGroupEvent.getGroupCode());

        //1.校验
        modifyHandleCheck(oldGroupEvent, input);

        //2.构建变化记录
        DoctorEventChangeDto changeDto = buildEventChange(oldGroupEvent, input);

        //3.更新事件
        DoctorGroupEvent newEvent = buildNewEvent(oldGroupEvent, input);
        doctorGroupEventDao.updateIncludeNull(newEvent);

        //4.创建事件完成后创建编辑记录
        createModifyLog(oldGroupEvent, newEvent);

        //5.更新猪群
        if (isUpdateGroup(changeDto)) {
            DoctorGroup oldGroup = doctorGroupDao.findById(oldGroupEvent.getGroupId());
            DoctorGroup newGroup = buildNewGroup(oldGroup, input);
            doctorGroupDao.update(newGroup);
        }

        //6.更新track
        if (isUpdateTrack(changeDto)) {
            DoctorGroupTrack oldTrack = doctorGroupTrackDao.findByGroupId(oldGroupEvent.getGroupId());
            DoctorGroupTrack newTrack = buildNewTrack(oldTrack, changeDto);
            doctorGroupTrackDao.update(newTrack);

            //自动关闭或开启猪群
            autoCloseOrOpen(newTrack);
        }

        //7.更新每日数据记录
        updateDailyForModify(oldGroupEvent, input, changeDto);

        //8.调用触发事件的编辑
        triggerEventModifyHandle(newEvent);

        log.info("modify group event handler ending");
    }
    
    @Override
    public Boolean canRollback(DoctorGroupEvent deleteGroupEvent) {
        return Objects.equals(deleteGroupEvent.getIsAuto(), IsOrNot.NO.getValue())
                && rollbackHandleCheck(deleteGroupEvent)
                && !UN_MODIFY.contains(deleteGroupEvent.getEventSource());
    }
    
    @Override
    public void rollbackHandle(DoctorGroupEvent deleteGroupEvent, Long operatorId, String operatorName) {

        log.info("rollback handle starting, deleteGroupEvent:{}", deleteGroupEvent);
        String key = "group" + deleteGroupEvent.getGroupId().toString();
        expectTrue(doctorConcurrentControl.setKey(key), "event.concurrent.error", deleteGroupEvent.getGroupCode());

        //2.删除触发事件
        triggerEventRollbackHandle(deleteGroupEvent, operatorId, operatorName);

        //3.删除事件
        doctorGroupEventDao.delete(deleteGroupEvent.getId());

        //4.删除记录
        createModifyLog(deleteGroupEvent);

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

                //自动关闭或开启猪群
                autoCloseOrOpen(newTrack);
            }
        }

        //7.更新报表
        updateDailyForDelete(deleteGroupEvent);

        log.info("rollback handle ending");
    }

    /**
     * 编辑校验的具体事件
     * @param oldGroupEvent 原事件
     * @param input 输入
     */
    protected void modifyHandleCheck(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        if (!Objects.equals(oldGroupEvent.getType(), GroupEventType.NEW.getValue())) {
            DoctorGroupEvent newCreateEvent = doctorGroupEventDao.findNewGroupByGroupId(oldGroupEvent.getGroupId());
            DoctorGroupEvent closeEvent = doctorGroupEventDao.findCloseGroupByGroupId(oldGroupEvent.getGroupId());
            validEventAt(DateUtil.toDate(input.getEventAt()), notNull(newCreateEvent) ? newCreateEvent.getEventAt() : null
                    , notNull(closeEvent) ? closeEvent.getEventAt() : null);
        }
    }

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
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
    public DoctorGroup buildNewGroup(DoctorGroup oldGroup, BaseGroupInput input) {
        return oldGroup;
    }

    @Override
    public DoctorGroupTrack buildNewTrack(DoctorGroupTrack oldGroupTrack, DoctorEventChangeDto changeDto) {
        return oldGroupTrack;
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
     * 编辑后自动关闭开启猪群
     * @param groupTrack 猪群track
     */
    protected void autoCloseOrOpen(DoctorGroupTrack groupTrack) {
        DoctorGroupEvent closeEvent = doctorGroupEventDao.findCloseGroupByGroupId(groupTrack.getGroupId());
        //1.数量不为0,却已关闭
        if (!Objects.equals(groupTrack.getQuantity(), 0) && notNull(closeEvent)) {
            DoctorBarn doctorBarn = doctorBarnDao.findById(closeEvent.getBarnId());
            expectTrue(Objects.equals(doctorBarn.getStatus(), DoctorBarn.Status.USING.getValue())
                    , "barn.is.not.used", doctorBarn.getName());

            DoctorGroup group = doctorGroupDao.findById(groupTrack.getGroupId());
            if (Objects.equals(group.getPigType(), PigType.DELIVER_SOW.getValue())) {
                List<DoctorGroup> groupList = doctorGroupDao.findByCurrentBarnId(group.getCurrentBarnId());
                expectTrue(groupList.isEmpty(), "lead.to.deliver.two.group", group.getId());
            }
            //(1).删除关闭事件
            doctorGroupEventDao.delete(closeEvent.getId());
            createModifyLog(closeEvent);

            //(2).更新猪群状态
            group.setStatus(DoctorGroup.Status.CREATED.getValue());
            doctorGroupDao.update(group);

            groupTrack.setCloseAt(DateUtil.toDate("1970-01-01"));
            doctorGroupTrackDao.update(groupTrack);
            return;
        }

        // TODO: 17/7/5 导致数量为零时,暂不关闭
//        //2.数量为零,未关闭
//        if (Objects.equals(groupTrack.getQuantity(), 0) && isNull(closeEvent)){
//            DoctorGroup group = doctorGroupDao.findById(groupTrack.getGroupId());
//
//            //(1).生成关闭事件
//            DoctorCloseGroupInput closeGroupInput = new DoctorCloseGroupInput();
//            closeGroupInput.setEventAt(DateUtil.toDateString(new Date()));
//            closeGroupInput.setIsAuto(IsOrNot.NO.getValue());
//            DoctorGroupEvent closeEvent1 = dozerGroupEvent(group, GroupEventType.CLOSE, closeGroupInput);
//            doctorGroupEventDao.create(closeEvent1);
//
//            //(2).更新猪群状态
//            group.setStatus(DoctorGroup.Status.CLOSED.getValue());
//            group.setCloseAt(new Date());
//            doctorGroupDao.update(group);
//        }
    }

    /**
     * 删除事件校验的具体实现(删除)
     * @param deleteGroupEvent 删除事件
     */
    public Boolean rollbackHandleCheck(DoctorGroupEvent deleteGroupEvent) {
        return true;
    }

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
    protected DoctorGroup buildNewGroupForRollback(DoctorGroupEvent deleteGroupEvent, DoctorGroup oldGroup){return oldGroup;}

    /**
     * 构建track(删除)
     * @param deleteGroupEvent 删除事件
     * @param oldGroupTrack 原track
     * @return 新 track
     */
    protected DoctorGroupTrack buildNewTrackForRollback(DoctorGroupEvent deleteGroupEvent, DoctorGroupTrack oldGroupTrack) {return oldGroupTrack;}
    /**
     * 更新日记录(删除)
     * @param deleteGroupEvent 删除事件
     */
    protected void updateDailyForDelete(DoctorGroupEvent deleteGroupEvent) {}

    /**
     * 删除事件更新日记录
     * @param oldGroupEvent 被删除事件
     */
    public void updateDailyOfDelete(DoctorGroupEvent oldGroupEvent) {}

    /**
     * 新建事件更新日记录
     * @param newGroupEvent 新建事件
     * @param input 新输入
     */
    public void updateDailyOfNew(DoctorGroupEvent newGroupEvent, BaseGroupInput input) {}

    /**
     * 构建猪群日记录
     * @param oldDailyGroup 原记录
     * @param changeDto 变化量
     * @return 新猪群记录
     */
    protected DoctorGroupDaily buildDailyGroup(DoctorGroupDaily oldDailyGroup, DoctorEventChangeDto changeDto) {
        return expectNotNull(oldDailyGroup, "daily.group.not.null");
    }

    /**
     * 是否需要更新猪群(编辑)
     * @param changeDto 变化记录
     * @return
     */
    private boolean isUpdateGroup(DoctorEventChangeDto changeDto) {
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
                .type(DoctorEventModifyRequest.TYPE.GROUP.getValue())
                .build();
        doctorEventModifyLogDao.create(modifyLog);
    }

    /**
     * 创建删除记录
     * @param deleteEvent 删除事件
     */
    private void createModifyLog(DoctorGroupEvent deleteEvent) {
        DoctorEventModifyLog modifyLog = DoctorEventModifyLog.builder()
                .businessId(deleteEvent.getGroupId())
                .businessCode(deleteEvent.getGroupCode())
                .farmId(deleteEvent.getFarmId())
                .deleteEvent(ToJsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(deleteEvent))
                .type(DoctorEventModifyRequest.TYPE.GROUP.getValue())
                .build();
        doctorEventModifyLogDao.create(modifyLog);
    }

    /**
     * 更新猪群某天及某天之后的存栏数量
     * @param farmId 猪场id
     * @param sumAt 统计时间
     * @param changeCount 存栏变化量
     */
    protected void updateDailyGroupLiveStock(Long farmId, Integer pigType, Date sumAt, Integer changeCount) {

        List<DoctorGroupDaily> groupDailyList = doctorGroupDailyDao.queryAfterSumAt(farmId, pigType, sumAt);
        groupDailyList.forEach(groupDaily -> {
            groupDaily.setStart(EventUtil.plusInt(groupDaily.getStart(), changeCount));
            groupDaily.setEnd(EventUtil.plusInt(groupDaily.getEnd(), changeCount));
            doctorGroupDailyDao.update(groupDaily);
        });
    }


    /**
     * 更新猪群某天及某天之后的存栏数量
     * @param groupId 猪群id
     * @param sumAt 统计时间
     * @param changeCount 存栏变化量
     */
    protected void oldUpdateDailyGroupLiveStock(Long groupId, Date sumAt, Integer changeCount) {
        oldDailyGroupDao.updateDailyGroupLiveStock(groupId, sumAt, changeCount);
    }

    /**
     * 校验猪群之后存栏数量(不小于零)
     * @param groupId 猪群id
     * @param sumAt 统计时间(包括)
     * @param changeCount 变化数量
     */
    public boolean validGroupLiveStockForDelete(Long groupId, Date sumAt, Integer changeCount) {
        List<Integer> includeTypes = Lists.newArrayList(GroupEventType.CHANGE.getValue(), GroupEventType.MOVE_IN.getValue(),
                GroupEventType.TRANS_FARM.getValue(), GroupEventType.TRANS_GROUP.getValue());
        List<DoctorGroupEvent> groupEventList = doctorGroupEventDao.findEventIncludeTypesForDesc(groupId, includeTypes, DateUtil.toDateString(sumAt));
        DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(groupId);
        int quantity = EventUtil.plusInt(groupTrack.getQuantity(), changeCount);
        for (DoctorGroupEvent groupEvent: groupEventList) {
            if (quantity < 0) {
                return false;
            }

            if (Objects.equals(groupEvent.getType(), GroupEventType.MOVE_IN.getValue())) {
                quantity = EventUtil.minusInt(quantity, groupEvent.getQuantity());
            } else {
                quantity = EventUtil.plusInt(quantity, groupEvent.getQuantity());
            }
        }

        oldValidGroupLiveStockForDelete(groupId, sumAt, changeCount);

        return true;
    }

    public void validGroupLiveStock(Long groupId, String groupCode, Date sumAt, Integer changeCount) {
        validGroupLiveStock(groupId, groupCode, sumAt, sumAt, null, null, changeCount);
    }

    /**
     * 猪群存栏校验
     * @param groupId 猪群id
     * @param oldEventAt 原时间
     * @param newEventAt 新时间
     * @param oldQuantity 原数量
     * @param newQuantity 新数量
     */
    protected void validGroupLiveStock(Long groupId, String groupCode, Date oldEventAt, Date newEventAt, Integer oldQuantity,
                                       Integer newQuantity,  Integer changeCount){
        oldEventAt = new DateTime(oldEventAt).withTimeAtStartOfDay().toDate();
        newEventAt = new DateTime(newEventAt).withTimeAtStartOfDay().toDate();
        Date sumAt = oldEventAt.before(newEventAt) ? oldEventAt : newEventAt;
        List<Integer> includeTypes = Lists.newArrayList(GroupEventType.CHANGE.getValue(), GroupEventType.MOVE_IN.getValue(),
                GroupEventType.TRANS_FARM.getValue(), GroupEventType.TRANS_GROUP.getValue());
        List<DoctorGroupEvent> groupEventList = doctorGroupEventDao.findEventIncludeTypesForDesc(groupId, includeTypes, DateUtil.toDateString(sumAt));
        DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(groupId);
        int quantity = oldEventAt.equals(newEventAt)
                ? EventUtil.plusInt(groupTrack.getQuantity(), changeCount)
                : groupTrack.getQuantity() + newQuantity - oldQuantity;

        for (DoctorGroupEvent groupEvent : groupEventList) {
            if (quantity < 0) {
                throw new InvalidException("group.live.stock.lower.zero", groupCode, DateUtil.toDateString(groupEvent.getEventAt()));
            }

            if (Objects.equals(groupEvent.getType(), GroupEventType.MOVE_IN.getValue())) {
                quantity = EventUtil.minusInt(quantity, groupEvent.getQuantity());
            } else {
                quantity = EventUtil.plusInt(quantity, groupEvent.getQuantity());
            }
        }

        oldValidGroupLiveStock(groupId, groupCode, oldEventAt, newEventAt, oldQuantity, newQuantity, changeCount);

    }


    /**
     * 校验猪群之后存栏数量(不小于零)
     * @param groupId 猪群id
     * @param sumAt 统计时间(包括)
     * @param changeCount 变化数量
     */
    public boolean oldValidGroupLiveStockForDelete(Long groupId, Date sumAt, Integer changeCount) {
        List<DoctorDailyGroup> dailyGroupList = oldDailyGroupDao.findAfterSumAt(groupId, DateUtil.toDateString(sumAt));
        for(DoctorDailyGroup dailyGroup : dailyGroupList){
            if (EventUtil.plusInt(dailyGroup.getEnd(), changeCount) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 猪群存栏校验
     * @param groupId 猪群id
     * @param oldEventAt 原时间
     * @param newEventAt 新时间
     * @param oldQuantity 原数量
     * @param newQuantity 新数量
     */
    protected void oldValidGroupLiveStock(Long groupId, String groupCode, Date oldEventAt, Date newEventAt, Integer oldQuantity, Integer newQuantity,  Integer changeCount){
        Date sumAt = oldEventAt.before(newEventAt) ? oldEventAt : newEventAt;
        List<DoctorDailyGroup> dailyGroupList = oldDailyGroupDao.findAfterSumAt(groupId, DateUtil.toDateString(sumAt));

        //如果新日期的猪群记录不存在则初始化一个进行校验
        DoctorDailyGroup doctorDailyGroup = oldDailyGroupDao.findByGroupIdAndSumAt(groupId, newEventAt);
        if (isNull(doctorDailyGroup)) {
            dailyGroupList.add(oldDailyReportManager.findByGroupIdAndSumAt(groupId, newEventAt));
        }

        if (Objects.equals(oldEventAt, newEventAt)) {
            dailyGroupList.stream()
                    .filter(dailyGroup -> !oldEventAt.after(dailyGroup.getSumAt()))
                    .forEach(dailyGroup -> dailyGroup.setEnd(EventUtil.plusInt(dailyGroup.getEnd(), changeCount)));
        } else {
            dailyGroupList.stream()
                    .filter(dailyGroup -> !oldEventAt.after(dailyGroup.getSumAt()))
                    .forEach(dailyGroup -> dailyGroup.setEnd(EventUtil.plusInt(dailyGroup.getEnd(), oldQuantity)));
            dailyGroupList.stream()
                    .filter(dailyGroup -> !newEventAt.after(dailyGroup.getSumAt()))
                    .forEach(dailyGroup -> dailyGroup.setEnd(EventUtil.plusInt(dailyGroup.getEnd(), newQuantity)));
        }
        for (DoctorDailyGroup dailyGroup : dailyGroupList) {
            expectTrue(notNull(dailyGroup.getEnd()) && dailyGroup.getEnd() >= 0,
                    "group.live.stock.lower.zero", groupCode, DateUtil.toDateString(dailyGroup.getSumAt()));
        }
    }

    /**
     * 获取日期下一天
     * @param date 日期
     * @return 下一天
     */
    public static Date getAfterDay(Date date) {
        return new DateTime(date).plusDays(1).toDate();
    }

    /**
     * 事件时间校验, 不小于下限时间, 不大与上限时间
     * @param eventAt 事件时间
     * @param downEventAt 下限时间
     * @param upEventAt 上限时间
     */
    public static void validEventAt(Date eventAt, Date downEventAt, Date upEventAt) {
        if ((notNull(downEventAt)
                && Dates.startOfDay(eventAt).before(Dates.startOfDay(downEventAt)))
                || (notNull(upEventAt) && Dates.startOfDay(eventAt).after(Dates.startOfDay(upEventAt)))) {
            throw new InvalidException("event.at.error", DateUtil.toDateString(downEventAt), DateUtil.toDateString(upEventAt));
        }
    }

    /**
     * 猪群触发关闭事件
     * @param groupEvent 原事件
     * @return 关闭输入
     */
    protected BaseGroupInput buildGroupCloseInput(DoctorGroupEvent groupEvent) {
        DoctorCloseGroupInput closeInput = new DoctorCloseGroupInput();
        closeInput.setIsAuto(IsOrNot.YES.getValue());
        closeInput.setEventAt(DateUtil.toDateString(groupEvent.getEventAt()));
        closeInput.setRelGroupEventId(groupEvent.getId());
        return closeInput;
    }

    protected DoctorGroupEvent dozerGroupEvent(DoctorGroup group, GroupEventType eventType, BaseGroupInput baseInput) {
        DoctorGroupEvent event = new DoctorGroupEvent();
        event.setEventAt(DateUtil.toDate(baseInput.getEventAt()));
        event.setOrgId(group.getOrgId());       //公司信息
        event.setOrgName(group.getOrgName());
        event.setFarmId(group.getFarmId());     //猪场信息
        event.setFarmName(group.getFarmName());
        event.setGroupId(group.getId());        //猪群信息
        event.setGroupCode(group.getGroupCode());
        event.setType(eventType.getValue());    //事件类型
        event.setName(eventType.getDesc());
        event.setBarnId(group.getCurrentBarnId());      //事件发生猪舍
        event.setBarnName(group.getCurrentBarnName());
        event.setPigType(group.getPigType());           //猪类
        event.setIsAuto(baseInput.getIsAuto());
        event.setCreatorId(baseInput.getCreatorId());   //创建人
        event.setCreatorName(baseInput.getCreatorName());
        event.setOperatorId(baseInput.getCreatorId());
        event.setOperatorName(baseInput.getCreatorName());
        event.setDesc(baseInput.generateEventDesc());
        event.setRemark(baseInput.getRemark());
        event.setRelGroupEventId(baseInput.getRelGroupEventId());
        event.setRelPigEventId(baseInput.getRelPigEventId());
        event.setStatus(EventStatus.VALID.getValue());
        event.setEventSource(SourceType.INPUT.getValue());
        return event;
    }
}
