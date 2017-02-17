package io.terminus.doctor.event.handler.group;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Dates;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.DoctorEventInfo;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.event.DoctorGroupEventListener;
import io.terminus.doctor.event.event.DoctorGroupPublishDto;
import io.terminus.doctor.event.event.ListenedGroupEvent;
import io.terminus.doctor.event.handler.DoctorGroupEventHandler;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.enums.PigType.FARROW_TYPES;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/18
 */
@Slf4j
public abstract class DoctorAbstractGroupEventHandler implements DoctorGroupEventHandler {

    protected static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    //产房仔猪允许转入的猪舍: 产房(分娩母猪舍)/保育舍
    private static final List<Integer> FARROW_ALLOW_TRANS = Lists.newArrayList(
            PigType.NURSERY_PIGLET.getValue(),
            PigType.DELIVER_SOW.getValue());

    //保育猪猪允许转入的猪舍: 保育舍/育肥舍/育种舍/后备舍(公母)
    private static final List<Integer> NURSERY_ALLOW_TRANS = Lists.newArrayList(
            PigType.NURSERY_PIGLET.getValue(),
            PigType.FATTEN_PIG.getValue(),
            PigType.RESERVE.getValue());

    //育肥猪允许转入的猪舍: 育肥舍/后备舍(公母)
    private static final List<Integer> FATTEN_ALLOW_TRANS = Lists.newArrayList(
            PigType.FATTEN_PIG.getValue(),
            PigType.RESERVE.getValue());

    protected final DoctorGroupSnapshotDao doctorGroupSnapshotDao;
    private final DoctorGroupTrackDao doctorGroupTrackDao;
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorBarnDao doctorBarnDao;

    @Autowired
    private DoctorGroupDao doctorGroupDao;

    @Autowired
    private DoctorGroupEventListener doctorGroupEventListener;

    @Autowired
    public DoctorAbstractGroupEventHandler(DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                           DoctorGroupTrackDao doctorGroupTrackDao,
                                           DoctorGroupEventDao doctorGroupEventDao,
                                           DoctorBarnDao doctorBarnDao) {
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorBarnDao = doctorBarnDao;
    }

    @Override
    public <I extends BaseGroupInput> void handle(List<DoctorEventInfo> eventInfoList, DoctorGroup group, DoctorGroupTrack groupTrack, I input) {
        handleEvent(eventInfoList, group, groupTrack, input);
        DoctorEventInfo eventInfo = DoctorEventInfo.builder()
                .businessId(group.getId())
                .businessType(DoctorEventInfo.Business_Type.GROUP.getValue())
                .eventAt(DateUtil.toDate(input.getEventAt()))
                .eventType(input.getEventType())
                .code(group.getGroupCode())
                .farmId(group.getFarmId())
                .orgId(group.getOrgId())
                .pigType(group.getPigType())
                .build();
        eventInfoList.add(eventInfo);

    }

    /**
     * 处理事件的抽象方法, 由继承的子类去实现
     * @param eventInfoList 事件信息列表 每发生一个事件记录下来
     * @param group       猪群
     * @param groupTrack  猪群跟踪
     * @param input       猪群录入
     * @param <I>         规定输入上界
     */
    protected abstract <I extends BaseGroupInput> void handleEvent(List<DoctorEventInfo> eventInfoList, DoctorGroup group, DoctorGroupTrack groupTrack, I input);

    //转换下猪群基本数据
    protected DoctorGroupEvent dozerGroupEvent(DoctorGroup group, GroupEventType eventType, BaseGroupInput baseInput) {
        DoctorGroupEvent event = new DoctorGroupEvent();
        event.setEventAt(getEventAt(baseInput.getEventAt()));
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
        event.setDesc(baseInput.generateEventDesc());
        event.setRemark(baseInput.getRemark());
        event.setRelGroupEventId(baseInput.getRelGroupEventId());
        event.setRelPigEventId(baseInput.getRelPigEventId());
        return event;
    }

    //如果当天, 要有时分秒
    private static Date getEventAt(String eventAt) {
        return eventAt.equals(DateUtil.toDateString(new Date())) ? new Date() : DateUtil.toDate(eventAt);
    }

    //更新猪群跟踪
    protected void updateGroupTrack(DoctorGroupTrack groupTrack, DoctorGroupEvent event) {
        groupTrack.setRelEventId(event.getId());    //关联此次的事件id
        groupTrack.setUpdatorId(event.getCreatorId());
        groupTrack.setUpdatorName(event.getCreatorName());
        groupTrack.setSex(DoctorGroupTrack.Sex.MIX.getValue());
        groupTrack.setBirthDate(DateTime.now().plusDays(1 - groupTrack.getAvgDayAge()).toDate());
        doctorGroupTrackDao.update(groupTrack);
    }

    //获取旧镜像
    protected DoctorGroupSnapShotInfo getOldSnapShotInfo(DoctorGroup group, DoctorGroupTrack groupTrack) {
        DoctorGroupEvent event = doctorGroupEventDao.findById(groupTrack.getRelEventId());
        if (event == null) {
            log.warn("this group has no relEventId, groupId:{}", group.getId());
            event = new DoctorGroupEvent();
        }
        return new DoctorGroupSnapShotInfo(
                BeanMapper.map(group, DoctorGroup.class),
                BeanMapper.map(event, DoctorGroupEvent.class),
                BeanMapper.map(groupTrack, DoctorGroupTrack.class));
    }

    //创建猪群镜像信息
    protected DoctorGroupSnapshot createGroupSnapShot(DoctorGroupSnapShotInfo oldShot, DoctorGroupSnapShotInfo newShot, GroupEventType eventType) {
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
        return groupSnapshot;
    }

    //校验数量
    protected static void checkQuantity(Integer max, Integer actual, String groupCode) {
        if (actual > max) {
            log.error("maxQty:{}, actualQty:{}", max, actual);
            throw new InvalidException("quantity.over.max", actual, max, groupCode);
        }
    }

    //校验 公 + 母 = 总和
    protected static void checkQuantityEqual(Integer all, Integer boar, Integer sow, String groupCode) {
        if (all != (boar + sow)) {
            log.error("allQty:{}, boarQty:{}, sowQty:{}", all, boar, sow);
            throw new InvalidException("quantity.not.equal", all, boar + sow, groupCode);
        }
    }

    //发布猪群猪舍事件(不发统计事件了，事务里套事务，事件区分不开，改成同步统计)
    protected void publistGroupAndBarn(DoctorGroupEvent event) {
        doctorGroupEventListener.handleGroupEvent(ListenedGroupEvent.builder()
                .orgId(event.getOrgId())
                .farmId(event.getFarmId())
                .groups(Lists.newArrayList(getPublishGroup(event)))
                .build());
    }

    private static DoctorGroupPublishDto getPublishGroup(DoctorGroupEvent event) {
        DoctorGroupPublishDto dto = new DoctorGroupPublishDto();
        dto.setGroupId(event.getGroupId());
        dto.setEventId(event.getId());
        dto.setEventAt(event.getEventAt());
        dto.setPigType(event.getPigType());
        return dto;
    }

    //品种校验, 如果猪群的品种已经确定, 那么录入的品种必须和猪群的品种一致
    protected static void checkBreed(Long groupBreedId, Long breedId, String groupCode) {
        if (notNull(groupBreedId) && notNull(breedId) && !groupBreedId.equals(breedId)) {
            log.error("groupBreed:{}, inBreed:{}", groupBreedId, breedId);
            throw new InvalidException("breed.not.equal", groupBreedId, breedId, groupCode);
        }
    }

    //日龄校验, 如果日龄相差超过100天, 则不允许转群
    protected void checkDayAge(Integer dayAge, DoctorTransGroupInput input) {
        if (!Objects.equals(input.getIsCreateGroup(), IsOrNot.YES.getValue())) {
            DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(input.getToGroupId());
            if (Math.abs(dayAge - groupTrack.getAvgDayAge()) > 100) {
                log.error("dayAge:{}, inDayAge:{}", dayAge, Math.abs(dayAge - groupTrack.getAvgDayAge()));
                throw new InvalidException("delta.dayAge.over.100", Math.abs(dayAge - groupTrack.getAvgDayAge()));
            }
        }
    }

    //产房(分娩母猪舍)只允许有一个猪群
    protected void  checkFarrowGroupUnique(Integer isCreateGroup, Long barnId, String groupCode) {
        if (isCreateGroup.equals(IsOrNot.YES.getValue())) {
            DoctorBarn doctorBarn = doctorBarnDao.findById(barnId);
            Integer barnType = doctorBarn.getPigType();
            //如果是分娩舍或者产房
            if (barnType.equals(PigType.DELIVER_SOW.getValue())) {
                List<DoctorGroup> groups = doctorGroupDao.findByCurrentBarnId(barnId);
                if (notEmpty(groups)) {
                    throw new InvalidException("group.count.over.1", doctorBarn.getName(), groupCode);
                }
            }
        }
    }

    //校验能否转入此舍(产房 => 产房(分娩母猪舍)/保育舍，保育舍 => 保育舍/育肥舍/育种舍，同类型可以互转)
    protected void checkCanTransBarn(Integer pigType, Long barnId, String groupCode) {
        Integer barnType = doctorBarnDao.findById(barnId).getPigType();

        //产房 => 产房(分娩母猪舍)/保育舍
        if (Objects.equals(pigType, PigType.DELIVER_SOW.getValue())) {
            barnType = 4;
            if (!FARROW_ALLOW_TRANS.contains(barnType)) {
                log.error("check can trans barn pigType:{}, barnType:{}", pigType, barnType);
                throw new InvalidException("farrow.can.not.trans", PigType.from(barnType).getDesc(), groupCode);
            }
            return;
        }
        //保育舍 => 保育舍/育肥舍/育种舍/后备舍(公母)
        if (Objects.equals(pigType, PigType.NURSERY_PIGLET.getValue())) {
            if (!NURSERY_ALLOW_TRANS.contains(barnType)) {
                log.error("check can trans barn pigType:{}, barnType:{}", pigType, barnType);
                throw new InvalidException("nursery.can.not.trans", PigType.from(barnType).getDesc(), groupCode);
            }
            return;
        }
        //育肥舍 => 育肥舍/后备舍(公母)
        if (Objects.equals(pigType, PigType.FATTEN_PIG.getValue())) {
            if (!FATTEN_ALLOW_TRANS.contains(barnType)) {
                log.error("check can trans barn pigType:{}, barnType:{}", pigType, barnType);
                throw new InvalidException("fatten.can.not.trans", PigType.from(barnType).getDesc(), groupCode);
            }
            return;
        }
        // 后备群 => 育肥舍/后备舍
        if (Objects.equals(pigType, PigType.RESERVE.getValue())) {
            if(barnType != PigType.RESERVE.getValue() && barnType != PigType.FATTEN_PIG.getValue()){
                throw new InvalidException("reserve.can.not.trans", PigType.from(barnType).getDesc(), groupCode);
            }
            return;
        }

        //其他 => 同类型
        if(!Objects.equals(pigType, barnType)) {
            log.error("check can trans barn pigType:{}, barnType:{}", pigType, barnType);
            throw new InvalidException("no.equal.type.can.not.trans", PigType.from(barnType).getDesc(), groupCode);
        }
    }

    //校验目标猪群的猪舍id与目标猪舍是否相同
    protected void checkCanTransGroup(Long toGroupId, Long toBarnId, Integer isCreate) {
        if (Objects.equals(isCreate, IsOrNot.YES.getValue())) {
            return;
        }
        if (toGroupId != null) {
            DoctorGroup toGroup = doctorGroupDao.findById(toGroupId);
            if (toGroup == null || !Objects.equals(toGroup.getCurrentBarnId(), toBarnId)) {
                log.error("check can trans group toGroupId:{}, toBarnId:{}", toGroupId, toBarnId);
                throw new ServiceException("group.toBarn.not.equal");
            }
        }
    }

    //判断内转还是外转
    protected static DoctorGroupEvent.TransGroupType getTransType(Integer inType, Integer pigType, DoctorBarn toBarn) {
        if (inType != null && !Objects.equals(inType, DoctorMoveInGroupEvent.InType.GROUP.getValue())) {
            return DoctorGroupEvent.TransGroupType.OUT;
        }
        return Objects.equals(pigType, toBarn.getPigType()) || (FARROW_TYPES.contains(pigType) && FARROW_TYPES.contains(toBarn.getPigType())) ?
                DoctorGroupEvent.TransGroupType.IN : DoctorGroupEvent.TransGroupType.OUT;
    }

    //重量如果 < 0  返回0
    protected static double getDeltaWeight(Double weight) {
        return weight == null || weight < 0 ? 0 : weight;
    }

    protected DoctorBarn getBarnById(Long barnId) {
        return doctorBarnDao.findById(barnId);
    }

    //校验产房0仔猪未断奶数量，如果还有未断奶的仔猪，转群/变动数量要限制
    protected void checkUnweanTrans(Integer pigType, Integer toType, DoctorGroupTrack groupTrack, Integer eventQty, String groupCode) {
        if (!Objects.equals(pigType, PigType.DELIVER_SOW.getValue()) || Objects.equals(pigType, toType)) {
            return;
        }
        Integer unwean = MoreObjects.firstNonNull(groupTrack.getUnweanQty(), 0);
        if (eventQty > (groupTrack.getQuantity() - unwean)) {
            throw new InvalidException("group.has.unwean", eventQty, groupTrack.getQuantity() - unwean, groupCode);
        }
    }
    //获取事件发生时，猪群的日龄
    protected static int getGroupEventAge(int groupAge, int deltaDays, String groupCode) {
        int eventAge = groupAge - deltaDays;
        if (eventAge < 0) {
            throw new InvalidException("day.age.error", groupCode);
        }
        return eventAge;
    }

    protected Date generateEventAt(Date eventAt){
        if(eventAt != null){
            Date now = new Date();
            if(DateUtil.inSameDate(eventAt, now)){
                // 如果处在今天, 则使用此刻瞬间
                return now;
            } else {
                // 如果不在今天, 则将时间置为0, 只保留日期
                return Dates.startOfDay(eventAt);
            }
        }
        return null;
    }
}
