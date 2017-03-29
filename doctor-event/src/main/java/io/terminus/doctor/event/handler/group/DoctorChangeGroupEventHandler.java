package io.terminus.doctor.event.handler.group;

import com.google.common.base.MoreObjects;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.enums.DoctorBasicEnums;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.DoctorChangeGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
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

    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorCommonGroupEventHandler doctorCommonGroupEventHandler;

    @Autowired
    public DoctorChangeGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                         DoctorGroupTrackDao doctorGroupTrackDao,
                                         DoctorGroupEventDao doctorGroupEventDao,
                                         DoctorCommonGroupEventHandler doctorCommonGroupEventHandler,
                                         DoctorBarnDao doctorBarnDao) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, doctorGroupEventDao, doctorBarnDao);
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorCommonGroupEventHandler = doctorCommonGroupEventHandler;
    }

    @Override
    protected <I extends BaseGroupInput> void handleEvent(List<DoctorEventInfo> eventInfoList, DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.CHANGE.getValue());
        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorChangeGroupInput change = (DoctorChangeGroupInput) input;

        checkQuantity(groupTrack.getQuantity(), change.getQuantity());
        checkQuantityEqual(change.getQuantity(), change.getBoarQty(), change.getSowQty());

        //非母猪触发事件
        if (!input.isSowEvent()) {
            checkUnweanTrans(group.getPigType(), null, groupTrack, change.getQuantity());
        }

        if(Objects.equals(group.getPigType(), PigType.NURSERY_PIGLET.getValue())){
            checkSalePrice(change.getChangeTypeId(), change.getPrice(), change.getBaseWeight(), change.getOverPrice());
        }

        //1.转换猪群变动事件
        DoctorChangeGroupEvent changeEvent = BeanMapper.map(change, DoctorChangeGroupEvent.class);

        //2.创建猪群变动事件
        DoctorGroupEvent<DoctorChangeGroupEvent> event = dozerGroupEvent(group, GroupEventType.CHANGE, change);
        event.setQuantity(change.getQuantity());

        event.setWeight(change.getWeight());            //总重
        event.setAvgWeight(EventUtil.getAvgWeight(change.getWeight(), change.getQuantity()));
        event.setChangeTypeId(changeEvent.getChangeTypeId());   //变动类型id

        //销售相关
        setSaleEvent(event, change, group.getPigType());

        event.setExtraMap(changeEvent);
        doctorGroupEventDao.create(event);

        //创建关联关系
        createEventRelation(event);

        change.setRelGroupEventId(event.getId());   //记录关联猪群事件id

        Integer oldQuantity = groupTrack.getQuantity();

        //3.更新猪群跟踪
        groupTrack.setQuantity(EventUtil.minusQuantity(groupTrack.getQuantity(), change.getQuantity()));

        //如果公猪数量 lt 0 按 0 计算
        Integer boarQty = EventUtil.minusQuantity(groupTrack.getBoarQty(), change.getBoarQty());
        boarQty = boarQty > groupTrack.getQuantity() ? groupTrack.getQuantity() : boarQty;
        groupTrack.setBoarQty(boarQty < 0 ? 0 : boarQty);
        groupTrack.setSowQty(EventUtil.minusQuantity(groupTrack.getQuantity(), groupTrack.getBoarQty()));

        //母猪触发的变动，要减掉未断奶数
        if (change.isSowEvent()) {
            if (groupTrack.getUnweanQty() == null || groupTrack.getUnweanQty() <= 0) {
                groupTrack.setUnweanQty(0);
            }
            groupTrack.setUnweanQty(groupTrack.getUnweanQty() - change.getQuantity());
        }
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, event, groupTrack), GroupEventType.CHANGE);

        //5.判断变动数量, 如果 = 猪群数量, 触发关闭猪群事件, 同时生成批次总结
        if (Objects.equals(oldQuantity, change.getQuantity())) {
            doctorCommonGroupEventHandler.autoGroupEventClose(eventInfoList, group, groupTrack, change, event.getEventAt(), change.getFcrFeed());
        }

        //发布统计事件
        //publistGroupAndBarn(event);
    }

    @Override
    public <I extends BaseGroupInput> DoctorGroupEvent buildGroupEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        input.setEventType(GroupEventType.CHANGE.getValue());
        DoctorChangeGroupInput change = (DoctorChangeGroupInput) input;
        //1.转换猪群变动事件
        DoctorChangeGroupEvent changeEvent = BeanMapper.map(change, DoctorChangeGroupEvent.class);

        //2.创建猪群变动事件
        DoctorGroupEvent<DoctorChangeGroupEvent> event = dozerGroupEvent(group, GroupEventType.CHANGE, change);
        event.setQuantity(change.getQuantity());

        event.setWeight(change.getWeight());            //总重
        event.setAvgWeight(EventUtil.getAvgWeight(change.getWeight(), change.getQuantity()));
        event.setChangeTypeId(changeEvent.getChangeTypeId());   //变动类型id

        //销售相关
        setSaleEvent(event, change, group.getPigType());

        event.setExtraMap(changeEvent);
        return event;
    }


    @Override
    public DoctorGroupTrack updateTrackOtherInfo(DoctorGroupEvent event, DoctorGroupTrack track) {
        DoctorChangeGroupEvent changeEvent = JSON_MAPPER.fromJson(event.getExtra(), DoctorChangeGroupEvent.class);
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

    //如果是销售事件, 记录价格与重量
    private void setSaleEvent(DoctorGroupEvent<DoctorChangeGroupEvent> event, DoctorChangeGroupInput change, Integer pigType) {
        event.setPrice(change.getPrice());          //销售单价(分)(基础价)
        event.setBaseWeight(change.getBaseWeight());//基础重量
        event.setOverPrice(change.getOverPrice());  //超出价格(分/kg)
        if (change.getChangeTypeId() == DoctorBasicEnums.SALE.getId()) {

            //保育猪的特殊逻辑, 其他猪类的销售 金额 = 重量 * 单价
            if (Objects.equals(PigType.NURSERY_PIGLET.getValue(), pigType)) {
                //销售总额(分) = 单价 * 数量 + 超出价格 * 超出重量
                event.setAmount((long) (change.getPrice() * change.getQuantity() +
                        MoreObjects.firstNonNull(change.getOverPrice(), 0L) *
                                (change.getWeight() - change.getQuantity() * MoreObjects.firstNonNull(change.getBaseWeight(), 0))));
            } else {
                event.setAmount((long) (change.getPrice() * change.getWeight()));
            }
            change.setAmount(event.getAmount());
        }
    }
}
