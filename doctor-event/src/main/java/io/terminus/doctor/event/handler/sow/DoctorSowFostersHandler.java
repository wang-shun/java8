package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.sow.DoctorFosterByDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFostersDto;
import io.terminus.doctor.event.dto.event.sow.DoctorWeanDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe: 注意拼窝事件关联事件信息的处理方式
 */
@Component
public class DoctorSowFostersHandler extends DoctorAbstractEventHandler {

    @Autowired
    private DoctorSowWeanHandler doctorSowWeanHandler;
    
    @Autowired
    private DoctorSowFostersByHandler doctorSowFostersByHandler;

    @Override
    protected DoctorPigTrack createOrUpdatePigTrack(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(inputDto.getPigId());
        DoctorFostersDto fostersDto = (DoctorFostersDto) inputDto;
        checkState(Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey()), "foster.currentSowStatus.error");

        //添加当前母猪的健崽猪的数量信息
        Integer unweanCount = MoreObjects.firstNonNull(doctorPigTrack.getUnweanQty(), 0);
        Integer fosterCount = fostersDto.getFostersCount();
        checkState(unweanCount >= fosterCount, "create.fostersBy.notEnough");

        doctorPigTrack.setUnweanQty(unweanCount - fosterCount);  //未断奶数
        doctorPigTrack.setWeanQty(MoreObjects.firstNonNull(doctorPigTrack.getWeanQty(), 0)); //断奶数不变
        Map<String, Object> extra = doctorPigTrack.getExtraMap();
        extra.put("farrowingLiveCount", doctorPigTrack.getUnweanQty());
        doctorPigTrack.setExtraMap(extra);


//        //全部断奶后, 初始化所有本次哺乳的信息
//        //Long pigEventId = (Long) context.get("doctorPigEventId");
//
//        if (doctorPigTrack.getUnweanQty() == 0) {
//            doctorPigTrack.setStatus(PigStatus.Wean.getKey());
//            doctorPigTrack.setGroupId(-1L);  //groupId = -1 置成 NULL
//            doctorPigTrack.setFarrowAvgWeight(0D); //分娩均重(kg)
//            doctorPigTrack.setFarrowQty(0);  //分娩数 0
//            doctorPigTrack.setWeanAvgWeight(0D);
//            DoctorPig doctorPig = doctorPigDao.findById(doctorPigTrack.getPigId());
//            DoctorPigEvent doctorPigEvent = DoctorPigEvent.builder()
//                    .type(PigEvent.WEAN.getKey())
//                    .pigId(doctorPigTrack.getPigId())
//                    .pigStatusBefore(PigStatus.FEED.getKey())
//                    .pigStatusAfter(PigStatus.Wean.getKey())
//                    .isAuto(IsOrNot.YES.getValue())
//                    .barnId(doctorPigTrack.getCurrentBarnId())
//                    .barnName(doctorPigTrack.getCurrentBarnName())
//                    .relPigEventId(pigEventId)
//                    .partweanDate(new Date())
//                    .farmId(doctorPigTrack.getFarmId())
//                    .weanCount(fosterCount)
//                    .weanAvgWeight(0D)
//                    .pigCode(doctorPig.getPigCode())
//                    .name(PigEvent.WEAN.getName())
//                    .eventAt(basic.generateEventAtFromExtra(extra))
//                    .kind(DoctorPig.PIG_TYPE.SOW.getKey())
//                    .orgId(basic.getOrgId()).orgName(basic.getOrgName())
//                    .npd(0)
//                    .dpnpd(0)
//                    .pfnpd(0)
//                    .plnpd(0)
//                    .psnpd(0)
//                    .pynpd(0)
//                    .ptnpd(0)
//                    .jpnpd(0)
//                    .farmName(basic.getFarmName())
//                    .build();
//            doctorPigEventDao.create(doctorPigEvent);
//        } else {
            doctorPigTrack.setStatus(PigStatus.FEED.getKey());
        //}
        //doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }

    @Override
    protected void triggerEvent(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        //断奶事件
        DoctorFostersDto fostersDto = (DoctorFostersDto) inputDto;
        if (doctorPigTrack.getUnweanQty() == 0) {
            DoctorWeanDto partWeanDto = DoctorWeanDto.builder()
                    .partWeanDate(DateUtil.toDate(fostersDto.getFostersDate()))
                    .partWeanPigletsCount(0)
                    .partWeanAvgWeight(0d)
                    .build();
            buildAutoEventCommonInfo(fostersDto, partWeanDto, basic, PigEvent.WEAN, doctorPigEvent.getId());
            doctorSowWeanHandler.handle(doctorEventInfoList, partWeanDto, basic);
        }
        //被拼窝事件
        DoctorFosterByDto fosterByDto = DoctorFosterByDto.builder()
                .fromSowId(fostersDto.getPigId())
                .fosterByDate(DateUtil.toDate(fostersDto.getFostersDate()))
                .fosterByCount(fostersDto.getFostersCount())
                .build();
        DoctorPigTrack fosterByTrack = doctorPigTrackDao.findByPigId(fostersDto.getFosterSowId());
        DoctorPig fosterByPig = doctorPigDao.findById(fostersDto.getFosterSowId());
        fosterByDto.setIsAuto(IsOrNot.YES.getValue());
        fosterByDto.setPigId(fosterByPig.getId());
        fosterByDto.setPigCode(fosterByPig.getPigCode());
        fosterByDto.setPigType(fosterByTrack.getPigType());
        fosterByDto.setBarnId(fosterByTrack.getCurrentBarnId());
        fosterByDto.setBarnName(fosterByTrack.getCurrentBarnName());
        fosterByDto.setRelPigEventId(doctorPigEvent.getId());
        basic.setEventName(PigEvent.FOSTERS_BY.getName());
        basic.setEventType(PigEvent.FOSTERS_BY.getKey());
        basic.setEventDesc(PigEvent.FOSTERS_BY.getDesc());
        doctorSowFostersByHandler.handle(doctorEventInfoList, fosterByDto, basic);
    }
}
