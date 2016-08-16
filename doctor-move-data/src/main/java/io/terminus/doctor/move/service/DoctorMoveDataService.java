package io.terminus.doctor.move.service;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Joiners;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.constants.DoctorBasicEnums;
import io.terminus.doctor.event.constants.DoctorFarmEntryConstants;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.event.boar.DoctorBoarConditionDto;
import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
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
import io.terminus.doctor.event.dto.event.sow.DoctorAbortionDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFostersDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPartWeanDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.dto.event.usual.DoctorConditionDto;
import io.terminus.doctor.event.dto.event.usual.DoctorDiseaseDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.dto.event.usual.DoctorRemovalDto;
import io.terminus.doctor.event.dto.event.usual.DoctorVaccinationDto;
import io.terminus.doctor.event.enums.BoarEntryType;
import io.terminus.doctor.event.enums.DoctorMatingType;
import io.terminus.doctor.event.enums.FarrowingType;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.MatingType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.move.handler.DoctorMoveWorkflowHandler;
import io.terminus.doctor.move.model.Proc_InventoryGain;
import io.terminus.doctor.move.model.SowOutFarmSoon;
import io.terminus.doctor.move.model.View_BoarCardList;
import io.terminus.doctor.move.model.View_EventListBoar;
import io.terminus.doctor.move.model.View_EventListGain;
import io.terminus.doctor.move.model.View_EventListSow;
import io.terminus.doctor.move.model.View_GainCardList;
import io.terminus.doctor.move.model.View_SowCardList;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.doctor.common.enums.PigType.FARROW_TYPES;
import static io.terminus.doctor.event.enums.PregCheckResult.YANG;
import static io.terminus.doctor.event.enums.PregCheckResult.from;

/**
 * Desc: 迁移数据
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/27
 */
@Slf4j
@Service
public class DoctorMoveDataService {

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private final DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;
    private final DoctorGroupDao doctorGroupDao;
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorGroupTrackDao doctorGroupTrackDao;
    private final DoctorPigDao doctorPigDao;
    private final DoctorPigTrackDao doctorPigTrackDao;
    private final DoctorPigEventDao doctorPigEventDao;
    private final DoctorMoveBasicService doctorMoveBasicService;
    private final DoctorPigReadService doctorPigReadService;
    private final DoctorMoveWorkflowHandler doctorMoveWorkflowHandler;

    @Autowired
    public DoctorMoveDataService(DoctorMoveDatasourceHandler doctorMoveDatasourceHandler,
                                 DoctorGroupDao doctorGroupDao,
                                 DoctorGroupEventDao doctorGroupEventDao,
                                 DoctorGroupTrackDao doctorGroupTrackDao,
                                 DoctorPigDao doctorPigDao,
                                 DoctorPigTrackDao doctorPigTrackDao,
                                 DoctorPigEventDao doctorPigEventDao,
                                 DoctorMoveBasicService doctorMoveBasicService,
                                 DoctorPigReadService doctorPigReadService,
                                 DoctorMoveWorkflowHandler doctorMoveWorkflowHandler) {
        this.doctorMoveDatasourceHandler = doctorMoveDatasourceHandler;
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorPigDao = doctorPigDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorMoveBasicService = doctorMoveBasicService;
        this.doctorPigReadService = doctorPigReadService;
        this.doctorMoveWorkflowHandler = doctorMoveWorkflowHandler;
    }

    //删除猪场所有猪相关的数据
    public void deleteAllPigs(Long farmId) {
        doctorPigDao.deleteByFarmId(farmId);
        doctorPigEventDao.deleteByFarmId(farmId);
        doctorPigTrackDao.deleteByFarmId(farmId);
    }

    /**
     * 迁移母猪工作流
     */
    @Transactional
    public void moveWorkflow(DoctorFarm farm) {
        DoctorPig search = new DoctorPig();
        search.setPigType(DoctorPig.PIG_TYPE.SOW.getKey());
        search.setIsRemoval(IsOrNot.NO.getValue());
        search.setFarmId(farm.getId());
        doctorMoveWorkflowHandler.handle(RespHelper.orServEx(
                doctorPigReadService.pagingDoctorInfoDtoByPig(search, 1, Integer.MAX_VALUE)).getData());
    }

    /**
     * 迁移猪群
     */
    @Transactional
    public void moveGroup(Long moveId, DoctorFarm farm) {
        //0. 基础数据准备: barn, basic, subUser, changeReason, customer
        Map<String, DoctorBarn> barnMap = doctorMoveBasicService.getBarnMap(farm.getId());
        Map<Integer, Map<String, DoctorBasic>> basicMap = doctorMoveBasicService.getBasicMap();
        Map<String, Long> subMap = doctorMoveBasicService.getSubMap(farm.getOrgId());
        Map<String, DoctorChangeReason> changeReasonMap = doctorMoveBasicService.getReasonMap();
        Map<String, DoctorCustomer> customerMap = doctorMoveBasicService.getCustomerMap(farm.getId());
        Map<String, DoctorBasicMaterial> vaccMap = doctorMoveBasicService.getVaccMap();

        //1. 迁移DoctorGroup
        List<DoctorGroup> groups = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, View_GainCardList.class, "DoctorGroup-GainCardList")).stream()
                .filter(loc -> isFarm(loc.getFarmOutId(), farm.getOutId()))
                .map(gain -> getGroup(farm, gain, barnMap, basicMap, subMap)).collect(Collectors.toList());
        doctorGroupDao.creates(groups);

        //查出刚插入的group, key = outId, 查询猪, 为转种猪事件做准备
        Map<String, DoctorGroup> groupMap = doctorGroupDao.findByFarmId(farm.getId()).stream().collect(Collectors.toMap(DoctorGroup::getOutId, v -> v));
        Map<String, DoctorPig> pigMap = Maps.newHashMap();
        doctorPigDao.findPigsByFarmId(farm.getId()).forEach(pig -> pigMap.put(pig.getPigCode(), pig));

        //2. 迁移DoctorGroupEvent
        List<DoctorGroupEvent> events = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, View_EventListGain.class, "DoctorGroupEvent-EventListGain")).stream()
                .map(gainEvent -> getGroupEvent(groupMap, gainEvent, subMap, barnMap, basicMap, changeReasonMap, customerMap, vaccMap, pigMap))
                .collect(Collectors.toList());
        doctorGroupEventDao.creates(events);

        //查出刚才插入的groupEvent, 按照猪群id groupBy
        Map<Long, List<DoctorGroupEvent>> eventMap = doctorGroupEventDao.findByFarmId(farm.getId()).stream().collect(Collectors.groupingBy(DoctorGroupEvent::getGroupId));

        //3. 迁移DoctorTrack, 先把统计结果转换成map, 在转换track
        String now = DateUtil.toDateTimeString(new Date());
        Map<String, Proc_InventoryGain> gainMap = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, Proc_InventoryGain.class, "DoctorGroupTrack-Proc_InventoryGain", ImmutableMap.of("date", now))).stream()
                .filter(loc -> isFarm(loc.getFarmOutId(), farm.getOutId()))
                .collect(Collectors.toMap(Proc_InventoryGain::getGroupOutId, v -> v));

        List<DoctorGroupTrack> groupTracks = groupMap.values().stream()
                .map(group -> getGroupTrack(group, gainMap.get(group.getOutId()), eventMap.get(group.getId())))
                .collect(Collectors.toList());
        doctorGroupTrackDao.creates(groupTracks);
    }

    /**
     * 迁移母猪公猪
     */
    public void movePig(Long moveId, DoctorFarm farm) {
        //0. 基础数据准备: barn, basic, subUser
        Map<String, DoctorBarn> barnMap = doctorMoveBasicService.getBarnMap(farm.getId());
        Map<Integer, Map<String, DoctorBasic>> basicMap = doctorMoveBasicService.getBasicMap();
        Map<String, Long> subMap = doctorMoveBasicService.getSubMap(farm.getOrgId());
        Map<String, DoctorChangeReason> changeReasonMap = doctorMoveBasicService.getReasonMap();
        Map<String, DoctorCustomer> customerMap = doctorMoveBasicService.getCustomerMap(farm.getId());
        Map<String, DoctorBasicMaterial> vaccMap = doctorMoveBasicService.getVaccMap();

        //1. 迁移boar
        moveBoar(moveId, farm, barnMap, basicMap, changeReasonMap, customerMap, subMap, vaccMap);

        //查出boar, 转换成map
        Map<String, DoctorPig> boarMap = Maps.newHashMap();
        doctorPigDao.findPigsByFarmIdAndPigType(farm.getId(), DoctorPig.PIG_TYPE.BOAR.getKey()).forEach(boar -> boarMap.put(boar.getPigCode(), boar));

        //2. 迁移sow
        moveSow(moveId, farm, basicMap, barnMap, subMap, customerMap, changeReasonMap, boarMap, vaccMap);
    }

    //迁移母猪
    private void moveSow(Long moveId, DoctorFarm farm, Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, DoctorBarn> barnMap,
                         Map<String, Long> subMap, Map<String, DoctorCustomer> customerMap, Map<String, DoctorChangeReason> changeReasonMap,
                         Map<String, DoctorPig> boarMap, Map<String, DoctorBasicMaterial> vaccMap) {
        //1. 迁移DoctorPig
        List<View_SowCardList> sowCards = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, View_SowCardList.class, "DoctorPig-SowCardList")).stream()
                .filter(loc -> isFarm(loc.getFarmOutId(), farm.getOutId()))
                .collect(Collectors.toList());
        doctorPigDao.creates(sowCards.stream().map(card -> getSow(card, farm, basicMap)).collect(Collectors.toList()));

        //查出母猪, 转换成map
        Map<String, DoctorPig> sowMap = doctorPigDao.findPigsByFarmIdAndPigType(farm.getId(), DoctorPig.PIG_TYPE.SOW.getKey()).stream()
                .collect(Collectors.toMap(DoctorPig::getOutId, v -> v));

        //数据量太大, 分成5页获取母猪事件
        List<View_EventListSow> sowEventViews = Lists.newArrayList();
        List<List<View_SowCardList>> sowLists = Lists.partition(sowCards, sowCards.size() / 5 + 1);

        sowLists.forEach(ss -> {
            String sowOutIds = Joiners.COMMA.join(ss.stream().map(s -> brace(s.getPigOutId())).collect(Collectors.toList()));
            sowEventViews.addAll(RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findByHbsSql(moveId, View_EventListSow.class, "DoctorPigEvent-EventListSow", ImmutableMap.of("sowOutIds", sowOutIds))).stream()
                    .filter(loc -> isFarm(loc.getFarmOutId(), farm.getOutId()))
                    .collect(Collectors.toList())
            );
        });

        //2. 迁移DoctorPigEvent
        List<DoctorPigEvent> sowEvents = sowEventViews.stream()
                .map(event -> getSowEvent(event, sowMap, barnMap, basicMap, subMap, customerMap, changeReasonMap, boarMap, vaccMap))
                .collect(Collectors.toList());

        //数据量略大, 分成5份插入吧
        Lists.partition(sowEvents, 5).forEach(doctorPigEventDao::creates);

        //查出母猪事件, 按照母猪分组
        Map<Long, List<DoctorPigEvent>> sowEventMap = doctorPigEventDao.findByFarmIdAndKind(farm.getId(), DoctorPig.PIG_TYPE.SOW.getKey())
                .stream().collect(Collectors.groupingBy(DoctorPigEvent::getPigId));

        //更新relEventId
        updatePigRelEventId(sowEventMap);

        //3. 迁移DoctorPigTrack
        List<DoctorPigTrack> sowTracks = sowCards.stream()
                .map(card -> {
                    DoctorPig sow = sowMap.get(card.getPigOutId());
                    return getSowTrack(card, sow, barnMap, sow == null ? null : sowEventMap.get(sow.getId()), moveId);
                })
                .filter(Arguments::notNull)
                .collect(Collectors.toList());
        doctorPigTrackDao.creates(sowTracks);

        //4. 更新公猪的全部配种次数
        updateBoarCurrentParity(sowEvents);
    }

    //更新公猪的配种数(根据配种事件)
    private void updateBoarCurrentParity(List<DoctorPigEvent> sowEvents) {
        sowEvents.stream()
                .filter(e -> Objects.equals(e.getType(), PigEvent.MATING.getKey()))
                .map(m -> JSON_MAPPER.fromJson(m.getExtra(), DoctorMatingDto.class))
                .filter(p -> p != null && p.getMatingBoarPigId() != null)
                .collect(Collectors.groupingBy(DoctorMatingDto::getMatingBoarPigId))
                .forEach((k, v) -> doctorPigTrackDao.updateBoarCurrentParity(k, v.size()));
    }

    //拼接母猪
    private DoctorPig getSow(View_SowCardList card, DoctorFarm farm, Map<Integer, Map<String, DoctorBasic>> basicMap) {
        DoctorPig sow = new DoctorPig();
        sow.setOrgId(farm.getOrgId());
        sow.setOrgName(farm.getOrgName());
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

    //拼接母猪事件(boarMap: key = boarCode, value = DoctorPig, 用于配种事件)
    private DoctorPigEvent getSowEvent(View_EventListSow event, Map<String, DoctorPig> sowMap, Map<String, DoctorBarn> barnMap,
                                       Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, Long> subMap,
                                       Map<String, DoctorCustomer> customerMap, Map<String, DoctorChangeReason> changeReasonMap,
                                       Map<String, DoctorPig> boarMap, Map<String, DoctorBasicMaterial> vaccMap) {
        DoctorPig sow = sowMap.get(event.getSowOutId());
        if (sow == null) {
            return null;
        }

        DoctorPigEvent sowEvent = new DoctorPigEvent();
        sowEvent.setOrgId(sow.getOrgId());
        sowEvent.setOrgName(sow.getOrgName());
        sowEvent.setFarmId(sow.getFarmId());
        sowEvent.setFarmName(sow.getFarmName());
        sowEvent.setPigId(sow.getId());
        sowEvent.setPigCode(sow.getPigCode());

        sowEvent.setEventAt(event.getEventAt());
        sowEvent.setKind(sow.getPigType());       // 猪类(公猪2,母猪1)
        sowEvent.setName(event.getEventName());
        sowEvent.setDesc(event.getEventDesc());
        sowEvent.setOutId(event.getEventOutId());
        sowEvent.setRemark(event.getRemark());

        //事件类型, (如果是转舍类型, 重新判断后还会覆盖掉)
        PigEvent eventType = PigEvent.from(event.getEventName());
        sowEvent.setType(eventType == null ? null : eventType.getKey());

        DoctorBarn barn = barnMap.get(event.getBarnOutId());
        if (barn != null) {
            sowEvent.setBarnId(barn.getId());
            sowEvent.setBarnName(barn.getName());
        }
        return getSowEventExtra(eventType, sowEvent, event, subMap, basicMap, barnMap, customerMap, changeReasonMap, boarMap, vaccMap);
    }

    //拼接母猪事件extra字段
    private DoctorPigEvent getSowEventExtra(PigEvent eventType, DoctorPigEvent sowEvent, View_EventListSow event, Map<String, Long> subMap,
                                            Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, DoctorBarn> barnMap,
                                            Map<String, DoctorCustomer> customerMap, Map<String, DoctorChangeReason> changeReasonMap,
                                            Map<String, DoctorPig> boarMap, Map<String, DoctorBasicMaterial> vaccMap) {

        if (eventType == null) {
            return sowEvent;
        }

        //switch 母猪事件
        switch (eventType) {
            case CHG_LOCATION:  //转舍, 根据转入猪舍类型拆成4种: 转舍, 转入妊娠舍, 转入配种舍, 去分娩
                DoctorChgLocationDto transBarn = new DoctorChgLocationDto();
                transBarn.setChangeLocationDate(event.getEventAt());
                DoctorBarn fromBarn = barnMap.get(event.getBarnOutId());    //来源猪舍
                if (fromBarn != null) {
                    transBarn.setChgLocationFromBarnId(fromBarn.getId());
                    transBarn.setChgLocationFromBarnName(fromBarn.getName());
                }
                DoctorBarn toBarn = barnMap.get(event.getToBarnOutId());    //去往猪舍
                if (toBarn != null) {
                    transBarn.setChgLocationToBarnId(toBarn.getId());
                    transBarn.setChgLocationToBarnName(toBarn.getName());
                    sowEvent.setType(getSowTransBarnEventType(toBarn.getPigType()));     //根据转入的猪舍类型, 重新覆盖事件类型, 这一步很重要
                }
                sowEvent.setExtra(JSON_MAPPER.toJson(transBarn));
                break;
            case CHG_FARM:      //转场
                DoctorChgFarmDto tranFarm = new DoctorChgFarmDto();
                tranFarm.setChgFarmDate(event.getEventAt());
                tranFarm.setFromFarmId(sowEvent.getFarmId());
                tranFarm.setFromFarmName(sowEvent.getFarmName());
                tranFarm.setFromBarnId(sowEvent.getBarnId());
                tranFarm.setFromBarnName(sowEvent.getBarnName());
                tranFarm.setRemark(event.getChgReason());
                sowEvent.setExtra(JSON_MAPPER.toJson(tranFarm));
                break;
            case CONDITION:     //体况
                DoctorConditionDto condition = new DoctorConditionDto();
                condition.setConditionDate(event.getEventAt()); //体况日期
                condition.setConditionJudgeScore(Double.valueOf(event.getScore()));    //体况评分
                condition.setConditionWeight(event.getEventWeight()); // 体况重量
                condition.setConditionBackWeight(event.getBackFat()); // 背膘
                condition.setConditionRemark(event.getRemark()); //体况注解
                sowEvent.setExtra(JSON_MAPPER.toJson(condition));
                break;
            case DISEASE:       //疾病
                DoctorDiseaseDto disease = new DoctorDiseaseDto();
                disease.setDiseaseDate(event.getEventAt());

                DoctorBasic ddd = basicMap.get(DoctorBasic.Type.DISEASE.getValue()).get(event.getDiseaseName());
                disease.setDiseaseId(ddd == null ? null : ddd.getId());
                disease.setDiseaseName(event.getDiseaseName());
                disease.setDiseaseStaff(event.getStaffName());
                disease.setDiseaseRemark(event.getRemark());
                sowEvent.setExtra(JSON_MAPPER.toJson(disease));
                break;
            case VACCINATION:   //防疫
                DoctorVaccinationDto vacc = new DoctorVaccinationDto();
                vacc.setVaccinationDate(event.getEventAt());

                //疫苗
                DoctorBasicMaterial vaccBasic = vaccMap.get(event.getDisease());
                vacc.setVaccinationId(vaccBasic == null ? null : vaccBasic.getId());
                vacc.setVaccinationName(event.getDisease());  //其实是疫苗名称
                vacc.setVaccinationStaffId(subMap.get(event.getChgReason()));
                vacc.setVaccinationStaffName(event.getStaffName());
                vacc.setVaccinationRemark(event.getRemark());
                sowEvent.setExtra(JSON_MAPPER.toJson(vacc));
                break;
            case REMOVAL:       //离场
                DoctorRemovalDto removal = getSowRemovalExtra(event, customerMap, basicMap, barnMap, changeReasonMap);
                sowEvent.setExtra(JSON_MAPPER.toJson(removal));
                sowEvent.setChangeTypeId(removal.getChgTypeId());
                sowEvent.setPrice(removal.getPrice());
                sowEvent.setAmount(removal.getSum());
                break;
            case ENTRY:         //进场
                sowEvent.setExtra(JSON_MAPPER.toJson(getSowEntryExtra(event, basicMap, barnMap)));
                break;
            case MATING:        //配种
                DoctorMatingDto mating = getSowMatingExtra(event, boarMap);
                sowEvent.setMattingDate(event.getEventAt());                //配种时间
                sowEvent.setExtra(JSON_MAPPER.toJson(mating));
                break;
            case PREG_CHECK:    //妊娠检查
                DoctorPregChkResultDto checkResult = getSowPregCheckExtra(event);
                sowEvent.setPregCheckResult(checkResult.getCheckResult());  //妊娠检查结果
                sowEvent.setCheckDate(event.getEventAt());                  //检查时间
                sowEvent.setExtra(JSON_MAPPER.toJson(checkResult));
                break;
            case ABORTION:      //流产, 只记录事件即可, 旧猪场软件并没有流产原因
                sowEvent.setAbortionDate(event.getEventAt());             //流产事件
                sowEvent.setExtra(JSON_MAPPER.toJson(DoctorAbortionDto.builder().abortionDate(event.getEventAt()).build()));
                break;
            case FARROWING:     //分娩
                DoctorFarrowingDto farrowing = getSowFarrowExtra(event, barnMap);
                sowEvent.setLiveCount(farrowing.getFarrowingLiveCount()); //活仔数
                sowEvent.setHealthCount(farrowing.getHealthCount());      //健仔数
                sowEvent.setWeakCount(farrowing.getWeakCount());          //弱仔数
                sowEvent.setMnyCount(farrowing.getMnyCount());            //木乃伊数
                sowEvent.setJxCount(farrowing.getJxCount());              //畸形数
                sowEvent.setDeadCount(farrowing.getDeadCount());          //死胎数
                sowEvent.setBlackCount(farrowing.getBlackCount());        //黑胎数
                sowEvent.setFarrowingDate(event.getEventAt());            //分娩时间
                sowEvent.setExtra(JSON_MAPPER.toJson(farrowing));
                break;
            case WEAN:          //断奶
                DoctorPartWeanDto wean = getSowWeanExtra(event);
                sowEvent.setWeanCount(wean.getPartWeanPigletsCount());  //断奶数
                sowEvent.setWeanAvgWeight(wean.getPartWeanAvgWeight()); //断奶均重
                sowEvent.setPartweanDate(event.getEventAt());           //断奶时间
                sowEvent.setExtra(JSON_MAPPER.toJson(wean));
                break;
            case FOSTERS:       //拼窝
                sowEvent.setExtra(JSON_MAPPER.toJson(getSowFosterExtra(event, basicMap)));
                break;
            case FOSTERS_BY:    //被拼窝
                sowEvent.setExtra(JSON_MAPPER.toJson(getSowFosterExtra(event, basicMap)));
                break;
            case PIGLETS_CHG:   //仔猪变动
                sowEvent.setExtra(JSON_MAPPER.toJson(getSowPigletChangeExtra(event, basicMap, changeReasonMap, customerMap)));
                break;
            default:
                break;
        }
        return sowEvent;
    }

    //拼接母猪猪离场extra
    private DoctorRemovalDto getSowRemovalExtra(View_EventListSow event, Map<String, DoctorCustomer> customerMap,
                                                Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, DoctorBarn> barnMap,
                                                Map<String, DoctorChangeReason> changeReasonMap) {
        DoctorRemovalDto remove = new DoctorRemovalDto();

        //变动类型, 变动原因
        DoctorBasic changeType = basicMap.get(DoctorBasic.Type.CHANGE_TYPE.getValue()).get(event.getChangeTypeName());
        remove.setChgTypeId(changeType == null ? null : changeType.getId());
        remove.setChgTypeName(event.getChgType());
        DoctorChangeReason reason = changeReasonMap.get(event.getChgReason());
        remove.setChgReasonId(reason == null ? null : reason.getId());
        remove.setChgReasonName(event.getChgReason());

        //重量 金额等
        remove.setWeight(event.getEventWeight());
        remove.setPrice(event.getPrice());
        remove.setSum(event.getAmount());
        remove.setRemark(event.getRemark());

        //猪舍 客户
        DoctorBarn barn = barnMap.get(event.getBarnOutId());
        remove.setToBarnId(barn == null ? null : barn.getId());
        DoctorCustomer customer = customerMap.get(event.getCustomer());
        remove.setCustomerId(customer == null ? null : customer.getId());
        return remove;
    }

    //拼接母猪进场extra
    private DoctorFarmEntryDto getSowEntryExtra(View_EventListSow event, Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, DoctorBarn> barnMap) {
        DoctorFarmEntryDto entry = new DoctorFarmEntryDto();

        entry.setEarCode(event.getPigCode()); //耳号取猪号
        entry.setParity(event.getParity()); //当前事件胎次
        entry.setLeft(event.getLeftCount());
        entry.setRight(event.getRightCount());
        entry.setPigType(DoctorPig.PIG_TYPE.SOW.getKey());  //类型: 母猪
        entry.setPigCode(event.getPigCode());       // pig code 猪 编号
        entry.setBirthday(event.getBirthDate());      // 猪生日
        entry.setInFarmDate(event.getInFarmDate());    // 进厂时间
        entry.setFatherCode(event.getPigFatherCode());    // 父类Code （非必填）
        entry.setMotherCode(event.getPigMotherCode());    // 母Code （非必填）
        entry.setEntryMark(event.getRemark());     // 非必填
        entry.setSource(event.getSource());

        DoctorBarn barn = barnMap.get(event.getBarnOutId());
        if (barn != null) {
            entry.setBarnId(barn.getId());
            entry.setBarnName(barn.getName());
        }
        //品种 品系
        DoctorBasic breed = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(event.getBreed());
        entry.setBreed(breed == null ? null : breed.getId());         //品种Id （basic Info）
        entry.setBreedName(event.getBreed());     //品种名称

        DoctorBasic gene = basicMap.get(DoctorBasic.Type.GENETICS.getValue()).get(event.getGenetic());
        entry.setBreedType(gene == null ? null : gene.getId());     //品系Id  (basic info)
        entry.setBreedTypeName(event.getGenetic()); //品系名称
        return entry;
    }

    //拼接母猪配种事件extra
    private DoctorMatingDto getSowMatingExtra(View_EventListSow event, Map<String, DoctorPig> boarMap) {
        DoctorMatingDto mating = new DoctorMatingDto();
        mating.setMatingDate(event.getEventAt()); // 配种日期
        mating.setMatingStaff(event.getStaffName()); // 配种人员
        mating.setMattingMark(event.getRemark()); // 配种mark
        mating.setJudgePregDate(event.getFarrowDate()); //预产日期

        // 配种类型
        MatingType type = MatingType.from(event.getServiceType());
        mating.setMatingType(type == null ? null : type.getKey());

        //配种公猪
        DoctorPig matingPig = boarMap.get(event.getBoarCode());
        mating.setMatingBoarPigId(matingPig == null ? null : matingPig.getId());
        mating.setMatingBoarPigCode(event.getBoarCode());
        return mating;
    }

    //拼接母猪妊娠检查extra
    private DoctorPregChkResultDto getSowPregCheckExtra(View_EventListSow event) {
        DoctorPregChkResultDto preg = new DoctorPregChkResultDto();
        preg.setCheckDate(event.getEventAt());
        preg.setCheckMark(event.getRemark());

        //妊娠检查结果
        PregCheckResult result = from(event.getPregCheckResult());
        preg.setCheckResult(result == null ? null : result.getKey());
        return preg;
    }

    //拼接母猪分娩extra
    private DoctorFarrowingDto getSowFarrowExtra(View_EventListSow event, Map<String, DoctorBarn> barnMap) {
        DoctorFarrowingDto farrow = new DoctorFarrowingDto();
        farrow.setFarrowingDate(event.getEventAt());       // 分娩日期
        farrow.setWeakCount(event.getWeakCount());         // 弱崽数量
        farrow.setMnyCount(event.getMummyCount());         // 木乃伊数量
        farrow.setJxCount(event.getJxCount());             // 畸形数量
        farrow.setDeadCount(event.getDeadCount());         // 死亡数量
        farrow.setBlackCount(event.getBlackCount());       // 黑太数量
        farrow.setHealthCount(event.getHealthyCount());    // 健仔数量
        farrow.setFarrowingLiveCount(event.getHealthyCount() + event.getWeakCount()); //活仔数 = 健 + 弱
        farrow.setFarrowRemark(event.getRemark());
        farrow.setBirthNestAvg(event.getEventWeight());    //出生窝重
        farrow.setFarrowStaff1(event.getStaffName());  //接生员1
        farrow.setFarrowStaff2(event.getStaffName());  //接生员2
        farrow.setIsHelp(event.getNeedHelp());     //  是否帮助
        farrow.setFarrowIsSingleManager(event.getIsSingleManage());    //是否个体管理
        farrow.setGroupCode(event.getToGroupCode());   // 仔猪猪群Code
        farrow.setNestCode(event.getNestCode()); // 窝号

        //分娩类型
        FarrowingType farrowingType = FarrowingType.from(event.getFarrowType());
        farrow.setFarrowingType(farrowingType == null ? null : farrowingType.getKey());

        //分娩猪舍
        DoctorBarn farrowBarn = barnMap.get(event.getBarnOutId());
        if (farrowBarn != null) {
            farrow.setBarnId(farrowBarn.getId());
            farrow.setBarnName(farrowBarn.getName());
        }
        return farrow;
    }

    //拼接断奶事件extra
    private DoctorPartWeanDto getSowWeanExtra(View_EventListSow event) {
        DoctorPartWeanDto wean = new DoctorPartWeanDto();
        wean.setPartWeanDate(event.getEventAt()); //断奶日期
        wean.setPartWeanRemark(event.getRemark());
        wean.setPartWeanPigletsCount(event.getWeanCount()); //断奶数量
        wean.setPartWeanAvgWeight(event.getWeanWeight());   //断奶平均重量
        return wean;
    }

    //拼接拼窝事件extra
    private DoctorFostersDto getSowFosterExtra(View_EventListSow event, Map<Integer, Map<String, DoctorBasic>> basicMap) {
        DoctorFostersDto foster = new DoctorFostersDto();
        foster.setFostersDate(event.getEventAt());   // 拼窝日期
        foster.setFostersCount(event.getNetOutCount());   //  拼窝数量
        foster.setFosterTotalWeight(event.getWeanWeight());   //拼窝总重量
        foster.setFosterSowCode(event.getDisease());      //拼窝母猪号
        foster.setFosterSowOutId(event.getNurseSow());      //拼窝母猪号

        //寄养原因
        DoctorBasic reason = basicMap.get(DoctorBasic.Type.FOSTER_REASON.getValue()).get(event.getFosterReasonName());
        foster.setFosterReason(reason == null ? null : reason.getId());
        foster.setFosterReasonName(event.getFosterReasonName());
        foster.setFosterRemark(event.getRemark());
        return foster;
    }

    //拼接母猪变动extra
    private DoctorPigletsChgDto getSowPigletChangeExtra(View_EventListSow event, Map<Integer, Map<String, DoctorBasic>> basicMap,
                                                        Map<String, DoctorChangeReason> changeReasonMap, Map<String, DoctorCustomer> customerMap) {
        DoctorPigletsChgDto change = new DoctorPigletsChgDto();
        change.setPigletsChangeDate(event.getEventAt()); // 仔猪变动日期
        change.setPigletsCount(event.getChgCount());   // 仔猪数量
        change.setPigletsWeight(event.getEventWeight());  // 变动重量 (非必填)
        change.setPigletsPrice(event.getPrice());   // 变动价格 （非必填）
        change.setPigletsSum(event.getAmount()); //  总价（非必填）
        change.setPigletsMark(event.getRemark());  //标识(非必填)
        change.setPigletsChangeTypeName(event.getChangeTypeName());
        change.setPigletsChangeReasonName(event.getChgReason());   // 仔猪变动原因

        //变动类型, 原因, 客户
        DoctorBasic changeType = basicMap.get(DoctorBasic.Type.CHANGE_TYPE.getValue()).get(event.getChangeTypeName());
        change.setPigletsChangeType(changeType == null ? null : changeType.getId());   // 仔猪变动类型
        DoctorChangeReason reason = changeReasonMap.get(event.getChgReason());
        change.setPigletsChangeReason(reason == null ? null : reason.getId());   // 仔猪变动原因
        DoctorCustomer customer = customerMap.get(event.getCustomer());
        change.setPigletsCustomerId(customer == null ? null : customer.getId());    //客户Id （非必填）
        return change;
    }

    //根据猪舍类型重新获取事件的类型
    private Integer getSowTransBarnEventType(Integer barnType) {
        if (Objects.equals(PigType.MATE_SOW.getValue(), barnType)) {
            return PigEvent.TO_MATING.getKey();
        } else if (Objects.equals(PigType.PREG_SOW.getValue(), barnType)) {
            return PigEvent.TO_PREG.getKey();
        } else if (Objects.equals(PigType.FARROW_PIGLET.getValue(), barnType)) {
            return PigEvent.TO_FARROWING.getKey();
        } else {
            return PigEvent.CHG_LOCATION.getKey();
        }
    }

    //拼接母猪跟踪
    private DoctorPigTrack getSowTrack(View_SowCardList card, DoctorPig sow, Map<String, DoctorBarn> barnMap, List<DoctorPigEvent> events, Long moveId) {
        if (sow == null) {
            return null;
        }

        //即将离场的状态是因为录离场事件录错了, 撤销后, 会到即将离场状态, 其实应该是上次的状态
        if ("即将离场".equals(card.getStatus())) {
            card.setStatus(getLeaveType(moveId, card.getPigOutId()));
        }

        //母猪状态枚举
        PigStatus status = PigStatus.from(card.getStatus());

        DoctorPigTrack track = new DoctorPigTrack();
        track.setFarmId(sow.getFarmId());
        track.setPigId(sow.getId());
        track.setPigType(sow.getPigType());
        track.setStatus(status == null ? null : status.getKey());
        track.setIsRemoval(sow.getIsRemoval());
        track.setWeight(card.getWeight());
        track.setOutFarmDate(DateUtil.toDate(card.getOutFarmDate()));
        track.setRemark(card.getRemark());
        track.setCurrentParity(card.getCurrentParity());

        if (notEmpty(events)) {
            //按照时间 asc 排序
            events = events.stream().sorted((a, b) -> a.getEventAt().compareTo(b.getEventAt())).collect(Collectors.toList());
            DoctorPigEvent lastEvent = events.get(events.size() - 1);
            track.setExtra(lastEvent.getExtra());   //extra字段保存最后一次event的extra

            //关联事件ids, Map<Parity, EventIds>, 按照胎次分组
            track.setRelEventIds(getSowRelEventIds(card.getFirstParity(), events));

            //母猪当前配种次数
            track.setCurrentMatingCount(getSowCurrentMatingCount(events, sow));

            //更新胎次 倒叙
            updateParity(events, track);

            //更新母猪事件当中的配种类型
            updateDoctorMateType(events);

            //更新事件的非生产天数
            updateNPD(events);

            //更新初配事件的是否已经分娩 和是否已经怀孕 的标志位
            updateFlag(events);

            //统计孕期 和 哺乳期
            updateDuring(events);

            //更新event
            events.forEach(doctorPigEventDao::update);
        }

        //猪舍
        DoctorBarn barn = barnMap.get(card.getCurrentBarnOutId());
        if (barn != null) {
            track.setCurrentBarnId(barn.getId());
            track.setCurrentBarnName(barn.getName());
        }
        return track;
    }

    //母猪按胎次分组关联事件
    private static String getSowRelEventIds(Integer firstParity, List<DoctorPigEvent> events) {
        Map<Integer, String> relMap = Maps.newHashMap();
        List<Long> ids = Lists.newArrayList();
        int i = 0;
        for (DoctorPigEvent event : events) {
            if (Objects.equals(event.getType(), PigEvent.MATING.getKey())) {
                relMap.put(firstParity + i, Joiners.COMMA.join(ids));  //遇到配种事件, 当做一个胎次
                ids.clear();                //清空list, 作为下一个胎次
                ids.add(event.getId());     //加入本次事件
                i++;                        //游标加1

            } else {
                ids.add(event.getId());
            }
        }
        //不要忘了最后一次
        relMap.put(firstParity + i, Joiners.COMMA.join(ids));
        return JSON_MAPPER.toJson(relMap);
    }

    //母猪的当前配种次数(初配, 复配等等)
    private static int getSowCurrentMatingCount(List<DoctorPigEvent> events, DoctorPig sow) {
        //离场的都置成0
        Boolean leave = false;
        if (Objects.equals(sow.getIsRemoval(), IsOrNot.YES.getValue())) {
            leave = true;
        }

        //未离场的重新从头判断下母猪的当前配种次数
        int count = 0;
        for (DoctorPigEvent event : events) {
            if (Objects.equals(event.getType(), PigEvent.MATING.getKey())) {
                count++;
                //给event赋值当前配种次数
                event.setCurrentMatingCount(count);
            } else if (Objects.equals(event.getType(), PigEvent.TO_MATING.getKey()) || isNotPreg(event) ||
                    Objects.equals(event.getType(), PigEvent.ABORTION.getKey())) {
                count = 0;
            }
        }
        if (leave) {
            return 0;
        } else {
            return count;
        }

    }

    //更新胎次
    private static void updateParity(List<DoctorPigEvent> events, DoctorPigTrack track) {
        List<DoctorPigEvent> revertList = Lists.reverse(events);
        int currentParity = track.getCurrentParity();
        for (DoctorPigEvent event : revertList) {
            //如果是分娩事件
            if (Objects.equals(event.getType(), PigEvent.FARROWING.getKey())) {
                int num = currentParity - 1;
                event.setParity(num);
                continue;
            }
            event.setParity(currentParity);
        }
    }

    //更新母猪事件的配种类型
    private static void updateDoctorMateType(List<DoctorPigEvent> events) {
        DoctorPigEvent lastFlag = null;
        for (DoctorPigEvent event : events) {
            if (Objects.equals(event.getType(), PigEvent.ENTRY.getKey()) ||
                    Objects.equals(event.getType(), PigEvent.PREG_CHECK.getKey()) ||
                    Objects.equals(event.getType(), PigEvent.WEAN.getKey()) ||
                    Objects.equals(event.getType(), PigEvent.ABORTION.getKey())
                    ) {
                lastFlag = event;
                continue;
            }

            //配种事件而且是初陪
            if (Objects.equals(event.getType(), PigEvent.MATING.getKey()) && event.getCurrentMatingCount() == 1) {
                if (lastFlag == null) {
                    log.warn("sow data wrong...");
                    log.warn("sow data event:{}", event);
                    continue;
                }
                //如果是进场
                if (Objects.equals(lastFlag.getType(), PigEvent.ENTRY.getKey()) && event.getParity() == 1) {
                    //第一个胎次
                    event.setDoctorMateType(DoctorMatingType.HP.getKey());
                }

                //如果是妊娠检查
                if (Objects.equals(lastFlag.getType(), PigEvent.PREG_CHECK.getKey())) {
                    if (lastFlag.getPregCheckResult() != null) {
                        switch (lastFlag.getPregCheckResult()) {
                            case 2:
                                event.setDoctorMateType(DoctorMatingType.YP.getKey());
                                continue;
                            case 3:
                                event.setDoctorMateType(DoctorMatingType.LPC.getKey());
                                continue;
                            case 4:
                                event.setDoctorMateType(DoctorMatingType.FP.getKey());
                                continue;
                        }
                    } else {
                        log.warn("event sow preg check result is null, event {}", lastFlag);
                    }
                    continue;
                }

                //如果是断奶
                if (Objects.equals(lastFlag.getType(), PigEvent.WEAN.getKey())) {
                    event.setDoctorMateType(DoctorMatingType.DP.getKey());
                }

                //如果是断奶
                if (Objects.equals(lastFlag.getType(), PigEvent.ABORTION.getKey())) {
                    event.setDoctorMateType(DoctorMatingType.LPL.getKey());
                }
            }
        }
    }

    //更新事件的非生产天数
    private static void updateNPD(List<DoctorPigEvent> events) {
        //上一个初配事件
        DoctorPigEvent lastMateFlag = null;
        //上一个断奶事件
        DoctorPigEvent lastWeanFlag = null;
        //上一个进场事件
        DoctorPigEvent lastEnterFlag = null;
        for (DoctorPigEvent event : events) {
            //如果当前事件是进场事件, 进行记录
            if (Objects.equals(event.getType(), PigEvent.ENTRY.getKey()) && lastMateFlag != null) {
                lastEnterFlag = event;
                continue;
            }

            //找到上一个初配事件
            if (Objects.equals(event.getType(), PigEvent.MATING.getKey()) && event.getCurrentMatingCount() == 1) {
                lastMateFlag = event;
                if (lastWeanFlag != null && lastMateFlag != null) {
                    int days = Days.daysBetween(new DateTime(lastMateFlag.getMattingDate()), new DateTime(lastWeanFlag.getPartweanDate())).getDays();
                    event.setDpnpd(Math.abs(days));
                    event.setNpd(Math.abs(days));
                    lastWeanFlag = null;
                    continue;
                }

                if (lastEnterFlag != null && lastMateFlag != null) {
                    int days = Days.daysBetween(new DateTime(lastMateFlag.getMattingDate()), new DateTime(lastEnterFlag.getEventAt())).getDays();
                    event.setJpnpd(Math.abs(days));
                    event.setNpd(Math.abs(days));
                    lastEnterFlag = null;
                    continue;
                }
                continue;

            }

            //当前事件是妊娠检查事件
            if (Objects.equals(event.getType(), PigEvent.PREG_CHECK.getKey()) && lastMateFlag != null) {
                int days = Days.daysBetween(new DateTime(lastMateFlag.getMattingDate()), new DateTime(event.getCheckDate())).getDays();
                if (event.getPregCheckResult() != null) {
                    switch (event.getPregCheckResult()) {
                        case 2:
                            //配种到阴性
                            event.setPynpd(Math.abs(days));
                            event.setNpd(Math.abs(days));
                            continue;
                        case 3:
                            //配种到流产
                            event.setPlnpd(Math.abs(days));
                            event.setNpd(Math.abs(days));
                            continue;
                        case 4:
                            //配种到返情
                            event.setPfnpd(Math.abs(days));
                            event.setNpd(Math.abs(days));
                            continue;
                    }
                } else {
                    log.warn("event sow preg check result is null, event {}", event);
                }
                continue;

            }

            //当前事件是流产事件
            if (Objects.equals(event.getType(), PigEvent.ABORTION.getKey()) && lastMateFlag != null) {
                int days = Days.daysBetween(new DateTime(lastMateFlag.getMattingDate()), new DateTime(event.getAbortionDate())).getDays();
                event.setPlnpd(Math.abs(days));
                event.setNpd(Math.abs(days));
                continue;
            }

            // 离场事件
            if (Objects.equals(event.getType(), PigEvent.REMOVAL.getKey()) && lastMateFlag != null) {
                //如果是死亡原因
                if (Objects.equals(event.getChangeTypeId(), DoctorBasicEnums.DEAD.getId())) {
                    int days = Days.daysBetween(new DateTime(lastMateFlag.getMattingDate()), new DateTime(event.getEventAt())).getDays();
                    event.setPsnpd(Math.abs(days));
                    event.setNpd(Math.abs(days));
                    continue;
                }

                //如果是淘汰原因
                if (Objects.equals(event.getChangeTypeId(), DoctorBasicEnums.ELIMINATE.getId())) {
                    int days = Days.daysBetween(new DateTime(lastMateFlag.getMattingDate()), new DateTime(event.getEventAt())).getDays();
                    event.setPtnpd(Math.abs(days));
                    event.setNpd(Math.abs(days));
                    continue;
                }
            }

            //如果当前事件是断奶事件, 进行记录
            if (Objects.equals(event.getType(), PigEvent.WEAN.getKey()) && lastMateFlag != null) {
                lastWeanFlag = event;
                continue;
            }
        }
    }

    //更新初配事件的是否已经分娩 和是否已经怀孕 的标志位
    private static void updateFlag(List<DoctorPigEvent> events) {
        //上一个初配事件
        DoctorPigEvent lastMateFlag = null;
        for (DoctorPigEvent event : events) {
            //找到上一个初配事件
            if (Objects.equals(event.getType(), PigEvent.MATING.getKey()) && event.getCurrentMatingCount() == 1) {
                lastMateFlag = event;
                continue;
            }
            //当前事件是妊娠检查事件 而且检查结果是阳性
            if (Objects.equals(event.getType(), PigEvent.WEAN.getKey())
                    && lastMateFlag != null
                    && Objects.equals(event.getPregCheckResult(), PregCheckResult.YANG.getKey())) {
                lastMateFlag.setIsImpregnation(1);
                continue;
            }

            //当前事件是分娩事件
            if (Objects.equals(event.getType(), PigEvent.FARROWING.getKey())
                    && lastMateFlag != null) {
                //更新分娩的标志位
                lastMateFlag.setIsDelivery(1);
                continue;
            }
        }
    }

    //统计孕期 和 哺乳期
    private static void updateDuring(List<DoctorPigEvent> events) {
        //上一个初配事件
        DoctorPigEvent lastMateFlag = null;
        //上一个分娩事件
        DoctorPigEvent lastFarrowingFlag = null;
        for (DoctorPigEvent event : events) {
            //找到上一个初配事件
            if (Objects.equals(event.getType(), PigEvent.MATING.getKey()) && event.getCurrentMatingCount() == 1) {
                lastMateFlag = event;
                continue;
            }
            //当前事件是分娩事件
            if (Objects.equals(event.getType(), PigEvent.FARROWING.getKey())
                    && lastMateFlag != null) {
                lastFarrowingFlag = event;
                //统计孕期
                int days = Days.daysBetween(new DateTime(event.getFarrowingDate()), new DateTime(lastMateFlag.getMattingDate())).getDays();
                event.setPregDays(Math.abs(days));
                continue;
            }

            //当前事件是断奶事件
            if (Objects.equals(event.getType(), PigEvent.WEAN.getKey())
                    && lastFarrowingFlag != null) {
                //统计哺乳期
                int days = Days.daysBetween(new DateTime(event.getPartweanDate()), new DateTime(lastFarrowingFlag.getFarrowingDate())).getDays();
                event.setFeedDays(Math.abs(days));
                continue;
            }
        }
    }

    //判断妊娠检查结果
    private static boolean isNotPreg(DoctorPigEvent event) {
        if (!Objects.equals(event.getType(), PigEvent.PREG_CHECK.getKey())) {
            return false;
        }
        DoctorPregChkResultDto result = JSON_MAPPER.fromJson(event.getExtra(), DoctorPregChkResultDto.class);
        return result != null && !Objects.equals(result.getCheckResult(), YANG.getKey());
    }

    //获取即将离场的母猪状态
    private String getLeaveType(Long moveId, String sowOutId) {
        try {
            List<SowOutFarmSoon> soons = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findByHbsSql(moveId, SowOutFarmSoon.class, "SowOutFarmSoon", ImmutableMap.of("sowOutId", sowOutId)));
            return notEmpty(soons) ? soons.get(0).getLeaveType() : "";
        } catch (Exception e) {
            log.error("get sow leave type failed, sowOutId:{}, cause:{}", sowOutId, Throwables.getStackTraceAsString(e));
            return "";
        }
    }

    //迁移公猪
    private void moveBoar(Long moveId, DoctorFarm farm, Map<String, DoctorBarn> barnMap, Map<Integer, Map<String, DoctorBasic>> basicMap,
                          Map<String, DoctorChangeReason> changeReasonMap, Map<String, DoctorCustomer> customerMap, Map<String, Long> subMap, Map<String, DoctorBasicMaterial> vaccMap) {
        //1. 迁移DoctorPig
        List<View_BoarCardList> boarCards = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, View_BoarCardList.class, "DoctorPig-BoarCardList")).stream()
                .filter(loc -> isFarm(loc.getFarmOutId(), farm.getOutId()))
                .collect(Collectors.toList());
        doctorPigDao.creates(boarCards.stream().map(card -> getBoar(card, farm, basicMap)).collect(Collectors.toList()));

        //查出公猪, 转换成map
        Map<String, DoctorPig> boarMap = doctorPigDao.findPigsByFarmIdAndPigType(farm.getId(), DoctorPig.PIG_TYPE.BOAR.getKey()).stream()
                .collect(Collectors.toMap(DoctorPig::getOutId, v -> v));

        //2. 迁移DoctorPigEvent
        List<DoctorPigEvent> boarEvents = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, View_EventListBoar.class, "DoctorPigEvent-EventListBoar")).stream()
                .filter(loc -> isFarm(loc.getFarmOutId(), farm.getOutId()))
                .map(event -> getBoarEvent(event, boarMap, barnMap, basicMap, subMap, customerMap, changeReasonMap, vaccMap)).collect(Collectors.toList());
        doctorPigEventDao.creates(boarEvents);

        //查出公猪事件, 按照公猪分组
        Map<Long, List<DoctorPigEvent>> boarEventMap = doctorPigEventDao.findByFarmIdAndKind(farm.getId(), DoctorPig.PIG_TYPE.BOAR.getKey())
                .stream().collect(Collectors.groupingBy(DoctorPigEvent::getPigId));

        //更新relEventId
        updatePigRelEventId(boarEventMap);

        //3. 迁移DoctorPigTrack
        List<DoctorPigTrack> boarTracks = boarCards.stream()
                .map(card -> {
                    DoctorPig boar = boarMap.get(card.getPigOutId());
                    return getBoarTrack(card, boar, barnMap, boar == null ? null : boarEventMap.get(boar.getId()));
                })
                .filter(Arguments::notNull)
                .collect(Collectors.toList());
        doctorPigTrackDao.creates(boarTracks);
    }

    //更新猪的relEventId, 后面的事件存一下前面事件的id
    private void updatePigRelEventId(Map<Long, List<DoctorPigEvent>> pigEventMap) {
        pigEventMap.values().forEach(events -> {
            //时间 ASC 排序
            events = events.stream().sorted((a, b) -> a.getEventAt().compareTo(b.getEventAt())).collect(Collectors.toList());
            List<Long> eventIds = events.stream().map(DoctorPigEvent::getId).collect(Collectors.toList());
            eventIds.add(0, null);  // 首位增加个null, 作为第一个事件的relEventId

            for (int i = 0; i < events.size(); i++) {
                DoctorPigEvent e = events.get(i);
                e.setRelEventId(eventIds.get(i));
                doctorPigEventDao.updateRelEventId(e);
            }
        });
    }

    //拼接公猪
    private DoctorPig getBoar(View_BoarCardList card, DoctorFarm farm, Map<Integer, Map<String, DoctorBasic>> basicMap) {
        DoctorPig boar = new DoctorPig();
        boar.setOrgId(farm.getOrgId());
        boar.setOrgName(farm.getOrgName());
        boar.setFarmId(farm.getId());
        boar.setFarmName(farm.getName());
        boar.setOutId(card.getPigOutId());           //外部OID
        boar.setPigCode(card.getPigCode());
        boar.setPigType(DoctorPig.PIG_TYPE.BOAR.getKey());  //猪类是公猪猪
        boar.setIsRemoval("已离场".equals(card.getStatus()) ? IsOrNot.YES.getValue() : IsOrNot.NO.getValue());
        boar.setPigFatherCode(card.getPigFatherCode());
        boar.setPigMotherCode(card.getPigMotherCode());
        boar.setSource(card.getSource());
        boar.setBirthDate(card.getBirthDate());
        boar.setBirthWeight(card.getBirthWeight());
        boar.setInFarmDate(card.getInFarmDate());
        boar.setInFarmDayAge(card.getInFarmDayAge());
        boar.setInitBarnName(card.getInitBarnName());
        boar.setRemark(card.getRemark());

        //品种
        DoctorBasic breed = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(card.getBreed());
        boar.setBreedId(breed == null ? null : breed.getId());
        boar.setBreedName(card.getBreed());

        //品系
        DoctorBasic gene = basicMap.get(DoctorBasic.Type.GENETICS.getValue()).get(card.getGenetic());
        boar.setGeneticId(gene == null ? null : gene.getId());
        boar.setGeneticName(card.getGenetic());

        //附加字段, 公猪类型
        BoarEntryType boarType = BoarEntryType.from(card.getBoarType());
        if (boarType != null) {
            boar.setExtraMap(ImmutableMap.of(
                    DoctorFarmEntryConstants.BOAR_TYPE_ID, boarType.getKey(),
                    DoctorFarmEntryConstants.BOAR_TYPE_NAME, boarType.getDesc()
            ));
        }
        return boar;
    }

    //拼接公猪事件
    private DoctorPigEvent getBoarEvent(View_EventListBoar event, Map<String, DoctorPig> boarMap, Map<String, DoctorBarn> barnMap,
                                        Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, Long> subMap,
                                        Map<String, DoctorCustomer> customerMap, Map<String, DoctorChangeReason> changeReasonMap,
                                        Map<String, DoctorBasicMaterial> vaccMap) {
        DoctorPig boar = boarMap.get(event.getGroupOutId());
        if (boar == null) {
            return null;
        }

        DoctorPigEvent boarEvent = new DoctorPigEvent();
        boarEvent.setOrgId(boar.getOrgId());
        boarEvent.setOrgName(boar.getOrgName());
        boarEvent.setFarmId(boar.getFarmId());
        boarEvent.setFarmName(boar.getFarmName());
        boarEvent.setPigId(boar.getId());
        boarEvent.setPigCode(boar.getPigCode());
        boarEvent.setEventAt(event.getEventAt());
        boarEvent.setKind(boar.getPigType());       // 猪类(公猪2,母猪1)
        boarEvent.setName(event.getEventName());
        boarEvent.setDesc(event.getEventDesc());
        boarEvent.setOutId(event.getEventOutId());
        boarEvent.setRemark(event.getRemark());

        //事件类型
        PigEvent eventType = PigEvent.from(event.getEventName());
        boarEvent.setType(eventType == null ? null : eventType.getKey());

        DoctorBarn barn = barnMap.get(event.getBarnOutId());
        if (barn != null) {
            boarEvent.setBarnId(barn.getId());
            boarEvent.setBarnName(barn.getName());
        }
        return getBoarEventExtra(eventType, boarEvent, event, subMap, basicMap, barnMap, customerMap, changeReasonMap, vaccMap);
    }

    //拼接公猪事件额外信息
    private DoctorPigEvent getBoarEventExtra(PigEvent eventType, DoctorPigEvent boarEvent, View_EventListBoar event, Map<String, Long> subMap,
                                             Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, DoctorBarn> barnMap,
                                             Map<String, DoctorCustomer> customerMap, Map<String, DoctorChangeReason> changeReasonMap,
                                             Map<String, DoctorBasicMaterial> vaccMap) {
        if (eventType == null) {
            return boarEvent;
        }
        //switch 公猪事件
        switch (eventType) {
            case CHG_LOCATION:  //转舍
                DoctorChgLocationDto transBarn = new DoctorChgLocationDto();
                transBarn.setChangeLocationDate(event.getEventAt());
                DoctorBarn fromBarn = barnMap.get(event.getBarnOutId());    //来源猪舍
                if (fromBarn != null) {
                    transBarn.setChgLocationFromBarnId(fromBarn.getId());
                    transBarn.setChgLocationFromBarnName(fromBarn.getName());
                }
                DoctorBarn toBarn = barnMap.get(event.getToBarnOutId());    //去往猪舍
                if (toBarn != null) {
                    transBarn.setChgLocationToBarnId(toBarn.getId());
                    transBarn.setChgLocationToBarnName(toBarn.getName());
                }
                boarEvent.setExtra(JSON_MAPPER.toJson(transBarn));
                break;
            case CHG_FARM:    //转场(一般没有, 简单处理)
                DoctorChgFarmDto tranFarm = new DoctorChgFarmDto();
                tranFarm.setChgFarmDate(event.getEventAt());
                tranFarm.setFromFarmId(boarEvent.getFarmId());
                tranFarm.setFromFarmName(boarEvent.getFarmName());
                tranFarm.setFromBarnId(boarEvent.getBarnId());
                tranFarm.setFromBarnName(boarEvent.getBarnName());
                tranFarm.setRemark(event.getChgReason());
                boarEvent.setExtra(JSON_MAPPER.toJson(tranFarm));
                break;
            case CONDITION:  //体况
                DoctorBoarConditionDto condition = new DoctorBoarConditionDto();
                condition.setCheckAt(event.getEventAt());
                condition.setScoreHuoli(event.getScoreHuoli());
                condition.setScoreMidu(event.getScoreHuoli());
                condition.setScoreXingtai(event.getScoreXingtai());
                condition.setScoreShuliang(event.getScoreShuliang());
                condition.setWeight(event.getEventWeight());
                boarEvent.setExtra(JSON_MAPPER.toJson(condition));
                break;
            case DISEASE:   //疾病
                DoctorDiseaseDto disease = new DoctorDiseaseDto();
                disease.setDiseaseDate(event.getEventAt());

                //疾病
                DoctorBasic ddd = basicMap.get(DoctorBasic.Type.DISEASE.getValue()).get(event.getDiseaseName());
                disease.setDiseaseId(ddd == null ? null : ddd.getId());
                disease.setDiseaseName(event.getDiseaseName());
                disease.setDiseaseStaff(event.getChgReason());  //疾病事件的人员名称
                disease.setDiseaseRemark(event.getRemark());
                boarEvent.setExtra(JSON_MAPPER.toJson(disease));
                break;
            case VACCINATION:  //防疫
                DoctorVaccinationDto vacc = new DoctorVaccinationDto();
                vacc.setVaccinationDate(event.getEventAt());

                DoctorBasicMaterial vaccBasic = vaccMap.get(event.getVaccName());
                vacc.setVaccinationId(vaccBasic == null ? null : vaccBasic.getId());
                vacc.setVaccinationName(event.getVaccName());
                vacc.setVaccinationStaffId(subMap.get(event.getChgReason()));
                vacc.setVaccinationStaffName(event.getChgReason()); //防疫事件人员名称
                vacc.setVaccinationRemark(event.getRemark());
                boarEvent.setExtra(JSON_MAPPER.toJson(vacc));
                break;
            case REMOVAL:   //离场
                DoctorRemovalDto removal = getBoarRemovalExtra(event, customerMap, basicMap, barnMap, changeReasonMap);
                boarEvent.setExtra(JSON_MAPPER.toJson(removal));
                boarEvent.setChangeTypeId(removal.getChgTypeId());
                boarEvent.setPrice(removal.getPrice());
                boarEvent.setAmount(removal.getSum());
                break;
            case ENTRY:     //进场
                boarEvent.setExtra(JSON_MAPPER.toJson(getBoarEntryExtra(event, basicMap, barnMap)));
                break;
            case SEMEN:     //采精
                boarEvent.setExtra(JSON_MAPPER.toJson(getBoarSemenExtra(event)));
                break;
            default:
                break;
        }
        return boarEvent;
    }

    //拼接公猪离场extra
    private DoctorRemovalDto getBoarRemovalExtra(View_EventListBoar event, Map<String, DoctorCustomer> customerMap,
                                                 Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, DoctorBarn> barnMap,
                                                 Map<String, DoctorChangeReason> changeReasonMap) {
        DoctorRemovalDto remove = new DoctorRemovalDto();

        //变动类型, 变动原因
        DoctorBasic changeType = basicMap.get(DoctorBasic.Type.CHANGE_TYPE.getValue()).get(event.getChgType());
        remove.setChgTypeId(changeType == null ? null : changeType.getId());
        remove.setChgTypeName(event.getChgType());
        DoctorChangeReason reason = changeReasonMap.get(event.getChgReason());
        remove.setChgReasonId(reason == null ? null : reason.getId());
        remove.setChgReasonName(event.getChgReason());

        //重量 金额等
        remove.setWeight(event.getEventWeight());
        remove.setPrice(event.getPrice());
        remove.setSum(event.getAmount());
        remove.setRemark(event.getRemark());

        //猪舍 客户
        DoctorBarn barn = barnMap.get(event.getBarnOutId());
        remove.setToBarnId(barn == null ? null : barn.getId());
        DoctorCustomer customer = customerMap.get(event.getCustomer());
        remove.setCustomerId(customer == null ? null : customer.getId());
        return remove;
    }

    //拼接公猪进场extra
    private DoctorFarmEntryDto getBoarEntryExtra(View_EventListBoar event, Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, DoctorBarn> barnMap) {
        DoctorFarmEntryDto entry = new DoctorFarmEntryDto();

        //公猪进场类型
        BoarEntryType type = BoarEntryType.from(event.getBoarType());
        entry.setBoarTypeId(type == null ? null : type.getKey());
        entry.setBoarTypeName(event.getBoarType());
        entry.setPigType(DoctorPig.PIG_TYPE.BOAR.getKey());  //类型: 公猪
        entry.setPigCode(event.getPigCode());       // pig code 猪 编号
        entry.setBirthday(event.getBirthDate());      // 猪生日
        entry.setInFarmDate(event.getInFarmDate());    // 进厂时间
        entry.setFatherCode(event.getPigFatherCode());    // 父类Code （非必填）
        entry.setMotherCode(event.getPigMotherCode());    // 母Code （非必填）
        entry.setEntryMark(event.getRemark());     // 非必填
        entry.setSource(event.getSource());

        DoctorBarn barn = barnMap.get(event.getBarnOutId());
        if (barn != null) {
            entry.setBarnId(barn.getId());
            entry.setBarnName(barn.getName());
        }
        //品种 品系
        DoctorBasic breed = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(event.getBreed());
        entry.setBreed(breed == null ? null : breed.getId());         //品种Id （basic Info）
        entry.setBreedName(event.getBreed());     //品种名称

        DoctorBasic gene = basicMap.get(DoctorBasic.Type.GENETICS.getValue()).get(event.getGenetic());
        entry.setBreedType(gene == null ? null : gene.getId());     //品系Id  (basic info)
        entry.setBreedTypeName(event.getGenetic()); //品系名称
        return entry;
    }

    //拼接公猪采精extra
    private DoctorSemenDto getBoarSemenExtra(View_EventListBoar event) {
        DoctorSemenDto semen = new DoctorSemenDto();
        semen.setSemenDate(event.getEventAt());       //采精日期
        semen.setSemenWeight(event.getEventWeight());     //采精重量
        semen.setDilutionRatio(event.getDilutionRatio());   //稀释倍数
        semen.setDilutionWeight(event.getDilutionWeight());  //稀释后重量
        semen.setSemenDensity(event.getSemenDensity());    //精液密度
        semen.setSemenActive(event.getSemenActive());     //精液活力
        semen.setSemenJxRatio(event.getSemenJxRatio());    //精液畸形率
        semen.setSemenPh(event.getSemenPh());         //精液PH
        semen.setSemenTotal(Double.valueOf(event.getScore()));      //精液总评!!!
        semen.setSemenRemark(event.getRemark());     //精液备注（非必填）
        return semen;
    }

    //拼接公猪跟踪
    private DoctorPigTrack getBoarTrack(View_BoarCardList card, DoctorPig boar, Map<String, DoctorBarn> barnMap, List<DoctorPigEvent> events) {
        if (boar == null) {
            return null;
        }
        DoctorPigTrack track = new DoctorPigTrack();
        track.setFarmId(boar.getFarmId());
        track.setPigId(boar.getId());
        track.setPigType(boar.getPigType());
        track.setStatus(Objects.equals(boar.getIsRemoval(), IsOrNot.NO.getValue()) ? PigStatus.BOAR_ENTRY.getKey() : PigStatus.BOAR_LEAVE.getKey());
        track.setIsRemoval(boar.getIsRemoval());
        track.setWeight(card.getWeight());
        track.setOutFarmDate(DateUtil.toDate(card.getOutFarmDate()));
        track.setRemark(card.getRemark());

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

    //拼接猪群事件
    private DoctorGroupEvent getGroupEvent(Map<String, DoctorGroup> groupMap, View_EventListGain gainEvent, Map<String, Long> subMap,
                                           Map<String, DoctorBarn> barnMap, Map<Integer, Map<String, DoctorBasic>> basicMap,
                                           Map<String, DoctorChangeReason> changeReasonMap, Map<String, DoctorCustomer> customerMap,
                                           Map<String, DoctorBasicMaterial> vaccMap, Map<String, DoctorPig> pigMap) {
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
        return getGroupEventExtra(type, event, gainEvent, basicMap, barnMap, groupMap, group, subMap, changeReasonMap, customerMap, vaccMap, pigMap);
    }

    //根据类型拼接猪群事件明细
    @SuppressWarnings("unchecked")
    private DoctorGroupEvent getGroupEventExtra(GroupEventType type, DoctorGroupEvent event, View_EventListGain gainEvent,
                                                Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, DoctorBarn> barnMap,
                                                Map<String, DoctorGroup> groupMap, DoctorGroup group, Map<String, Long> subMap,
                                                Map<String, DoctorChangeReason> changeReasonMap, Map<String, DoctorCustomer> customerMap,
                                                Map<String, DoctorBasicMaterial> vaccMap, Map<String, DoctorPig> pigMap) {
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
                DoctorMoveInGroupEvent moveIn = getMoveInEvent(gainEvent, basicMap, groupMap, group);
                event.setExtraMap(moveIn);
                event.setTransGroupType(getTransType(event.getPigType(), moveIn.getFromBarnType()).getValue());  //区分内转还是外转
                break;
            case CHANGE:
                DoctorChangeGroupEvent changeEvent = getChangeEvent(gainEvent, basicMap, changeReasonMap, customerMap);
                event.setExtraMap(changeEvent);
                event.setChangeTypeId(changeEvent.getChangeTypeId());
                event.setPrice(changeEvent.getPrice());
                event.setAmount(changeEvent.getAmount());
                break;
            case TRANS_GROUP:
                DoctorTransGroupEvent transGroupEvent = getTranGroupEvent(gainEvent, basicMap, barnMap, groupMap, group);
                event.setExtraMap(transGroupEvent);
                event.setTransGroupType(getTransType(event.getPigType(), transGroupEvent.getToBarnType()).getValue());  //区分内转还是外转
                break;
            case TURN_SEED:
                event.setExtraMap(getTurnSeedEvent(gainEvent, basicMap, barnMap, pigMap));
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

                //疫苗
                DoctorBasicMaterial vaccBasic = vaccMap.get(gainEvent.getNotDisease());
                anti.setVaccinId(vaccBasic == null ? null : vaccBasic.getId());
                anti.setVaccinName(gainEvent.getNotDisease());

                DoctorAntiepidemicGroupEvent.VaccinResult result = DoctorAntiepidemicGroupEvent.VaccinResult.from(gainEvent.getContext());
                anti.setVaccinResult(result == null ? null : result.getValue());
                anti.setVaccinStaffId(subMap.get(gainEvent.getStaffName()));
                anti.setVaccinStaffName(gainEvent.getStaffName());
                anti.setQuantity(gainEvent.getQuantity());
                event.setExtraMap(anti);
                break;
            case TRANS_FARM: //转场不区分内转外转, 相当于空降
                DoctorTransFarmGroupEvent transFarmEvent = getTranFarmEvent(gainEvent, basicMap, barnMap, groupMap, group);
                event.setExtraMap(transFarmEvent);
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

    //判断内转还是外转
    private static DoctorGroupEvent.TransGroupType getTransType(Integer pigType, Integer toBarnType) {
        return Objects.equals(pigType, toBarnType) || (FARROW_TYPES.contains(pigType) && FARROW_TYPES.contains(toBarnType)) ?
                DoctorGroupEvent.TransGroupType.IN : DoctorGroupEvent.TransGroupType.OUT;
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
        moveIn.setFromBarnType(group.getPigType());

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
                transGroup.setToBarnType(barn.getPigType());
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
                transGroup.setToBarnType(barn.getPigType());
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
            transFarm.setToBarnType(barn.getPigType());
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
    private DoctorTurnSeedGroupEvent getTurnSeedEvent(View_EventListGain gainEvent, Map<Integer, Map<String, DoctorBasic>> basicMap,
                                                      Map<String, DoctorBarn> barnMap, Map<String, DoctorPig> pigMap) {
        DoctorTurnSeedGroupEvent turnSeed = new DoctorTurnSeedGroupEvent();
        DoctorPig pig = pigMap.get(gainEvent.getPigCode());
        //turnSeed.setPigId(pig == null ? null : pig.getId());

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

//        DoctorTurnSeedGroupEvent.Sex sex = DoctorTurnSeedGroupEvent.Sex.from(gainEvent.getSexName());
//        turnSeed.setSex(sex == null ? null : sex.getValue());
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
    private DoctorGroup getGroup(DoctorFarm farm, View_GainCardList gain, Map<String, DoctorBarn> barnMap,
                                 Map<Integer, Map<String, DoctorBasic>> basicMap, Map<String, Long> subMap) {
        DoctorGroup group = BeanMapper.map(gain, DoctorGroup.class);

        group.setOrgId(farm.getOrgId());
        group.setOrgName(farm.getOrgName());
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

    private static String brace(String name) {
        return "'" + name + "'";
    }

    //判断猪场id是否相同
    private static boolean isFarm(String farmOID, String outId) {
        return Objects.equals(farmOID, outId);
    }


    public static void main(String[] args) {
        System.out.println(PigEvent.MATING.getKey());
    }
}
