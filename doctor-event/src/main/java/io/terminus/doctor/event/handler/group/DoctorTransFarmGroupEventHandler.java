package io.terminus.doctor.event.handler.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.event.group.DoctorTransFarmGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.manager.DoctorGroupManager;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Desc: 转场事件处理器
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
@Component
public class DoctorTransFarmGroupEventHandler extends DoctorAbstractGroupEventHandler {

    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorCommonGroupEventHandler doctorCommonGroupEventHandler;
    private final DoctorGroupManager doctorGroupManager;

    @Autowired
    public DoctorTransFarmGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                            DoctorGroupTrackDao doctorGroupTrackDao,
                                            CoreEventDispatcher coreEventDispatcher,
                                            DoctorGroupEventDao doctorGroupEventDao,
                                            DoctorCommonGroupEventHandler doctorCommonGroupEventHandler,
                                            DoctorGroupManager doctorGroupManager) {
        super(doctorGroupSnapshotDao, doctorGroupTrackDao, coreEventDispatcher);
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorCommonGroupEventHandler = doctorCommonGroupEventHandler;
        this.doctorGroupManager = doctorGroupManager;
    }

    @Override
    protected <I extends BaseGroupInput> void handleEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        DoctorTransFarmGroupInput transFarm = (DoctorTransFarmGroupInput) input;

        checkQuantity(groupTrack.getQuantity(), transFarm.getQuantity());
        checkQuantity(groupTrack.getBoarQty(), transFarm.getBoarQty());
        checkQuantity(groupTrack.getSowQty(), transFarm.getSowQty());
        checkQuantityEqual(transFarm.getQuantity(), transFarm.getBoarQty(), transFarm.getSowQty());

        //1.转换转场事件
        DoctorTransFarmGroupEvent transFarmEvent = BeanMapper.map(transFarm, DoctorTransFarmGroupEvent.class);

        //2.创建转场事件
        DoctorGroupEvent<DoctorTransFarmGroupEvent> event = dozerGroupEvent(group, GroupEventType.TRANS_FARM, transFarm);
        event.setQuantity(transFarm.getQuantity());
        event.setAvgDayAge(groupTrack.getAvgDayAge());  //转群的日龄不需要录入, 直接取猪群的日龄
        event.setWeight(transFarm.getWeight());
        event.setAvgWeight(EventUtil.getAvgWeight(transFarm.getWeight(), transFarm.getQuantity()));
        event.setExtraMap(transFarmEvent);
        doctorGroupEventDao.create(event);

        Integer oldQuantity = groupTrack.getQuantity();

        //3.更新猪群跟踪
        groupTrack.setQuantity(EventUtil.minusQuantity(groupTrack.getQuantity(), transFarm.getQuantity()));
        groupTrack.setBoarQty(EventUtil.minusQuantity(groupTrack.getBoarQty(), transFarm.getBoarQty()));
        groupTrack.setSowQty(EventUtil.minusQuantity(groupTrack.getSowQty(), transFarm.getSowQty()));

        //重新计算重量
        groupTrack.setWeight(groupTrack.getWeight() - transFarm.getWeight());
        groupTrack.setAvgWeight(EventUtil.getAvgWeight(groupTrack.getWeight(), groupTrack.getQuantity()));

        updateGroupTrack(groupTrack, event);

        //4.创建镜像 todo 其他字段
        createGroupSnapShot(group, event, groupTrack, GroupEventType.TRANS_FARM);

        //5.判断转场数量, 如果 = 猪群数量, 触发关闭猪群事件
        if (Objects.equals(oldQuantity, transFarm.getQuantity())) {
            doctorCommonGroupEventHandler.autoGroupEventClose(group, groupTrack, transFarm);
        }

        //设置来源为外场
        transFarm.setSource(PigSource.OUTER.getKey());

        //6.判断是否新建群,触发目标群的转入仔猪事件
        if (Objects.equals(transFarm.getIsCreateGroup(), IsOrNot.YES.getValue())) {
            //新建猪群
            Long toGroupId = autoTransFarmEventNew(group, groupTrack, transFarm);
            transFarm.setToGroupId(toGroupId);

            //转入猪群
            doctorCommonGroupEventHandler.autoTransEventMoveIn(group, groupTrack, transFarm);
        } else {
            doctorCommonGroupEventHandler.autoTransEventMoveIn(group, groupTrack, transFarm);
        }

        //发布统计事件
        publishCountGroupEvent(group.getOrgId(), group.getFarmId());
        publistGroupAndBarn(group.getId(), group.getCurrentBarnId());
    }

    /**
     * 系统触发的自动新建猪群事件(转场触发)
     */
    private Long autoTransFarmEventNew(DoctorGroup fromGroup, DoctorGroupTrack fromGroupTrack, DoctorTransFarmGroupInput transFarm) {
        DoctorNewGroupInput newGroupInput = new DoctorNewGroupInput();
        newGroupInput.setFarmId(transFarm.getToFarmId());
        newGroupInput.setGroupCode(transFarm.getToGroupCode());    //录入猪群号
        newGroupInput.setEventAt(transFarm.getEventAt());          //事件发生日期
        newGroupInput.setBarnId(transFarm.getToBarnId());          //转到的猪舍id
        newGroupInput.setBarnName(transFarm.getToBarnName());
        newGroupInput.setPigType(fromGroup.getPigType());           //猪类去原先的猪类 // TODO: 16/5/30 还是取猪舍的猪类?
        newGroupInput.setSex(fromGroupTrack.getSex());
        newGroupInput.setBreedId(transFarm.getBreedId());           //品种
        newGroupInput.setBreedName(fromGroup.getBreedName());
        newGroupInput.setGeneticId(fromGroup.getGeneticId());
        newGroupInput.setGeneticName(fromGroup.getGeneticName());
        newGroupInput.setSource(PigSource.OUTER.getKey());          //来源:外购
        newGroupInput.setIsAuto(IsOrNot.YES.getValue());
        newGroupInput.setRemark(transFarm.getRemark());

        DoctorGroup toGroup = BeanMapper.map(newGroupInput, DoctorGroup.class);
        toGroup.setFarmName(transFarm.getToFarmName());
        toGroup.setOrgId(fromGroup.getOrgId());       //转入公司
        toGroup.setOrgName(fromGroup.getOrgName());
        toGroup.setCreatorId(0L);    //创建人id = 0, 标识系统自动创建
        return doctorGroupManager.createNewGroup(toGroup, newGroupInput);
    }
}
