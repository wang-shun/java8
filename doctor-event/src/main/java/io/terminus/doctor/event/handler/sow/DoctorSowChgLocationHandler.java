package io.terminus.doctor.event.handler.sow;

import com.google.common.collect.Maps;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-31
 * Email:yaoqj@terminus.io
 * Descirbe: 对应的母猪状态信息流转(转舍)
 */
@Component
public class DoctorSowChgLocationHandler extends DoctorAbstractEventHandler {

    @Autowired
    private DoctorBarnDao doctorBarnDao;

    @Override
    public DoctorPigTrack createOrUpdatePigTrack(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorChgLocationDto chgLocationDto = (DoctorChgLocationDto) inputDto;
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(chgLocationDto.getPigId());
        //doctorPigTrack.setExtraMap(chgLocationDto.toMap());

        Long toBarnId = chgLocationDto.getChgLocationToBarnId();
        DoctorBarn toBarn = doctorBarnDao.findById(toBarnId);
        checkState(!isNull(toBarnId), "input.toBarnId.fail");

        doctorPigTrack.setCurrentBarnId(toBarnId);
        doctorPigTrack.setCurrentBarnName(toBarn.getName());
        doctorPigTrack.setUpdatorId(basic.getStaffId());
        doctorPigTrack.setUpdatorName(basic.getStaffName());

        // 修改对应的状态信息
        if (Objects.equals(basic.getEventType(), PigEvent.TO_MATING.getKey())) {
            //转入配种舍,置当前配种数为0
            doctorPigTrack.setCurrentMatingCount(0);

            // 设置断奶到配置舍标志
            Map<String, Object> newExtraMap = Maps.newHashMap();
            if (Objects.equals(doctorPigTrack.getStatus(), PigStatus.Wean.getKey())) {
                newExtraMap.put("hasWeanToMating", true);
            }

            //清空对应的Map 信息内容 （有一次生产过程）
            doctorPigTrack.setExtraMap(newExtraMap);
        } else if (Objects.equals(basic.getEventType(), PigEvent.TO_FARROWING.getKey())) {
            doctorPigTrack.setStatus(PigStatus.Farrow.getKey());
        }
        //doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }
}
