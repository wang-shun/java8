package io.terminus.doctor.event.handler.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorSowMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.manager.DoctorGroupManager;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private DoctorCommonGroupEventHandler(DoctorCloseGroupEventHandler doctorCloseGroupEventHandler,
                                          DoctorMoveInGroupEventHandler doctorMoveInGroupEventHandler,
                                          DoctorGroupReadService doctorGroupReadService,
                                          DoctorGroupManager doctorGroupManager) {
        this.doctorCloseGroupEventHandler = doctorCloseGroupEventHandler;
        this.doctorMoveInGroupEventHandler = doctorMoveInGroupEventHandler;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorGroupManager = doctorGroupManager;
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

        //1. 转换新建猪群字段
        DoctorGroup group = BeanMapper.map(input, DoctorGroup.class);
        DoctorNewGroupInput newGroupInput = BeanMapper.map(input, DoctorNewGroupInput.class);
        DoctorMoveInGroupInput moveIn = BeanMapper.map(input, DoctorMoveInGroupInput.class);

        //2. 新建猪群
        Long groupId = doctorGroupManager.createNewGroup(group, newGroupInput);

        //3. 转入猪群
        DoctorGroupDetail groupDetail = RespHelper.orServEx(doctorGroupReadService.findGroupDetailByGroupId(groupId));
        doctorMoveInGroupEventHandler.handleEvent(groupDetail.getGroup(), groupDetail.getGroupTrack(), moveIn);
        return groupId;
    }
}
