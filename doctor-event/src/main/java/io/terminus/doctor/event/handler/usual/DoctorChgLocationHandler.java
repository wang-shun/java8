package io.terminus.doctor.event.handler.usual;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.handler.group.DoctorTransGroupEventHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.doctor.common.enums.PigType.*;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Slf4j
@Component
public class DoctorChgLocationHandler extends DoctorAbstractEventHandler{

    @Autowired
    private DoctorBarnDao doctorBarnDao;
    @Autowired
    private DoctorGroupDao doctorGroupDao;
    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired
    private DoctorTransGroupEventHandler transGroupEventHandler;

    @Override
    public void handleCheck(BasePigEventInputDto eventDto, DoctorBasicInputInfoDto basic) {
        DoctorChgLocationDto chgLocationDto = (DoctorChgLocationDto) eventDto;
        checkState(!Objects.equals(chgLocationDto.getChgLocationFromBarnId(), chgLocationDto.getChgLocationToBarnId()), "同舍不可转,猪号:" + eventDto.getPigCode());
    }

    @Override
    protected DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent =  super.buildPigEvent(basic, inputDto);
        DoctorChgLocationDto chgLocationDto = (DoctorChgLocationDto) inputDto;
        DoctorBarn fromBarn = doctorBarnDao.findById(chgLocationDto.getChgLocationFromBarnId());
        DoctorBarn toBarn = doctorBarnDao.findById(chgLocationDto.getChgLocationToBarnId());
        if (Objects.equals(fromBarn.getPigType(), PREG_SOW.getValue()) && Objects.equals(toBarn.getPigType(), PigType.DELIVER_SOW.getValue())) {
            doctorPigEvent.setType(PigEvent.TO_FARROWING.getKey());
            doctorPigEvent.setName(PigEvent.TO_FARROWING.getName());
        } else if (Objects.equals(fromBarn.getPigType(), PigType.DELIVER_SOW.getValue()) && MATING_TYPES.contains(toBarn.getPigType())) {
            doctorPigEvent.setType(PigEvent.TO_MATING.getKey());
            doctorPigEvent.setName(PigEvent.TO_MATING.getName());
        }
        return doctorPigEvent;
    }

    @Override
    protected DoctorPigTrack createOrUpdatePigTrack(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(inputDto.getPigId());
        DoctorChgLocationDto chgLocationDto = (DoctorChgLocationDto) inputDto;
        Long toBarnId = chgLocationDto.getChgLocationToBarnId();

        //校验猪舍类型是否相同, 只有同类型才可以普通转舍
        DoctorBarn fromBarn = doctorBarnDao.findById(doctorPigTrack.getCurrentBarnId());
        DoctorBarn toBarn = doctorBarnDao.findById(toBarnId);
        checkState(checkBarnTypeEqual(fromBarn, toBarn, doctorPigTrack.getStatus()), "猪舍类型不可转,猪号:" + chgLocationDto.getPigCode());
        if (Objects.equals(fromBarn.getPigType(), PREG_SOW.getValue()) && Objects.equals(toBarn.getPigType(), PigType.DELIVER_SOW.getValue())) {
            doctorPigTrack.setStatus(PigStatus.Farrow.getKey());
        } else if (Objects.equals(fromBarn.getPigType(), PigType.DELIVER_SOW.getValue()) && MATING_TYPES.contains(toBarn.getPigType())) {
            // 设置断奶到配置舍标志
            Map<String, Object> newExtraMap = Maps.newHashMap();
            newExtraMap.put("hasWeanToMating", true);
            //清空对应的Map 信息内容 （有一次生产过程）
            doctorPigTrack.setExtraMap(newExtraMap);
            //设置当前配种次数为零
            doctorPigTrack.setCurrentMatingCount(0);
        }
        doctorPigTrack.setCurrentBarnId(toBarnId);
        doctorPigTrack.setCurrentBarnName(toBarn.getName());
        doctorPigTrack.setCurrentBarnType(toBarn.getPigType());
        return doctorPigTrack;
    }

    @Override
    protected void triggerEvent(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent doctorPigEvent, DoctorPigTrack doctorPigTrack, BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        DoctorChgLocationDto chgLocationDto = (DoctorChgLocationDto) inputDto;
        DoctorBarn fromBarn = doctorBarnDao.findById(chgLocationDto.getChgLocationFromBarnId());
        DoctorBarn toBarn = doctorBarnDao.findById(chgLocationDto.getChgLocationToBarnId());
        Map<String, Object> extraMap = doctorPigTrack.getExtraMap();

        // 来源和前往都是 1 和 7 时, 仔猪也要跟着转群
        if(PigType.FARROW_TYPES.contains(fromBarn.getPigType())
                && PigType.FARROW_TYPES.contains(toBarn.getPigType())
                && doctorPigTrack.getGroupId() != null){
            Long groupId = pigletTrans(doctorEventInfoList, doctorPigTrack, basic, chgLocationDto, toBarn, doctorPigEvent.getId());

            doctorPigTrack.setExtraMap(extraMap);
            doctorPigTrack.setGroupId(groupId);  //更新猪群id
            doctorPigTrackDao.update(doctorPigTrack);
        }
    }

    //未断奶仔猪转群
    private Long pigletTrans(List<DoctorEventInfo> eventInfoList, DoctorPigTrack pigTrack, DoctorBasicInputInfoDto basic, DoctorChgLocationDto chgLocationDto, DoctorBarn doctorToBarn, Long pigEventId) {
        //未断奶仔猪id
        DoctorTransGroupInput input = new DoctorTransGroupInput();
        input.setToBarnId(doctorToBarn.getId());
        input.setToBarnName(doctorToBarn.getName());
        List<DoctorGroup> groupList = doctorGroupDao.findByCurrentBarnId(doctorToBarn.getId());
        if (notEmpty(groupList)) {
            input.setIsCreateGroup(IsOrNot.NO.getValue());
            DoctorGroup toGroup = groupList.get(0);
            input.setToGroupId(toGroup.getId());
            input.setToGroupCode(toGroup.getGroupCode());
        } else {
            input.setIsCreateGroup(IsOrNot.YES.getValue());
        }

        DoctorGroup group = doctorGroupDao.findById(pigTrack.getGroupId());
        DoctorGroupTrack groupTrack= doctorGroupTrackDao.findByGroupId(pigTrack.getGroupId());
        input.setEventAt(DateUtil.toDateString(chgLocationDto.eventAt()));
        input.setIsAuto(IsOrNot.YES.getValue());
        input.setCreatorId(basic.getStaffId());
        input.setCreatorName(basic.getStaffName());
        input.setBreedId(group.getBreedId());
        input.setBreedName(group.getBreedName());
        input.setSource(PigSource.LOCAL.getKey());
        input.setSowEvent(true);    //由母猪触发的猪群事件

        //未断奶的数量 = 总 - 断奶
        input.setQuantity(pigTrack.getUnweanQty());
        input.setBoarQty(0);
        input.setSowQty(input.getQuantity() - input.getBoarQty());
        input.setAvgWeight((MoreObjects.firstNonNull(pigTrack.getFarrowAvgWeight(), 0D)));
        input.setWeight(input.getAvgWeight() * input.getQuantity());
        input.setRelPigEventId(pigEventId);

        transGroupEventHandler.handle(eventInfoList, group, groupTrack, input);
        if (Objects.equals(input.getIsCreateGroup(), IsOrNot.YES.getValue())) {
            DoctorGroup toGroup = doctorGroupDao.findByFarmIdAndGroupCode(group.getFarmId(), input.getToGroupCode());
            return toGroup.getId();
        }
        return input.getToGroupId();
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
