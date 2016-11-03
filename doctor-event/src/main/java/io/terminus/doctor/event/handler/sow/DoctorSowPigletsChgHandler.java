package io.terminus.doctor.event.handler.sow;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventFlowHandler;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.workflow.core.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
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
public class DoctorSowPigletsChgHandler extends DoctorAbstractEventFlowHandler {

    private final DoctorGroupWriteService doctorGroupWriteService;

    private final DoctorGroupReadService doctorGroupReadService;

    @Autowired
    public DoctorSowPigletsChgHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao,
                                      DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao,
                                      DoctorRevertLogDao doctorRevertLogDao,
                                      DoctorGroupWriteService doctorGroupWriteService,
                                      DoctorGroupReadService doctorGroupReadService,
                                      DoctorBarnDao doctorBarnDao) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao, doctorBarnDao);
        this.doctorGroupWriteService = doctorGroupWriteService;
        this.doctorGroupReadService = doctorGroupReadService;
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(Execution execution, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String, Object> context) {
        checkState(Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey()), "piglets.chgSowStatus.error");

        // 校验转出的数量信息
        Integer unweanCount = doctorPigTrack.getUnweanQty();        //未断奶数量
        Integer weanCount = doctorPigTrack.getWeanQty();            //已断奶数量

        //变动数量
        Integer changeCount = (Integer) extra.get("pigletsCount");
        checkState(changeCount != null, "quantity.not.null");
        checkState(changeCount <= unweanCount, "wean.countInput.error");
        doctorPigTrack.setUnweanQty(unweanCount - changeCount);  //未断奶数量 - 变动数量, 已断奶数量不用变

        //变动重量
        Double changeWeight = (Double) extra.get("pigletsWeight");
        checkState(changeWeight != null, "weight.not.null");
        //checkState(changeWeight <= unweanCount, "wean.countInput.error");

        //更新extra字段
        extra.put("partWeanPigletsCount", weanCount);
        extra.put("farrowingLiveCount", doctorPigTrack.getUnweanQty());
        doctorPigTrack.addAllExtraMap(extra);
        Long groupId = doctorPigTrack.getGroupId();
        //全部断奶后, 初始化所有本次哺乳的信息
        Long pigEventId = (Long) context.get("doctorPigEventId");
        //全部断奶后, 初始化所有本次哺乳的信息
        if (doctorPigTrack.getUnweanQty() == 0) {
            doctorPigTrack.setStatus(PigStatus.Wean.getKey());
            doctorPigTrack.setGroupId(-1L);  //groupId = -1 置成 NULL
            doctorPigTrack.setFarrowQty(0);  //分娩数 0
            doctorPigTrack.setFarrowAvgWeight(0D);
            doctorPigTrack.setWeanAvgWeight(0D);
            DoctorPig doctorPig = doctorPigDao.findById(doctorPigTrack.getPigId());
            DoctorPigEvent doctorPigEvent = DoctorPigEvent.builder()
                    .type(PigEvent.WEAN.getKey())
                    .pigId(doctorPigTrack.getPigId())
                    .pigStatusBefore(PigStatus.FEED.getKey())
                    .pigStatusAfter(PigStatus.Wean.getKey())
                    .isAuto(IsOrNot.YES.getValue())
                    .barnId(doctorPigTrack.getCurrentBarnId())
                    .barnName(doctorPigTrack.getCurrentBarnName())
                    .relPigEventId(pigEventId)
                    .partweanDate(new Date())
                    .farmId(doctorPigTrack.getFarmId())
                    .weanCount(changeCount)
                    .pigCode(doctorPig.getPigCode())
                    .name(PigEvent.WEAN.getName())
                    .weanAvgWeight(0D)
                    .farmName(basic.getFarmName())
                    .eventAt(basic.generateEventAtFromExtra(extra))
                    .kind(DoctorPig.PIG_TYPE.SOW.getKey())
                    .orgId(basic.getOrgId()).orgName(basic.getOrgName())
                    .npd(0)
                    .dpnpd(0)
                    .pfnpd(0)
                    .plnpd(0)
                    .psnpd(0)
                    .pynpd(0)
                    .ptnpd(0)
                    .jpnpd(0)
                    .build();
            doctorPigEventDao.create(doctorPigEvent);

        }
        doctorPigTrack.addPigEvent(basic.getPigType(), pigEventId);
        execution.getExpression().put("leftCount", (doctorPigTrack.getUnweanQty()));

        // 调用对应的猪猪群事件,对应的操作方式
        checkState(notNull(doctorPigTrack.getGroupId()), "pigletsChg.groupId.notFound");
        changePigletsChangeInfo(groupId, extra, basic, pigEventId);

        return doctorPigTrack;
    }


    private void changePigletsChangeInfo(Long groupId, Map<String, Object> extra, DoctorBasicInputInfoDto basic, Long pigEventId) {
        RespHelper.orServEx(doctorGroupWriteService.groupEventChange(RespHelper.orServEx(doctorGroupReadService.findGroupDetailByGroupId(groupId)),
                buildInputInfo(extra, basic, pigEventId)));
    }

    private DoctorChangeGroupInput buildInputInfo(Map<String, Object> mapInfo, DoctorBasicInputInfoDto basic, Long pigEventId) {
        DoctorPigletsChgDto dto = BeanMapper.map(mapInfo, DoctorPigletsChgDto.class);
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
        return doctorChangeGroupInput;
    }
}
