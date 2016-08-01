package io.terminus.doctor.move.service;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Joiners;
import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.dao.DoctorChangeReasonDao;
import io.terminus.doctor.basic.dao.DoctorCustomerDao;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.constants.DoctorFarmEntryConstants;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.event.group.DoctorAntiepidemicGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorChangeGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorCloseGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorDiseaseGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorLiveStockGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorNewGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorTransFarmGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorTransGroupEvent;
import io.terminus.doctor.event.dto.event.group.DoctorTurnSeedGroupEvent;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.move.handler.DoctorMoveTableEnum;
import io.terminus.doctor.move.model.B_ChangeReason;
import io.terminus.doctor.move.model.B_Customer;
import io.terminus.doctor.move.model.Proc_InventoryGain;
import io.terminus.doctor.move.model.TB_FieldValue;
import io.terminus.doctor.move.model.View_EventListGain;
import io.terminus.doctor.move.model.View_GainCardList;
import io.terminus.doctor.move.model.View_PigLocationList;
import io.terminus.doctor.move.model.View_SowCardList;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorUser;
import io.terminus.doctor.user.model.Sub;
import io.terminus.parana.user.impl.dao.UserProfileDao;
import io.terminus.parana.user.model.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc: 迁移数据, TODO: 注意如果一个数据源有多个猪场的情况!!!
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/27
 */
@Slf4j
@Service
public class DoctorMoveDataService implements CommandLineRunner {

    private final DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;
    private final DoctorBarnDao doctorBarnDao;
    private final DoctorCustomerDao doctorCustomerDao;
    private final DoctorChangeReasonDao doctorChangeReasonDao;
    private final DoctorBasicDao doctorBasicDao;
    private final DoctorStaffDao doctorStaffDao;
    private final UserProfileDao userProfileDao;
    private final DoctorGroupDao doctorGroupDao;
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorGroupTrackDao doctorGroupTrackDao;
    private final DoctorPigDao doctorPigDao;
    private final DoctorPigTrackDao doctorPigTrackDao;
    private final DoctorPigEventDao doctorPigEventDao;

    @Autowired
    public DoctorMoveDataService(DoctorMoveDatasourceHandler doctorMoveDatasourceHandler,
                                 DoctorBarnDao doctorBarnDao,
                                 DoctorCustomerDao doctorCustomerDao,
                                 DoctorChangeReasonDao doctorChangeReasonDao,
                                 DoctorBasicDao doctorBasicDao,
                                 DoctorStaffDao doctorStaffDao,
                                 UserProfileDao userProfileDao,
                                 DoctorGroupDao doctorGroupDao,
                                 DoctorGroupEventDao doctorGroupEventDao,
                                 DoctorGroupTrackDao doctorGroupTrackDao,
                                 DoctorPigDao doctorPigDao,
                                 DoctorPigTrackDao doctorPigTrackDao,
                                 DoctorPigEventDao doctorPigEventDao) {
        this.doctorMoveDatasourceHandler = doctorMoveDatasourceHandler;
        this.doctorBarnDao = doctorBarnDao;
        this.doctorCustomerDao = doctorCustomerDao;
        this.doctorChangeReasonDao = doctorChangeReasonDao;
        this.doctorBasicDao = doctorBasicDao;
        this.doctorStaffDao = doctorStaffDao;
        this.userProfileDao = userProfileDao;
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorPigDao = doctorPigDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorPigEventDao = doctorPigEventDao;
    }

    /**
     * 迁移基础数据
     */
    @Transactional
    public Response<Boolean> moveBasic(Long moveId) {
        try {
            //基础数据按照类型名称分组
            Map<String, List<DoctorBasic>> basicsMap = doctorBasicDao.listAll().stream().collect(Collectors.groupingBy(DoctorBasic::getTypeName));
            Map<String, List<TB_FieldValue>> fieldsMap = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findByHbsSql(moveId, TB_FieldValue.class, "TB_FieldValue")).stream()
                    .collect(Collectors.groupingBy(TB_FieldValue::getTypeId));

            //按照遍历doctor里的基础数据, 如果有缺失的, 就补充进来
            for (Map.Entry<String, List<DoctorBasic>> basic : basicsMap.entrySet()) {
                //取出基础字段名称
                List<String> basicNames = basic.getValue().stream().map(DoctorBasic::getName).collect(Collectors.toList());

                List<TB_FieldValue> fieldValues = fieldsMap.get(basic.getKey());
                if (!notEmpty(fieldValues)) {
                    continue;
                }

                //把过滤的结果放到doctor_basics里
                fieldValues.stream()
                        .filter(field -> !basicNames.contains(field.getFieldText()))
                        .forEach(fn -> doctorBasicDao.create(getBasic(fn)));
            }
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("move basic failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
            return Response.fail("move.basic.fail");
        }
    }

    //拼接基础数据
    private DoctorBasic getBasic(TB_FieldValue field) {
        DoctorBasic.Type type = DoctorBasic.Type.from(field.getTypeId());
        DoctorBasic basic = new DoctorBasic();
        basic.setName(field.getFieldText());
        basic.setType(type == null ? null : type.getValue());
        basic.setTypeName(field.getTypeId());
        basic.setContext(field.getRemark());
        basic.setOutId(field.getOID());
        basic.setSrm(field.getSrm());
        basic.setIsValid(IsOrNot.YES.getValue());
        return basic;
    }

    /**
     * 迁移Barn
     */
    @Transactional
    public Response<Boolean> moveBarn(Long moveId) {
        try {
            List<DoctorBarn> barns = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findAllData(moveId, View_PigLocationList.class, DoctorMoveTableEnum.view_PigLocationList)).stream()
                    .filter(loc -> isFarm(loc.getFarmOID(), mockFarm().getOutId()))     //这一步很重要, 如果一个公司有多个猪场, 猪场id必须匹配!
                    .filter(f -> true) // TODO: 16/7/28 多个猪场注意过滤outId
                    .map(location -> getBarn(mockOrg(), mockFarm(), mockSub(), location))
                    .collect(Collectors.toList());

            if (notEmpty(barns)) {
                doctorBarnDao.creates(barns);
            }
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("move barn failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
            return Response.fail("move.barn.fail");
        }
    }

    /**
     * 迁移客户
     */
    @Transactional
    public Response<Boolean> moveCustomer(Long moveId) {
        try {
            List<DoctorCustomer> customers = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findAllData(moveId, B_Customer.class, DoctorMoveTableEnum.B_Customer)).stream()
                    .filter(f -> true) // TODO: 16/7/28 多个猪场注意过滤outId
                    .map(cus -> getCustomer(mockFarm(), mockUser(), cus))
                    .collect(Collectors.toList());

            if (notEmpty(customers)) {
                doctorCustomerDao.creates(customers);
            }
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("move customer failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
            return Response.fail("move.customer.fail");
        }
    }

    /**
     * 迁移变动原因
     */
    @Transactional
    public Response<Boolean> moveChangeReason(Long moveId) {
        try {
            //查出所有的变动
            List<DoctorBasic> changeTypes = doctorBasicDao.findByType(DoctorBasic.Type.CHANGE_TYPE.getValue());

            //查出每个变动下的变动原因, 组装成map
            Map<DoctorBasic, List<DoctorChangeReason>> changeTypeMap = Maps.newHashMap();
            changeTypes.forEach(type -> changeTypeMap.put(type, doctorChangeReasonDao.findByChangeTypeIdAndSrm(type.getId(), null)));

            //查出猪场软件里的所有变动原因, 并按照变动类型 group by
            Map<String, List<B_ChangeReason>> reasonMap = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findByHbsSql(moveId, B_ChangeReason.class, "changeReason")).stream()
                    .collect(Collectors.groupingBy(B_ChangeReason::getChangeType));

            //遍历每个变动类型的变动原因, 过滤掉重复的插入
            for (Map.Entry<DoctorBasic, List<DoctorChangeReason>> changeType : changeTypeMap.entrySet()) {
                //当前doctor里存在的reason名称
                List<String> changeReasons = changeType.getValue().stream().map(DoctorChangeReason::getReason).collect(Collectors.toList());
                List<B_ChangeReason> reasons = reasonMap.get(changeType.getKey().getName());

                if (!notEmpty(reasons)) {
                    continue;
                }

                //过滤掉重复的原因, 插入doctor_change_reasons 表
                reasons.stream()
                        .filter(r -> !changeReasons.contains(r.getReasonName()))
                        .forEach(reason -> doctorChangeReasonDao.create(getReason(reason, changeType.getKey().getId())));
            }

            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("move customer failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
            return Response.fail("move.customer.fail");
        }
    }

    /**
     * 迁移猪群
     */
    @Transactional
    public Response<Boolean> moveGroup(Long moveId) {
        try {
            //0. 基础数据准备: barn, basic, subUser, changeReason, customer
            Map<String, DoctorBarn> barnMap = doctorBarnDao.findByFarmId(mockFarm().getId()).stream().collect(Collectors.toMap(DoctorBarn::getOutId, v -> v));
            Map<Integer, Map<String, DoctorBasic>> basicMap = getBasicMap();
            Map<String, Long> subMap = getSubMap(mockOrg().getId());
            Map<String, DoctorChangeReason> changeReasonMap = getReasonMap();
            Map<String, DoctorCustomer> customerMap = getCustomerMap(mockFarm().getId());

            //1. 迁移DoctorGroup
            List<DoctorGroup> groups = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findByHbsSql(moveId, View_GainCardList.class, "DoctorGroup-GainCardList")).stream()
                    .filter(f -> true) // TODO: 16/7/28 多个猪场注意过滤outId
                    .map(gain -> getGroup(mockOrg(), mockFarm(), gain, barnMap, basicMap, subMap)).collect(Collectors.toList());
            doctorGroupDao.creates(groups);

            //查出刚插入的group, key = outId
            Map<String, DoctorGroup> groupMap = doctorGroupDao.findByFarmId(mockFarm().getId()).stream().collect(Collectors.toMap(DoctorGroup::getOutId, v -> v));

            //2. 迁移DoctorGroupEvent
            List<DoctorGroupEvent> events = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findByHbsSql(moveId, View_EventListGain.class, "DoctorGroupEvent-EventListGain")).stream()
                    .map(gainEvent -> getGroupEvent(groupMap, gainEvent, subMap, barnMap, basicMap, changeReasonMap, customerMap))
                    .collect(Collectors.toList());
            doctorGroupEventDao.creates(events);

            //查出刚才插入的groupEvent, 按照猪群id groupBy
            Map<Long, List<DoctorGroupEvent>> eventMap = doctorGroupEventDao.findByFarmId(mockFarm().getId()).stream().collect(Collectors.groupingBy(DoctorGroupEvent::getGroupId));

            //3. 迁移DoctorTrack, 先把统计结果转换成map, 在转换track
            String now = DateUtil.toDateTimeString(new Date());
            Map<String, Proc_InventoryGain> gainMap = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findByHbsSql(moveId, Proc_InventoryGain.class, "DoctorGroupTrack-Proc_InventoryGain", ImmutableMap.of("date", now))).stream()
                    .filter(f -> true) // TODO: 16/7/28 多个猪场注意过滤outId
                    .collect(Collectors.toMap(Proc_InventoryGain::getGroupOutId, v -> v));

            List<DoctorGroupTrack> groupTracks = groupMap.values().stream()
                    .map(group -> getGroupTrack(group, gainMap.get(group.getOutId()), eventMap.get(group.getId())))
                    .collect(Collectors.toList());
            doctorGroupTrackDao.creates(groupTracks);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("move group failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
            return Response.fail("move.group.fail");
        }
    }

    /**
     * 迁移母猪公猪
     */
    @Transactional
    public Response<Boolean> movePig(Long moveId) {
       try {
           //0. 基础数据准备: barn, basic, subUser
           Map<String, DoctorBarn> barnMap = doctorBarnDao.findByFarmId(mockFarm().getId()).stream().collect(Collectors.toMap(DoctorBarn::getOutId, v -> v));
           Map<Integer, Map<String, DoctorBasic>> basicMap = getBasicMap();
           Map<String, Long> subMap = getSubMap(mockOrg().getId());

           //1. 迁移sow
           moveSow(moveId, mockOrg(), mockFarm(), basicMap);

           //2. 迁移boar
           moveBoar();
           return Response.ok(Boolean.TRUE);
       } catch (Exception e) {
           log.error("move pig failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
           return Response.fail("move.pig.fail");
       }
    }

    //迁移母猪
    private void moveSow(Long moveId, DoctorOrg org, DoctorFarm farm, Map<Integer, Map<String, DoctorBasic>> basicMap) {
        //1. 迁移DoctorPig
        List<DoctorPig> sows = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, View_SowCardList.class, "DoctorPig-SowCardList")).stream()
                .filter(f -> true) // TODO: 16/7/28 多个猪场注意过滤outId
                .map(card -> getSow(card, org, farm, basicMap)).collect(Collectors.toList());
        doctorPigDao.creates(sows);


        //2. 迁移DoctorPigEvent

        //3. 迁移DoctorPigTrack
    }

    //拼接母猪
    private DoctorPig getSow(View_SowCardList card, DoctorOrg org, DoctorFarm farm, Map<Integer, Map<String, DoctorBasic>> basicMap) {
        DoctorPig sow = new DoctorPig();
        sow.setOrgId(org.getId());
        sow.setOrgName(org.getName());
        sow.setFarmId(farm.getId());
        sow.setFarmName(farm.getName());
        sow.setOutId(card.getPigOutId());           //外部OID
        sow.setPigCode(card.getPigCode());
        sow.setPigType(DoctorPig.PIG_TYPE.SOW.getKey());  //猪类是母猪
        sow.setIsRemoval("已离场".equals(card.getStatus()) ? IsOrNot.YES.getValue() : IsOrNot.NO.getValue());
        sow.setPigFatherCode(card.getPigFatherCode());
        sow.setPigMotherCode(card.getPigMotherCode());
        sow.setSource(card.getSource());
        sow.setBirthDate(card.getBirthDate());
        sow.setBirthWeight(card.getBirthWeight());
        sow.setInFarmDate(card.getInFarmDate());
        sow.setInFarmDayAge(card.getInFarmDayAge());
        sow.setInitBarnName(card.getInitBarnName());
        sow.setRemark(card.getRemark());

        //品种
        DoctorBasic breed = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(card.getBreed());
        sow.setBreedId(breed == null ? null : breed.getId());
        sow.setBreedName(card.getBreed());

        //品系
        DoctorBasic gene = basicMap.get(DoctorBasic.Type.GENETICS.getValue()).get(card.getGenetic());
        sow.setGeneticId(gene == null ? null : gene.getId());
        sow.setGeneticName(card.getGenetic());

        //附加字段
        sow.setExtraMap(ImmutableMap.of(
                DoctorFarmEntryConstants.EAR_CODE, card.getPigCode(),   //耳缺号取猪号
                DoctorFarmEntryConstants.FIRST_PARITY, card.getFirstParity(),
                DoctorFarmEntryConstants.LEFT_COUNT, card.getLeftCount(),
                DoctorFarmEntryConstants.RIGHT_COUNT, card.getRightCount()
        ));
        return sow;
    }

    //拼接母猪跟踪
    private DoctorPigTrack getSowTrack(View_SowCardList card, DoctorPig sow, Map<String, DoctorBarn> barnMap, List<DoctorPigEvent> events) {
        //card.getStatus(); // TODO: 16/8/1 即将离场 的情况

        //母猪状态枚举
        PigStatus status = PigStatus.from(card.getStatus());

        DoctorPigTrack track = new DoctorPigTrack();
        track.setFarmId(sow.getFarmId());
        track.setPigId(sow.getId());
        track.setPigType(sow.getPigType());
        track.setStatus(status == null ? null : status.getKey());
        track.setIsRemoval(sow.getIsRemoval());
        track.setWeight(card.getWeight());
        track.setOutFarmDate(card.getOutFarmDate());
        track.setCurrentParity(card.getCurrentParity());

        if (notEmpty(events)) {
            //按照时间 asc 排序
            events = events.stream().sorted((a, b) -> a.getEventAt().compareTo(b.getEventAt())).collect(Collectors.toList());
            track.setExtra(events.get(events.size() - 1).getExtra());   //extra字段保存最后一次event的extra
            track.setRelEventIds(Joiners.COMMA.join(events.stream().map(DoctorPigEvent::getId).collect(Collectors.toList()))); //关联事件ids, 逗号分隔
        }

        //猪舍
        DoctorBarn barn = barnMap.get(card.getCurrentBarnOutId());
        if (barn != null) {
            track.setCurrentBarnId(barn.getId());
            track.setCurrentBarnName(barn.getName());
        }
        return track;
    }

    //迁移公猪
    private void moveBoar() {
        //1. 迁移DoctorPig

        //2. 迁移DoctorPigEvent

        //3. 迁移DoctorPigTrack
    }

    //拼接猪群事件
    private DoctorGroupEvent getGroupEvent(Map<String, DoctorGroup> groupMap, View_EventListGain gainEvent, Map<String, Long> subMap, Map<String, DoctorBarn> barnMap,
                                           Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, DoctorChangeReason> changeReasonMap, Map<String, DoctorCustomer> customerMap) {
        DoctorGroup group = groupMap.get(gainEvent.getGroupOutId());
        if (group == null) {
            return null;
        }

        DoctorGroupEvent event = new DoctorGroupEvent();
        event.setOrgId(group.getOrgId());
        event.setOrgName(group.getOrgName());
        event.setFarmId(group.getFarmId());
        event.setFarmName(group.getFarmName());
        event.setGroupId(group.getId());
        event.setGroupCode(group.getGroupCode());
        event.setEventAt(gainEvent.getEventAt());

        //转换事件类型
        GroupEventType type = GroupEventType.from(gainEvent.getEventTypeName());

        event.setType(type == null ? null : type.getValue());
        event.setName(gainEvent.getEventTypeName());
        event.setDesc(gainEvent.getEventDesc());

        //事件发生猪舍
        DoctorBarn barn = barnMap.get(gainEvent.getBarnOutId());
        if (barn != null) {
            event.setBarnId(barn.getId());
            event.setBarnName(barn.getName());
        }
        event.setPigType(group.getPigType());
        event.setQuantity(gainEvent.getQuantity());
        event.setWeight(gainEvent.getWeight());
        event.setAvgWeight(gainEvent.getAvgWeight());
        event.setAvgDayAge(gainEvent.getAvgDayAge());
        event.setIsAuto(gainEvent.getIsAuto());
        event.setOutId(gainEvent.getGroupEventOutId());
        event.setRemark(gainEvent.getRemark());
        return getGroupEventExtra(type, event, gainEvent, basicMap, barnMap, groupMap, group, subMap, changeReasonMap, customerMap);
    }

    //根据类型拼接猪群事件明细
    @SuppressWarnings("unchecked")
    private DoctorGroupEvent getGroupEventExtra(GroupEventType type, DoctorGroupEvent event, View_EventListGain gainEvent,
                                                Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, DoctorBarn> barnMap,
                                                Map<String, DoctorGroup> groupMap, DoctorGroup group, Map<String, Long> subMap,
                                                Map<String, DoctorChangeReason> changeReasonMap, Map<String, DoctorCustomer> customerMap) {
        if (type == null) {
            return event;
        }

        switch (type) {
            case NEW:
                DoctorNewGroupEvent newEvent = new DoctorNewGroupEvent();
                PigSource source = PigSource.from(gainEvent.getSource());
                newEvent.setSource(source == null ? null : source.getKey());
                event.setExtraMap(newEvent);
                break;
            case MOVE_IN:
                event.setExtraMap(getMoveInEvent(gainEvent, basicMap, groupMap, group));
                break;
            case CHANGE:
                event.setExtraMap(getChangeEvent(gainEvent, basicMap, changeReasonMap, customerMap));
                break;
            case TRANS_GROUP:
                event.setExtraMap(getTranGroupEvent(gainEvent, basicMap, barnMap, groupMap, group));
                break;
            case TURN_SEED:
                event.setExtraMap(getTurnSeedEvent(gainEvent, basicMap, barnMap));
                break;
            case LIVE_STOCK:
                DoctorLiveStockGroupEvent liveStock = new DoctorLiveStockGroupEvent();
                liveStock.setMeasureAt(DateUtil.toDateTimeString(gainEvent.getEventAt()));
                event.setExtraMap(liveStock);
                break;
            case DISEASE:
                DoctorDiseaseGroupEvent disease = new DoctorDiseaseGroupEvent();
                DoctorBasic basic = basicMap.get(DoctorBasic.Type.DISEASE.getValue()).get(gainEvent.getDiseaseName());
                disease.setDiseaseId(basic == null ? null : basic.getId());
                disease.setDiseaseName(gainEvent.getDiseaseName());
                disease.setDoctorId(subMap.get(gainEvent.getStaffName()));
                disease.setDoctorName(gainEvent.getStaffName());
                disease.setQuantity(gainEvent.getQuantity());
                event.setExtraMap(disease);
                break;
            case ANTIEPIDEMIC:
                DoctorAntiepidemicGroupEvent anti = new DoctorAntiepidemicGroupEvent();
                //anti.setVaccinId(); // TODO: 16/7/30 需要疫苗的基础数据
                anti.setVaccinName(gainEvent.getNotDisease());

                DoctorAntiepidemicGroupEvent.VaccinResult result = DoctorAntiepidemicGroupEvent.VaccinResult.from(gainEvent.getContext());
                anti.setVaccinResult(result == null ? null : result.getValue());
                anti.setVaccinStaffId(subMap.get(gainEvent.getStaffName()));
                anti.setVaccinStaffName(gainEvent.getStaffName());
                anti.setQuantity(gainEvent.getQuantity());
                event.setExtraMap(anti);
                break;
            case TRANS_FARM:
                event.setExtraMap(getTranFarmEvent(gainEvent, basicMap, barnMap, groupMap, group));
                break;
            case CLOSE:
                DoctorCloseGroupEvent close = new DoctorCloseGroupEvent();
                close.setCloseAt(DateUtil.toDateTimeString(gainEvent.getEventAt()));
                event.setExtraMap(close);
                break;
            default:
                break;
        }
        return event;
    }

    //转入事件详细信息
    private DoctorMoveInGroupEvent getMoveInEvent(View_EventListGain gainEvent, Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, DoctorGroup> groupMap, DoctorGroup group) {
        DoctorMoveInGroupEvent moveIn = new DoctorMoveInGroupEvent();

        DoctorMoveInGroupEvent.InType inType = DoctorMoveInGroupEvent.InType.from(gainEvent.getInTypeName());
        moveIn.setInType(inType == null ? null : inType.getValue());
        moveIn.setInTypeName(gainEvent.getInTypeName());

        //来源
        PigSource source = PigSource.from(gainEvent.getSource());
        moveIn.setSource(source == null ? null : source.getKey());
        moveIn.setSex(DoctorGroupTrack.Sex.MIX.getValue());

        //品种
        DoctorBasic basic = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(gainEvent.getBreed());
        moveIn.setBreedId(basic == null ? null : basic.getId());
        moveIn.setBreedName(gainEvent.getBreed());

        //来源猪舍
        moveIn.setFromBarnId(group.getCurrentBarnId());
        moveIn.setFromBarnName(group.getCurrentBarnName());

        //再转入猪舍里, 表示的是来源猪群
        DoctorGroup fromGroup = groupMap.get(gainEvent.getToGroupOutId());
        if (fromGroup != null) {
            moveIn.setFromGroupId(fromGroup.getId());
            moveIn.setFromGroupCode(fromGroup.getGroupCode());
        }
        moveIn.setBoarQty(gainEvent.getBoarQty());
        moveIn.setSowQty(gainEvent.getSowQty());
        moveIn.setAmount(gainEvent.getAmount());
        return moveIn;
    }

    //变动事件
    private DoctorChangeGroupEvent getChangeEvent(View_EventListGain gainEvent, Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, DoctorChangeReason> changeReasonMap, Map<String, DoctorCustomer> custormerMap) {
        DoctorChangeGroupEvent change = new DoctorChangeGroupEvent();

        //变动类型, 原因, 品种, 客户
        DoctorBasic changeType = basicMap.get(DoctorBasic.Type.CHANGE_TYPE.getValue()).get(gainEvent.getChangTypeName());
        change.setChangeTypeId(changeType == null ? null : changeType.getId());
        change.setChangeTypeName(gainEvent.getChangTypeName());

        DoctorChangeReason reason = changeReasonMap.get(gainEvent.getChangeReasonName());
        change.setChangeReasonId(reason == null ? null : reason.getId());
        change.setChangeReasonName(gainEvent.getChangeReasonName());

        DoctorBasic basic = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(gainEvent.getBreed());
        change.setBreedId(basic == null ? null : basic.getId());
        change.setBreedName(gainEvent.getBreed());

        DoctorCustomer customer = custormerMap.get(gainEvent.getCustomer());
        change.setCustomerId(customer == null ? null : customer.getId());
        change.setCustomerName(gainEvent.getCustomer());

        //单价 金额 数量
        change.setPrice(gainEvent.getPrice());
        change.setAmount(gainEvent.getAmount());
        change.setBoarQty(gainEvent.getBoarQty());
        change.setSowQty(gainEvent.getSowQty());
        return change;
    }

    //转群事件
    private DoctorTransGroupEvent getTranGroupEvent(View_EventListGain gainEvent, Map<Integer, Map<String, DoctorBasic>> basicMap,
                                                    Map<String, DoctorBarn> barnMap, Map<String, DoctorGroup> groupMap, DoctorGroup group) {
        DoctorTransGroupEvent transGroup = new DoctorTransGroupEvent();
        transGroup.setTransGroupAt(DateUtil.toDateTimeString(gainEvent.getEventAt()));

        //来源猪舍 猪群
        transGroup.setFromBarnId(group.getCurrentBarnId());
        transGroup.setFromBarnName(group.getCurrentBarnName());
        transGroup.setFromGroupId(group.getId());
        transGroup.setFromGroupCode(group.getGroupCode());

        //ChgReason=群间转移， 说明这是转群事件！ Treament: 转入猪舍， OutDest: 转入猪群
        if ("群间转移".equals(gainEvent.getChangeReasonName())) {
            DoctorBarn barn = barnMap.get(gainEvent.getContext()); //就是TreatMent
            if (barn != null) {
                transGroup.setToBarnId(barn.getId());
                transGroup.setToBarnName(barn.getName());
            }
            DoctorGroup toGroup = groupMap.get(gainEvent.getToBarnOutId()); //就是 OutDestination
            if (toGroup != null) {
                transGroup.setToGroupId(toGroup.getId());
                transGroup.setToGroupCode(toGroup.getGroupCode());
            }
        } else {
            DoctorBarn barn = barnMap.get(gainEvent.getToBarnOutId()); // 注意和 "群间转移" 区别
            if (barn != null) {
                transGroup.setToBarnId(barn.getId());
                transGroup.setToBarnName(barn.getName());
            }
            DoctorGroup toGroup = groupMap.get(gainEvent.getToGroupOutId());
            if (toGroup != null) {
                transGroup.setToGroupId(toGroup.getId());
                transGroup.setToGroupCode(toGroup.getGroupCode());
            }
            //是否新建猪群 Treatment
            IsOrNot is = IsOrNot.from(gainEvent.getContext());
            transGroup.setIsCreateGroup(is == null ? null : is.getValue());
        }

        //品种
        DoctorBasic basic = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(gainEvent.getBreed());
        transGroup.setBreedId(basic == null ? null : basic.getId());
        transGroup.setBreedName(gainEvent.getBreed());

        transGroup.setBoarQty(gainEvent.getBoarQty());
        transGroup.setSowQty(gainEvent.getSowQty());
        return transGroup;
    }

    //转场事件
    private DoctorTransFarmGroupEvent getTranFarmEvent(View_EventListGain gainEvent, Map<Integer, Map<String, DoctorBasic>> basicMap,
                                                       Map<String, DoctorBarn> barnMap, Map<String, DoctorGroup> groupMap, DoctorGroup group) {
        DoctorTransFarmGroupEvent transFarm = new DoctorTransFarmGroupEvent();

        //来源猪舍 猪群
        transFarm.setFromFarmId(group.getFarmId());
        transFarm.setFromFarmName(group.getFarmName());
        transFarm.setFromBarnId(group.getCurrentBarnId());
        transFarm.setFromBarnName(group.getCurrentBarnName());
        transFarm.setFromGroupId(group.getId());
        transFarm.setFromGroupCode(group.getGroupCode());

        DoctorBarn barn = barnMap.get(gainEvent.getToBarnOutId());
        if (barn != null) {
            transFarm.setToBarnId(barn.getId());
            transFarm.setToBarnName(barn.getName());
        }
        DoctorGroup toGroup = groupMap.get(gainEvent.getToGroupOutId());
        if (toGroup != null) {
            transFarm.setToGroupId(toGroup.getId());
            transFarm.setToGroupCode(toGroup.getGroupCode());
        }
        //是否新建猪群 Treatment
        IsOrNot is = IsOrNot.from(gainEvent.getContext());
        transFarm.setIsCreateGroup(is == null ? null : is.getValue());

        //品种
        DoctorBasic basic = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(gainEvent.getBreed());
        transFarm.setBreedId(basic == null ? null : basic.getId());
        transFarm.setBreedName(gainEvent.getBreed());

        transFarm.setBoarQty(gainEvent.getBoarQty());
        transFarm.setSowQty(gainEvent.getSowQty());
        return transFarm;
    }

    //商品猪转为种猪事件
    private DoctorTurnSeedGroupEvent getTurnSeedEvent(View_EventListGain gainEvent, Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, DoctorBarn> barnMap) {
        DoctorTurnSeedGroupEvent turnSeed = new DoctorTurnSeedGroupEvent();
        //turnSeed.setPigId();  // TODO: 16/7/30 需要先迁移pig
        //turnSeed.setMotherPigCode();

        //转后的猪号
        turnSeed.setPigCode(gainEvent.getPigCode());

        //转的时间
        turnSeed.setTransInAt(DateUtil.toDateTimeString(gainEvent.getEventAt()));
        turnSeed.setBirthDate(DateUtil.toDateTimeString(gainEvent.getBirthDate()));

        //品种 品系 猪舍 性别
        DoctorBasic breed = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(gainEvent.getBreed());
        turnSeed.setBreedId(breed == null ? null : breed.getId());
        turnSeed.setBreedName(gainEvent.getBreed());

        DoctorBasic genetic = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(gainEvent.getSource()); //这里吗 source 为品系
        turnSeed.setGeneticId(genetic == null ? null : genetic.getId());
        turnSeed.setGeneticName(gainEvent.getSource());

        DoctorBarn barn = barnMap.get(gainEvent.getContext());
        if (barn != null) {
            turnSeed.setToBarnId(barn.getId());
            turnSeed.setToBarnName(barn.getName());
        }

        DoctorTurnSeedGroupEvent.Sex sex = DoctorTurnSeedGroupEvent.Sex.from(gainEvent.getSexName());
        turnSeed.setSex(sex == null ? null : sex.getValue());
        return turnSeed;
    }

    //拼接猪群跟踪
    private DoctorGroupTrack getGroupTrack(DoctorGroup group, Proc_InventoryGain gain, List<DoctorGroupEvent> events) {
        DoctorGroupTrack groupTrack = new DoctorGroupTrack();
        groupTrack.setGroupId(group.getId());
        groupTrack.setSex(DoctorGroupTrack.Sex.MIX.getValue());
        groupTrack.setExtraEntity(getGroupTrackExtra(events));

        //如果猪群已经关闭, 大部分的统计值可以置成0
        if (Objects.equals(group.getStatus(), DoctorGroup.Status.CLOSED.getValue())) {
            return getCloseGroupTrack(groupTrack, group, events);
        }

        //未关闭的猪群, 拼接
        if (gain != null) {
            groupTrack.setQuantity(MoreObjects.firstNonNull(gain.getQuantity(), 0));
            groupTrack.setBoarQty(gain.getQuantity() / 2);
            groupTrack.setSowQty(groupTrack.getQuantity() - groupTrack.getBoarQty());
            groupTrack.setAvgDayAge(gain.getAvgDayAge());
            groupTrack.setBirthDate(DateTime.now().minusDays(groupTrack.getAvgDayAge()).toDate());
            groupTrack.setAvgWeight(MoreObjects.firstNonNull(gain.getAvgWeight(), 0D));
            groupTrack.setWeight(groupTrack.getAvgWeight() * groupTrack.getQuantity());
            groupTrack.setPrice(MoreObjects.firstNonNull(groupTrack.getPrice(), 0L));
            groupTrack.setAmount(MoreObjects.firstNonNull(groupTrack.getAmount(), 0L));
            groupTrack.setSaleQty(MoreObjects.firstNonNull(groupTrack.getSaleQty(), 0));
        }
        DoctorGroupEvent lastEvent = events.stream().sorted((a, b) -> b.getEventAt().compareTo(a.getEventAt())).findFirst().orElse(null);
        groupTrack.setRelEventId(lastEvent == null ? null : lastEvent.getId());
        return groupTrack;
    }

    //关闭猪群的猪群跟踪
    private DoctorGroupTrack getCloseGroupTrack(DoctorGroupTrack groupTrack, DoctorGroup group, List<DoctorGroupEvent> events) {
        DoctorGroupEvent closeEvent = events.stream().filter(e -> Objects.equals(GroupEventType.CLOSE.getValue(), e.getType())).findFirst().orElse(null);
        if (closeEvent != null) {
            groupTrack.setRelEventId(closeEvent.getId());
        }
        groupTrack.setBirthDate(group.getOpenAt());
        groupTrack.setQuantity(0);
        groupTrack.setBoarQty(0);
        groupTrack.setSowQty(0);
        groupTrack.setAvgDayAge(0);
        groupTrack.setBirthDate(DateTime.now().toDate());
        groupTrack.setAvgWeight(0D);
        groupTrack.setWeight(0D);
        groupTrack.setPrice(0L);
        groupTrack.setAmount(0L);
        groupTrack.setSaleQty(0);
        return groupTrack;
    }

    //拼接猪群跟踪的extra
    private DoctorGroupTrack.Extra getGroupTrackExtra(List<DoctorGroupEvent> events) {
        DoctorGroupTrack.Extra extra = new DoctorGroupTrack.Extra();
        for (DoctorGroupEvent event : events) {

            //记录不同事件类型的时间
            GroupEventType type = GroupEventType.from(event.getType());
            if (type != null) {
                switch (type) {
                    case NEW:
                        extra.setNewAt(event.getEventAt());
                        break;
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
            }
        }
        return extra;
    }

    //拼接猪群
    private DoctorGroup getGroup(DoctorOrg org, DoctorFarm farm, View_GainCardList gain,
                                 Map<String, DoctorBarn> barnMap, Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, Long> subMap) {
        DoctorGroup group = BeanMapper.map(gain, DoctorGroup.class);

        group.setOrgId(org.getId());
        group.setOrgName(org.getName());
        group.setFarmId(farm.getId());
        group.setFarmName(farm.getName());

        //猪舍
        DoctorBarn barn = barnMap.get(gain.getBarnOutId());
        if (barn != null) {
            group.setInitBarnId(barn.getId());
            group.setInitBarnName(barn.getName());
            group.setCurrentBarnId(barn.getId());
            group.setCurrentBarnName(barn.getName());
            group.setPigType(barn.getPigType());
        }
        //品种
        if (notEmpty(gain.getBreed())) {
            DoctorBasic breed = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(gain.getBreed());
            group.setBreedId(breed == null ? null : breed.getId());
            group.setBreedName(gain.getBreed());
        }
        //品系
        if (notEmpty(gain.getGenetic())) {
            DoctorBasic gene = basicMap.get(DoctorBasic.Type.GENETICS.getValue()).get(gain.getGenetic());
            group.setGeneticId(gene == null ? null : gene.getId());
            group.setGeneticName(gain.getGenetic());
        }
        if (notEmpty(gain.getStaffName())) {
            group.setStaffId(subMap.get(gain.getStaffName()));
        }
        return group;
    }

    //分别是 Map<DoctorBasic.TypeEnum, Map<DoctorBasic.name, DoctorBasic>>
    private Map<Integer, Map<String, DoctorBasic>> getBasicMap() {
        Map<Integer, Map<String, DoctorBasic>> basicMap = Maps.newHashMap();
        doctorBasicDao.listAll().stream()
                .collect(Collectors.groupingBy(DoctorBasic::getType)).entrySet()
                .forEach(basic -> basicMap.put(basic.getKey(),
                        basic.getValue().stream().collect(Collectors.toMap(DoctorBasic::getName, v -> v))));
        return basicMap;
    }

    //拼接staff,  Map<真实姓名, userId>
    private Map<String, Long> getSubMap(Long orgId) {
        Map<String, Long> staffMap = Maps.newHashMap();
        doctorStaffDao.findByOrgId(orgId).forEach(staff -> {
            UserProfile profile = userProfileDao.findByUserId(staff.getUserId());
            if (profile != null && notEmpty(profile.getRealName())) {
                staffMap.put(profile.getRealName(), staff.getUserId());
            }
        });
        return staffMap;
    }

    //拼接变动原因map
    private Map<String, DoctorChangeReason> getReasonMap() {
        Map<String, DoctorChangeReason> reasonMap = Maps.newHashMap();
        doctorChangeReasonDao.listAll().forEach(reason -> reasonMap.put(reason.getReason(), reason));
        return reasonMap;
    }

    //拼接客户map
    private Map<String, DoctorCustomer> getCustomerMap(Long farmId) {
        Map<String, DoctorCustomer> customerMap = Maps.newHashMap();
        doctorCustomerDao.findByFarmId(farmId).forEach(customer -> customerMap.put(customer.getName(), customer));
        return customerMap;
    }

    //拼接变动原因
    private DoctorChangeReason getReason(B_ChangeReason reason, Long changeTypeId) {
        DoctorChangeReason changeReason = new DoctorChangeReason();
        changeReason.setChangeTypeId(changeTypeId);
        changeReason.setReason(reason.getReasonName());
        changeReason.setOutId(reason.getOID());
        return changeReason;
    }

    //拼接客户
    private DoctorCustomer getCustomer(DoctorFarm farm, DoctorUser user, B_Customer cus) {
        DoctorCustomer customer = new DoctorCustomer();
        customer.setName(cus.getCustomerName());
        customer.setFarmId(farm.getId());
        customer.setFarmName(farm.getName());
        customer.setMobile(cus.getMobilePhone());
        customer.setEmail(cus.getEMail());
        customer.setOutId(cus.getOID());
        customer.setCreatorId(user.getId());
        customer.setCreatorName(user.getName());
        return customer;
    }

    //拼接barn
    private DoctorBarn getBarn(DoctorOrg org, DoctorFarm farm, Sub sub, View_PigLocationList location) {
        //转换pigtype
        PigType pigType = PigType.from(location.getTypeName());

        DoctorBarn barn = new DoctorBarn();
        barn.setName(location.getBarn());
        barn.setOrgId(org.getId());
        barn.setOrgName(org.getName());
        barn.setFarmId(farm.getId());
        barn.setFarmName(farm.getName());
        barn.setPigType(pigType == null ? 0: pigType.getValue());
        barn.setCanOpenGroup("可以".equals(location.getCanOpenGroupText()) ? 1 : -1);
        barn.setStatus("在用".equals(location.getIsStopUseText()) ? 1 : 0);
        barn.setStaffId(sub.getUserId());
        barn.setStaffName(sub.getRealName());
        barn.setOutId(location.getOID());
        return barn;
    }

    //判断猪场id是否相同
    private static boolean isFarm(String farmOID, String outId) {
        return Objects.equals(farmOID, outId);
    }

    private static DoctorFarm mockFarm() {
        DoctorFarm farm = new DoctorFarm();
        farm.setId(9999L);
        farm.setName("测试迁移猪场");
        return farm;
    }

    private static DoctorOrg mockOrg() {
        DoctorOrg org = new DoctorOrg();
        org.setId(9999L);
        org.setName("测试迁移公司");
        return org;
    }

    private static DoctorUser mockUser() {
        DoctorUser user = new DoctorUser();
        user.setId(9999L);
        user.setName("测试迁移管理员");
        return user;
    }

    private static Sub mockSub() {
        Sub sub = new Sub();
        sub.setUserId(9999L);
        sub.setRealName("测试姓名");
        return sub;
    }


    @Override
    public void run(String... strings) throws Exception {
        // Just for test!
    }
}
