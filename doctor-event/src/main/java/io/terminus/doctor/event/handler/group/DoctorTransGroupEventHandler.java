package io.terminus.doctor.event.handler.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorTransGroupEvent;
import io.terminus.doctor.event.dto.event.group.edit.BaseGroupEdit;
import io.terminus.doctor.event.dto.event.group.edit.DoctorTransEdit;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.manager.DoctorGroupManager;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Desc: 转群事件处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class DoctorTransGroupEventHandler extends DoctorAbstractGroupEventHandler {

    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorCommonGroupEventHandler doctorCommonGroupEventHandler;
    private final DoctorGroupManager doctorGroupManager;
    private final DoctorBarnReadService doctorBarnReadService;

    @Autowired
    public DoctorTransGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                        DoctorGroupTrackDao doctorGroupTrackDao,
                                        CoreEventDispatcher coreEventDispatcher,
                                        DoctorGroupEventDao doctorGroupEventDao,
                                        DoctorCommonGroupEventHandler doctorCommonGroupEventHandler,
                                        DoctorGroupManager doctorGroupManager,
                                        DoctorBarnReadService doctorBarnReadService) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, coreEventDispatcher, doctorGroupEventDao);
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorCommonGroupEventHandler = doctorCommonGroupEventHandler;
        this.doctorGroupManager = doctorGroupManager;
        this.doctorBarnReadService = doctorBarnReadService;
    }

    @Override
    protected <I extends BaseGroupInput> void handleEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        DoctorGroupSnapShotInfo oldShot = getOldSnapShotInfo(group, groupTrack);
        DoctorTransGroupInput transGroup = (DoctorTransGroupInput) input;

        //校验数量, 日龄差
        checkQuantity(groupTrack.getQuantity(), transGroup.getQuantity());
        checkQuantity(groupTrack.getBoarQty(), transGroup.getBoarQty());
        checkQuantity(groupTrack.getSowQty(), transGroup.getSowQty());
        checkQuantityEqual(transGroup.getQuantity(), transGroup.getBoarQty(), transGroup.getSowQty());
        checkDayAge(groupTrack.getAvgDayAge(), transGroup);

        //1.转换转群事件
        DoctorTransGroupEvent transGroupEvent = BeanMapper.map(transGroup, DoctorTransGroupEvent.class);
        checkBreed(group.getBreedId(), transGroupEvent.getBreedId());

        //2.创建转群事件
        DoctorGroupEvent<DoctorTransGroupEvent> event = dozerGroupEvent(group, GroupEventType.TRANS_GROUP, transGroup);
        event.setQuantity(transGroup.getQuantity());
        event.setAvgDayAge(groupTrack.getAvgDayAge());  //转群的日龄不需要录入, 直接取猪群的日龄
        event.setWeight(transGroup.getWeight());
        event.setAvgWeight(EventUtil.getAvgWeight(transGroup.getWeight(), transGroup.getQuantity()));
        event.setExtraMap(transGroupEvent);
        doctorGroupEventDao.create(event);

        Integer oldQuantity = groupTrack.getQuantity();

        //3.更新猪群跟踪
        groupTrack.setQuantity(EventUtil.minusQuantity(groupTrack.getQuantity(), transGroup.getQuantity()));
        groupTrack.setBoarQty(EventUtil.minusQuantity(groupTrack.getBoarQty(), transGroup.getBoarQty()));
        groupTrack.setSowQty(EventUtil.minusQuantity(groupTrack.getSowQty(), transGroup.getSowQty()));

        //重新计算重量
        groupTrack.setWeight(groupTrack.getWeight() - transGroup.getWeight());
        groupTrack.setAvgWeight(EventUtil.getAvgWeight(groupTrack.getWeight(), groupTrack.getQuantity()));

        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(oldShot, new DoctorGroupSnapShotInfo(group, event, groupTrack), GroupEventType.TRANS_GROUP);

        //5.判断转群数量, 如果 = 猪群数量, 触发关闭猪群事件
        if (Objects.equals(oldQuantity, transGroup.getQuantity())) {
            doctorCommonGroupEventHandler.autoGroupEventClose(group, groupTrack, transGroup);
        }

        //设置来源为本场
        transGroup.setSource(PigSource.LOCAL.getKey());

        //6.判断是否新建群,触发目标群的转入仔猪事件
        if (Objects.equals(transGroup.getIsCreateGroup(), IsOrNot.YES.getValue())) {
            //新建猪群
            Long toGroupId = autoTransGroupEventNew(group, groupTrack, transGroup);
            transGroup.setToGroupId(toGroupId);

            //转入猪群
            doctorCommonGroupEventHandler.autoTransEventMoveIn(group, groupTrack, transGroup);
        } else {
            doctorCommonGroupEventHandler.autoTransEventMoveIn(group, groupTrack, transGroup);
        }

        //发布统计事件
        publishCountGroupEvent(group.getOrgId(), group.getFarmId());
        publistGroupAndBarn(group.getId(), group.getCurrentBarnId());
    }

    @Override
    protected <E extends BaseGroupEdit> void editEvent(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorGroupEvent event, E edit) {
        DoctorTransEdit transEdit = (DoctorTransEdit) edit;

        //更新字段
        DoctorTransGroupEvent transEvent = JSON_MAPPER.fromJson(event.getExtra(), DoctorTransGroupEvent.class);

    }

    /**
     * 系统触发的自动新建猪群事件(转群触发)
     */
    private Long autoTransGroupEventNew(DoctorGroup fromGroup, DoctorGroupTrack fromGroupTrack, DoctorTransGroupInput transGroup) {
        DoctorNewGroupInput newGroupInput = new DoctorNewGroupInput();
        newGroupInput.setFarmId(fromGroup.getFarmId());
        newGroupInput.setGroupCode(transGroup.getToGroupCode());    //录入猪群号
        newGroupInput.setEventAt(transGroup.getEventAt());          //事件发生日期
        newGroupInput.setBarnId(transGroup.getToBarnId());          //转到的猪舍id
        newGroupInput.setBarnName(transGroup.getToBarnName());
        newGroupInput.setPigType(getBarn(transGroup.getToBarnId()).getPigType());    //猪类取猪舍的猪类
        newGroupInput.setSex(fromGroupTrack.getSex());
        newGroupInput.setBreedId(transGroup.getBreedId());          //品种
        newGroupInput.setBreedName(transGroup.getBreedName());
        newGroupInput.setGeneticId(fromGroup.getGeneticId());
        newGroupInput.setGeneticName(fromGroup.getGeneticName());
        newGroupInput.setSource(PigSource.LOCAL.getKey());          //来源:本场
        newGroupInput.setIsAuto(IsOrNot.YES.getValue());
        newGroupInput.setRemark(transGroup.getRemark());

        DoctorGroup toGroup = BeanMapper.map(newGroupInput, DoctorGroup.class);
        toGroup.setFarmName(fromGroup.getFarmName());
        toGroup.setOrgId(fromGroup.getId());
        toGroup.setOrgName(fromGroup.getOrgName());
        toGroup.setCreatorId(transGroup.getCreatorId());    //创建人取录入转群事件的人
        toGroup.setCreatorName(transGroup.getCreatorName());
        return doctorGroupManager.createNewGroup(toGroup, newGroupInput);
    }

    private DoctorBarn getBarn(Long barnId) {
        return RespHelper.orServEx(doctorBarnReadService.findBarnById(barnId));
    }
}
