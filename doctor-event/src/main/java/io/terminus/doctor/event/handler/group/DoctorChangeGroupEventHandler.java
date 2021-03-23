package io.terminus.doctor.event.handler.group;

import com.google.common.base.MoreObjects;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupChangeEventHandler;
import io.terminus.doctor.event.enums.DoctorBasicEnums;
import io.terminus.doctor.event.enums.GroupEventType;
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
 * Desc: 猪群变动事件处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class DoctorChangeGroupEventHandler extends DoctorAbstractGroupEventHandler {

    @Autowired
    private DoctorModifyGroupChangeEventHandler doctorModifyGroupChangeEventHandler;

    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorCommonGroupEventHandler doctorCommonGroupEventHandler;

    @Autowired
    public DoctorChangeGroupEventHandler(DoctorGroupTrackDao doctorGroupTrackDao,
                                         DoctorGroupEventDao doctorGroupEventDao,
                                         DoctorCommonGroupEventHandler doctorCommonGroupEventHandler,
                                         DoctorBarnDao doctorBarnDao) {
        super(doctorGroupTrackDao, doctorGroupEventDao, doctorBarnDao);
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorCommonGroupEventHandler = doctorCommonGroupEventHandler;
    }

    @Override
    protected <I extends BaseGroupInput> void handleEvent(List<DoctorEventInfo> eventInfoList, DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.CHANGE.getValue());
        DoctorChangeGroupInput change = (DoctorChangeGroupInput) input;

        if (Objects.equals(change.getEventSource(), SourceType.INPUT.getValue())) {
            doctorModifyGroupChangeEventHandler.validGroupLiveStock(group.getId(), group.getGroupCode(), DateUtil.toDate(change.getEventAt()), -change.getQuantity());
        }
        checkQuantity(groupTrack.getQuantity(), change.getQuantity());
        checkQuantityEqual(change.getQuantity(), change.getBoarQty(), change.getSowQty());

        //非母猪触发事件
        if (!input.isSowEvent()) {
            checkUnweanTrans(group.getPigType(), null, groupTrack, change.getQuantity());
        }

        if (change.isSowEvent()) {
            checkQuantity(MoreObjects.firstNonNull(groupTrack.getUnweanQty(), 0), change.getQuantity());
        } else {
            if (Objects.equals(group.getPigType(), PigType.DELIVER_SOW.getValue())) {
                checkWeanQuantity(groupTrack.getQuantity() - MoreObjects.firstNonNull(groupTrack.getUnweanQty(), 0), change.getQuantity());
            } else {
                checkQuantity(groupTrack.getQuantity(), change.getQuantity());
            }
        }

        if(Objects.equals(group.getPigType(), PigType.NURSERY_PIGLET.getValue())){
            checkSalePrice(change.getChangeTypeId(), change.getPrice(), change.getBaseWeight(), change.getOverPrice());
        }

        //1.转换猪群变动事件
        DoctorChangeGroupInput changeEvent = BeanMapper.map(change, DoctorChangeGroupInput.class);

        //2.创建猪群变动事件
        DoctorGroupEvent<DoctorChangeGroupInput> event = dozerGroupEvent(group, GroupEventType.CHANGE, change);
        event.setQuantity(change.getQuantity());
        event.setCustomerId(change.getCustomerId());
        event.setCustomerName(change.getCustomerName());

        event.setWeight(change.getWeight());            //总重
        // 得到均重（四舍五入保留三位小数 陈娟 2018-10-23）
        event.setAvgWeight(new BigDecimal(EventUtil.getAvgWeight(change.getWeight(), change.getQuantity())).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
        //        event.setAvgWeight(EventUtil.getAvgWeight(change.getWeight(), change.getQuantity()));
        event.setAvgDayAge(groupTrack.getAvgDayAge());
        event.setChangeTypeId(changeEvent.getChangeTypeId());   //变动类型id
        event.setSowId(change.getSowId());
        event.setSowCode(change.getSowCode());
        //销售相关
        doctorModifyGroupChangeEventHandler.setSaleEvent(event, change, group.getPigType());

        event.setExtraMap(change);
        doctorGroupEventDao.create(event);

        change.setRelGroupEventId(event.getId());   //记录关联猪群事件id

        Integer oldQuantity = groupTrack.getQuantity();

        //3.更新猪群跟踪
        groupTrack.setQuantity(EventUtil.minusQuantity(groupTrack.getQuantity(), change.getQuantity()));

        //如果公猪数量 lt 0 按 0 计算
        groupTrack.setBoarQty(getBoarQty(groupTrack, EventUtil.minusInt(0, change.getBoarQty())));
        groupTrack.setSowQty(getSowQty(groupTrack, EventUtil.minusInt(0, change.getSowQty())));

        //母猪触发的变动，要减掉未断奶数
        if (change.isSowEvent()) {
            if (groupTrack.getUnweanQty() == null || groupTrack.getUnweanQty() <= 0) {
                groupTrack.setUnweanQty(0);
            }
            groupTrack.setUnweanQty(groupTrack.getUnweanQty() - change.getQuantity());
        }
        updateGroupTrack(groupTrack, event);
        if (Objects.equals(event.getEventSource(), SourceType.INPUT.getValue())) {

            updateDailyForNew(event);

            //5.判断变动数量, 如果 = 猪群数量, 触发关闭猪群事件, 同时生成批次总结
            if (Objects.equals(oldQuantity, change.getQuantity())) {
                doctorCommonGroupEventHandler.autoGroupEventClose(eventInfoList, group, groupTrack, change, event.getEventAt(), change.getFcrFeed());
            }
        }

        //发布统计事件
        //publistGroupAndBarn(event);
    }

    @Override
    public <I extends BaseGroupInput> DoctorGroupEvent buildGroupEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.CHANGE.getValue());
        DoctorChangeGroupInput change = (DoctorChangeGroupInput) input;
        //2.创建猪群变动事件
        DoctorGroupEvent<DoctorChangeGroupInput> event = dozerGroupEvent(group, GroupEventType.CHANGE, change);
        event.setQuantity(change.getQuantity());

        event.setSowId(change.getSowId());
        event.setSowCode(change.getSowCode());
        event.setWeight(change.getWeight());            //总重
        event.setAvgWeight(EventUtil.getAvgWeight(change.getWeight(), change.getQuantity()));
        event.setAvgDayAge(groupTrack.getAvgDayAge());
        event.setChangeTypeId(change.getChangeTypeId());   //变动类型id

        //销售相关
        doctorModifyGroupChangeEventHandler.setSaleEvent(event, change, group.getPigType());

        event.setExtraMap(change);
        return event;
    }


    @Override
    public DoctorGroupTrack updateTrackOtherInfo(DoctorGroupEvent event, DoctorGroupTrack track) {
        DoctorChangeGroupInput changeEvent = JSON_MAPPER.fromJson(event.getExtra(), DoctorChangeGroupInput.class);
        track.setQuantity(EventUtil.minusQuantity(track.getQuantity(), event.getQuantity()));

        //计算公猪、母猪数量
        if(!Arguments.isNull(track.getBoarQty()) || !Arguments.isNull(changeEvent.getBoarQty())){
            track.setBoarQty(EventUtil.minusInt(track.getBoarQty(), changeEvent.getBoarQty()));
        }
        if(!Arguments.isNull(track.getSowQty()) || !Arguments.isNull(changeEvent.getSowQty())){
            track.setSowQty(EventUtil.minusInt(track.getSowQty(), changeEvent.getSowQty()));
        }

        //母猪触发的变动，要减掉未断奶数
        if (Objects.nonNull(event.getRelPigEventId())) {
            if (track.getUnweanQty() == null || track.getUnweanQty() <= 0) {
                track.setUnweanQty(0);
            }
            track.setUnweanQty(track.getUnweanQty() - event.getQuantity());
        }
        return track;
    }

    @Override
    protected void updateDailyForNew(DoctorGroupEvent newGroupEvent) {
        BaseGroupInput input = JSON_MAPPER.fromJson(newGroupEvent.getExtra(), DoctorChangeGroupInput.class);
        doctorModifyGroupChangeEventHandler.updateDailyOfNew(newGroupEvent, input);
    }

    //校验金额不能为空, 基础重量不能为空
    private static void checkSalePrice(Long changeTypeId, Long price, Integer baseWeight, Long overPrice) {
        if (changeTypeId == DoctorBasicEnums.SALE.getId()) {
            if ((price == null || overPrice == null)) {
                throw new InvalidException("sale.money.not.null");
            }
            if (baseWeight == null) {
                throw new InvalidException("sale.weight.not.null");
            }
        }
    }


}
