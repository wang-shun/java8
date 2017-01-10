package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.sow.DoctorWeanDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
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
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

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

    @Override
    protected DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent = super.buildPigEvent(basic, inputDto);
        DoctorWeanDto weanDto = (DoctorWeanDto) inputDto;
        DoctorPigEvent lastFarrow = doctorPigEventDao.queryLastFarrowing(weanDto.getPigId());
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
        return doctorPigEvent;
    }

    @Override
    protected void triggerEvent(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        DoctorWeanDto partWeanDto = (DoctorWeanDto) inputDto;

        if (Objects.equals(partWeanDto.getPartWeanPigletsCount(), partWeanDto.getFarrowingLiveCount()) && partWeanDto.getChgLocationToBarnId() != null) {
            DoctorBarn doctorBarn = doctorBarnDao.findById(partWeanDto.getChgLocationToBarnId());
            DoctorChgLocationDto chgLocationDto = DoctorChgLocationDto.builder()
                    .changeLocationDate(partWeanDto.getPartWeanDate())
                    .chgLocationFromBarnId(partWeanDto.getBarnId())
                    .chgLocationFromBarnName(partWeanDto.getBarnName())
                    .chgLocationToBarnId(partWeanDto.getChgLocationToBarnId())
                    .chgLocationToBarnName(doctorBarn.getName())
                    .build();
            buildAutoEventCommonInfo(partWeanDto, chgLocationDto, basic, PigEvent.CHG_LOCATION, doctorPigEvent.getId());
            chgLocationHandler.handle(doctorEventInfoList, chgLocationDto, basic);
        }

        updateGroupInfo(doctorPigEvent, basic.getWeanGroupId());
    }

    @Override
    protected DoctorPigTrack createOrUpdatePigTrack(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorWeanDto partWeanDto = (DoctorWeanDto) inputDto;
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(partWeanDto.getPigId());

        checkState(Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey()), "not.feed.sow");

        //未断奶数
        Integer unweanCount = doctorPigTrack.getUnweanQty();    //未断奶数量
        Integer weanCount = doctorPigTrack.getWeanQty();        //断奶数量
        Integer toWeanCount = partWeanDto.getPartWeanPigletsCount();
        checkState(toWeanCount <= unweanCount, "wean.countInput.error");
        doctorPigTrack.setUnweanQty(unweanCount - toWeanCount); //未断奶数减
        doctorPigTrack.setWeanQty(weanCount + toWeanCount);     //断奶数加

        //断奶均重
        Double toWeanAvgWeight = partWeanDto.getPartWeanAvgWeight();
        checkState(toWeanAvgWeight != null, "weight.not.null");
        Double weanAvgWeight = ((MoreObjects.firstNonNull(doctorPigTrack.getWeanAvgWeight(), 0D) * weanCount) + toWeanAvgWeight * toWeanCount ) / (weanCount + toWeanCount);
        doctorPigTrack.setWeanAvgWeight(weanAvgWeight);

        Map<String, Object> extra = doctorPigTrack.getExtraMap();

        //更新extra字段
        extra.put("hasWeanToMating", true);

        doctorPigTrack.setExtraMap(extra);

        //设置下此时的猪群id，下面肯能会把它刷掉
        basic.setWeanGroupId(doctorPigTrack.getGroupId());

        //全部断奶后, 初始化所有本次哺乳的信息
        if (doctorPigTrack.getUnweanQty() == 0) {
            doctorPigTrack.setStatus(PigStatus.Wean.getKey());
            doctorPigTrack.setGroupId(-1L);  //groupId = -1 置成 NULL
            doctorPigTrack.setFarrowAvgWeight(0D);
            doctorPigTrack.setFarrowQty(0);  //分娩数 0
            doctorPigTrack.setWeanAvgWeight(0D);
        }
        return doctorPigTrack;
    }

    private void updateGroupInfo(DoctorPigEvent doctorPigEvent, Long farrowGroupId) {
        log.info("updateGroupInfo farrow group track, groupId:{}, event:{}", farrowGroupId, doctorPigEvent);

        //触发一下修改猪群的事件
        DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(farrowGroupId);
        if (groupTrack == null) {
            log.error("this farrow pig track groupId({}) not found, please check!", farrowGroupId);
            return;
        }
        groupTrack.setQuaQty(EventUtil.plusInt(groupTrack.getQuaQty(), doctorPigEvent.getHealthCount()));
        groupTrack.setUnqQty(EventUtil.plusInt(groupTrack.getUnqQty(), doctorPigEvent.getWeakCount()));
        groupTrack.setWeanQty(EventUtil.plusInt(groupTrack.getWeanQty(), doctorPigEvent.getWeanCount()));
        groupTrack.setUnweanQty(EventUtil.plusInt(groupTrack.getUnweanQty(), -doctorPigEvent.getWeanCount()));
        groupTrack.setWeanWeight(EventUtil.plusDouble(groupTrack.getWeanWeight(), doctorPigEvent.getWeanAvgWeight() * doctorPigEvent.getWeanCount()));
        doctorGroupTrackDao.update(groupTrack);
    }
}
