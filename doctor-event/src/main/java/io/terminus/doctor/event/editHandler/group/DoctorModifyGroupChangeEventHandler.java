package io.terminus.doctor.event.editHandler.group;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.enums.DoctorBasicEnums;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroupDaily;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.event.editHandler.pig.DoctorModifyPigRemoveEventHandler.*;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 14:01 2017/4/15
 */
@Component
public class DoctorModifyGroupChangeEventHandler extends DoctorAbstractModifyGroupEventHandler {

    @Autowired
    private DoctorModifyGroupCloseEventHandler modifyGroupCloseEventHandler;

    @Override
    protected void modifyHandleCheck(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        super.modifyHandleCheck(oldGroupEvent, input);
        DoctorChangeGroupInput newInput = (DoctorChangeGroupInput) input;
        validGroupLiveStock(oldGroupEvent.getGroupId(), oldGroupEvent.getGroupCode(),
                oldGroupEvent.getEventAt(), DateUtil.toDate(newInput.getEventAt()),
                oldGroupEvent.getQuantity(), -newInput.getQuantity(),
                EventUtil.minusInt(oldGroupEvent.getQuantity(), newInput.getQuantity()));
    }

    @Override
    public DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorChangeGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorChangeGroupInput.class);
        DoctorChangeGroupInput newInput = (DoctorChangeGroupInput) input;
        return DoctorEventChangeDto.builder()
                .farmId(oldGroupEvent.getFarmId())
                .pigType(oldGroupEvent.getPigType())
                .businessId(oldGroupEvent.getGroupId())
                .newEventAt(DateUtil.toDate(newInput.getEventAt()))
                .oldEventAt(DateUtil.toDate(oldInput.getEventAt()))
                .oldChangeTypeId(oldInput.getChangeTypeId())
                .newChangeTypeId(newInput.getChangeTypeId())
                .quantityChange(EventUtil.minusInt(newInput.getQuantity(), oldInput.getQuantity()))
                .weightChange(EventUtil.minusDouble(newInput.getWeight(), oldInput.getWeight()))
                .priceChange(EventUtil.minusLong(newInput.getPrice(), oldInput.getPrice()))
                .overPriceChange(EventUtil.minusLong(newInput.getOverPrice(), oldInput.getOverPrice()))
                .isSowTrigger(notNull(oldGroupEvent.getSowId()))
                .build();
    }

    @Override
    public DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, BaseGroupInput input) {
        DoctorGroupEvent newGroupEvent = super.buildNewEvent(oldGroupEvent, input);
        DoctorChangeGroupInput newInput = (DoctorChangeGroupInput) input;
        newGroupEvent.setChangeTypeId(newInput.getChangeTypeId());
        newGroupEvent.setQuantity(newInput.getQuantity());
        newGroupEvent.setWeight(newInput.getWeight());
        newGroupEvent.setAvgWeight(EventUtil.getAvgWeight(newGroupEvent.getWeight(), newGroupEvent.getQuantity()));
        setSaleEvent(newGroupEvent, newInput, oldGroupEvent.getPigType());
        return newGroupEvent;
    }

    @Override
    public DoctorGroupTrack buildNewTrack(DoctorGroupTrack oldGroupTrack, DoctorEventChangeDto changeDto) {
        oldGroupTrack.setQuantity(EventUtil.minusInt(oldGroupTrack.getQuantity(), changeDto.getQuantityChange()));
        if (changeDto.getIsSowTrigger()) {
            oldGroupTrack.setUnweanQty(EventUtil.minusInt(oldGroupTrack.getUnweanQty(), changeDto.getQuantityChange()));
        }
        return oldGroupTrack;
    }

    @Override
    protected void updateDailyForModify(DoctorGroupEvent oldGroupEvent, BaseGroupInput input, DoctorEventChangeDto changeDto) {
        if (Objects.equals(changeDto.getNewEventAt(), changeDto.getOldEventAt())) {
                       DoctorGroupDaily oldDailyGroup = doctorDailyReportManager.findDoctorGroupDaily(changeDto.getFarmId(), 
                               changeDto.getPigType(), changeDto.getOldEventAt());
            if (Objects.equals(changeDto.getNewChangeTypeId(), changeDto.getOldChangeTypeId())) {
                changeDto.setChangeTypeId(changeDto.getOldChangeTypeId());
                buildDailyGroup(oldDailyGroup, changeDto);
            } else {
                //原变动类型更新
                DoctorChangeGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorChangeGroupInput.class);
                DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                        .quantityChange(EventUtil.minusInt(0, oldInput.getQuantity()))
                        .weightChange(EventUtil.minusDouble(0.0, oldInput.getWeight()))
                        .changeTypeId(oldInput.getChangeTypeId())
                        .isSowTrigger(notNull(oldGroupEvent.getSowId()))
                        .build();
                buildDailyGroup(oldDailyGroup, changeDto1);
                //新变动类型更新
                DoctorChangeGroupInput newInput = (DoctorChangeGroupInput) input;
                DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                        .quantityChange(newInput.getQuantity())
                        .weightChange(newInput.getWeight())
                        .changeTypeId(newInput.getChangeTypeId())
                        .isSowTrigger(notNull(oldGroupEvent.getSowId()))
                        .build();
                buildDailyGroup(oldDailyGroup, changeDto2);
            }
            doctorGroupDailyDao.update(oldDailyGroup);
            updateDailyGroupLiveStock(changeDto.getFarmId(), changeDto.getPigType(), getAfterDay(changeDto.getOldEventAt())
                    , -changeDto.getQuantityChange());
        } else {
            updateDailyForDelete(oldGroupEvent);
            updateDailyOfNew(oldGroupEvent, input);
        }
    }

    @Override
    protected void triggerEventModifyHandle(DoctorGroupEvent newEvent) {
        //关闭事件编辑
        DoctorGroupEvent closeEvent = doctorGroupEventDao.findByRelGroupEventIdAndType(newEvent.getId(), GroupEventType.CLOSE.getValue());
        if (notNull(closeEvent)) {
            modifyGroupCloseEventHandler.modifyHandle(closeEvent, buildGroupCloseInput(newEvent));
        }
    }

    @Override
    public Boolean rollbackHandleCheck(DoctorGroupEvent deleteGroupEvent) {
        //关闭事件回滚
        DoctorGroupEvent closeEvent = doctorGroupEventDao.findByRelGroupEventIdAndType(deleteGroupEvent.getId(), GroupEventType.CLOSE.getValue());
        if (notNull(closeEvent)) {
            return modifyGroupCloseEventHandler.rollbackHandleCheck(closeEvent);
        }
        return true;
    }

    @Override
    protected void triggerEventRollbackHandle(DoctorGroupEvent deleteGroupEvent, Long operatorId, String operatorName) {
        DoctorGroupEvent closeEvent = doctorGroupEventDao.findByRelGroupEventIdAndType(deleteGroupEvent.getId(), GroupEventType.CLOSE.getValue());
        if (notNull(closeEvent)) {
            modifyGroupCloseEventHandler.rollbackHandle(closeEvent, operatorId, operatorName);
        }
    }

    @Override
    protected DoctorGroupTrack buildNewTrackForRollback(DoctorGroupEvent deleteGroupEvent, DoctorGroupTrack oldGroupTrack) {
        oldGroupTrack.setQuantity(EventUtil.plusInt(oldGroupTrack.getQuantity(), deleteGroupEvent.getQuantity()));
        if (notNull(deleteGroupEvent.getSowId())) {
            oldGroupTrack.setUnweanQty(EventUtil.plusInt(oldGroupTrack.getUnweanQty(), deleteGroupEvent.getQuantity()));
        }
        return oldGroupTrack;
    }

    @Override
    protected void updateDailyForDelete(DoctorGroupEvent deleteGroupEvent) {
       updateDailyOfDelete(deleteGroupEvent);
    }

    @Override
    public void updateDailyOfDelete(DoctorGroupEvent oldGroupEvent) {
        DoctorChangeGroupInput oldInput = JSON_MAPPER.fromJson(oldGroupEvent.getExtra(), DoctorChangeGroupInput.class);
        DoctorEventChangeDto changeDto1 = DoctorEventChangeDto.builder()
                .quantityChange(EventUtil.minusInt(0, oldInput.getQuantity()))
                .weightChange(EventUtil.minusDouble(0.0, oldInput.getWeight()))
                .changeTypeId(oldInput.getChangeTypeId())
                .build();
        DoctorGroupDaily oldDailyGroup1 = doctorDailyReportManager.findDoctorGroupDaily(oldGroupEvent.getFarmId(), oldGroupEvent.getPigType(), oldGroupEvent.getEventAt());
        doctorGroupDailyDao.update(buildDailyGroup(oldDailyGroup1, changeDto1));
        updateDailyGroupLiveStock(oldGroupEvent.getFarmId(), oldGroupEvent.getPigType(), getAfterDay(oldGroupEvent.getEventAt()), -changeDto1.getQuantityChange());
        
    }

    @Override
    public void updateDailyOfNew(DoctorGroupEvent newGroupEvent, BaseGroupInput input) {
        Date eventAt = DateUtil.toDate(input.getEventAt());
        DoctorChangeGroupInput newInput = (DoctorChangeGroupInput) input;
        DoctorEventChangeDto changeDto2 = DoctorEventChangeDto.builder()
                .quantityChange(newInput.getQuantity())
                .weightChange(newInput.getWeight())
                .changeTypeId(newInput.getChangeTypeId())
                .build();
        DoctorGroupDaily oldDailyGroup2 = doctorDailyReportManager.findDoctorGroupDaily(newGroupEvent.getFarmId(), newGroupEvent.getPigType(), eventAt);
        doctorDailyReportManager.createOrUpdateGroupDaily(buildDailyGroup(oldDailyGroup2, changeDto2));
        updateDailyGroupLiveStock(newGroupEvent.getFarmId(), newGroupEvent.getPigType(), getAfterDay(eventAt), -changeDto2.getQuantityChange());
    }

    @Override
    protected DoctorGroupDaily buildDailyGroup(DoctorGroupDaily oldDailyGroup, DoctorEventChangeDto changeDto) {
        oldDailyGroup = super.buildDailyGroup(oldDailyGroup, changeDto);
        updateChange(oldDailyGroup, changeDto.getQuantityChange(), changeDto.getWeightChange(), changeDto.getChangeTypeId());
        oldDailyGroup.setEnd(EventUtil.minusInt(oldDailyGroup.getEnd(), changeDto.getQuantityChange()));
        return oldDailyGroup;
    }

    /**
     * 更新变动
     * @param oldDailyGroup 原猪群日记录
     * @param quantityChange 变动数量
     * @param changeTypeId 变动类型
     */
    private void updateChange(DoctorGroupDaily oldDailyGroup, Integer quantityChange, Double weightChange, Long changeTypeId) {
        oldDailyGroup.setTurnOutWeight(EventUtil.plusDouble(oldDailyGroup.getSaleWeight(), weightChange));
        if (Objects.equals(changeTypeId, SALE)) {
            oldDailyGroup.setSale(EventUtil.plusInt(oldDailyGroup.getSale(), quantityChange));
            oldDailyGroup.setSaleWeight(EventUtil.plusDouble(oldDailyGroup.getSaleWeight(), weightChange));
        } else if (Objects.equals(changeTypeId, DEAD)) {
            oldDailyGroup.setDead(EventUtil.plusInt(oldDailyGroup.getDead(), quantityChange));
        } else if (Objects.equals(changeTypeId, WEED)) {
            oldDailyGroup.setWeedOut(EventUtil.plusInt(oldDailyGroup.getWeedOut(), quantityChange));
        } else {
            oldDailyGroup.setOtherChange(EventUtil.plusInt(oldDailyGroup.getOtherChange(), quantityChange));
        }
    }

    //如果是销售事件, 记录价格与重量
    public void setSaleEvent(DoctorGroupEvent event, DoctorChangeGroupInput change, Integer pigType) {
        event.setPrice(change.getPrice());          //销售单价(分)(基础价)
        event.setBaseWeight(change.getBaseWeight());//基础重量
        event.setOverPrice(change.getOverPrice());  //超出价格(分/kg)
        event.setAmount(change.getAmount());
        if (Objects.equals(change.getChangeTypeId(), DoctorBasicEnums.SALE.getId())) {

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
