package io.terminus.doctor.event.editHandler.group;

import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 22:05 17/3/8
 */

@Slf4j
@Component
public class DoctorEditMoveInGroupEventHandler extends DoctorAbstractEditGroupEventHandler{

    @Override
    protected void handlerGroupEvent(DoctorGroupTrack doctorGroupTrack, DoctorGroupEvent doctorGroupEvent, DoctorGroupEvent preDoctorGroupEvent) {


        DoctorMoveInGroupEvent doctorMoveInGroupEvent = JSON_MAPPER.fromJson(doctorGroupEvent.getExtra(), DoctorMoveInGroupEvent.class);
        //1.更新猪群跟踪
        Integer oldQty = doctorGroupTrack.getQuantity();
        doctorGroupTrack.setQuantity(EventUtil.plusInt(doctorGroupTrack.getQuantity(), doctorGroupEvent.getQuantity()));
        doctorGroupTrack.setBoarQty(EventUtil.plusInt(doctorGroupTrack.getBoarQty(), doctorMoveInGroupEvent.getBoarQty()));
        doctorGroupTrack.setSowQty(doctorGroupTrack.getQuantity() - doctorGroupTrack.getBoarQty());

        //空降产房仔猪，断奶统计要重新计算
        if (doctorMoveInGroupEvent.getFromBarnId() == null && Objects.equals(doctorGroupEvent.getPigType(), PigType.DELIVER_SOW.getValue())) {
            doctorGroupTrack.setQuaQty(EventUtil.plusInt(doctorGroupTrack.getQuaQty(), doctorGroupEvent.getQuantity()));
            doctorGroupTrack.setWeanQty(EventUtil.plusInt(doctorGroupTrack.getWeanQty(), doctorGroupEvent.getQuantity()));
            doctorGroupTrack.setWeanWeight(EventUtil.plusDouble(doctorGroupTrack.getWeanWeight(), doctorGroupEvent.getAvgWeight() * doctorGroupEvent.getQuantity()));
        }


        //重新计算日龄, 按照事件录入日期计算
        Integer oldAvgDayAge = doctorGroupTrack.getAvgDayAge();
        Integer avgDayAgePlus = DateUtil.getDeltaDaysAbs(preDoctorGroupEvent.getEventAt(), doctorGroupEvent.getEventAt());
        Integer newAvgDayAge = EventUtil.getAvgDayAge(oldAvgDayAge + avgDayAgePlus, oldQty, doctorGroupEvent.getAvgDayAge(), doctorGroupEvent.getQuantity());
        doctorGroupTrack.setAvgDayAge(newAvgDayAge);

        //如果是母猪分娩转入或母猪转舍转入，窝数，分娩统计字段需要累加
        if (doctorGroupEvent.getIsAuto() == IsOrNot.YES.getValue()) {
            doctorGroupTrack.setNest(EventUtil.plusInt(doctorGroupTrack.getNest(), 1));  //窝数加 1
            doctorGroupTrack.setLiveQty(EventUtil.plusInt(doctorGroupTrack.getLiveQty(), doctorGroupEvent.getQuantity()));
            doctorGroupTrack.setWeakQty(EventUtil.plusInt(doctorGroupTrack.getWeakQty(), doctorMoveInGroupEvent.getWeakQty()));
            doctorGroupTrack.setHealthyQty(doctorGroupTrack.getLiveQty() - doctorGroupTrack.getWeakQty());    //健仔数 = 活仔数 - 弱仔数
            doctorGroupTrack.setUnweanQty(EventUtil.plusInt(doctorGroupTrack.getUnweanQty(), doctorGroupEvent.getQuantity()));    //分娩时，未断奶数累加
            doctorGroupTrack.setBirthWeight(EventUtil.plusDouble(doctorGroupTrack.getBirthWeight(), doctorGroupEvent.getWeight()));
        }


        doctorGroupTrack.setUpdatorId(doctorGroupEvent.getCreatorId());
        doctorGroupTrack.setUpdatorName(doctorGroupEvent.getCreatorName());
        doctorGroupTrack.setSex(DoctorGroupTrack.Sex.MIX.getValue());
        doctorGroupTrack.setBirthDate(new DateTime(doctorGroupEvent.getEventAt()).plusDays(1 - doctorGroupTrack.getAvgDayAge()).toDate());

    }
}
