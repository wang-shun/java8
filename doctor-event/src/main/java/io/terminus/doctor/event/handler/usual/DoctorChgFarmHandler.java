package io.terminus.doctor.event.handler.usual;

import com.google.common.collect.Maps;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Slf4j
public class DoctorChgFarmHandler extends DoctorAbstractEventHandler{

    @Autowired
    private DoctorBarnDao doctorBarnDao;

    @Override
    protected DoctorPigTrack createOrUpdatePigTrack(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorChgFarmDto chgFarmDto = (DoctorChgFarmDto) inputDto;
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(chgFarmDto.getPigId());

        // 当前状态为哺乳状态的母猪不允许转
        checkState(!Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey()), "feedStatus.sowChangFarm.error");

        // 校验转相同的猪舍
        DoctorBarn doctorCurrentBarn = doctorBarnDao.findById(doctorPigTrack.getCurrentBarnId());
        DoctorBarn doctorToBarn = doctorBarnDao.findById(chgFarmDto.getToBarnId());
        checkState(Objects.equals(doctorCurrentBarn.getPigType(), doctorToBarn.getPigType()), "sowChgFarm.diffBarn.error");

        // update barn info
        doctorPigTrack.setFarmId(chgFarmDto.getToFarmId());
        doctorPigTrack.setCurrentBarnId(doctorToBarn.getId());
        doctorPigTrack.setCurrentBarnName(doctorToBarn.getName());

        //doctorPigTrack.addAllExtraMap(chgFarmDto.toMap());
        //doctorPigTrack.addPigEvent(basic.getPigType(), (Long) context.get("doctorPigEventId"));
        return doctorPigTrack;
    }

    @Override
    protected void specialHandle(DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        DoctorChgFarmDto chgFarmDto = (DoctorChgFarmDto) inputDto;
        // doctor pig update farm info
        DoctorPig doctorPig = doctorPigDao.findById(doctorPigTrack.getPigId());
        doctorPig.setFarmId(chgFarmDto.getToFarmId());
        doctorPig.setFarmName(chgFarmDto.getToFarmName());
        doctorPigDao.update(doctorPig);

        // 修改对应的 pig event 对应的事件信息转入对应的猪场
        Map<String,Object> params = Maps.newHashMap();
        params.put("pigId", doctorPig.getId());
        params.put("farmId", chgFarmDto.getToFarmId());
        params.put("farmName", chgFarmDto.getToFarmName());
        checkState(doctorPigEventDao.updatePigEventFarmIdByPigId(params), "chgFarm.updateEventInfo.fail");
    }
}
