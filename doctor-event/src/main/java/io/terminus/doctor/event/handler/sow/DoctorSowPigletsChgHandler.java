package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.enums.IsOrNot;
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

        //变动信息
        Integer toWeanCount = (Integer)extra.get("pigletsCount");       //变动数量
        checkState(toWeanCount != null, "quantity.not.null");

        Integer oldWeanCount = 0;
        //如果之前存在部分断奶事件, 取出
        if(extraMap.containsKey("partWeanPigletsCount")){
            oldWeanCount = (Integer) extraMap.get("partWeanPigletsCount");
        }

        //取出均重, 如果存在, 重新计算
        Double weanAvgWeight = (Double)extra.get("pigletsWeight");      //变动重量
        checkState(weanAvgWeight != null, "weight.not.null");
        if (extraMap.containsKey("partWeanAvgWeight")) {
            Double oldWeanAvgWeight = MoreObjects.firstNonNull((Double) extraMap.get("partWeanAvgWeight"), 0D);

            //重新计算均重
            weanAvgWeight = ((weanAvgWeight * toWeanCount) + (oldWeanAvgWeight * oldWeanCount)) / (toWeanCount + oldWeanCount);
        }

        //断奶数量需要累加
        toWeanCount += oldWeanCount;
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
        changePigletsChangeInfo(Long.valueOf(extraMap.get("farrowingPigletGroupId").toString()), extra, basic);

        return doctorPigTrack;
    }


    private void changePigletsChangeInfo(Long groupId,  Map<String, Object> extra, DoctorBasicInputInfoDto basic){
        doctorGroupWriteService.groupEventChange(RespHelper.orServEx(doctorGroupReadService.findGroupDetailByGroupId(groupId)),
                buildInputInfo(extra, basic));
    }

    private DoctorChangeGroupInput buildInputInfo(Map<String,Object> mapInfo, DoctorBasicInputInfoDto basic){
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
        doctorChangeGroupInput.setPrice(dto.getPigletsPrice());
        doctorChangeGroupInput.setCustomerId(dto.getPigletsCustomerId());
        doctorChangeGroupInput.setRemark(dto.getPigletsMark());
        doctorChangeGroupInput.setIsAuto(IsOrNot.YES.getValue());           //自动生成事件标识
        doctorChangeGroupInput.setCreatorId(basic.getStaffId());
        doctorChangeGroupInput.setCreatorName(basic.getStaffName());
        return doctorChangeGroupInput;
    }
}
