package io.terminus.doctor.event.manager;

import com.google.common.base.MoreObjects;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorAntiepidemicGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorChangeGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorCloseGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorDiseaseGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorLiveStockGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorNewGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorTransFarmGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorTransGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Slf4j
@Component
public class DoctorGroupManager {

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private final DoctorGroupDao doctorGroupDao;
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorGroupSnapshotDao doctorGroupSnapshotDao;
    private final DoctorGroupTrackDao doctorGroupTrackDao;
    private final DoctorGroupReadService doctorGroupReadService;

    @Autowired
    public DoctorGroupManager(DoctorGroupDao doctorGroupDao,
                              DoctorGroupEventDao doctorGroupEventDao,
                              DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                              DoctorGroupTrackDao doctorGroupTrackDao,
                              DoctorGroupReadService doctorGroupReadService) {
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorGroupReadService = doctorGroupReadService;
    }

    /**
     * 新建猪群
     * @param group 猪群
     * @param newGroupInput 新建猪群录入信息
     * @return 猪群id
     */
    @Transactional
    public Long createNewGroup(DoctorGroup group, DoctorNewGroupInput newGroupInput) {
        //0.校验猪群号是否重复
        checkGroupCodeExist(newGroupInput.getFarmId(), newGroupInput.getGroupCode());

        //1. 创建猪群
        doctorGroupDao.create(getNewGroup(group, newGroupInput));
        Long groupId = group.getId();

        //2. 创建新建猪群事件
        DoctorGroupEvent<DoctorNewGroupEvent> groupEvent = getNewGroupEvent(group, newGroupInput);
        doctorGroupEventDao.create(groupEvent);

        //3. 创建猪群跟踪
        DoctorGroupTrack groupTrack = BeanMapper.map(groupEvent, DoctorGroupTrack.class);
        groupTrack.setExtra(null);  //dozer不需要转换extra字段
        groupTrack.setGroupId(groupId);
        groupTrack.setRelEventId(groupEvent.getId());
        groupTrack.setBoarQty(0);
        groupTrack.setSowQty(0);
        groupTrack.setQuantity(0);
        groupTrack.setAvgDayAge(0);             //日龄
        groupTrack.setBirthDate(new Date());    //出生日期(用于计算日龄)
        groupTrack.setAvgWeight(0D);            //均重
        groupTrack.setWeight(0D);               //总重
        groupTrack.setPrice(0L);                //单价
        groupTrack.setAmount(0L);               //金额
        groupTrack.setSex(EventUtil.getSex(groupTrack.getBoarQty(), groupTrack.getSowQty()));
        doctorGroupTrackDao.create(groupTrack);

        //4. 创建猪群镜像
        createGroupSnapShot(group, groupEvent, groupTrack, GroupEventType.NEW);
        return groupId;
    }

    private DoctorGroup getNewGroup(DoctorGroup group, DoctorNewGroupInput newGroupInput) {
        //设置猪舍
        group.setInitBarnId(newGroupInput.getBarnId());
        group.setInitBarnName(newGroupInput.getBarnName());
        group.setCurrentBarnId(newGroupInput.getBarnId());
        group.setCurrentBarnName(newGroupInput.getBarnName());

        //建群时间与状态
        group.setOpenAt(DateUtil.toDate(newGroupInput.getEventAt()));
        group.setStatus(DoctorGroup.Status.CREATED.getValue());
        return group;
    }

    //构造新建猪群事件
    private DoctorGroupEvent<DoctorNewGroupEvent> getNewGroupEvent(DoctorGroup group, DoctorNewGroupInput newGroupInput) {
        DoctorGroupEvent<DoctorNewGroupEvent> groupEvent = new DoctorGroupEvent<>();

        groupEvent.setGroupId(group.getId());   //关联猪群id

        groupEvent.setOrgId(group.getOrgId());
        groupEvent.setOrgName(group.getOrgName());
        groupEvent.setFarmId(group.getFarmId());
        groupEvent.setFarmName(group.getFarmName());
        groupEvent.setGroupCode(group.getGroupCode());

        //事件信息
        groupEvent.setEventAt(group.getOpenAt());
        groupEvent.setType(GroupEventType.NEW.getValue());
        groupEvent.setName(GroupEventType.NEW.getDesc());
        groupEvent.setDesc("todo 事件描述");

        groupEvent.setBarnId(group.getInitBarnId());
        groupEvent.setBarnName(group.getInitBarnName());
        groupEvent.setPigType(group.getPigType());

        groupEvent.setIsAuto(newGroupInput.getIsAuto());
        groupEvent.setCreatorId(group.getCreatorId());
        groupEvent.setCreatorName(group.getCreatorName());
        groupEvent.setRemark(group.getRemark());

        DoctorNewGroupEvent newGroupEvent = new DoctorNewGroupEvent();
        newGroupEvent.setSource(newGroupInput.getSource());
        groupEvent.setExtraMap(newGroupEvent);
        return groupEvent;
    }

    /**
     * 防疫事件
     */
    @Transactional
    public void groupEventAntiepidemic(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorAntiepidemicGroupInput antiepidemic) {
        checkQuantity(groupTrack.getQuantity(), antiepidemic.getQuantity());
        //1.转换下防疫信息
        DoctorAntiepidemicGroupEvent antiEvent = BeanMapper.map(antiepidemic, DoctorAntiepidemicGroupEvent.class);

        //2.创建防疫事件
        DoctorGroupEvent<DoctorAntiepidemicGroupEvent> event = dozerGroupEvent(group, GroupEventType.ANTIEPIDEMIC, antiepidemic);
        event.setQuantity(antiepidemic.getQuantity());
        event.setExtraMap(antiEvent);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(group, event, groupTrack, GroupEventType.ANTIEPIDEMIC);
    }

    /**
     * 疾病事件
     */
    @Transactional
    public void groupEventDisease(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorDiseaseGroupInput disease) {
        checkQuantity(groupTrack.getQuantity(), disease.getQuantity());

        //1.转换下疾病信息
        DoctorDiseaseGroupEvent diseaseEvent = BeanMapper.map(disease, DoctorDiseaseGroupEvent.class);

        //2.创建疾病事件
        DoctorGroupEvent<DoctorDiseaseGroupEvent> event = dozerGroupEvent(group, GroupEventType.DISEASE, disease);
        event.setQuantity(disease.getQuantity());
        event.setExtraMap(diseaseEvent);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(group, event, groupTrack, GroupEventType.DISEASE);
    }

    /**
     * 关闭猪群事件
     */
    @Transactional
    public void groupEventClose(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorCloseGroupInput close) {
        //1.转换下信息
        DoctorCloseGroupEvent closeEvent = BeanMapper.map(close, DoctorCloseGroupEvent.class);

        //2.创建关闭猪群事件
        DoctorGroupEvent<DoctorCloseGroupEvent> event = dozerGroupEvent(group, GroupEventType.CLOSE, close);
        event.setExtraMap(closeEvent);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        updateGroupTrack(groupTrack, event);

        //4.猪群状态改为关闭
        group.setStatus(DoctorGroup.Status.CLOSED.getValue());
        doctorGroupDao.update(group);

        //5.创建镜像
        createGroupSnapShot(group, event, groupTrack, GroupEventType.CLOSE);
    }

    /**
     * 猪只存栏事件
     */
    @Transactional
    public void groupEventLiveStock(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorLiveStockGroupInput liveStock) {
        //1.转换下猪只存栏信息
        DoctorLiveStockGroupEvent liveStockEvent = BeanMapper.map(liveStock, DoctorLiveStockGroupEvent.class);

        //2.创建猪只存栏事件
        DoctorGroupEvent<DoctorLiveStockGroupEvent> event = dozerGroupEvent(group, GroupEventType.LIVE_STOCK, liveStock);
        event.setQuantity(groupTrack.getQuantity());  //猪群存栏数量 = 猪群数量
        event.setAvgDayAge(groupTrack.getAvgDayAge());
        event.setAvgWeight(liveStock.getAvgWeight());
        event.setWeight(event.getQuantity() * event.getAvgWeight()); // 总活体重 = 数量 * 均重
        event.setExtraMap(liveStockEvent);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(group, event, groupTrack, GroupEventType.LIVE_STOCK);
    }

    /**
     * 转入猪群事件
     */
    @Transactional
    public void groupEventMoveIn(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorMoveInGroupInput moveIn) {
        checkQuantityEqual(moveIn.getQuantity(), moveIn.getBoarQty(), moveIn.getSowQty());

        //1.转换转入猪群事件
        DoctorMoveInGroupEvent moveInEvent = BeanMapper.map(moveIn, DoctorMoveInGroupEvent.class);

        //2.创建转入猪群事件
        DoctorGroupEvent<DoctorMoveInGroupEvent> event = dozerGroupEvent(group, GroupEventType.MOVE_IN, moveIn);
        event.setQuantity(moveIn.getQuantity());
        event.setAvgDayAge(moveIn.getAvgDayAge());
        event.setAvgWeight(moveIn.getAvgWeight());
        event.setWeight(EventUtil.getWeight(event.getAvgWeight(), event.getQuantity()));
        event.setExtraMap(moveInEvent);
        doctorGroupEventDao.create(event);

        //3.更新猪群跟踪
        groupTrack.setQuantity(EventUtil.plusQuantity(groupTrack.getQuantity(), moveIn.getQuantity()));
        groupTrack.setBoarQty(EventUtil.plusQuantity(groupTrack.getBoarQty(), moveIn.getBoarQty()));
        groupTrack.setSowQty(EventUtil.plusQuantity(groupTrack.getSowQty(), moveIn.getSowQty()));

        //重新计算日龄
        groupTrack.setAvgDayAge(EventUtil.getAvgDayAge(groupTrack.getAvgDayAge(), groupTrack.getQuantity(), moveIn.getAvgDayAge(), moveIn.getQuantity()));
        groupTrack.setBirthDate(EventUtil.getBirthDate(new Date(), groupTrack.getAvgDayAge()));

        //重新计算重量
        groupTrack.setAvgWeight(EventUtil.getAvgWeight(groupTrack.getWeight(), EventUtil.getWeight(moveIn.getAvgWeight(), moveIn.getQuantity()), groupTrack.getQuantity()));
        groupTrack.setWeight(EventUtil.getWeight(groupTrack.getAvgWeight(), groupTrack.getQuantity()));

        //重新计算金额
        groupTrack.setAmount(groupTrack.getAmount() + MoreObjects.firstNonNull(moveIn.getAmount(), 0L));
        groupTrack.setPrice(EventUtil.getPrice(groupTrack.getAmount(), groupTrack.getQuantity()));
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(group, event, groupTrack, GroupEventType.MOVE_IN);
    }

    /**
     * 猪群变动事件 // TODO: 16/5/30 销售要记录销售数量
     */
    @Transactional
    public void groupEventChange(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorChangeGroupInput change) {
        checkQuantity(groupTrack.getQuantity(), change.getQuantity());
        checkQuantity(groupTrack.getBoarQty(), change.getBoarQty());
        checkQuantity(groupTrack.getSowQty(), change.getSowQty());
        checkQuantityEqual(change.getQuantity(), change.getBoarQty(), change.getSowQty());

        //1.转换猪群变动事件
        DoctorChangeGroupEvent changeEvent = BeanMapper.map(change, DoctorChangeGroupEvent.class);

        //2.创建猪群变动事件
        DoctorGroupEvent<DoctorChangeGroupEvent> event = dozerGroupEvent(group, GroupEventType.CHANGE, change);
        event.setQuantity(change.getQuantity());
        event.setAvgDayAge(groupTrack.getAvgDayAge());  //变动的日龄不需要录入, 直接取猪群的日龄
        event.setWeight(change.getWeight());
        event.setAvgWeight(EventUtil.getAvgWeight(change.getWeight(), change.getQuantity()));
        event.setExtraMap(changeEvent);
        doctorGroupEventDao.create(event);

        Integer oldQuantity = groupTrack.getQuantity();

        //3.更新猪群跟踪
        groupTrack.setQuantity(EventUtil.minusQuantity(groupTrack.getQuantity(), change.getQuantity()));
        groupTrack.setBoarQty(EventUtil.minusQuantity(groupTrack.getBoarQty(), change.getBoarQty()));
        groupTrack.setSowQty(EventUtil.minusQuantity(groupTrack.getSowQty(), change.getSowQty()));

        //重新计算重量
        groupTrack.setWeight(groupTrack.getWeight() - change.getWeight());
        groupTrack.setAvgWeight(EventUtil.getAvgWeight(groupTrack.getWeight(), groupTrack.getQuantity()));

        //重新计算金额
        groupTrack.setAmount(groupTrack.getAmount() - MoreObjects.firstNonNull(change.getAmount(), 0L));
        groupTrack.setPrice(EventUtil.getPrice(groupTrack.getAmount(), groupTrack.getQuantity()));
        updateGroupTrack(groupTrack, event);

        //4.创建镜像
        createGroupSnapShot(group, event, groupTrack, GroupEventType.CHANGE);

        //5.判断变动数量, 如果 = 猪群数量, 触发关闭猪群事件
        if (Objects.equals(oldQuantity, change.getQuantity())) {
            autoGroupEventClose(group, groupTrack, change);
        }
    }

    /**
     * 转群事件
     */
    @Transactional
    public void groupEventTransGroup(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorTransGroupInput transGroup) {
        checkQuantity(groupTrack.getQuantity(), transGroup.getQuantity());
        checkQuantity(groupTrack.getBoarQty(), transGroup.getBoarQty());
        checkQuantity(groupTrack.getSowQty(), transGroup.getSowQty());
        checkQuantityEqual(transGroup.getQuantity(), transGroup.getBoarQty(), transGroup.getSowQty());

        //1.转换转群事件
        DoctorTransGroupEvent transGroupEvent = BeanMapper.map(transGroup, DoctorTransGroupEvent.class);

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

        //4.创建镜像 todo 其他字段
        createGroupSnapShot(group, event, groupTrack, GroupEventType.TRANS_GROUP);

        //5.判断转群数量, 如果 = 猪群数量, 触发关闭猪群事件
        if (Objects.equals(oldQuantity, transGroup.getQuantity())) {
            autoGroupEventClose(group, groupTrack, transGroup);
        }

        //设置来源为本场
        transGroup.setSource(PigSource.LOCAL.getKey());

        //6.判断是否新建群,触发目标群的转入仔猪事件
        if (Objects.equals(transGroup.getIsCreateGroup(), IsOrNot.YES.getValue())) {
            //新建猪群
            Long toGroupId = autoTransGroupEventNew(group, groupTrack, transGroup);
            transGroup.setToGroupId(toGroupId);

            //转入猪群
            autoTransEventMoveIn(group, groupTrack, transGroup);
        } else {
            autoTransEventMoveIn(group, groupTrack, transGroup);
        }
    }

    /**
     * 系统触发的自动关闭猪群事件
     */
    private void autoGroupEventClose(DoctorGroup group, DoctorGroupTrack groupTrack, BaseGroupInput baseInput) {
        DoctorCloseGroupInput closeInput = new DoctorCloseGroupInput();
        closeInput.setIsAuto(IsOrNot.YES.getValue());   //系统触发事件, 属于自动生成
        closeInput.setEventAt(baseInput.getEventAt());
        groupEventClose(group, groupTrack, closeInput);
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
        newGroupInput.setPigType(fromGroup.getPigType());           //猪类去原先的猪类 // TODO: 16/5/30 还是取猪舍的猪类?
        newGroupInput.setSex(fromGroupTrack.getSex());
        newGroupInput.setBreedId(transGroup.getBreedId());          //品种
        newGroupInput.setBreedName(transGroup.getBreedName());
        newGroupInput.setGeneticId(fromGroup.getGeneticId());
        newGroupInput.setGeneticName(fromGroup.getGeneticName());
        newGroupInput.setSource(PigSource.LOCAL.getKey());          //来源:本场
        newGroupInput.setIsAuto(IsOrNot.YES.getValue());

        DoctorGroup toGroup = BeanMapper.map(newGroupInput, DoctorGroup.class);
        toGroup.setFarmName(fromGroup.getFarmName());
        toGroup.setOrgId(fromGroup.getId());
        toGroup.setOrgName(fromGroup.getOrgName());
        toGroup.setCreatorId(transGroup.getCreatorId());    //创建人取录入转群事件的人
        toGroup.setCreatorName(transGroup.getCreatorName());
        return createNewGroup(toGroup, newGroupInput);
    }

    /**
     * 系统触发的自动转入转入猪群事件(群间转移, 转群/转场触发)
     */
    private void autoTransEventMoveIn(DoctorGroup fromGroup, DoctorGroupTrack fromGroupTrack, DoctorTransGroupInput transGroup) {
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
        groupEventMoveIn(groupDetail.getGroup(), groupDetail.getGroupTrack(), moveIn);
    }

    /**
     * 转场事件
     */
    @Transactional
    public void groupEventTransFarm(DoctorGroup group, DoctorGroupTrack groupTrack, DoctorTransFarmGroupInput transFarm) {
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
            autoGroupEventClose(group, groupTrack, transFarm);
        }

        //设置来源为外场
        transFarm.setSource(PigSource.OUTER.getKey());

        //6.判断是否新建群,触发目标群的转入仔猪事件
        if (Objects.equals(transFarm.getIsCreateGroup(), IsOrNot.YES.getValue())) {
            //新建猪群
            Long toGroupId = autoTransFarmEventNew(group, groupTrack, transFarm);
            transFarm.setToGroupId(toGroupId);

            //转入猪群
            autoTransEventMoveIn(group, groupTrack, transFarm);
        } else {
            autoTransEventMoveIn(group, groupTrack, transFarm);
        }
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
        return createNewGroup(toGroup, newGroupInput);
    }

    //转换下猪群基本数据
    private DoctorGroupEvent dozerGroupEvent(DoctorGroup group, GroupEventType eventType, BaseGroupInput baseInput) {
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
    private void updateGroupTrack(DoctorGroupTrack groupTrack, DoctorGroupEvent event) {
        groupTrack.setRelEventId(event.getId());    //关联此次的事件id
        groupTrack.setUpdatorId(event.getCreatorId());
        groupTrack.setUpdatorName(event.getCreatorName());
        groupTrack.setSex(EventUtil.getSex(groupTrack.getBoarQty(), groupTrack.getSowQty()));
        doctorGroupTrackDao.update(groupTrack);
    }

    //创建猪群镜像信息
    private void createGroupSnapShot(DoctorGroup group, DoctorGroupEvent groupEvent, DoctorGroupTrack groupTrack, GroupEventType eventType) {
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

    //校验数量
    private static void checkQuantity(Integer max, Integer actual) {
        if (actual > max) {
            throw new ServiceException("quantity.over.max");
        }
    }

    //校验 公 + 母 = 总和
    private static void checkQuantityEqual(Integer all, Integer boar, Integer sow) {
        if (all != (boar + sow)) {
            throw new ServiceException("quantity.not.equal");
        }
    }

    //校验猪群号是否重复
    private void checkGroupCodeExist(Long farmId, String groupCode) {
        List<DoctorGroup> groups = RespHelper.or500(doctorGroupReadService.findGroupsByFarmId(farmId));
        if (groups.stream().map(DoctorGroup::getGroupCode).collect(Collectors.toList()).contains(groupCode)) {
            throw new ServiceException("group.code.exist");
        }
    }
}
