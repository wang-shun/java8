package io.terminus.doctor.event.handler.sow;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.sow.DoctorWeanDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
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

import static com.google.common.base.Preconditions.checkState;
import static io.terminus.common.utils.Arguments.notNull;

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

    @Override
    protected DoctorPigTrack createOrUpdatePigTrack(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigletsChgDto pigletsChgDto = (DoctorPigletsChgDto) inputDto;
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(pigletsChgDto.getPigId());
        checkState(Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey()), "piglets.chgSowStatus.error");

        // 校验转出的数量信息
        Integer unweanCount = doctorPigTrack.getUnweanQty();        //未断奶数量
        Integer weanCount = doctorPigTrack.getWeanQty();            //已断奶数量

        //变动数量
        Integer changeCount = pigletsChgDto.getPigletsCount();
        checkState(changeCount != null, "quantity.not.null");
        checkState(changeCount <= unweanCount, "change.countInput.error");
        doctorPigTrack.setUnweanQty(unweanCount - changeCount);  //未断奶数量 - 变动数量, 已断奶数量不用变

        //变动重量
        Double changeWeight = pigletsChgDto.getPigletsWeight();
        checkState(changeWeight != null, "weight.not.null");
        //checkState(changeWeight <= unweanCount, "wean.countInput.error");

        doctorPigTrack.setCurrentMatingCount(0);

        Map<String, Object> extra = doctorPigTrack.getExtraMap();
        //更新extra字段
        extra.put("partWeanPigletsCount", weanCount);
        extra.put("farrowingLiveCount", doctorPigTrack.getUnweanQty());
        doctorPigTrack.setExtraMap(extra);
        //Long groupId = doctorPigTrack.getGroupId();
        //全部断奶后, 初始化所有本次哺乳的信息
        //Long pigEventId = (Long) context.get("doctorPigEventId");
//        //全部断奶后, 初始化所有本次哺乳的信息
//        if (doctorPigTrack.getUnweanQty() == 0) {
//            doctorPigTrack.setStatus(PigStatus.Wean.getKey());
//            doctorPigTrack.setGroupId(-1L);  //groupId = -1 置成 NULL
//            doctorPigTrack.setFarrowQty(0);  //分娩数 0
//            doctorPigTrack.setFarrowAvgWeight(0D);
//            doctorPigTrack.setWeanAvgWeight(0D);
//
//        }
       // doctorPigTrack.addPigEvent(basic.getPigType(), pigEventId);

        // 调用对应的猪猪群事件,对应的操作方式
        checkState(notNull(doctorPigTrack.getGroupId()), "pigletsChg.groupId.notFound");
        return doctorPigTrack;
    }

    @Override
    protected void triggerEvent(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        if (Objects.equals(doctorPigTrack.getUnweanQty(), 0)) {
            DoctorPigletsChgDto pigletsChgDto = (DoctorPigletsChgDto) inputDto;
            DoctorWeanDto partWeanDto = DoctorWeanDto.builder()
                    .partWeanDate(doctorPigEvent.getEventAt())
                    .partWeanPigletsCount(pigletsChgDto.getPigletsCount())
                    .partWeanAvgWeight(0d)
                    .build();
            buildAutoEventCommonInfo(pigletsChgDto, partWeanDto, basic, PigEvent.WEAN, doctorPigEvent.getId());
            doctorSowWeanHandler.handle(doctorEventInfoList, partWeanDto, basic);
        }

        changePigletsChangeInfo(doctorPigTrack.getGroupId(), inputDto, basic, doctorPigEvent.getId());
    }

    private void changePigletsChangeInfo(Long groupId, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic, Long pigEventId) {
        RespHelper.orServEx(doctorGroupWriteService.groupEventChange(RespHelper.orServEx(doctorGroupReadService.findGroupDetailByGroupId(groupId)),
                buildInputInfo(inputDto, basic, pigEventId)));
    }

    private DoctorChangeGroupInput buildInputInfo(BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic, Long pigEventId) {
        DoctorPigletsChgDto dto = (DoctorPigletsChgDto) inputDto;
        DoctorChangeGroupInput doctorChangeGroupInput = new DoctorChangeGroupInput();
        doctorChangeGroupInput.setEventAt(DateUtil.toDateString(dto.getPigletsChangeDate()));
        doctorChangeGroupInput.setChangeTypeId(dto.getPigletsChangeType());             //变动类型id
        doctorChangeGroupInput.setChangeTypeName(dto.getPigletsChangeTypeName());       //变动类型名称
        doctorChangeGroupInput.setChangeReasonId(dto.getPigletsChangeReason());         //变动原因id
        doctorChangeGroupInput.setChangeReasonName(dto.getPigletsChangeReasonName());   //变动原因名称
        doctorChangeGroupInput.setQuantity(dto.getPigletsCount());                      //变动仔猪数量
        doctorChangeGroupInput.setSowQty(dto.getSowPigletsCount());
        doctorChangeGroupInput.setBoarQty(dto.getBoarPigletsCount());
        doctorChangeGroupInput.setWeight(dto.getPigletsWeight());
        doctorChangeGroupInput.setPrice(dto.getPigletsPrice());                             //单价
        if (dto.getPigletsPrice() != null) {
            doctorChangeGroupInput.setAmount(dto.getPigletsPrice() * dto.getPigletsCount());    //总额
        }
        doctorChangeGroupInput.setCustomerId(dto.getPigletsCustomerId());
        doctorChangeGroupInput.setRemark(dto.getPigletsMark());
        doctorChangeGroupInput.setIsAuto(IsOrNot.YES.getValue());           //自动生成事件标识
        doctorChangeGroupInput.setCreatorId(basic.getStaffId());
        doctorChangeGroupInput.setCreatorName(basic.getStaffName());
        doctorChangeGroupInput.setRelPigEventId(pigEventId);        //猪事件id
        doctorChangeGroupInput.setSowEvent(true);   //母猪触发的变动事件
        return doctorChangeGroupInput;
    }
}
