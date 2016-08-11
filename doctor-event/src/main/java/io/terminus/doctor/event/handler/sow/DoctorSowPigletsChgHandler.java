package io.terminus.doctor.event.handler.sow;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventFlowHandler;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.workflow.core.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by yaoqijun.
 * Date:2016-06-01
 * Email:yaoqj@terminus.io
 * Descirbe: 仔猪变动事件 （仔猪转出信息的录入方式）
 */
@Component
public class DoctorSowPigletsChgHandler extends DoctorAbstractEventFlowHandler{

    private final DoctorGroupWriteService doctorGroupWriteService;

    private final DoctorGroupReadService doctorGroupReadService;

    @Autowired
    public DoctorSowPigletsChgHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao,
                                      DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao,
                                      DoctorRevertLogDao doctorRevertLogDao,
                                      DoctorGroupWriteService doctorGroupWriteService, DoctorGroupReadService doctorGroupReadService) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao);
        this.doctorGroupWriteService= doctorGroupWriteService;
        this.doctorGroupReadService = doctorGroupReadService;
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(Execution execution, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic, Map<String, Object> extra, Map<String,Object> context) {
        // 校验母猪的状态信息
        checkState(Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey()), "piglets.chgSowStatus.error");

        // 校验转出的数量信息
        Map<String,Object> extraMap = doctorPigTrack.getExtraMap();
        Integer healthCount = (Integer) extraMap.get("farrowingLiveCount");
        Integer toWeanCount = (Integer)extra.get("partWeanPigletsCount");
        Double weanAvgWeight = (Double)extra.get("partWeanAvgWeight");

        // 历史数据修改信息内容
        if(extraMap.containsKey("partWeanPigletsCount")){
            // 已经包含断奶信息
            toWeanCount += (Integer) extraMap.get("partWeanPigletsCount");
            weanAvgWeight += (Double) extraMap.get("partWeanAvgWeight");
            weanAvgWeight = weanAvgWeight/2;
        }
        checkState(toWeanCount<= healthCount, "wean.countInput.error");

        // update info
        extra.put("partWeanPigletsCount", toWeanCount);
        extra.put("partWeanAvgWeight", weanAvgWeight);
        doctorPigTrack.addAllExtraMap(extra);

        if(Objects.equals(toWeanCount, healthCount)){
            doctorPigTrack.setStatus(PigStatus.Wean.getKey());
        }

        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        execution.getExpression().put("leftCount", (healthCount - toWeanCount));

        // 调用对应的猪猪群事件,对应的操作方式
        checkState(extraMap.containsKey("farrowingPigletGroupId"), "pigletsChg.groupId.notFound");
        changePigletsChangeInfo(Long.valueOf(extraMap.get("farrowingPigletGroupId").toString()), extra);

        return doctorPigTrack;
    }


    private void changePigletsChangeInfo(Long groupId,  Map<String, Object> extra){
        doctorGroupWriteService.groupEventChange(RespHelper.orServEx(doctorGroupReadService.findGroupDetailByGroupId(groupId)),
                buildInputInfo(extra));
    }

    private DoctorChangeGroupInput buildInputInfo(Map<String,Object> mapInfo){
        DoctorPigletsChgDto dto = new DoctorPigletsChgDto();
        BeanMapper.copy(mapInfo, dto);

        DoctorChangeGroupInput doctorChangeGroupInput = new DoctorChangeGroupInput();
        doctorChangeGroupInput.setChangeTypeId(dto.getPigletsChangeType());
        doctorChangeGroupInput.setChangeReasonId(dto.getPigletsChangeReason());
        doctorChangeGroupInput.setQuantity(dto.getPigletsCount());
        doctorChangeGroupInput.setSowQty(dto.getSowPigletsCount());
        doctorChangeGroupInput.setBoarQty(dto.getBoarPigletsCount());
        doctorChangeGroupInput.setWeight(dto.getPigletsWeight());
        doctorChangeGroupInput.setPrice(dto.getPigletsPrice());
        doctorChangeGroupInput.setCustomerId(dto.getPigletsCustomerId());
        return doctorChangeGroupInput;
    }
}
