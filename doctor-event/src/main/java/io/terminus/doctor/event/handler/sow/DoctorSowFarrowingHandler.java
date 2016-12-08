package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dao.DoctorRevertLogDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.DoctorSowMoveInGroupInput;
import io.terminus.doctor.event.enums.FarrowingType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigSex;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventFlowHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.workflow.core.Execution;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe: 母猪分娩事件
 */
@Component
@Slf4j
public class DoctorSowFarrowingHandler extends DoctorAbstractEventFlowHandler {

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    private final DoctorGroupWriteService doctorGroupWriteService;

    @Autowired
    public DoctorSowFarrowingHandler(DoctorPigDao doctorPigDao, DoctorPigEventDao doctorPigEventDao,
                                     DoctorPigTrackDao doctorPigTrackDao, DoctorPigSnapshotDao doctorPigSnapshotDao,
                                     DoctorRevertLogDao doctorRevertLogDao, DoctorGroupWriteService doctorGroupWriteService,
                                     DoctorBarnDao doctorBarnDao) {
        super(doctorPigDao, doctorPigEventDao, doctorPigTrackDao, doctorPigSnapshotDao, doctorRevertLogDao, doctorBarnDao);
        this.doctorGroupWriteService = doctorGroupWriteService;
    }

    @Override
    protected IsOrNot eventCreatePreHandler(Execution execution, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto, Map<String, Object> extra, Map<String, Object> context) {
        // 助产 信息
        extra.put("isHelp", 1);

        // 校验 是否早产信息
        DateTime pregJudgeDate = new DateTime(Long.valueOf(doctorPigTrack.getExtraMap().get("judgePregDate").toString()));
        //分娩时间
        DateTime farrowingDate = new DateTime(Long.valueOf(extra.get("farrowingDate").toString()));
        doctorPigEvent.setFarrowingDate(farrowingDate.toDate());
        //查找最近一次初配种事件
        DoctorPigEvent firstMate = doctorPigEventDao.queryLastFirstMate(doctorPigTrack.getPigId(), doctorPigTrack.getCurrentParity());
        if (notNull(firstMate)) {
            DateTime mattingDate = new DateTime(firstMate.getEventAt());

            //计算孕期
            doctorPigEvent.setPregDays(Math.abs(Days.daysBetween(farrowingDate, mattingDate).getDays()));
        }

        //分娩窝重
        doctorPigEvent.setFarrowWeight(Doubles.tryParse(Objects.toString(extra.get("birthNestAvg"))));
        //分娩仔猪只数信息
        doctorPigEvent.setLiveCount(Ints.tryParse(Objects.toString(extra.get("farrowingLiveCount"))));
        doctorPigEvent.setHealthCount(Ints.tryParse(Objects.toString(extra.get("healthCount"))));
        doctorPigEvent.setWeakCount(Ints.tryParse(Objects.toString(extra.get("weakCount"))));
        doctorPigEvent.setMnyCount(Ints.tryParse(Objects.toString(extra.get("mnyCount"))));
        doctorPigEvent.setJxCount(Ints.tryParse(Objects.toString(extra.get("jxCount"))));
        doctorPigEvent.setDeadCount(Ints.tryParse(Objects.toString(extra.get("deadCount"))));
        doctorPigEvent.setBlackCount(Ints.tryParse(Objects.toString(extra.get("blackCount"))));

        if (farrowingDate.isBefore(pregJudgeDate)) {
            extra.put("farrowingType", FarrowingType.EARLY.getKey());
        } else {
            extra.put("farrowingType", FarrowingType.USUAL.getKey());
        }
        return IsOrNot.NO;
    }

    @Override
    public DoctorPigTrack updateDoctorPigTrackInfo(Execution execution,
                                                   DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basic,
                                                   Map<String, Object> extra, Map<String, Object> context) {

        // 对应的 仔猪 猪舍的 信息
        extra.put("toBarnId", doctorPigTrack.getCurrentBarnId());
        extra.put("toBarnName", doctorPigTrack.getCurrentBarnName());

        Long pigEventId = (Long) context.get("doctorPigEventId");

        //分娩时记录下 分娩数量
        doctorPigTrack.setFarrowQty(Integer.valueOf(MoreObjects.firstNonNull(extra.get("farrowingLiveCount"), 0).toString()));
        doctorPigTrack.setUnweanQty(doctorPigTrack.getFarrowQty());
        doctorPigTrack.setWeanQty(0);  //分娩时 断奶数为0
        doctorPigTrack.setFarrowAvgWeight(Double.valueOf(extra.get("birthNestAvg").toString()));
        doctorPigTrack.setWeanAvgWeight(0D); //分娩时, 断奶均重置成0

        doctorPigTrack.addAllExtraMap(extra);
        doctorPigTrack.setStatus(PigStatus.FEED.getKey());  //母猪进入哺乳的状态

        // 对应的 猪群信息
        Long groupId = buildPigGroupCountInfo(basic, extra, pigEventId);
        extra.put("farrowingPigletGroupId", groupId);
        //分娩时记录下 哺乳猪群号
        doctorPigTrack.setGroupId(groupId);

        doctorPigTrack.addPigEvent(basic.getPigType(), pigEventId);
        return doctorPigTrack;
    }

    /**
     * 创建对应的猪群
     *
     * @param basic
     * @param extra
     */
    protected Long buildPigGroupCountInfo(DoctorBasicInputInfoDto basic, Map<String, Object> extra, Long pigEventId) {
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(basic.getPigId());

        // Build 新建猪群操作方式
        DoctorSowMoveInGroupInput input = new DoctorSowMoveInGroupInput();
        input.setOrgId(basic.getOrgId());
        input.setOrgName(basic.getOrgName());
        input.setFarmId(basic.getFarmId());
        input.setFarmName(basic.getFarmName());
        input.setGroupCode(extra.get("groupCode").toString());

        input.setFromBarnId(doctorPigTrack.getCurrentBarnId());
        input.setFromBarnName(doctorPigTrack.getCurrentBarnName());
        input.setToBarnId(Long.valueOf(String.valueOf(extra.get("toBarnId"))));
        input.setToBarnName(String.valueOf(extra.get("toBarnName")));
        input.setPigType(PigType.DELIVER_SOW.getValue());
        input.setInType(DoctorMoveInGroupEvent.InType.PIGLET.getValue());
        input.setInTypeName(DoctorMoveInGroupEvent.InType.PIGLET.getDesc());
        input.setSource(PigSource.LOCAL.getKey());

        Integer farrowingLiveCount = Integer.valueOf(MoreObjects.firstNonNull(extra.get("farrowingLiveCount"), 0).toString());
        Integer sowCount = Integer.valueOf(MoreObjects.firstNonNull(extra.get("liveSowCount"), 0).toString());
        Integer boarCount = Integer.valueOf(MoreObjects.firstNonNull(extra.get("liveBoarCount"), 0).toString());
        if (sowCount == 0 && boarCount == 0) sowCount = farrowingLiveCount;

        input.setSex(judgePigSex(sowCount, boarCount).getKey());
        input.setQuantity(farrowingLiveCount);
        input.setSowQty(sowCount);
        input.setBoarQty(boarCount);
        input.setAvgDayAge(1);
        input.setAvgWeight(Double.valueOf(extra.get("birthNestAvg").toString()));
        input.setEventAt(DateUtil.toDateString(basic.generateEventAtFromExtra(extra)));
        input.setIsAuto(1);
        input.setCreatorId(basic.getStaffId());
        input.setCreatorName(basic.getStaffName());

        input.setSowEvent(true);  //设置为分娩转入
        input.setWeakQty(Ints.tryParse(Objects.toString(extra.get("weakCount"))));
        input.setHealthyQty(Ints.tryParse(Objects.toString(extra.get("healthCount"))));

        input.setRelPigEventId(pigEventId);
        Response<Long> response = doctorGroupWriteService.sowGroupEventMoveIn(input);
        if (response.isSuccess()) {
            return response.getResult();
        } else {
            throw new IllegalStateException(response.getError());
        }
    }

    private PigSex judgePigSex(Integer sowCount, Integer boarCount) {
        if (sowCount == 0 && boarCount == 0) {
            return PigSex.MIX;
        }

        if (sowCount == 0) {
            return PigSex.BOAR;
        }

        if (boarCount == 0) {
            return PigSex.SOW;
        }
        return PigSex.MIX;
    }

    @Override
    protected void afterEventCreateHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, Long farrowGroupId, Long farrowBarnId) {
        Map<String, Object> extraMap = doctorPigEvent.getExtraMap();
        extraMap.put("farrowGroupId", doctorPigTrack.getGroupId());
        doctorPigEvent.setExtraMap(extraMap);

        //对应的最近一次的 周期配种的初陪 的 isDelivery 字段变成true
        DoctorPigEvent firstMate = doctorPigEventDao.queryLastFirstMate(doctorPigTrack.getPigId(), doctorPigTrack.getCurrentParity());
        if (notNull(firstMate)) {
            firstMate.setIsDelivery(1);
            doctorPigEventDao.update(firstMate);
        }
    }
}
