package io.terminus.doctor.event.handler.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorSowMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.enums.BoarEntryType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.manager.DoctorGroupManager;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorPigEventWriteService;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.doctor.common.utils.RespHelper.orServEx;

/**
 * Desc: 通用事件处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/20
 */
@Slf4j
@Component
public class DoctorCommonGroupEventHandler {

    private final DoctorCloseGroupEventHandler doctorCloseGroupEventHandler;
    private final DoctorMoveInGroupEventHandler doctorMoveInGroupEventHandler;
    private final DoctorGroupReadService doctorGroupReadService;
    private final DoctorGroupManager doctorGroupManager;
    private final DoctorPigEventWriteService doctorPigEventWriteService;

    @Autowired
    public DoctorCommonGroupEventHandler(DoctorCloseGroupEventHandler doctorCloseGroupEventHandler,
                                         DoctorMoveInGroupEventHandler doctorMoveInGroupEventHandler,
                                         DoctorGroupReadService doctorGroupReadService,
                                         DoctorGroupManager doctorGroupManager,
                                         DoctorPigEventWriteService doctorPigEventWriteService) {
        this.doctorCloseGroupEventHandler = doctorCloseGroupEventHandler;
        this.doctorMoveInGroupEventHandler = doctorMoveInGroupEventHandler;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorGroupManager = doctorGroupManager;
        this.doctorPigEventWriteService = doctorPigEventWriteService;
    }

    /**
     * 系统触发的自动关闭猪群事件
     */
    public void autoGroupEventClose(DoctorGroup group, DoctorGroupTrack groupTrack, BaseGroupInput baseInput) {
        DoctorCloseGroupInput closeInput = new DoctorCloseGroupInput();
        closeInput.setIsAuto(IsOrNot.YES.getValue());   //系统触发事件, 属于自动生成
        closeInput.setEventAt(baseInput.getEventAt());
        doctorCloseGroupEventHandler.handle(group, groupTrack, closeInput);
    }

    /**
     * 系统触发的自动转入转入猪群事件(群间转移, 转群/转场触发)
     */
    public void autoTransEventMoveIn(DoctorGroup fromGroup, DoctorGroupTrack fromGroupTrack, DoctorTransGroupInput transGroup) {
        DoctorMoveInGroupInput moveIn = new DoctorMoveInGroupInput();
        moveIn.setEventAt(transGroup.getEventAt());
        moveIn.setIsAuto(IsOrNot.YES.getValue());
        moveIn.setCreatorId(transGroup.getCreatorId());
        moveIn.setCreatorName(transGroup.getCreatorName());

        moveIn.setInType(DoctorMoveInGroupEvent.InType.GROUP.getValue());       //转入类型
        moveIn.setInTypeName(DoctorMoveInGroupEvent.InType.GROUP.getDesc());
        moveIn.setSource(transGroup.getSource());                 //来源可以分为 本场(转群), 外场(转场)
        moveIn.setSex(fromGroupTrack.getSex());
        moveIn.setBreedId(transGroup.getBreedId());
        moveIn.setBreedName(transGroup.getBreedName());
        moveIn.setFromBarnId(fromGroup.getCurrentBarnId());         //来源猪舍
        moveIn.setFromBarnName(fromGroup.getCurrentBarnName());
        moveIn.setFromGroupId(fromGroup.getId());                   //来源猪群
        moveIn.setFromGroupCode(fromGroup.getGroupCode());
        moveIn.setQuantity(transGroup.getQuantity());
        moveIn.setBoarQty(transGroup.getBoarQty());
        moveIn.setSowQty(transGroup.getSowQty());
        moveIn.setAvgDayAge(fromGroupTrack.getAvgDayAge());     //日龄
        moveIn.setAvgWeight(EventUtil.getAvgWeight(transGroup.getWeight(), transGroup.getQuantity()));  //转入均重

        //调用转入猪群事件
        DoctorGroupDetail groupDetail = RespHelper.orServEx(doctorGroupReadService.findGroupDetailByGroupId(transGroup.getToGroupId()));
        doctorMoveInGroupEventHandler.handleEvent(groupDetail.getGroup(), groupDetail.getGroupTrack(), moveIn);
    }

    /**
     * 母猪事件触发的仔猪转入猪群事件, 同时新建猪群
     * @param input 录入信息
     * @return 创建的猪群id
     */
    @Transactional
    public Long sowGroupEventMoveIn(DoctorSowMoveInGroupInput input) {
        input.setIsAuto(IsOrNot.YES.getValue());    //设置为自动事件
        input.setRemark(notEmpty(input.getRemark()) ? input.getRemark() : "系统自动生成的从母猪舍转入猪群的仔猪转入事件");

        //1. 转换新建猪群字段
        DoctorGroup group = BeanMapper.map(input, DoctorGroup.class);
        group.setRemark(null);  //dozer不需要转换remark
        group.setStaffId(input.getCreatorId());
        group.setStaffName(input.getCreatorName());

        //2. 转换录入信息字段
        DoctorNewGroupInput newGroupInput = BeanMapper.map(input, DoctorNewGroupInput.class);
        newGroupInput.setBarnId(input.getToBarnId());
        newGroupInput.setBarnName(input.getToBarnName());
        DoctorMoveInGroupInput moveIn = BeanMapper.map(input, DoctorMoveInGroupInput.class);

        //3. 新建猪群事件
        Long groupId = doctorGroupManager.createNewGroup(group, newGroupInput);

        //4. 转入猪群事件
        DoctorGroupDetail groupDetail = RespHelper.orServEx(doctorGroupReadService.findGroupDetailByGroupId(groupId));
        doctorMoveInGroupEventHandler.handleEvent(groupDetail.getGroup(), groupDetail.getGroupTrack(), moveIn);
        return groupId;
    }

    /**
     * 商品猪转种猪触发的猪进场事件
     * @param sex    本次性别
     * @param input
     * @param group
     * @param barn
     */
    public void autoPigEntryEvent(DoctorPig.PIG_TYPE sex, DoctorTurnSeedGroupInput input, DoctorGroup group, DoctorBarn barn) {
        DoctorBasicInputInfoDto basicDto = new DoctorBasicInputInfoDto();
        DoctorFarmEntryDto farmEntryDto = new DoctorFarmEntryDto();

        ///恭母猪进场字段
        if (Objects.equals(sex, DoctorPig.PIG_TYPE.BOAR)) {
            basicDto.setPigType(DoctorPig.PIG_TYPE.BOAR.getKey());
            farmEntryDto.setBoarTypeId(BoarEntryType.HGZ.getKey());
            farmEntryDto.setBoarTypeName(BoarEntryType.HGZ.getCode());
        } else {
            basicDto.setPigType(DoctorPig.PIG_TYPE.SOW.getKey());
            farmEntryDto.setParity(1);
            farmEntryDto.setEarCode(input.getEarCode());
        }

        //基本信息
        basicDto.setPigCode(input.getPigCode());
        basicDto.setBarnId(barn.getId());
        basicDto.setBarnName(barn.getName());
        basicDto.setFarmId(group.getFarmId());
        basicDto.setFarmName(group.getFarmName());
        basicDto.setOrgId(group.getOrgId());
        basicDto.setOrgName(group.getOrgName());
        basicDto.setEventType(PigEvent.ENTRY.getKey());
        basicDto.setEventName(PigEvent.ENTRY.getName());
        basicDto.setEventDesc(PigEvent.ENTRY.getDesc());
        basicDto.setStaffId(input.getCreatorId());
        basicDto.setStaffName(input.getCreatorName());

        //进场信息
        farmEntryDto.setPigType(basicDto.getPigType());
        farmEntryDto.setPigCode(input.getPigCode());
        farmEntryDto.setBirthday(DateUtil.toDate(input.getBirthDate()));
        farmEntryDto.setInFarmDate(DateUtil.toDate(input.getEventAt()));
        farmEntryDto.setBarnId(barn.getId());
        farmEntryDto.setBarnName(barn.getName());
        farmEntryDto.setSource(PigSource.LOCAL.getKey());
        farmEntryDto.setBreed(input.getBreedId());
        farmEntryDto.setBreedName(input.getBreedName());
        farmEntryDto.setBreedType(input.getGeneticId());
        farmEntryDto.setBreedTypeName(input.getGeneticName());
        farmEntryDto.setMotherCode(input.getMotherEarCode());
        farmEntryDto.setEarCode(input.getEarCode());

        orServEx(doctorPigEventWriteService.pigEntryEvent(basicDto, farmEntryDto));
    }
}
