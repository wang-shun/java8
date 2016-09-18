package io.terminus.doctor.event.handler.group;

import com.google.common.base.MoreObjects;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.event.constants.DoctorBasicEnums;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorChangeGroupEvent;
import io.terminus.doctor.event.dto.event.group.edit.BaseGroupEdit;
import io.terminus.doctor.event.dto.event.group.edit.DoctorChangeGroupEdit;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private final DoctorGroupTrackDao doctorGroupTrackDao;

    @Autowired
    public DoctorChangeGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                         DoctorGroupTrackDao doctorGroupTrackDao,
                                         CoreEventDispatcher coreEventDispatcher,
                                         DoctorGroupEventDao doctorGroupEventDao,
                                         DoctorCommonGroupEventHandler doctorCommonGroupEventHandler,
                                         DoctorBarnReadService doctorBarnReadService) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, coreEventDispatcher, doctorGroupEventDao, doctorBarnReadService);
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorCommonGroupEventHandler = doctorCommonGroupEventHandler;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
    }

    @Override
    protected <I extends BaseGroupInput> void handleEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorChangeGroupInput change = (DoctorChangeGroupInput) input;

        checkQuantity(groupTrack.getQuantity(), change.getQuantity());
        checkQuantityEqual(change.getQuantity(), change.getBoarQty(), change.getSowQty());
        checkSalePrice(change.getChangeTypeId(), change.getPrice(), change.getBaseWeight(), change.getOverPrice());

        //1.转换猪群变动事件
        DoctorChangeGroupEvent changeEvent = BeanMapper.map(change, DoctorChangeGroupEvent.class);

        //2.创建猪群变动事件
        DoctorGroupEvent<DoctorChangeGroupEvent> event = dozerGroupEvent(group, GroupEventType.CHANGE, change);
        event.setQuantity(change.getQuantity());
        event.setAvgDayAge(groupTrack.getAvgDayAge());  //变动的日龄不需要录入, 直接取猪群的日龄
        event.setWeight(change.getWeight());            //总重
        event.setAvgWeight(EventUtil.getAvgWeight(change.getWeight(), change.getQuantity()));
        event.setChangeTypeId(changeEvent.getChangeTypeId());   //变动类型id

        //销售相关
        setSaleEvent(event, change);

        event.setExtraMap(changeEvent);
        doctorGroupEventDao.create(event);

        Integer oldQuantity = groupTrack.getQuantity();

        //3.更新猪群跟踪
        groupTrack.setQuantity(EventUtil.minusQuantity(groupTrack.getQuantity(), change.getQuantity()));

        //如果公猪数量 lt 0 按 0 计算
        Integer boarQty = EventUtil.minusQuantity(groupTrack.getBoarQty(), change.getBoarQty());
        boarQty = boarQty > groupTrack.getQuantity() ? groupTrack.getQuantity() : boarQty;
        groupTrack.setBoarQty(boarQty < 0 ? 0 : boarQty);
        groupTrack.setSowQty(EventUtil.minusQuantity(groupTrack.getQuantity(), groupTrack.getBoarQty()));
        groupTrack.setSaleQty(Objects.equals(DoctorBasicEnums.SALE.getId(), change.getChangeTypeId()) ? change.getQuantity() : 0);  //直接判断是否是销售
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, event, groupTrack), GroupEventType.CHANGE);

        //5.判断变动数量, 如果 = 猪群数量, 触发关闭猪群事件, 同时生成批次总结
        if (Objects.equals(oldQuantity, change.getQuantity())) {
            doctorCommonGroupEventHandler.createGroupBatchSummaryWhenClosed(group, groupTrack, event.getEventAt());
            doctorCommonGroupEventHandler.autoGroupEventClose(group, groupTrack, change);
        }

        //发布统计事件
        publistGroupAndBarn(group.getOrgId(), group.getFarmId(), group.getId(), group.getCurrentBarnId(), event.getId());
    }

    @Override
    protected <E extends BaseGroupEdit> void editEvent(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorGroupEvent event, E edit) {
        DoctorChangeGroupEdit changeEdit = (DoctorChangeGroupEdit) edit;
        checkQuantityEqual(event.getQuantity(), changeEdit.getBoarQty(), changeEdit.getSowQty());

        DoctorChangeGroupEvent changeEvent = JSON_MAPPER.fromJson(event.getExtra(), DoctorChangeGroupEvent.class);

        //更新track(如果相关字段没有变动, 就不改了)
        if (!Objects.equals(event.getWeight(), changeEdit.getWeight())) {
            groupTrack.setWeight(groupTrack.getWeight() + event.getWeight() - changeEdit.getWeight());
            groupTrack.setAvgWeight(EventUtil.getAvgWeight(groupTrack.getWeight(), groupTrack.getQuantity()));
        }
        if (changeEdit.getAmount() != null) {
            groupTrack.setAmount(groupTrack.getAmount() + MoreObjects.firstNonNull(changeEvent.getAmount(), 0L) - changeEdit.getAmount());
            groupTrack.setPrice(EventUtil.getPrice(groupTrack.getAmount(), groupTrack.getQuantity()));
        }
        if (!Objects.equals(changeEdit.getBoarQty(), changeEvent.getBoarQty())) {
            groupTrack.setBoarQty(groupTrack.getBoarQty() + changeEvent.getBoarQty() - changeEdit.getBoarQty());
            groupTrack.setSowQty(groupTrack.getSowQty() + changeEvent.getSowQty() - changeEdit.getSowQty());
        }
        doctorGroupTrackDao.update(groupTrack);

        //更新事件字段
        changeEvent.setChangeReasonId(changeEdit.getChangeReasonId());
        changeEvent.setChangeReasonName(changeEdit.getChangeReasonName());
        changeEvent.setBreedId(changeEdit.getBreedId());
        changeEvent.setBreedName(changeEdit.getBreedName());
        changeEvent.setBoarQty(changeEdit.getBoarQty());
        changeEvent.setSowQty(changeEdit.getSowQty());
        changeEvent.setPrice(changeEdit.getPrice());
        changeEvent.setAmount(changeEdit.getAmount());
        changeEvent.setCustomerId(changeEdit.getCustomerId());
        changeEvent.setCustomerName(changeEdit.getCustomerName());
        event.setExtraMap(changeEvent);

        event.setWeight(changeEdit.getWeight());
        event.setAvgWeight(EventUtil.getAvgWeight(event.getWeight(), event.getQuantity()));
        editGroupEvent(event, edit);

        //更新猪群镜像
        editGroupSnapShot(group, groupTrack, event);
    }

    //校验金额不能为空, 基础重量不能为空
    private static void checkSalePrice(Long changeTypeId, Long price, Integer baseWeight, Long overPrice) {
        if (changeTypeId == DoctorBasicEnums.SALE.getId()) {
            if ((price == null || overPrice == null)) {
                throw new ServiceException("money.not.null");
            }
            if (baseWeight == null) {
                throw new ServiceException("weight.not.null");
            }
        }
    }

    //如果是销售事件, 记录价格与重量
    private void setSaleEvent(DoctorGroupEvent<DoctorChangeGroupEvent> event, DoctorChangeGroupInput change) {
        event.setPrice(change.getPrice());          //销售单价(分)(基础价)
        event.setBaseWeight(change.getBaseWeight());//基础重量
        event.setOverPrice(change.getOverPrice());  //超出价格(分/kg)
        if (change.getChangeTypeId() == DoctorBasicEnums.SALE.getId()) {
            //销售总额(分) = 单价 * 数量 + 超出价格 * 超出重量
            event.setAmount((long) (change.getPrice() * change.getQuantity() +
                    MoreObjects.firstNonNull(change.getOverPrice(), 0L) * (change.getWeight() - MoreObjects.firstNonNull(change.getBaseWeight(), 0))));
        }
    }
}
