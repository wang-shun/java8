package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorWeanGroupInput;
import io.terminus.doctor.event.dto.event.sow.DoctorWeanDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.handler.group.DoctorCommonGroupEventHandler;
import io.terminus.doctor.event.handler.usual.DoctorChgLocationHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by .
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Slf4j
@Component
public class DoctorSowWeanHandler extends DoctorAbstractEventHandler {

    @Autowired
    private DoctorChgLocationHandler chgLocationHandler;
    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired
    private DoctorBarnDao doctorBarnDao;
    @Autowired
    private DoctorCommonGroupEventHandler doctorCommonGroupEventHandler;

    @Override
    public void handleCheck(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        super.handleCheck(executeEvent, fromTrack);
        expectTrue(Objects.equals(fromTrack.getStatus(), PigStatus.FEED.getKey())
                ,"pig.status.failed", PigStatus.from(fromTrack.getStatus()).getName());
        DoctorWeanDto weanDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorWeanDto.class);
        expectTrue(MoreObjects.firstNonNull(weanDto.getQualifiedCount(), 0) + MoreObjects.firstNonNull(weanDto.getNotQualifiedCount(), 0) <= weanDto.getFarrowingLiveCount(), "qualified.add.noQualified.over.live", weanDto.getPigCode());
        if (Objects.equals(weanDto.getFarrowingLiveCount(), 0) && !Objects.equals(weanDto.getPartWeanAvgWeight(), 0d)) {
            throw new InvalidException("wean.avg.weight.not.zero", weanDto.getPigCode());
        }
        if (!Objects.equals(weanDto.getFarrowingLiveCount(), 0) && (weanDto.getPartWeanAvgWeight() < 3 || weanDto.getPartWeanAvgWeight() > 9)) {
            throw new InvalidException("wean.avg.weight.range.error", weanDto.getPigCode());
        }
    }

    @Override
    public DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent = super.buildPigEvent(basic, inputDto);
        DoctorWeanDto weanDto = (DoctorWeanDto) inputDto;
        DoctorPigEvent lastFarrow = doctorPigEventDao.queryLastFarrowing(weanDto.getPigId());
        expectTrue(notNull(lastFarrow), "last.farrow.not.null", inputDto.getPigId());
        //分娩时间
        DateTime farrowingDate = new DateTime(lastFarrow.getEventAt());

        //断奶时间
        DateTime partWeanDate = new DateTime(weanDto.eventAt());
        doctorPigEvent.setPartweanDate(partWeanDate.toDate());

        //哺乳天数
        doctorPigEvent.setFeedDays(Math.abs(Days.daysBetween(farrowingDate, partWeanDate).getDays()));

        //断奶只数和断奶均重
        doctorPigEvent.setWeanCount(weanDto.getPartWeanPigletsCount());
        doctorPigEvent.setWeanAvgWeight(weanDto.getPartWeanAvgWeight());

        Integer quaQty = doctorPigEvent.getWeanCount();
        if (weanDto.getQualifiedCount() != null) {
            quaQty = weanDto.getQualifiedCount();
        }
        doctorPigEvent.setHealthCount(quaQty);    //额 这个字段存一下合格数吧
        doctorPigEvent.setWeakCount(doctorPigEvent.getWeanCount() - quaQty);

        DoctorPigTrack pigTrack = doctorPigTrackDao.findByPigId(doctorPigEvent.getPigId());
        expectTrue(notNull(pigTrack), "pig.track.not.null", inputDto.getPigId());
        expectTrue(notNull(pigTrack.getGroupId()), "farrow.groupId.not.null", weanDto.getPigId());
        doctorPigEvent.setGroupId(pigTrack.getGroupId());   //必须设置下断奶的groupId
        return doctorPigEvent;
    }

    @Override
    protected DoctorPigTrack buildPigTrack(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        DoctorPigTrack toTrack = super.buildPigTrack(executeEvent, fromTrack);
        expectTrue(Objects.equals(toTrack.getStatus(), PigStatus.FEED.getKey()), "sow.status.not.feed", PigStatus.from(toTrack.getStatus()).getName());

        //未断奶数
        Integer unweanCount = toTrack.getUnweanQty();    //未断奶数量
        Integer weanCount = toTrack.getWeanQty();        //断奶数量
        Integer toWeanCount = executeEvent.getWeanCount();
        expectTrue(Objects.equals(toWeanCount,unweanCount), "need.all.wean", toWeanCount, unweanCount);
        toTrack.setUnweanQty(unweanCount - toWeanCount); //未断奶数减
        toTrack.setWeanQty(weanCount + toWeanCount);     //断奶数加

        //断奶均重
        Double toWeanAvgWeight = executeEvent.getWeanAvgWeight();
        Double weanAvgWeight = ((MoreObjects.firstNonNull(toTrack.getWeanAvgWeight(), 0D) * weanCount) + toWeanAvgWeight * toWeanCount ) / (weanCount + toWeanCount);
        toTrack.setWeanAvgWeight(weanAvgWeight);

        //设置下此时的猪群id，下面肯能会把它刷掉
        //basic.setWeanGroupId(doctorPigTrack.getGroupId());

        //全部断奶后, 初始化所有本次哺乳的信息
        if (toTrack.getUnweanQty() == 0) {
            toTrack.setStatus(PigStatus.Wean.getKey());
            toTrack.setGroupId(-1L);  //groupId = -1 置成 NULL
            toTrack.setFarrowAvgWeight(0D);
            toTrack.setFarrowQty(0);  //分娩数 0
            toTrack.setWeanAvgWeight(0D);
        }
        return toTrack;
    }

    @Override
    protected void triggerEvent(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack) {
        DoctorWeanDto partWeanDto = JSON_MAPPER.fromJson(doctorPigEvent.getExtra(), DoctorWeanDto.class);
        //触发猪群断奶事件
        DoctorWeanGroupInput input = (DoctorWeanGroupInput) buildTriggerGroupEventInput(doctorPigEvent);
        doctorCommonGroupEventHandler.sowWeanGroupEvent(doctorEventInfoList, input);
        //触发转舍事件
        if (Objects.equals(partWeanDto.getPartWeanPigletsCount(), partWeanDto.getFarrowingLiveCount()) && partWeanDto.getChgLocationToBarnId() != null) {
            DoctorBarn doctorBarn = doctorBarnDao.findById(partWeanDto.getChgLocationToBarnId());
            DoctorChgLocationDto chgLocationDto = DoctorChgLocationDto.builder()
                    .changeLocationDate(partWeanDto.getPartWeanDate())
                    .chgLocationFromBarnId(partWeanDto.getBarnId())
                    .chgLocationFromBarnName(partWeanDto.getBarnName())
                    .chgLocationToBarnId(partWeanDto.getChgLocationToBarnId())
                    .chgLocationToBarnName(doctorBarn.getName())
                    .build();
            buildAutoEventCommonInfo(partWeanDto, chgLocationDto, PigEvent.CHG_LOCATION, doctorPigEvent.getId());
            //构建basic
            DoctorBasicInputInfoDto basic = DoctorBasicInputInfoDto.builder()
                    .orgId(doctorPigEvent.getOrgId())
                    .orgName(doctorPigEvent.getOrgName())
                    .farmId(doctorPigEvent.getFarmId())
                    .farmName(doctorPigEvent.getFarmName())
                    .staffId(doctorPigEvent.getOperatorId())
                    .staffName(doctorPigEvent.getOperatorName())
                    .build();
            chgLocationHandler.handle(doctorEventInfoList, chgLocationHandler.buildPigEvent(basic, chgLocationDto), doctorPigTrack);
        }

        //更新猪群信息
//        updateGroupInfo(doctorPigEvent);
    }

    @Override
    public BaseGroupInput buildTriggerGroupEventInput(DoctorPigEvent pigEvent) {
        DoctorWeanDto partWeanDto = JSON_MAPPER.fromJson(pigEvent.getExtra(), DoctorWeanDto.class);
        DoctorWeanGroupInput input = new DoctorWeanGroupInput();
        input.setPartWeanDate(partWeanDto.getPartWeanDate());
        input.setPartWeanPigletsCount(partWeanDto.getPartWeanPigletsCount());
        input.setPartWeanAvgWeight(partWeanDto.getPartWeanAvgWeight());
        input.setQualifiedCount(partWeanDto.getQualifiedCount());
        input.setNotQualifiedCount(partWeanDto.getNotQualifiedCount());
        input.setGroupId(pigEvent.getGroupId());
        input.setEventAt(DateUtil.toDateString(pigEvent.getEventAt()));
        input.setIsAuto(1);
        input.setCreatorId(pigEvent.getCreatorId());
        input.setCreatorName(pigEvent.getCreatorName());
        input.setRelPigEventId(pigEvent.getId());
        return input;
    }


    private void updateGroupInfo(DoctorPigEvent doctorPigEvent) {
        Long farrowGroupId = doctorPigEvent.getGroupId();
        log.info("updateGroupInfo farrow group track, groupId:{}, event:{}", farrowGroupId, doctorPigEvent);

        //触发一下修改猪群的事件
        DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(farrowGroupId);
        expectTrue(notNull(groupTrack), "farrow.group.track.not.null", farrowGroupId);
        groupTrack.setQuaQty(EventUtil.plusInt(groupTrack.getQuaQty(), doctorPigEvent.getHealthCount()));
        groupTrack.setUnqQty(EventUtil.plusInt(groupTrack.getUnqQty(), doctorPigEvent.getWeakCount()));
        groupTrack.setWeanQty(EventUtil.plusInt(groupTrack.getWeanQty(), doctorPigEvent.getWeanCount()));
        groupTrack.setUnweanQty(EventUtil.plusInt(groupTrack.getUnweanQty(), -doctorPigEvent.getWeanCount()));
        groupTrack.setWeanWeight(EventUtil.plusDouble(groupTrack.getWeanWeight(), doctorPigEvent.getWeanAvgWeight() * doctorPigEvent.getWeanCount()));
        doctorGroupTrackDao.update(groupTrack);
    }
}
