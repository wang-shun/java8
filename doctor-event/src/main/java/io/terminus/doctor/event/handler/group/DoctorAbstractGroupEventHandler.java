package io.terminus.doctor.event.handler.group;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.event.DoctorGroupCountEvent;
import io.terminus.doctor.event.handler.DoctorGroupEventHandler;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
public abstract class DoctorAbstractGroupEventHandler implements DoctorGroupEventHandler {

    protected static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private final DoctorGroupSnapshotDao doctorGroupSnapshotDao;
    private final DoctorGroupTrackDao doctorGroupTrackDao;
    private final CoreEventDispatcher coreEventDispatcher;
    private final DoctorGroupEventDao doctorGroupEventDao;

    @Autowired(required = false)
    private Publisher publisher;

    @Autowired
    public DoctorAbstractGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                           DoctorGroupTrackDao doctorGroupTrackDao,
                                           CoreEventDispatcher coreEventDispatcher,
                                           DoctorGroupEventDao doctorGroupEventDao) {
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.coreEventDispatcher = coreEventDispatcher;
        this.doctorGroupEventDao = doctorGroupEventDao;
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
        event.setDesc(getDesc(eventType, baseInput.getIsAuto()));
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

    //获取旧镜像
    protected DoctorGroupSnapShotInfo getOldSnapShotInfo(DoctorGroup group, DoctorGroupTrack groupTrack) {
        DoctorGroupEvent event = doctorGroupEventDao.findById(groupTrack.getRelEventId());
        return new DoctorGroupSnapShotInfo(
                BeanMapper.map(group, DoctorGroup.class),
                BeanMapper.map(event, DoctorGroupEvent.class),
                BeanMapper.map(groupTrack, DoctorGroupTrack.class));
    }

    //创建猪群镜像信息
    protected void createGroupSnapShot(DoctorGroupSnapShotInfo oldShot, DoctorGroupSnapShotInfo newShot, GroupEventType eventType) {
        DoctorGroupSnapshot groupSnapshot = new DoctorGroupSnapshot();
        groupSnapshot.setEventType(eventType.getValue());  //猪群事件类型

        //录入前的数据
        groupSnapshot.setFromGroupId(oldShot.getGroup().getId());
        groupSnapshot.setFromEventId(oldShot.getGroupEvent().getId());
        groupSnapshot.setFromInfo(JSON_MAPPER.toJson(DoctorGroupSnapShotInfo.builder()
                .group(oldShot.getGroup())
                .groupEvent(oldShot.getGroupEvent())
                .groupTrack(oldShot.getGroupTrack())
                .build()));

        //录入后的数据
        groupSnapshot.setToGroupId(newShot.getGroup().getId());
        groupSnapshot.setToEventId(newShot.getGroupEvent().getId());
        groupSnapshot.setToInfo(JSON_MAPPER.toJson(DoctorGroupSnapShotInfo.builder()
                .group(newShot.getGroup())
                .groupEvent(newShot.getGroupEvent())
                .groupTrack(newShot.getGroupTrack())
                .build()));
        doctorGroupSnapshotDao.create(groupSnapshot);
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

    private String getDesc(GroupEventType eventType, Integer isAuto) {
        if (Objects.equals(isAuto, IsOrNot.YES.getValue())) {
            return "系统自动生成的" + eventType.getDesc() + "事件";
        }
        return "手工录入的" + eventType.getDesc() + "事件";
    }

    //品种校验, 如果猪群的品种已经确定, 那么录入的品种必须和猪群的品种一致
    protected static void checkBreed(Long groupBreedId, Long breedId) {
        if (notNull(groupBreedId) && notNull(breedId) && !groupBreedId.equals(breedId)) {
            throw new ServiceException("breed.not.equal");
        }
    }

    //日龄校验, 如果日龄相差超过100天, 则不允许转群
    protected void checkDayAge(Integer dayAge, DoctorTransGroupInput input) {
        if (!Objects.equals(input.getIsCreateGroup(), IsOrNot.YES.getValue())) {
            DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(input.getToGroupId());
            if (Math.abs(dayAge - groupTrack.getAvgDayAge()) > 100) {
                throw new ServiceException("delta.dayAge.over.100");
            }
        }
    }
}
