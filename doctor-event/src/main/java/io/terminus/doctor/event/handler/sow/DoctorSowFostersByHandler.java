package io.terminus.doctor.event.handler.sow;

import com.google.common.base.MoreObjects;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.sow.DoctorFosterByDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFostersDto;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by yaoqijun.
 * Date:2016-06-08
 * Email:yaoqj@terminus.io
 * Descirbe:  被拼窝的母猪事件信息的录入
 */
@Component
@Slf4j
public class DoctorSowFostersByHandler extends DoctorAbstractEventHandler {

    @Autowired
    private DoctorGroupWriteService doctorGroupWriteService;

    @Autowired
    private DoctorGroupReadService doctorGroupReadService;

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Override
    protected DoctorPigTrack createOrUpdatePigTrack(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorFosterByDto fosterByDto = (DoctorFosterByDto) inputDto;
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(fosterByDto.getPigId());

        checkState(Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey()) ||
                Objects.equals(doctorPigTrack.getStatus(), PigStatus.Wean.getKey()), "foster.currentSowStatus.error");


        Long groupId = doctorPigTrack.getGroupId();

        //被拼窝数量
        Integer fosterCount = fosterByDto.getFosterByCount();

        doctorPigTrack.setGroupId(groupId);  //groupId = -1 置成 NULL
        doctorPigTrack.setUnweanQty(MoreObjects.firstNonNull(doctorPigTrack.getUnweanQty(), 0) + fosterCount);  //未断奶数
        doctorPigTrack.setWeanQty(MoreObjects.firstNonNull(doctorPigTrack.getWeanQty(), 0));    //断奶数不变

        Map<String, Object> extra = doctorPigTrack.getExtraMap();
        extra.put("farrowingLiveCount", doctorPigTrack.getUnweanQty());
        extra.put("farrowingPigletGroupId", groupId);
        doctorPigTrack.setExtraMap(extra);

        // 修改当前的母猪状态信息
        doctorPigTrack.setStatus(PigStatus.FEED.getKey());
        //doctorPigTrack.addPigEvent(basic.getPigType(), pigEventId);
        return doctorPigTrack;
    }

    @Override
    protected void triggerEvent(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        DoctorFosterByDto fosterByDto = (DoctorFosterByDto) inputDto;
        DoctorPigTrack fromPigTrack = doctorPigTrackDao.findByPigId(fosterByDto.getFromSowId());
        //如果不是一个猪舍的拼窝，需要转群操作
        if (!Objects.equals(doctorPigTrack.getCurrentBarnId(), fromPigTrack.getCurrentBarnId())) {
            Long groupId = groupSowEventCreate(doctorPigTrack, basic, inputDto, doctorPigEvent.getId());
            doctorPigTrack.setGroupId(groupId);
            doctorPigTrackDao.update(doctorPigTrack);
        }

    }

    /**
     * 对应的仔猪转群操作
     *
     * @param basicInputInfoDto
     */
    private Long groupSowEventCreate(DoctorPigTrack doctorPigTrack, DoctorBasicInputInfoDto basicInputInfoDto, BasePigEventInputDto inputDto, Long pigEventId) {
        DoctorFostersDto fostersDto = (DoctorFostersDto) inputDto;
        //拼窝的数据extra
        Long fromSowId = fostersDto.getPigId();
        DoctorPigTrack fromSowTrack = doctorPigTrackDao.findByPigId(fromSowId);
        Long fromGroupId = fromSowTrack.getGroupId();

        //被拼窝的数据extra
        Long toGroupId = doctorPigTrack.getGroupId();
        String toGroupCode = RespHelper.orServEx(doctorGroupReadService.findGroupById(toGroupId)).getGroupCode();

        // 构建Input 信息
        DoctorTransGroupInput doctorTransGroupInput = new DoctorTransGroupInput();
        doctorTransGroupInput.setToBarnId(doctorPigTrack.getCurrentBarnId());
        doctorTransGroupInput.setToBarnName(doctorPigTrack.getCurrentBarnName());
        doctorTransGroupInput.setToGroupId(toGroupId);
        doctorTransGroupInput.setToGroupCode(toGroupCode);

        if (Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey())) {
            doctorTransGroupInput.setIsCreateGroup(0);
        } else {
            doctorTransGroupInput.setIsCreateGroup(1);
        }

        doctorTransGroupInput.setQuantity(fostersDto.getFostersCount());
        doctorTransGroupInput.setBoarQty(fostersDto.getBoarFostersCount());
        doctorTransGroupInput.setSowQty(doctorTransGroupInput.getQuantity() - doctorTransGroupInput.getBoarQty());
        doctorTransGroupInput.setWeight(fostersDto.getFosterTotalWeight());
        doctorTransGroupInput.setAvgWeight(doctorTransGroupInput.getWeight() / doctorTransGroupInput.getQuantity());    //均重
        doctorTransGroupInput.setEventAt(DateTime.now().toString(DTF));
        doctorTransGroupInput.setIsAuto(1);
        doctorTransGroupInput.setCreatorId(basicInputInfoDto.getStaffId());
        doctorTransGroupInput.setCreatorName(basicInputInfoDto.getStaffName());
        doctorTransGroupInput.setRelPigEventId(pigEventId);

        return RespHelper.orServEx(doctorGroupWriteService.groupEventTransGroup(
                RespHelper.orServEx(doctorGroupReadService.findGroupDetailByGroupId(fromGroupId)),
                doctorTransGroupInput));
    }
}
