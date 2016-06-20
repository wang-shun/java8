package io.terminus.doctor.event.handler.group;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.event.DoctorGroupCountEvent;
import io.terminus.doctor.event.handler.DoctorGroupEventHandler;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.event.util.EventUtil;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notNull;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
public abstract class DoctorAbstractGroupEventHandler implements DoctorGroupEventHandler {

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private final DoctorGroupDao doctorGroupDao;
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorGroupSnapshotDao doctorGroupSnapshotDao;
    private final DoctorGroupTrackDao doctorGroupTrackDao;
    private final DoctorGroupReadService doctorGroupReadService;
    private final CoreEventDispatcher coreEventDispatcher;
    private final DoctorGroupWriteService doctorGroupWriteService;

    @Autowired(required = false)
    private Publisher publisher;

    @Autowired
    public DoctorAbstractGroupEventHandler(DoctorGroupDao doctorGroupDao,
                                           DoctorGroupEventDao doctorGroupEventDao,
                                           DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                           DoctorGroupTrackDao doctorGroupTrackDao,
                                           DoctorGroupReadService doctorGroupReadService,
                                           CoreEventDispatcher coreEventDispatcher,
                                           DoctorGroupWriteService doctorGroupWriteService) {
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorGroupReadService = doctorGroupReadService;
        this.coreEventDispatcher = coreEventDispatcher;
        this.doctorGroupWriteService = doctorGroupWriteService;
    }

    @Override
    public <I extends BaseGroupInput> void handle(DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        handleEvent(group, groupTrack, input);
    }

    /**
     * 处理事件的抽象方法, 由继承的子类去实现
     * @param group       猪群
     * @param groupTrack  猪群跟踪
     * @param input       猪群录入
     * @param <I>         规定输入上界
     */
    protected abstract <I extends BaseGroupInput> void handleEvent(DoctorGroup group, DoctorGroupTrack groupTrack, I input);

    //转换下猪群基本数据
    protected DoctorGroupEvent dozerGroupEvent(DoctorGroup group, GroupEventType eventType, BaseGroupInput baseInput) {
        DoctorGroupEvent event = new DoctorGroupEvent();
        event.setEventAt(DateUtil.toDate(baseInput.getEventAt()));
        event.setOrgId(group.getOrgId());       //公司信息
        event.setOrgName(group.getOrgName());
        event.setFarmId(group.getFarmId());     //猪场信息
        event.setFarmName(group.getFarmName());
        event.setGroupId(group.getId());        //猪群信息
        event.setGroupCode(group.getGroupCode());
        event.setType(eventType.getValue());    //事件类型
        event.setName(eventType.getDesc());
        event.setBarnId(group.getCurrentBarnId());      //事件发生猪舍
        event.setBarnName(group.getCurrentBarnName());
        event.setPigType(group.getPigType());           //猪类
        event.setIsAuto(baseInput.getIsAuto());
        event.setCreatorId(baseInput.getCreatorId());   //创建人
        event.setCreatorName(baseInput.getCreatorName());
        event.setDesc("todo 事件描述");
        event.setRemark(baseInput.getRemark());
        return event;
    }

    //更新猪群跟踪
    protected void updateGroupTrack(DoctorGroupTrack groupTrack, DoctorGroupEvent event) {
        groupTrack.setRelEventId(event.getId());    //关联此次的事件id
        groupTrack.setUpdatorId(event.getCreatorId());
        groupTrack.setUpdatorName(event.getCreatorName());
        groupTrack.setSex(EventUtil.getSex(groupTrack.getBoarQty(), groupTrack.getSowQty()));

        DoctorGroupTrack.Extra extra = groupTrack.getExtraEntity();
        switch (GroupEventType.from(event.getType())) {
            case MOVE_IN:
                extra.setMoveInAt(event.getEventAt());
                break;
            case CHANGE:
                extra.setChangeAt(event.getEventAt());
                break;
            case TRANS_GROUP:
                extra.setTransGroupAt(event.getEventAt());
                break;
            case TURN_SEED:
                extra.setTurnSeedAt(event.getEventAt());
                break;
            case LIVE_STOCK:
                extra.setLiveStockAt(event.getEventAt());
                break;
            case DISEASE:
                extra.setDiseaseAt(event.getEventAt());
                break;
            case ANTIEPIDEMIC:
                extra.setAntiepidemicAt(event.getEventAt());
                break;
            case TRANS_FARM:
                extra.setTransFarmAt(event.getEventAt());
                break;
            case CLOSE:
                extra.setCloseAt(event.getEventAt());
                break;
            default:
                break;
        }
        groupTrack.setExtraEntity(extra);
        doctorGroupTrackDao.update(groupTrack);
    }

    //创建猪群镜像信息
    protected void createGroupSnapShot(DoctorGroup group, DoctorGroupEvent groupEvent, DoctorGroupTrack groupTrack, GroupEventType eventType) {
        DoctorGroupSnapshot groupSnapshot = new DoctorGroupSnapshot();
        groupSnapshot.setEventType(eventType.getValue());  //猪群事件类型
        groupSnapshot.setToGroupId(group.getId());
        groupSnapshot.setToEventId(groupEvent.getId());
        groupSnapshot.setToInfo(JSON_MAPPER.toJson(DoctorGroupSnapShotInfo.builder()
                .group(group)
                .groupEvent(groupEvent)
                .groupTrack(groupTrack)
                .build()));
        doctorGroupSnapshotDao.create(groupSnapshot);
    }

    /**
     * 系统触发的自动关闭猪群事件
     */
    protected void autoGroupEventClose(DoctorGroup group, DoctorGroupTrack groupTrack, BaseGroupInput baseInput) {
        DoctorCloseGroupInput closeInput = new DoctorCloseGroupInput();
        closeInput.setIsAuto(IsOrNot.YES.getValue());   //系统触发事件, 属于自动生成
        closeInput.setEventAt(baseInput.getEventAt());
        RespHelper.orServEx(doctorGroupWriteService.groupEventClose(new DoctorGroupDetail(group, groupTrack), closeInput));
    }

    /**
     * 系统触发的自动转入转入猪群事件(群间转移, 转群/转场触发)
     */
    protected void autoTransEventMoveIn(DoctorGroup fromGroup, DoctorGroupTrack fromGroupTrack, DoctorTransGroupInput transGroup) {
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
        RespHelper.orServEx(doctorGroupWriteService.groupEventMoveIn(groupDetail, moveIn));
    }

    //校验数量
    protected static void checkQuantity(Integer max, Integer actual) {
        if (actual > max) {
            throw new ServiceException("quantity.over.max");
        }
    }

    //校验 公 + 母 = 总和
    protected static void checkQuantityEqual(Integer all, Integer boar, Integer sow) {
        if (all != (boar + sow)) {
            throw new ServiceException("quantity.not.equal");
        }
    }

    //校验猪群号是否重复
    protected void checkGroupCodeExist(Long farmId, String groupCode) {
        List<DoctorGroup> groups = RespHelper.or500(doctorGroupReadService.findGroupsByFarmId(farmId));
        if (groups.stream().map(DoctorGroup::getGroupCode).collect(Collectors.toList()).contains(groupCode)) {
            throw new ServiceException("group.code.exist");
        }
    }

    //发布统计猪群事件 // TODO: 16/6/16 可以用 publishZookeeperEvent 替代
    protected void publishCountGroupEvent(Long orgId, Long farmId) {
        coreEventDispatcher.publish(new DoctorGroupCountEvent(orgId, farmId));
    }

    //发布猪群猪舍事件
    protected void publistGroupAndBarn(Long groupId, Long barnId) {
        publishZookeeperEvent(DataEventType.GroupEventCreate.getKey(), ImmutableMap.of("doctorGroupId", groupId));
        publishZookeeperEvent(DataEventType.BarnUpdate.getKey(), ImmutableMap.of("doctorBarnId", barnId));
    }

    //发布zk事件, 用于更新es索引
    protected  <T> void publishZookeeperEvent(Integer eventType, T data) {
        if (notNull(publisher)) {
            try {
                publisher.publish(DataEvent.toBytes(eventType, data));
            } catch (Exception e) {
                log.error("publish zk event, eventType:{}, data:{} cause:{}", eventType, data, Throwables.getStackTraceAsString(e));
            }
        } else {
            coreEventDispatcher.publish(DataEvent.make(eventType, data));
        }
    }
}
