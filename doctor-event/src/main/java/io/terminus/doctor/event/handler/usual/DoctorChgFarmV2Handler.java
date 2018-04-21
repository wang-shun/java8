package io.terminus.doctor.event.handler.usual;

import com.google.common.base.MoreObjects;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorChgFarmInfoDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.editHandler.pig.DoctorModifyPigChgFarmEventHandler;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorAbstractEventHandler;
import io.terminus.doctor.event.handler.group.DoctorTransGroupEventHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorChgFarmInfo;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.*;
import static io.terminus.doctor.common.enums.PigType.MATING_FARROW_TYPES;
import static io.terminus.doctor.common.enums.PigType.MATING_TYPES;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by xjn on 18/4/20.
 * email:xiaojiannan@terminus.io
 */
@Component
public class DoctorChgFarmV2Handler extends DoctorAbstractEventHandler{
    @Autowired
    private DoctorBarnDao doctorBarnDao;
    @Autowired
    private DoctorGroupDao doctorGroupDao;
    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired
    private DoctorTransGroupEventHandler transGroupEventHandler;
    @Autowired
    private DoctorModifyPigChgFarmEventHandler modifyPigChgFarmEventHandler;
    @Autowired
    private DoctorChgFarmInV2Handler doctorChgFarmInHandler;
    @Autowired
    private DoctorChgFarmInfoDao doctorChgFarmInfoDao;

    @Override
    public void handleCheck(DoctorPigEvent executeEvent, DoctorPigTrack fromTrack) {
        super.handleCheck(executeEvent, fromTrack);
        DoctorChgFarmDto chgFarmDto = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorChgFarmDto.class);
//        expectTrue(!Objects.equals(fromTrack.getStatus(), PigStatus.FEED.getKey()), "feed.sow.not.chg.farm");
        DoctorBarn doctorCurrentBarn = doctorBarnDao.findById(fromTrack.getCurrentBarnId());
        expectTrue(notNull(doctorCurrentBarn), "barn.not.null", fromTrack.getCurrentBarnId());
        DoctorBarn doctorToBarn = doctorBarnDao.findById(chgFarmDto.getToBarnId());
        expectTrue(notNull(doctorToBarn), "barn.not.null", chgFarmDto.getToBarnId());
        List<Long> barns = doctorBarnDao.findByFarmId(chgFarmDto.getToFarmId())
                .stream().map(DoctorBarn::getId).collect(Collectors.toList());
        expectTrue(barns.contains(doctorToBarn.getId()), "toBarn.not.in.toFarm");
        expectTrue(checkBarnTypeEqual(doctorCurrentBarn, doctorToBarn, fromTrack.getStatus()), "not.trans.barn.type",
                PigType.from(doctorCurrentBarn.getPigType()).getDesc(), PigType.from(doctorToBarn.getPigType()).getDesc());

    }

    @Override
    protected void specialHandle(DoctorPigEvent executeEvent, DoctorPigTrack toTrack) {
        super.specialHandle(executeEvent, toTrack);
    }

    @Override
    protected void updateDailyForNew(DoctorPigEvent newPigEvent) {
        BasePigEventInputDto inputDto = JSON_MAPPER.fromJson(newPigEvent.getExtra(), DoctorChgFarmDto.class);
        modifyPigChgFarmEventHandler.updateDailyOfNew(newPigEvent, inputDto);
    }

    @Override
    protected void triggerEvent(List<DoctorEventInfo> doctorEventInfoList, DoctorPigEvent executeEvent, DoctorPigTrack toTrack) {
        DoctorChgFarmDto doctorChgFarmDto  = JSON_MAPPER.fromJson(executeEvent.getExtra(), DoctorChgFarmDto.class);
        DoctorBarn toBarn = doctorBarnDao.findById(doctorChgFarmDto.getToBarnId());
        //构建basic
        DoctorBasicInputInfoDto basic = DoctorBasicInputInfoDto.builder()
                .orgId(toBarn.getOrgId())
                .orgName(toBarn.getOrgName())
                .farmId(toBarn.getFarmId())
                .farmName(toBarn.getFarmName())
                .staffId(executeEvent.getOperatorId())
                .staffName(executeEvent.getOperatorName())
                .build();
        DoctorChgFarmDto chgFarmIn = new DoctorChgFarmDto();
        BeanMapper.copy(doctorChgFarmDto, chgFarmIn);
        chgFarmIn.setBarnId(toBarn.getId());
        chgFarmIn.setBarnName(toBarn.getName());
        chgFarmIn.setBarnType(toBarn.getPigType());
        chgFarmIn.setEventType(PigEvent.CHG_FARM_IN.getKey());
        chgFarmIn.setEventName(PigEvent.CHG_FARM_IN.getName());
        chgFarmIn.setEventDesc(PigEvent.CHG_FARM_IN.getDesc());

        doctorChgFarmInHandler.handle(doctorEventInfoList, doctorChgFarmInHandler.buildPigEvent(basic, chgFarmIn), toTrack);

        if (Objects.equals(toTrack.getStatus(), PigStatus.FEED.getKey())) {
            Long groupId = pigletTrans(doctorEventInfoList, executeEvent, toTrack, doctorChgFarmDto, toBarn);
            toTrack.setGroupId(groupId);  //更新猪群id
            doctorPigTrackDao.update(toTrack);
        }
    }

    //未断奶仔猪转群
    private Long pigletTrans(List<DoctorEventInfo> eventInfoList, DoctorPigEvent executeEvent, DoctorPigTrack pigTrack, DoctorChgFarmDto chgLocationDto, DoctorBarn doctorToBarn) {
        expectTrue(notNull(pigTrack.getGroupId()), "farrow.groupId.not.null", pigTrack.getPigId());
        //未断奶仔猪id
        DoctorTransGroupInput input = new DoctorTransGroupInput();
        input.setSowId(chgLocationDto.getPigId());
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
            input.setToGroupCode(grateGroupCode(doctorToBarn.getName(), chgLocationDto.eventAt()));
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

    /**
     * 创建转场信息
     * @param chgFarmEvent
     * @param toTrack
     */
    private void createChgFarmInfo(DoctorPigEvent chgFarmEvent, DoctorPigTrack toTrack ) {
        DoctorPigTrack track = new DoctorPigTrack();
        BeanMapper.copy(toTrack, track);
        if (Objects.equals(chgFarmEvent.getKind(), DoctorPig.PigSex.SOW.getKey())) {
            track.setStatus(PigStatus.Removal.getKey());
        } else {
            track.setStatus(PigStatus.BOAR_LEAVE.getKey());
        }
        track.setIsRemoval(IsOrNot.YES.getValue());
        String trackJson = TO_JSON_MAPPER.toJson(track);

        DoctorPig doctorPig = doctorPigDao.findById(toTrack.getPigId());
        String pigJson = TO_JSON_MAPPER.toJson(doctorPig);

        DoctorChgFarmInfo doctorChgFarmInfo = doctorChgFarmInfoDao.findByFarmIdAndPigId(track.getFarmId(), track.getPigId());
        if (isNull(doctorChgFarmInfo)) {
            doctorChgFarmInfo = new DoctorChgFarmInfo();
            doctorChgFarmInfo.setFarmId(track.getFarmId());
            doctorChgFarmInfo.setPigId(toTrack.getPigId());
            doctorChgFarmInfo.setPigCode(doctorPig.getPigCode());
            doctorChgFarmInfo.setRfid(doctorPig.getRfid());
        }

        doctorChgFarmInfo.setBarnId(track.getCurrentBarnId());
        doctorChgFarmInfo.setEventId(chgFarmEvent.getId());
        doctorChgFarmInfo.setTrack(trackJson);
        doctorChgFarmInfo.setPig(pigJson);

        if (isNull(doctorChgFarmInfo.getId())) {
            doctorChgFarmInfoDao.create(doctorChgFarmInfo);
        } else {
            doctorChgFarmInfoDao.update(doctorChgFarmInfo);
        }
    }
}
