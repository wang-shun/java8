package io.terminus.doctor.event.handler.sow;

import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigPigletsChgEventHandler;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by yaoqijun.
 * Date:2016-06-01
 * Email:yaoqj@terminus.io
 * Descirbe: 仔猪变动事件 （仔猪转出信息的录入方式）
 */
@Component
public class DoctorSowPigletsChgHandler extends DoctorAbstractEventHandler {

    @Autowired
    private DoctorGroupWriteService doctorGroupWriteService;

    @Autowired
    private DoctorGroupReadService doctorGroupReadService;

    @Autowired
    private DoctorSowWeanHandler doctorSowWeanHandler;
    @Autowired
    private DoctorModifyPigPigletsChgEventHandler doctorModifyPigPigletsChgEventHandler;

    @Override
    public DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent = super.buildPigEvent(basic, inputDto);
        DoctorPigletsChgDto pigletsChgDto = (DoctorPigletsChgDto) inputDto;
        doctorPigEvent.setQuantity(pigletsChgDto.getPigletsCount());
        doctorPigEvent.setWeight(pigletsChgDto.getPigletsWeight());
        doctorPigEvent.setPrice(pigletsChgDto.getPigletsPrice());
        Double amount = doctorPigEvent.getPrice() * doctorPigEvent.getWeight();
        doctorPigEvent.setAmount(amount.longValue());
        doctorPigEvent.setCustomerId(pigletsChgDto.getPigletsCustomerId());
        doctorPigEvent.setCustomerName(pigletsChgDto.getPigletsCustomerName());
        return doctorPigEvent;
    }

    @Override
    public void handleCheck(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        super.handleCheck(executeEvent, fromTrack);
        expectTrue(Objects.equals(fromTrack.getStatus(), PigStatus.FEED.getKey()), "sow.status.not.feed", PigStatus.from(fromTrack.getStatus()).getName());
    }

    @Override
    public DoctorPigTrack buildPigTrack(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        DoctorPigTrack toTrack = super.buildPigTrack(executeEvent, fromTrack);
        DoctorPigletsChgDto pigletsChgDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorPigletsChgDto.class);

        // 校验转出的数量信息
        Integer unweanCount = toTrack.getUnweanQty();        //未断奶数量
        Integer weanCount = toTrack.getWeanQty();            //已断奶数量

        //变动数量
        Integer changeCount = pigletsChgDto.getPigletsCount();
        expectTrue(changeCount <= unweanCount, "change.count.not.enough", pigletsChgDto.getPigCode());
        toTrack.setUnweanQty(unweanCount - changeCount);  //未断奶数量 - 变动数量, 已断奶数量不用变

        //变动重量
        Double changeWeight = pigletsChgDto.getPigletsWeight();
        //expectTrue(changeWeight <= unweanCount, "wean.countInput.error");

        toTrack.setCurrentMatingCount(0);

        Map<String, Object> extra = toTrack.getExtraMap();
        //更新extra字段
        extra.put("partWeanPigletsCount", weanCount);
        extra.put("farrowingLiveCount", toTrack.getUnweanQty());
        toTrack.setExtraMap(extra);

        // 调用对应的猪猪群事件,对应的操作方式
        return toTrack;
    }


    @Override
    protected void triggerEvent(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack) {
        // TODO: 17/2/28 业务变动, 全部仔猪变动不触发断奶事件 先注释
//        if (Objects.equals(doctorPigTrack.getUnweanQty(), 0)) {
//            DoctorPigletsChgDto pigletsChgDto = (DoctorPigletsChgDto) inputDto;
//            DoctorWeanDto partWeanDto = DoctorWeanDto.builder()
//                    .partWeanDate(doctorPigEvent.getEventAt())
//                    .partWeanPigletsCount(0)
//                    .partWeanAvgWeight(0d)
//                    .build();
//            buildAutoEventCommonInfo(pigletsChgDto, partWeanDto, basic, PigEvent.WEAN, doctorPigEvent.getId());
//            doctorSowWeanHandler.handle(doctorEventInfoList, partWeanDto, basic);
//        }
//
        //触发猪群变动事件
        changePigletsChangeInfo(doctorPigTrack.getGroupId(), doctorPigEvent);
    }

    private void changePigletsChangeInfo(Long groupId, DoctorPigEvent doctorPigEvent) {
        RespHelper.orServEx(doctorGroupWriteService.groupEventChange(RespHelper.orServEx(doctorGroupReadService.findGroupDetailByGroupId(groupId)),
                (DoctorChangeGroupInput) buildTriggerGroupEventInput(doctorPigEvent)));
    }

    @Override
    public BaseGroupInput buildTriggerGroupEventInput(DoctorPigEvent pigEvent) {
        return doctorModifyPigPigletsChgEventHandler.buildTriggerGroupEventInput(pigEvent);
    }
}
