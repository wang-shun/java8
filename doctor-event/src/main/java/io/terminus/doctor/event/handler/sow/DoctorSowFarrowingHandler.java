package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.DoctorSowMoveInGroupInput;
import io.terminus.doctor.event.enums.FarrowingType;
import io.terminus.doctor.event.enums.PigSex;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventFlowHandler;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.workflow.core.Execution;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
@Slf4j
public class DoctorSowFarrowingHandler extends DoctorAbstractEventFlowHandler {

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    private final DoctorGroupWriteService doctorGroupWriteService;

    @Autowired
    public DoctorSowFarrowingHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao,
                                     DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao,
                                     DoctorRevertLogDao doctorRevertLogDao,
                                     DoctorGroupWriteService doctorGroupWriteService) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao);
        this.doctorGroupWriteService = doctorGroupWriteService;
    }

    @Override
    protected void eventCreatePrepare(Execution execution, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto, Map<String, Object> extra, Map<String, Object> context) {

        // 助产 信息
        extra.put("isHelp", 1);

        // 校验 是否早产信息
        DateTime pregJudgeDate = new DateTime(Long.valueOf(doctorPigTrack.getExtraMap().get("judgePregDate").toString()));
        DateTime farrowingDate = new DateTime(Long.valueOf(extra.get("farrowingDate").toString()));
        if(farrowingDate.isBefore(pregJudgeDate)){
            extra.put("farrowingType", FarrowingType.EARLY.getKey());
        }else {
            extra.put("farrowingType", FarrowingType.USUAL.getKey());
        }
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(Execution execution,
                                                   DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic,
                                                   Map<String, Object> extra, Map<String,Object> context) {

        // 对应的 仔猪 猪舍的 信息
        extra.put("toBarnId", doctorPigTrack.getCurrentBarnId());
        extra.put("toBarnName", doctorPigTrack.getCurrentBarnName());

        // 对应的 猪群信息
        extra.put("farrowingPigletGroupId",buildPigGroupCountInfo(basic, extra));

        doctorPigTrack.addAllExtraMap(extra);
        doctorPigTrack.setStatus(PigStatus.FEED.getKey());  //母猪进入哺乳的状态
        doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }

    /**
     * 创建对应的猪群
     * @param basic
     * @param extra
     */
    protected Long buildPigGroupCountInfo(DoctorBasicInputInfoDto basic, Map<String, Object> extra) {
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(basic.getPigId());

        log.info("********** extra:{}", extra);

        // Build 新建猪群操作方式
        DoctorSowMoveInGroupInput input = new DoctorSowMoveInGroupInput();
        input.setOrgId(basic.getOrgId());
        input.setOrgName(basic.getOrgName());
        input.setFarmId(basic.getFarmId());
        input.setFarmName(basic.getFarmName());
        input.setGroupCode(extra.get("groupCode").toString());

        input.setFromBarnId(doctorPigTrack.getCurrentBarnId());
        input.setFromBarnName(doctorPigTrack.getCurrentBarnName());
        input.setToBarnId(Long.valueOf(extra.get("toBarnId").toString()));
        input.setToBarnName(extra.get("toBarnName").toString());
        input.setPigType(PigType.FARROW_PIGLET.getValue());
        input.setInType(DoctorMoveInGroupEvent.InType.PIGLET.getValue());
        input.setInTypeName(DoctorMoveInGroupEvent.InType.PIGLET.getDesc());
        input.setSource(PigSource.LOCAL.getKey());

        Integer sowCount = Integer.valueOf(MoreObjects.firstNonNull(extra.get("liveSowCount"), 0).toString());
        Integer boarCount = Integer.valueOf(MoreObjects.firstNonNull(extra.get("liveBoarCount"), 0).toString());

        input.setSex(judgePigSex(sowCount, boarCount).getKey());
        input.setQuantity(sowCount + boarCount);
        input.setSowQty(sowCount);
        input.setBoarQty(boarCount);
        input.setAvgDayAge(Integer.valueOf(extra.get("dayAgeAvg").toString()));
        input.setAvgWeight(Double.valueOf(extra.get("birthNestAvg").toString()));
        input.setEventAt(DateTime.now().toString(DTF));
        input.setIsAuto(1);
        input.setCreatorId(basic.getStaffId());
        input.setCreatorName(basic.getStaffName());
        Response<Long> response = doctorGroupWriteService.sowGroupEventMoveIn(input);
        if(response.isSuccess()){
            return response.getResult();
        }else {
            throw new IllegalStateException(response.getError());
        }
    }

    private PigSex judgePigSex(Integer sowCount, Integer boarCount){
        if(sowCount == 0 && boarCount == 0){
            throw new IllegalStateException("farrow.pigCount.error");
        }

        if(sowCount == 0){
            return PigSex.BOAR;
        }

        if(boarCount == 0){
            return PigSex.SOW;
        }
        return PigSex.MIX;
    }
}
