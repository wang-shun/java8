package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.sow.DoctorFosterByDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFostersDto;
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

import static io.terminus.doctor.common.utils.Checks.expectTrue;

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
    public DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent = super.buildPigEvent(basic, inputDto);
        DoctorFostersDto fostersDto = (DoctorFostersDto) inputDto;
        doctorPigEvent.setQuantity(fostersDto.getFostersCount());
        return doctorPigEvent;
    }

    @Override
    public void handleCheck(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        super.handleCheck(executeEvent, fromTrack);
        DoctorFostersDto fostersDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorFostersDto.class);
        expectTrue(!Objects.equals(fostersDto.getPigId(), fostersDto.getFosterSowId()), "not.foster.userself", fostersDto.getPigCode());
    }

    @Override
    public DoctorPigTrack buildPigTrack(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        DoctorPigTrack toTrack = super.buildPigTrack(executeEvent, fromTrack);
        DoctorFostersDto fostersDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorFostersDto.class);
        expectTrue(Objects.equals(toTrack.getStatus(), PigStatus.FEED.getKey()), "foster.currentSowStatus.error");

        //添加当前母猪的健崽猪的数量信息
        Integer unweanCount = MoreObjects.firstNonNull(toTrack.getUnweanQty(), 0);
        Integer fosterCount = fostersDto.getFostersCount();
        expectTrue(unweanCount >= fosterCount, "fosters.count.not.enough", fosterCount, unweanCount);

        toTrack.setUnweanQty(unweanCount - fosterCount);  //未断奶数
        toTrack.setWeanQty(MoreObjects.firstNonNull(toTrack.getWeanQty(), 0)); //断奶数不变
        Map<String, Object> extra = toTrack.getExtraMap();
        extra.put("farrowingLiveCount", toTrack.getUnweanQty());
        toTrack.setExtraMap(extra);
        toTrack.setStatus(PigStatus.FEED.getKey());
        return toTrack;
    }

    @Override
    protected void triggerEvent(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack) {
        DoctorFostersDto fostersDto = JSON_MAPPER.fromJson(doctorPigEvent.getExtra(), DoctorFostersDto.class);
        // TODO: 17/2/28 业务逻辑修改,全部拼窝不触发断奶,先注释掉
//        //断奶事件
//        if (doctorPigTrack.getUnweanQty() == 0) {
//            DoctorWeanDto partWeanDto = DoctorWeanDto.builder()
//                    .partWeanDate(DateUtil.toDate(fostersDto.getFostersDate()))
//                    .partWeanPigletsCount(0)
//                    .partWeanAvgWeight(0d)
//                    .build();
//            buildAutoEventCommonInfo(fostersDto, partWeanDto, basic, PigEvent.WEAN, doctorPigEvent.getId());
//            doctorSowWeanHandler.handle(doctorEventInfoList, partWeanDto, basic);
//        }
        //被拼窝事件
        DoctorFosterByDto fosterByDto = DoctorFosterByDto.builder()
                .fromSowId(fostersDto.getPigId())
                .fromSowCode(fostersDto.getPigCode())
                .fosterByDate(DateUtil.toDate(fostersDto.getFostersDate()))
                .fosterByCount(fostersDto.getFostersCount())
                .boarFostersByCount(fostersDto.getBoarFostersCount())
                .sowFostersByCount(fostersDto.getSowFostersCount())
                .fosterByTotalWeight(fostersDto.getFosterTotalWeight())
                .fromGroupId(doctorPigTrack.getGroupId())
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
        fosterByDto.setEventName(PigEvent.FOSTERS_BY.getName());
        fosterByDto.setEventType(PigEvent.FOSTERS_BY.getKey());
        fosterByDto.setEventDesc(PigEvent.FOSTERS_BY.getDesc());

        //构建basic
        DoctorBasicInputInfoDto basic = DoctorBasicInputInfoDto.builder()
                .orgId(doctorPigEvent.getOrgId())
                .orgName(doctorPigEvent.getOrgName())
                .farmId(doctorPigEvent.getFarmId())
                .farmName(doctorPigEvent.getFarmName())
                .staffId(doctorPigEvent.getOperatorId())
                .staffName(doctorPigEvent.getOperatorName())
                .build();
        doctorSowFostersByHandler.handle(doctorEventInfoList, doctorSowFostersByHandler.buildPigEvent(basic, fosterByDto), fosterByTrack);
    }
}
