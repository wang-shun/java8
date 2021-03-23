package io.terminus.doctor.event.handler.group;

import com.google.common.base.MoreObjects;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupTransFarmEventHandler;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.manager.DoctorGroupManager;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Desc: 转场事件处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class DoctorTransFarmGroupEventHandler extends DoctorAbstractGroupEventHandler {

    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorCommonGroupEventHandler doctorCommonGroupEventHandler;
    private final DoctorGroupManager doctorGroupManager;
    private final DoctorBarnDao doctorBarnDao;
    @Autowired
    private DoctorModifyGroupTransFarmEventHandler doctorModifyGroupTransFarmEventHandler;

    @Autowired
    public DoctorTransFarmGroupEventHandler(DoctorGroupTrackDao doctorGroupTrackDao,
                                            DoctorGroupEventDao doctorGroupEventDao,
                                            DoctorCommonGroupEventHandler doctorCommonGroupEventHandler,
                                            DoctorGroupManager doctorGroupManager,
                                            DoctorBarnDao doctorBarnDao) {
        super(doctorGroupTrackDao, doctorGroupEventDao, doctorBarnDao);
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorCommonGroupEventHandler = doctorCommonGroupEventHandler;
        this.doctorGroupManager = doctorGroupManager;
        this.doctorBarnDao = doctorBarnDao;
    }

    @Override
    public <I extends BaseGroupInput> DoctorGroupEvent buildGroupEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.TRANS_FARM.getValue());

        DoctorTransFarmGroupInput transFarm = (DoctorTransFarmGroupInput) input;

        //转入猪舍
        DoctorBarn toBarn = getBarn(transFarm.getToBarnId());

        //1.转换转场事件
        checkBreed(group.getBreedId(), transFarm.getBreedId());
        transFarm.setToBarnType(toBarn.getPigType());

        //2.创建转场事件
        DoctorGroupEvent event = dozerGroupEvent(group, GroupEventType.TRANS_FARM, transFarm);
        event.setQuantity(transFarm.getQuantity());

        event.setWeight(transFarm.getWeight());
        event.setAvgWeight(EventUtil.getAvgWeight(transFarm.getWeight(), transFarm.getQuantity()));
        event.setAvgDayAge(groupTrack.getAvgDayAge());
        event.setTransGroupType(DoctorGroupEvent.TransGroupType.OUT.getValue());   //转场肯定是外转
        event.setOtherBarnId(toBarn.getId());          //目标猪舍id
        event.setOtherBarnType(toBarn.getPigType());   //目标猪舍类型
        event.setExtraMap(transFarm);
        return event;
    }

    @Override
    public DoctorGroupTrack updateTrackOtherInfo(DoctorGroupEvent event, DoctorGroupTrack track) {
        DoctorTransFarmGroupInput doctorTransFarmGroupEvent = JSON_MAPPER.fromJson(event.getExtra(), DoctorTransFarmGroupInput.class);
        if(Arguments.isNull(doctorTransFarmGroupEvent)) {
            log.error("parse doctorTransFarmGroupEvent faild, doctorGroupEvent = {}", event);
            throw new InvalidException("transfarm.group.event.info.broken", event.getId());
        }
        //更新track
        track.setQuantity(EventUtil.minusQuantity(track.getQuantity(), event.getQuantity()));

        //如果公猪数量 lt 0 按 0 计算
        Integer boarQty = EventUtil.minusQuantity(track.getBoarQty(), doctorTransFarmGroupEvent.getBoarQty());
        boarQty = boarQty > track.getQuantity() ? track.getQuantity() : boarQty;
        track.setBoarQty(boarQty < 0 ? 0 : boarQty);
        track.setSowQty(EventUtil.minusQuantity(track.getQuantity(), track.getBoarQty()));
        return track;
    }

    @Override
    protected <I extends BaseGroupInput> void handleEvent(List<DoctorEventInfo> eventInfoList, DoctorGroup group, DoctorGroupTrack groupTrack, I input) {

        input.setEventType(GroupEventType.TRANS_FARM.getValue());

        DoctorTransFarmGroupInput transFarm = (DoctorTransFarmGroupInput) input;
        if (Objects.equals(transFarm.getEventSource(), SourceType.INPUT.getValue())) {
            doctorModifyGroupTransFarmEventHandler.validGroupLiveStock(group.getId(), group.getGroupCode(), DateUtil.toDate(transFarm.getEventAt()), -transFarm.getQuantity());
        }
        //校验能否转群, 数量, 日龄差, 转群总重
        checkCanTransBarn(group.getPigType(), transFarm.getToBarnId());
        checkCanTransGroup(transFarm.getToGroupId(), transFarm.getToBarnId(), transFarm.getIsCreateGroup());
        checkFarrowGroupUnique(transFarm.getIsCreateGroup(), transFarm.getToBarnId());
        checkQuantity(groupTrack.getQuantity(), transFarm.getQuantity());
        checkQuantityEqual(transFarm.getQuantity(), transFarm.getBoarQty(), transFarm.getSowQty());

        //转入猪舍
        DoctorBarn toBarn = getBarn(transFarm.getToBarnId());
        if (!input.isSowEvent()) {
            checkUnweanTrans(group.getPigType(), toBarn.getPigType(), groupTrack, transFarm.getQuantity());
        }

        //1.转换转场事件
        DoctorTransFarmGroupInput transFarmEvent = BeanMapper.map(transFarm, DoctorTransFarmGroupInput.class);
        checkBreed(group.getBreedId(), transFarmEvent.getBreedId());
        transFarmEvent.setToBarnType(toBarn.getPigType());

        //2.创建转场事件
        DoctorGroupEvent event = dozerGroupEvent(group, GroupEventType.TRANS_FARM, transFarm);
        event.setQuantity(transFarm.getQuantity());

        event.setWeight(transFarm.getWeight());
        // 得到均重（四舍五入保留三位小数 陈娟 2018-10-23）
        event.setAvgWeight(new BigDecimal(EventUtil.getAvgWeight(transFarm.getWeight(), transFarm.getQuantity())).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
//        event.setAvgWeight(EventUtil.getAvgWeight(transFarm.getWeight(), transFarm.getQuantity()));
        event.setAvgDayAge(groupTrack.getAvgDayAge());  //重算日龄
        event.setTransGroupType(DoctorGroupEvent.TransGroupType.OUT.getValue());   //转场肯定是外转
        event.setOtherBarnId(toBarn.getId());          //目标猪舍id
        event.setOtherBarnType(toBarn.getPigType());   //目标猪舍类型
        event.setExtraMap(transFarm);
        doctorGroupEventDao.create(event);

        transFarm.setRelGroupEventId(event.getId());

        Integer oldQuantity = groupTrack.getQuantity();

        //3.更新猪群跟踪
        groupTrack.setQuantity(EventUtil.minusQuantity(groupTrack.getQuantity(), transFarm.getQuantity()));

        //如果公猪数量 lt 0 按 0 计算
        groupTrack.setBoarQty(getBoarQty(groupTrack, EventUtil.minusInt(0, transFarm.getBoarQty())));
        groupTrack.setSowQty(getSowQty(groupTrack, EventUtil.minusInt(0, transFarm.getSowQty())));


        if (Objects.equals(transFarm.isSowEvent(), true)) {
            groupTrack.setNest(EventUtil.plusInt(groupTrack.getNest(), -1));
            groupTrack.setLiveQty(EventUtil.plusInt(groupTrack.getLiveQty(), - event.getQuantity()));
            groupTrack.setHealthyQty(groupTrack.getLiveQty() - MoreObjects.firstNonNull(groupTrack.getWeakQty(), 0));
            groupTrack.setUnweanQty(EventUtil.plusInt(groupTrack.getUnweanQty(), -event.getQuantity()));
            groupTrack.setBirthWeight(EventUtil.plusDouble(groupTrack.getBirthWeight(), - event.getAvgWeight() * event.getQuantity()));
        }

        updateGroupTrack(groupTrack, event);
        //设置来源为外场
        transFarm.setSource(PigSource.OUTER.getKey());
        if (Objects.equals(event.getEventSource(), SourceType.INPUT.getValue())) {
            updateDailyForNew(event);

            //5.判断转场数量, 如果 = 猪群数量, 触发关闭猪群事件, 同时生成批次总结
            if (Objects.equals(oldQuantity, transFarm.getQuantity())) {
                doctorCommonGroupEventHandler.autoGroupEventClose(eventInfoList, group, groupTrack, transFarm, event.getEventAt(), transFarm.getFcrFeed());

                // TODO: 17/9/25 并不知道有什么用处 先注释吧
//                Long toGroupEventId = doctorGroupEventDao.findByRelGroupEventIdAndType(event.getId(), GroupEventType.CLOSE.getValue()).getId();
//                DoctorGroupEvent closeEvent = doctorGroupEventDao.findById(toGroupEventId);
//                transFarm.setRelGroupEventId(closeEvent.getId());    //如果发生关闭猪群事件，关联事件id要换下
            }


            //6.判断是否新建群,触发目标群的转入仔猪事件
            if (Objects.equals(transFarm.getIsCreateGroup(), IsOrNot.YES.getValue())) {
                //新建猪群
                Long toGroupId = autoTransFarmEventNew(eventInfoList, group, groupTrack, transFarm, toBarn);
                transFarm.setToGroupId(toGroupId);

                //刷新最新事件id
                doctorGroupEventDao.findLastEventByGroupId(toGroupId);

                //转入猪群
                doctorCommonGroupEventHandler.autoTransEventMoveIn(eventInfoList, group, groupTrack, transFarm);
            } else {
                doctorCommonGroupEventHandler.autoTransEventMoveIn(eventInfoList, group, groupTrack, transFarm);
            }
        }

    }

    /**
     * 系统触发的自动新建猪群事件(转场触发)
     */
    private Long autoTransFarmEventNew(List<DoctorEventInfo> eventInfoList, DoctorGroup fromGroup, DoctorGroupTrack fromGroupTrack, DoctorTransFarmGroupInput transFarm, DoctorBarn toBarn) {
        DoctorNewGroupInput newGroupInput = new DoctorNewGroupInput();
        newGroupInput.setFarmId(transFarm.getToFarmId());
        newGroupInput.setGroupCode(transFarm.getToGroupCode());    //录入猪群号
        newGroupInput.setEventAt(transFarm.getEventAt());          //事件发生日期
        newGroupInput.setBarnId(transFarm.getToBarnId());          //转到的猪舍id
        newGroupInput.setBarnName(transFarm.getToBarnName());
        newGroupInput.setPigType(toBarn.getPigType());    //猪类取猪舍的猪类
        newGroupInput.setSex(fromGroupTrack.getSex());
        newGroupInput.setBreedId(transFarm.getBreedId());           //品种
        newGroupInput.setBreedName(fromGroup.getBreedName());
        newGroupInput.setGeneticId(fromGroup.getGeneticId());
        newGroupInput.setGeneticName(fromGroup.getGeneticName());
        newGroupInput.setSource(PigSource.OUTER.getKey());          //来源:外购
        newGroupInput.setIsAuto(IsOrNot.YES.getValue());
        newGroupInput.setRemark(transFarm.getRemark());
        newGroupInput.setRelGroupEventId(transFarm.getRelGroupEventId());

        DoctorGroup toGroup = BeanMapper.map(newGroupInput, DoctorGroup.class);
        toGroup.setFarmName(transFarm.getToFarmName());
        toGroup.setOrgId(fromGroup.getOrgId());       //转入公司
        toGroup.setOrgName(fromGroup.getOrgName());
        toGroup.setCreatorId(0L);    //创建人id = 0, 标识系统自动创建
        return doctorGroupManager.createNewGroup(eventInfoList, toGroup, newGroupInput);
    }

    private DoctorBarn getBarn(Long barnId) {
        return doctorBarnDao.findById(barnId);
    }


    @Override
    protected void updateDailyForNew(DoctorGroupEvent newGroupEvent) {
        BaseGroupInput input = JSON_MAPPER.fromJson(newGroupEvent.getExtra(), DoctorTransFarmGroupInput.class);
        doctorModifyGroupTransFarmEventHandler.updateDailyOfNew(newGroupEvent, input);
    }
}
