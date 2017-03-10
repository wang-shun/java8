package io.terminus.doctor.event.handler.usual;

import io.terminus.doctor.common.enums.PigType;
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

import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.enums.PigType.MATING_FARROW_TYPES;
import static io.terminus.doctor.common.enums.PigType.MATING_TYPES;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

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
    public DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        return super.buildPigEvent(basic, inputDto);
    }

    @Override
    protected DoctorPigTrack buildPigTrack(DoctorPigEvent executeEvent, DoctorPigTrack toTrack) {
        DoctorChgFarmDto chgFarmDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorChgFarmDto.class);
        // 当前状态为哺乳状态的母猪不允许转
        expectTrue(!Objects.equals(toTrack.getStatus(), PigStatus.FEED.getKey()), "feed.sow.not.chg.farm", chgFarmDto.getPigCode());

        // 校验转相同的猪舍
        DoctorBarn doctorCurrentBarn = doctorBarnDao.findById(toTrack.getCurrentBarnId());
        expectTrue(notNull(doctorCurrentBarn), "barn.not.null", toTrack.getCurrentBarnId());
        DoctorBarn doctorToBarn = doctorBarnDao.findById(chgFarmDto.getToBarnId());
        expectTrue(notNull(doctorToBarn), "barn.not.null", chgFarmDto.getToBarnId());
        expectTrue(checkBarnTypeEqual(doctorCurrentBarn, doctorToBarn, toTrack.getStatus()), "not.trans.barn.type", PigType.from(doctorCurrentBarn.getPigType()).getDesc(), PigType.from(doctorToBarn.getPigType()).getDesc());

        // update barn info
        toTrack.setFarmId(chgFarmDto.getToFarmId());
        toTrack.setCurrentBarnId(doctorToBarn.getId());
        toTrack.setCurrentBarnName(doctorToBarn.getName());
        return toTrack;
    }

    @Override
    protected void specialHandle(DoctorPigEvent executeEvent, DoctorPigTrack toTrack) {
        DoctorPig doctorPig = doctorPigDao.findById(executeEvent.getPigId());
//        //// TODO: 17/3/9 转场有问题
//        doctorPig.setFarmId(chgFarmDto.getToFarmId());
//        doctorPig.setFarmName(chgFarmDto.getToFarmName());
//        doctorPigDao.update(doctorPig);
//
//        // 修改对应的 pig event 对应的事件信息转入对应的猪场
//        Map<String,Object> params = Maps.newHashMap();
//        params.put("pigId", doctorPig.getId());
//        params.put("farmId", chgFarmDto.getToFarmId());
//        params.put("farmName", chgFarmDto.getToFarmName());
//        doctorPigEventDao.updatePigEventFarmIdByPigId(params);
    }

    /**
     * 校验是否可以转舍
     * @param fromBarn 源舍
     * @param toBarn 转入舍
     * @param pigStatus 状态
     * @return 是否准许转舍
     */
    private Boolean checkBarnTypeEqual(DoctorBarn fromBarn, DoctorBarn toBarn, Integer pigStatus) {
        if (fromBarn == null || toBarn == null) {
            return false;
        }
        return (Objects.equals(fromBarn.getPigType(), toBarn.getPigType())
                || (MATING_TYPES.contains(fromBarn.getPigType()) && MATING_TYPES.contains(toBarn.getPigType()))
                || (Objects.equals(fromBarn.getPigType(), PigType.DELIVER_SOW.getValue()) && MATING_FARROW_TYPES.contains(toBarn.getPigType())))
                || Objects.equals(pigStatus, PigStatus.Pregnancy.getKey()) && MATING_FARROW_TYPES.contains(toBarn.getPigType());
    }
}
