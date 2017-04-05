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
import io.terminus.doctor.event.handler.DoctorEventSelector;
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
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.enums.PigType.MATING_TYPES;
import static io.terminus.doctor.common.enums.PigType.PREG_SOW;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

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
    public void handleCheck(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        super.handleCheck(executeEvent, fromTrack);
        DoctorChgLocationDto chgLocationDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorChgLocationDto.class);
        expectTrue(!Objects.equals(chgLocationDto.getChgLocationFromBarnId(), chgLocationDto.getChgLocationToBarnId()), "same.barn.not.trans");
    }

    @Override
    public DoctorPigEvent buildPigEvent(DoctorBasicInputInfoDto basic, BasePigEventInputDto inputDto) {
        DoctorPigEvent doctorPigEvent =  super.buildPigEvent(basic, inputDto);
        DoctorChgLocationDto chgLocationDto = (DoctorChgLocationDto) inputDto;
        DoctorBarn fromBarn = doctorBarnDao.findById(chgLocationDto.getChgLocationFromBarnId());
        expectTrue(notNull(fromBarn), "barn.not.null", chgLocationDto.getChgLocationFromBarnId());
        DoctorBarn toBarn = doctorBarnDao.findById(chgLocationDto.getChgLocationToBarnId());
        expectTrue(notNull(toBarn), "barn.not.null", chgLocationDto.getChgLocationToBarnId());
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
    public DoctorPigTrack buildPigTrack(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        DoctorPigTrack toTrack = super.buildPigTrack(executeEvent, fromTrack);
        DoctorChgLocationDto chgLocationDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorChgLocationDto.class);
        Long toBarnId = chgLocationDto.getChgLocationToBarnId();

        //校验猪舍类型是否相同, 只有同类型才可以普通转舍
        DoctorBarn fromBarn = doctorBarnDao.findById(fromTrack.getCurrentBarnId());
        expectTrue(notNull(fromBarn), "barn.not.null", fromTrack.getCurrentBarnId());
        DoctorBarn toBarn = doctorBarnDao.findById(toBarnId);
        expectTrue(notNull(toBarn), "barn.not.null", chgLocationDto.getChgLocationToBarnId());
        expectTrue(checkBarnTypeEqual(fromBarn.getPigType(), toTrack.getStatus(), toBarn.getPigType()), "not.trans.barn.type", PigType.from(fromBarn.getPigType()).getDesc(), PigType.from(toBarn.getPigType()).getDesc());
        //expectTrue(!(Objects.equals(doctorPigTrack.getStatus(), PigStatus.FEED.getKey()) && PigType.MATING_TYPES.contains(toBarn.getPigType())), "", new Object[]{chgLocationDto.getPigCode()});

        if (Objects.equals(fromBarn.getPigType(), PREG_SOW.getValue()) && Objects.equals(toBarn.getPigType(), PigType.DELIVER_SOW.getValue())) {
            toTrack.setStatus(PigStatus.Farrow.getKey());
        } else if (Objects.equals(toTrack.getStatus(), PigStatus.Wean.getKey()) && Objects.equals(fromBarn.getPigType(), PigType.DELIVER_SOW.getValue()) && MATING_TYPES.contains(toBarn.getPigType())) {
            // 设置断奶到配置舍标志
            Map<String, Object> newExtraMap = Maps.newHashMap();
            newExtraMap.put("hasWeanToMating", true);
            //清空对应的Map 信息内容 （有一次生产过程）
            toTrack.setExtraMap(newExtraMap);
            //设置当前配种次数为零
            toTrack.setCurrentMatingCount(0);
        }
        toTrack.setCurrentBarnId(toBarnId);
        toTrack.setCurrentBarnName(toBarn.getName());
        toTrack.setCurrentBarnType(toBarn.getPigType());
        return toTrack;
    }

    @Override
    protected void triggerEvent(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent executeEvent, DoctorPigTrack toTrack) {
        DoctorChgLocationDto chgLocationDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorChgLocationDto.class);
        DoctorBarn fromBarn = doctorBarnDao.findById(chgLocationDto.getChgLocationFromBarnId());
        DoctorBarn toBarn = doctorBarnDao.findById(chgLocationDto.getChgLocationToBarnId());
        Map<String, Object> extraMap = toTrack.getExtraMap();

        // 来源和前往都是 1 和 7 时, 仔猪也要跟着转群
        if(PigType.FARROW_TYPES.contains(fromBarn.getPigType())
                && PigType.FARROW_TYPES.contains(toBarn.getPigType())
                && toTrack.getGroupId() != null){
            Long groupId = pigletTrans(doctorEventInfoList, executeEvent, toTrack, chgLocationDto, toBarn);

            toTrack.setExtraMap(extraMap);
            toTrack.setGroupId(groupId);  //更新猪群id
            doctorPigTrackDao.update(toTrack);
        }
    }

    //未断奶仔猪转群
    private Long pigletTrans(List<DoctorEventInfo> eventInfoList,DoctorPigEvent executeEvent, DoctorPigTrack pigTrack, DoctorChgLocationDto chgLocationDto, DoctorBarn doctorToBarn) {
        expectTrue(notNull(pigTrack.getGroupId()), "farrow.groupId.not.null", pigTrack.getPigId());
        //未断奶仔猪id
        DoctorTransGroupInput input = new DoctorTransGroupInput();
        input.setSowCode(chgLocationDto.getPigCode());
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
            input.setToGroupCode(grateGroupCode(doctorToBarn.getName()));
        }

        DoctorGroup group = doctorGroupDao.findById(pigTrack.getGroupId());
        expectTrue(notNull(group), "group.not.null", pigTrack.getGroupId());
        DoctorGroupTrack groupTrack= doctorGroupTrackDao.findByGroupId(pigTrack.getGroupId());
        expectTrue(notNull(groupTrack), "farrow.group.track.not.null", pigTrack.getGroupId());
        input.setEventAt(DateUtil.toDateString(chgLocationDto.eventAt()));
        input.setIsAuto(IsOrNot.YES.getValue());
        input.setCreatorId(executeEvent.getOperatorId());
        input.setCreatorName(executeEvent.getOperatorName());
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
        input.setRelPigEventId(executeEvent.getId());

        transGroupEventHandler.handle(eventInfoList, group, groupTrack, input);
        if (Objects.equals(input.getIsCreateGroup(), IsOrNot.YES.getValue())) {
            //DoctorGroup toGroup = doctorGroupDao.findByFarmIdAndGroupCode(group.getFarmId(), input.getToGroupCode());
            return input.getToGroupId();
        }
        return input.getToGroupId();
    }

    /**
     * 校验是否可以转舍
     * @param fromPigType 源舍
     * @param toPigType 转入舍
     * @param pigStatus 状态
     * @return 是否准许转舍
     */
    private Boolean checkBarnTypeEqual(Integer fromPigType, Integer pigStatus, Integer toPigType) {
        List<Integer> allows = DoctorEventSelector.selectBarn(PigStatus.from(pigStatus), PigType.from(fromPigType)).stream()
                .map(PigType::getValue)
                .collect(Collectors.toList());
        return allows.contains(toPigType);
    }
}
