package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.sow.DoctorPartWeanDto;
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
import org.joda.time.DateTime;
import org.joda.time.Days;
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
 * Descirbe:
 */
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
        DoctorPartWeanDto partWeanDto = (DoctorPartWeanDto) inputDto;
        DoctorPigEvent lastFarrow = doctorPigEventDao.queryLastFarrowing(partWeanDto.getPigId());
        //分娩时间
        DateTime farrowingDate = new DateTime(lastFarrow.getEventAt());

        //断奶时间
        DateTime partWeanDate = new DateTime(partWeanDto.eventAt());
        doctorPigEvent.setPartweanDate(partWeanDate.toDate());

        //哺乳天数
        doctorPigEvent.setFeedDays(Math.abs(Days.daysBetween(farrowingDate, partWeanDate).getDays()));

        //断奶只数和断奶均重
        doctorPigEvent.setWeanCount(partWeanDto.getPartWeanPigletsCount());
        doctorPigEvent.setWeanAvgWeight(partWeanDto.getPartWeanAvgWeight());

//        Integer quaQty = doctorPigEvent.getWeanCount();
//        if (extra.containsKey("qualifiedCount") && extra.get("qualifiedCount") != null) {
//            quaQty = Ints.tryParse(Objects.toString(extra.get("qualifiedCount")));
//        }
//        doctorPigEvent.setHealthCount(quaQty);    //额 这个字段存一下合格数吧
//        doctorPigEvent.setWeakCount(Ints.tryParse(Objects.toString(extra.get("notQualifiedCount"))));
        return doctorPigEvent;
    }

    @Override
    protected void triggerEvent(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        DoctorPartWeanDto partWeanDto = (DoctorPartWeanDto) inputDto;
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
        int i = 1/0;
        updateGroupInfo(doctorPigEvent, doctorPigTrack, doctorPigTrack.getGroupId(), doctorPigTrack.getCurrentBarnId());
    }

    @Override
    protected DoctorPigTrack createOrUpdatePigTrack(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPartWeanDto partWeanDto = (DoctorPartWeanDto) inputDto;
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
        extra.put("partWeanAvgWeight", weanAvgWeight);
        extra.put("partWeanPigletsCount", doctorPigTrack.getWeanQty());
        extra.put("farrowingLiveCount", doctorPigTrack.getUnweanQty());
        extra.put("hasWeanToMating", true);

        //不合格数 合格数 累加
        extra.put("notQualifiedCount", getIntFromExtra(doctorPigTrack.getExtraMap(), "notQualifiedCount") + getIntFromExtra(extra, "notQualifiedCount"));
        extra.put("qualifiedCount", getIntFromExtra(doctorPigTrack.getExtraMap(), "qualifiedCount") + getIntFromExtra(extra, "qualifiedCount"));
        doctorPigTrack.setExtraMap(extra);

        //全部断奶后, 初始化所有本次哺乳的信息
        if (doctorPigTrack.getUnweanQty() == 0) {
            doctorPigTrack.setStatus(PigStatus.Wean.getKey());
            doctorPigTrack.setGroupId(-1L);  //groupId = -1 置成 NULL
            doctorPigTrack.setFarrowAvgWeight(0D);
            doctorPigTrack.setFarrowQty(0);  //分娩数 0
            doctorPigTrack.setWeanAvgWeight(0D);
        }

        //doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }


    private static int getIntFromExtra(Map<String, Object> extraMap, String key) {
        try {
            return Integer.valueOf(String.valueOf(extraMap.get(key)));
        } catch (Exception e) {
            return 0;
        }
    }

    private void updateGroupInfo(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, Long farrowGroupId, Long farrowBarnId) {
        //触发一下修改猪群的事件
        DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(farrowGroupId);
        if (groupTrack != null) {
            groupTrack.setQuaQty(EventUtil.plusInt(groupTrack.getQuaQty(), doctorPigEvent.getHealthCount()));
            groupTrack.setUnqQty(EventUtil.plusInt(groupTrack.getUnqQty(), doctorPigEvent.getWeakCount()));
            groupTrack.setWeanQty(EventUtil.plusInt(groupTrack.getWeanQty(), doctorPigEvent.getWeanCount()));
            groupTrack.setUnweanQty(EventUtil.plusInt(groupTrack.getUnweanQty(), -doctorPigEvent.getWeanCount()));
            groupTrack.setWeanWeight(EventUtil.plusDouble(groupTrack.getWeanWeight(), doctorPigEvent.getWeanAvgWeight() * doctorPigEvent.getWeanCount()));
            doctorGroupTrackDao.update(groupTrack);
        }
    }
}
