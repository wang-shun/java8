package io.terminus.doctor.move.service;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Joiners;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.basic.service.DoctorMaterialConsumeProviderReadService;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.constants.DoctorFarmEntryConstants;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorGroupSnapshotDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.DoctorPigSnapShotInfo;
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
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFostersDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPigletsChgDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPregChkResultDto;
import io.terminus.doctor.event.dto.event.sow.DoctorWeanDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.dto.event.usual.DoctorConditionDto;
import io.terminus.doctor.event.dto.event.usual.DoctorDiseaseDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.dto.event.usual.DoctorRemovalDto;
import io.terminus.doctor.event.dto.event.usual.DoctorVaccinationDto;
import io.terminus.doctor.event.enums.BoarEntryType;
import io.terminus.doctor.event.enums.DoctorBasicEnums;
import io.terminus.doctor.event.enums.DoctorMatingType;
import io.terminus.doctor.event.enums.EventStatus;
import io.terminus.doctor.event.enums.FarrowingType;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.MatingType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.handler.sow.DoctorSowMatingHandler;
import io.terminus.doctor.event.manager.DoctorGroupReportManager;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupBatchSummary;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorGroupBatchSummaryReadService;
import io.terminus.doctor.event.service.DoctorGroupBatchSummaryWriteService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.move.model.DoctorSowFarrowWeight;
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

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.*;
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

    private static final JsonMapperUtil JSON_MAPPER = JsonMapperUtil.nonEmptyMapper();
    private static final JsonMapperUtil MAPPER = JsonMapperUtil.nonDefaultMapperWithFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    private final DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;
    private final DoctorGroupDao doctorGroupDao;
    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorGroupTrackDao doctorGroupTrackDao;
    private final DoctorPigDao doctorPigDao;
    private final DoctorPigTrackDao doctorPigTrackDao;
    private final DoctorPigEventDao doctorPigEventDao;
    private final DoctorMoveBasicService doctorMoveBasicService;
    private final DoctorPigReadService doctorPigReadService;
    private final DoctorGroupReportManager doctorGroupReportManager;
    private final DoctorBarnDao doctorBarnDao;
    private final DoctorGroupBatchSummaryReadService doctorGroupBatchSummaryReadService;
    private final DoctorGroupBatchSummaryWriteService doctorGroupBatchSummaryWriteService;
    private final DoctorMaterialConsumeProviderReadService doctorMaterialConsumeProviderReadService;
    private final DoctorGroupSnapshotDao doctorGroupSnapshotDao;
    private final DoctorPigSnapshotDao doctorPigSnapshotDao;

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
                                 DoctorGroupReportManager doctorGroupReportManager,
                                 DoctorBarnDao doctorBarnDao,
                                 DoctorGroupBatchSummaryReadService doctorGroupBatchSummaryReadService,
                                 DoctorGroupBatchSummaryWriteService doctorGroupBatchSummaryWriteService,
                                 DoctorMaterialConsumeProviderReadService doctorMaterialConsumeProviderReadService,
                                 DoctorGroupSnapshotDao doctorGroupSnapshotDao,
                                 DoctorPigSnapshotDao doctorPigSnapshotDao) {
        this.doctorMoveDatasourceHandler = doctorMoveDatasourceHandler;
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorPigDao = doctorPigDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorMoveBasicService = doctorMoveBasicService;
        this.doctorPigReadService = doctorPigReadService;
        this.doctorGroupReportManager = doctorGroupReportManager;
        this.doctorBarnDao = doctorBarnDao;
        this.doctorGroupBatchSummaryReadService = doctorGroupBatchSummaryReadService;
        this.doctorGroupBatchSummaryWriteService = doctorGroupBatchSummaryWriteService;
        this.doctorMaterialConsumeProviderReadService = doctorMaterialConsumeProviderReadService;
        this.doctorGroupSnapshotDao = doctorGroupSnapshotDao;
        this.doctorPigSnapshotDao = doctorPigSnapshotDao;
    }

    //删除猪场所有猪相关的数据
    public void deleteAllPigs(Long farmId) {
        doctorPigDao.deleteByFarmId(farmId);
        doctorPigEventDao.deleteByFarmId(farmId);
        doctorPigTrackDao.deleteByFarmId(farmId);
    }

    /**
     * 更新关闭猪群的日龄
     */
    @Transactional
    public void updateClosedGroupDayAge(Long moveId, DoctorFarm farm) {
        Map<String, Integer> ageMap = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, Proc_InventoryGain.class, "DoctorGroupTrack-Proc_InventoryGain", ImmutableMap.of("date", DateUtil.toDateTimeString(new Date())))).stream()
                .collect(Collectors.toMap(Proc_InventoryGain::getGroupOutId, Proc_InventoryGain::getAvgDayAge));

        DoctorGroupSearchDto search = new DoctorGroupSearchDto();
        search.setFarmId(farm.getId());
        search.setStatus(DoctorGroup.Status.CLOSED.getValue());
        doctorGroupDao.findBySearchDto(search).forEach(group -> {
            DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(group.getId());

            //日龄<=0 的和 查到日龄的, 重新修改下日龄
            if (groupTrack.getAvgDayAge() <= 0) {
                Integer age = ageMap.get(group.getOutId());
                if (age != null) {
                    DoctorGroupTrack updateTrack = new DoctorGroupTrack();
                    updateTrack.setId(groupTrack.getId());
                    updateTrack.setAvgDayAge(age);
                    doctorGroupTrackDao.update(updateTrack);
                }
            }
        });
    }

    /**
     * 已关闭的猪群生成批次总结
     */
    @Transactional
    public void createClosedGroupSummary(Long farmId) {
        DoctorGroupSearchDto search = new DoctorGroupSearchDto();
        search.setFarmId(farmId);
        search.setStatus(DoctorGroup.Status.CLOSED.getValue());
        List<DoctorGroup> groups = doctorGroupDao.findBySearchDto(search);
        groups.forEach(group -> {
            DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(group.getId());
            Double frcFeed = RespHelper.or(doctorMaterialConsumeProviderReadService.sumConsumeFeed(null, null, null, null, null, group.getId(), null, null), 0D);
            Response<DoctorGroupBatchSummary> result = doctorGroupBatchSummaryReadService.getSummaryByGroupDetail(new DoctorGroupDetail(group, groupTrack), frcFeed);
            if (result.isSuccess() && result.getResult() != null) {
                doctorGroupBatchSummaryWriteService.createGroupBatchSummary(result.getResult());
            }
        });
    }

    /**
     * 修正产房仔猪数据
     */
    @Transactional
    public void updateFarrowGroupTrack(DoctorFarm farm) {
        DoctorGroupSearchDto search = new DoctorGroupSearchDto();
        search.setFarmId(farm.getId());
        search.setPigTypes(PigType.FARROW_TYPES);

        doctorGroupDao.findBySearchDto(search).forEach(group -> {
            DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(group.getId());

            DoctorGroupTrack updateTrack = new DoctorGroupTrack();
            updateTrack.setId(groupTrack.getId());
            doctorGroupTrackDao.update(doctorGroupReportManager.updateGroupTrackReport(groupTrack, group.getPigType()));
        });
    }

    /**
     * 修正哺乳母猪数据
     */
    @Transactional
    public void updateBuruTrack(DoctorFarm farm) {
        updateBuruSowTrack(farm);
    }

    /**
     * 更新猪群事件的目标/来源猪舍
     */
    @Transactional
    public void updateGroupEventOtherBarn(DoctorFarm farm) {
        Map<Long, DoctorBarn> barnMap = doctorBarnDao.findByFarmId(farm.getId()).stream().collect(Collectors.toMap(DoctorBarn::getId, v -> v));

        //仔猪转入
        doctorGroupEventDao.findGroupEventsByEventTypeAndDate(farm.getId(), GroupEventType.MOVE_IN.getValue(), null, null).forEach(event -> {
            DoctorMoveInGroupEvent moveIn = JSON_MAPPER.fromJson(event.getExtra(), DoctorMoveInGroupEvent.class);
            DoctorGroupEvent updateEvent = new DoctorGroupEvent();
            updateEvent.setId(event.getId());
            updateEvent.setInType(moveIn.getInType());
            DoctorBarn barn = barnMap.get(moveIn.getFromBarnId());
            if (barn != null) {
                updateEvent.setOtherBarnId(barn.getId());
                updateEvent.setOtherBarnType(barn.getPigType());
            }
            doctorGroupEventDao.update(updateEvent);
        });

        //转群
        doctorGroupEventDao.findGroupEventsByEventTypeAndDate(farm.getId(), GroupEventType.TRANS_GROUP.getValue(), null, null).forEach(event -> {
            DoctorTransGroupEvent transGroup = JSON_MAPPER.fromJson(event.getExtra(), DoctorTransGroupEvent.class);
            updateGroupOtherBarn(event.getId(), barnMap.get(transGroup.getToBarnId()));
        });

        //转场
        doctorGroupEventDao.findGroupEventsByEventTypeAndDate(farm.getId(), GroupEventType.TRANS_FARM.getValue(), null, null).forEach(event -> {
            DoctorTransFarmGroupEvent transFarm = JSON_MAPPER.fromJson(event.getExtra(), DoctorTransFarmGroupEvent.class);
            updateGroupOtherBarn(event.getId(), barnMap.get(transFarm.getToBarnId()));

        });

        //转种猪
        doctorGroupEventDao.findGroupEventsByEventTypeAndDate(farm.getId(), GroupEventType.TURN_SEED.getValue(), null, null).forEach(event -> {
            DoctorTurnSeedGroupEvent turnSeed = JSON_MAPPER.fromJson(event.getExtra(), DoctorTurnSeedGroupEvent.class);
            updateGroupOtherBarn(event.getId(), barnMap.get(turnSeed.getToBarnId()));
        });
    }

    private void updateGroupOtherBarn(Long eventId, DoctorBarn barn) {
        if (barn != null) {
            DoctorGroupEvent updateEvent = new DoctorGroupEvent();
            updateEvent.setId(eventId);
            updateEvent.setOtherBarnId(barn.getId());
            updateEvent.setOtherBarnType(barn.getPigType());
            doctorGroupEventDao.update(updateEvent);
        }
    }

    /**
     * 修正配种类型
     */
    @Transactional
    public void updateMateType(DoctorFarm farm) {
        //找到所有没有设置 mate_type 的配种事件
        doctorPigEventDao.findByFarmIdAndKindAndEventTypes(farm.getId(), DoctorPig.PigSex.SOW.getKey(), Lists.newArrayList(PigEvent.MATING.getKey())).stream()
                .filter(e -> isNull(e.getDoctorMateType()) && e.getCurrentMatingCount() == 1)  //只改初配
                .forEach(event -> {
                    List<DoctorPigEvent> pigEvents = doctorPigEventDao.queryAllEventsByPigIdForASC(event.getPigId());
                    DoctorMatingType mateType = DoctorSowMatingHandler.getPigMateType(pigEvents, event.getEventAt());

                    DoctorPigEvent updateEvent = new DoctorPigEvent();
                    updateEvent.setId(event.getId());
                    updateEvent.setDoctorMateType(mateType.getKey());
                    doctorPigEventDao.update(updateEvent);
                });
    }

    /**
     * 更新猪群内转外转
     */
    @Transactional
    public void updateTranGroupType(DoctorFarm farm) {
        doctorGroupEventDao.findGroupEventsByEventTypeAndDate(farm.getId(), GroupEventType.MOVE_IN.getValue(), null, null)
                .forEach(event -> {
                    DoctorMoveInGroupEvent moveIn = JSON_MAPPER.fromJson(event.getExtra(), DoctorMoveInGroupEvent.class);
                    DoctorGroupEvent updateEvent = new DoctorGroupEvent();
                    updateEvent.setId(event.getId());
                    updateEvent.setTransGroupType(getTransType(moveIn.getInType(), event.getPigType(), moveIn.getFromBarnType()).getValue());
                    doctorGroupEventDao.update(updateEvent);
                });

        doctorGroupEventDao.findGroupEventsByEventTypeAndDate(farm.getId(), GroupEventType.TRANS_GROUP.getValue(), null, null)
                .forEach(event -> {
                    DoctorTransGroupEvent trans = JSON_MAPPER.fromJson(event.getExtra(), DoctorTransGroupEvent.class);
                    DoctorGroupEvent updateEvent = new DoctorGroupEvent();
                    updateEvent.setId(event.getId());
                    Integer toBarnType = trans.getToBarnType();
                    if (toBarnType == null) {
                        DoctorBarn toBarn = doctorBarnDao.findById(trans.getToBarnId());
                        if (toBarn != null) {
                            toBarnType = toBarn.getPigType();
                        }
                    }
                    updateEvent.setTransGroupType(getTransType(null, event.getPigType(), toBarnType).getValue());
                    doctorGroupEventDao.update(updateEvent);
                });
    }

    /**
     * 更新母猪流产事件
     */
    @Transactional
    public void updateSowAbortion(DoctorFarm farm) {
        List<DoctorPigEvent> events = doctorPigEventDao.findByFarmIdAndKindAndEventTypes(farm.getId(),
                DoctorPig.PigSex.SOW.getKey(), Lists.newArrayList(13));
        if (notEmpty(events)) {
            events.forEach(event -> {
                DoctorPigEvent updateEvent = new DoctorPigEvent();
                updateEvent.setId(event.getId());
                updateEvent.setType(PigEvent.PREG_CHECK.getKey());
                updateEvent.setName(PigEvent.PREG_CHECK.getName());
                updateEvent.setCheckDate(event.getEventAt());
                updateEvent.setPregCheckResult(PregCheckResult.LIUCHAN.getKey());

                //流产
                DoctorPregChkResultDto dto = new DoctorPregChkResultDto();
                dto.setCheckDate(event.getEventAt());
                dto.setCheckResult(PregCheckResult.LIUCHAN.getKey());
                updateEvent.setExtra(JSON_MAPPER.toJson(dto));
                doctorPigEventDao.update(updateEvent);
            });
        }

        //更新流产状态为空怀
        List<DoctorPigTrack> pigTracks = doctorPigTrackDao.findByFarmIdAndStatus(farm.getId(), 6);
        if (notEmpty(pigTracks)) {
            pigTracks.forEach(pigTrack -> {
                DoctorPigTrack updateTrack = new DoctorPigTrack();
                updateTrack.setId(pigTrack.getId());
                updateTrack.setStatus(PigStatus.KongHuai.getKey());
                doctorPigTrackDao.update(updateTrack);
            });
        }
    }

    /**
     * 更新母猪分娩总重
     */
    public void updateSowFarrowWeight(Long moveId, DoctorFarm farm) {

        //事件id和重量的map
        Map<String, Double> fwmap = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, DoctorSowFarrowWeight.class, "DoctorSowFarrowWeight", ImmutableMap.of("farmOutId", farm.getOutId()))).stream()
                .collect(Collectors.toMap(DoctorSowFarrowWeight::getGroupOutId, DoctorSowFarrowWeight::getFarrowWeight));

        List<DoctorPigEvent> events = doctorPigEventDao.findByFarmIdAndKindAndEventTypes(farm.getId(),
                DoctorPig.PigSex.SOW.getKey(), Lists.newArrayList(PigEvent.FARROWING.getKey()));
        if (notEmpty(events)) {
            events.forEach(event -> {
                DoctorPigEvent updateEvent = new DoctorPigEvent();
                updateEvent.setId(event.getId());
                updateEvent.setFarrowWeight(fwmap.get(event.getOutId()));   //从map里取出重量
                doctorPigEventDao.update(updateEvent);
            });
        }
    }

    /**
     * 更新母猪转舍, 区分出类型
     */
    @Transactional
    public void updateSowTransBarn(DoctorFarm farm) {
        Map<Long, Integer> barnTypeMap = doctorMoveBasicService.getBarnIdMap(farm.getId());

        //转舍事件
        List<Integer> tarnsBarnTypes = Lists.newArrayList(
                PigEvent.CHG_LOCATION.getKey(),
                PigEvent.TO_PREG.getKey(),
                PigEvent.TO_MATING.getKey(),
                PigEvent.TO_FARROWING.getKey()
        );

        doctorPigEventDao.findByFarmIdAndKindAndEventTypes(farm.getId(), DoctorPig.PigSex.SOW.getKey(), tarnsBarnTypes)
                .forEach(event -> {
                    DoctorChgLocationDto dto = JSON_MAPPER.fromJson(event.getExtra(), DoctorChgLocationDto.class);
                    if (dto != null) {
                        Integer fromBarnType = barnTypeMap.get(dto.getChgLocationFromBarnId());
                        Integer toBarnType = barnTypeMap.get(dto.getChgLocationToBarnId());
                        PigEvent type = getTransBarnType(fromBarnType, toBarnType);

                        DoctorPigEvent updateEvent = new DoctorPigEvent();
                        updateEvent.setId(event.getId());
                        updateEvent.setType(type.getKey());
                        updateEvent.setName(type.getName());
                        doctorPigEventDao.update(updateEvent);
                    }
                });
    }

    //根据from to的类型, 判断转舍事件类型
    private static PigEvent getTransBarnType(Integer fromBarnType, Integer toBarnType) {
        //转入配种舍
        if (Objects.equals(toBarnType, PigType.MATE_SOW.getValue()) && !Objects.equals(fromBarnType, toBarnType)) {
            return PigEvent.TO_MATING;
        }
        //转入妊娠舍
        if (Objects.equals(toBarnType, PigType.PREG_SOW.getValue()) && !Objects.equals(fromBarnType, toBarnType)) {
            return PigEvent.TO_PREG;
        }
        //去分娩
        if (Objects.equals(toBarnType, PigType.DELIVER_SOW.getValue()) && !Objects.equals(fromBarnType, toBarnType)) {
            return PigEvent.TO_FARROWING;
        }
        return PigEvent.CHG_LOCATION;
    }

    /**
     * 更新分娩母猪的猪群号, 更新待配种母猪的状态(status = 4 and barnType = 7) 在产房的阳性猪, 状态应该是 7 待分娩
     */
    public void updateFarrowSow(DoctorFarm farm) {
        //猪场号, 猪群id
        Map<String, Long> groupMap = Maps.newHashMap();
        doctorGroupDao.findByFarmId(farm.getId()).forEach(group -> groupMap.put(group.getGroupCode(), group.getId()));

        Map<String, DoctorBarn> barnMap = doctorMoveBasicService.getBarnMap2(farm.getId());

        //只更新未离场的吧
        doctorPigTrackDao.list(DoctorPigTrack.builder()
                .farmId(farm.getId())
                .pigType(DoctorPig.PigSex.SOW.getKey())
                .isRemoval(IsOrNot.NO.getValue()).build())
                .forEach(track -> {
                    Map<String, Object> extraMap = JSON_MAPPER.fromJson(track.getExtra(), JSON_MAPPER.createCollectionType(Map.class, String.class, Object.class));
                    if (Objects.equals(track.getStatus(), PigStatus.Pregnancy.getKey()) &&
                            barnMap.get(track.getCurrentBarnName()).getPigType() == PigType.DELIVER_SOW.getValue()) {
                        track.setStatus(PigStatus.Farrow.getKey());
                        doctorPigTrackDao.update(track);
                    }
                    if (extraMap.containsKey("groupCode")) {
                        extraMap.put("farrowingPigletGroupId", groupMap.get(String.valueOf(extraMap.get("groupCode"))));
                        track.setExtra(JSON_MAPPER.toJson(extraMap));
                        doctorPigTrackDao.update(track);

                    }
                });
    }

    /**
     * 更新母猪的trackExtraMap
     */
    public void updateSowTrackExtraMap(DoctorFarm farm) {
        //只更新未离场的吧
        doctorPigTrackDao.list(DoctorPigTrack.builder()
                .farmId(farm.getId())
                .pigType(DoctorPig.PigSex.SOW.getKey())
                .isRemoval(IsOrNot.NO.getValue()).build())
                .forEach(track -> {
                    List<DoctorPigEvent> events = doctorPigEventDao.queryAllEventsByPigIdForASC(track.getPigId()).stream()
                            .sorted((a, b) -> a.getEventAt().compareTo(b.getEventAt()))
                            .collect(Collectors.toList());

                    DoctorPigTrack updateTrack = new DoctorPigTrack();
                    updateTrack.setId(track.getId());
                    updateTrack.setExtra(JSON_MAPPER.toJson(getSowExtraMap(events)));
                    doctorPigTrackDao.update(updateTrack);
                });
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
        if(!groups.isEmpty()){
            doctorGroupDao.creates(groups);
        }

        //查出刚插入的group, key = outId, 查询猪, 为转种猪事件做准备
        Map<String, DoctorGroup> groupMap = doctorGroupDao.findByFarmId(farm.getId()).stream().collect(Collectors.toMap(DoctorGroup::getOutId, v -> v));
        Map<String, DoctorPig> pigMap = Maps.newHashMap();
        doctorPigDao.findPigsByFarmId(farm.getId()).forEach(pig -> pigMap.put(pig.getPigCode(), pig));

        //2. 迁移DoctorGroupEvent
        List<DoctorGroupEvent> events = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, View_EventListGain.class, "DoctorGroupEvent-EventListGain")).stream()
                .map(gainEvent -> getGroupEvent(groupMap, gainEvent, subMap, barnMap, basicMap, changeReasonMap, customerMap, vaccMap, pigMap))
                .filter(event -> event != null)
                .collect(Collectors.toList());
        if(!events.isEmpty()){
            doctorGroupEventDao.creates(events);
        }

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
        if(!groupTracks.isEmpty()){
            doctorGroupTrackDao.creates(groupTracks);
        }

        groupTracks.forEach(groupTrack -> {
            DoctorGroupEvent groupEvent = doctorGroupEventDao.findLastEventByGroupId(groupTrack.getGroupId());
            DoctorGroup group = doctorGroupDao.findById(groupTrack.getGroupId());
            //groupTrack关联最新事件
            groupTrack.setRelEventId(groupEvent.getId());
            doctorGroupTrackDao.update(groupTrack);

            //创建镜像
            DoctorGroupSnapshot groupSnapshot = new DoctorGroupSnapshot();
            groupSnapshot.setFromEventId(0L);
            groupSnapshot.setToEventId(groupEvent.getId());
            groupSnapshot.setGroupId(group.getId());
            DoctorGroupSnapShotInfo snapShotInfo = DoctorGroupSnapShotInfo.builder()
                    .group(group)
                    .groupTrack(groupTrack)
                    .groupEvent(groupEvent)
                    .build();
            groupSnapshot.setToInfo(JsonMapperUtil.JSON_NON_DEFAULT_MAPPER.toJson(snapShotInfo));
            doctorGroupSnapshotDao.create(groupSnapshot);
        });

        //批次总结
        createClosedGroupSummary(farm.getId());
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
        doctorPigDao.findPigsByFarmIdAndPigType(farm.getId(), DoctorPig.PigSex.BOAR.getKey()).forEach(boar -> boarMap.put(boar.getPigCode(), boar));

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
        List<DoctorPig> pigs = sowCards.stream().map(card -> getSow(card, farm, basicMap)).collect(Collectors.toList());
        if(!pigs.isEmpty()){
            doctorPigDao.creates(pigs);
        }

        //查出母猪, 转换成map
        Map<String, DoctorPig> sowMap = doctorPigDao.findPigsByFarmIdAndPigType(farm.getId(), DoctorPig.PigSex.SOW.getKey()).stream()
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
        if(!sowEvents.isEmpty()){
            Lists.partition(sowEvents, 5).forEach(doctorPigEventDao::creates);
        }

        //查出母猪事件, 按照母猪分组
        Map<Long, List<DoctorPigEvent>> sowEventMap = doctorPigEventDao.findByFarmIdAndKind(farm.getId(), DoctorPig.PigSex.SOW.getKey())
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
        if(!sowTracks.isEmpty()){
            doctorPigTrackDao.creates(sowTracks);
        }

        sowTracks.forEach(sowTrack -> {
            DoctorPig sow = doctorPigDao.findById(sowTrack.getPigId());
            DoctorPigSnapshot pigSnapshot = DoctorPigSnapshot.builder()
                    .pigId(sow.getId())
                    .fromEventId(0L)
                    .toEventId(sowTrack.getCurrentEventId())
                    .toPigInfo(JsonMapperUtil.JSON_NON_DEFAULT_MAPPER.toJson(new DoctorPigSnapShotInfo(sow, sowTrack)))
                    .build();
            doctorPigSnapshotDao.create(pigSnapshot);
        });

        //4. 更新公猪的全部配种次数
        updateBoarCurrentParity(sowEvents);

        //更新母猪胎次,配种的公猪号
        updateParityAndBoarCode(farm);

        //更新拼哺事件extra
        updateFosterSowCode(farm);
    }

    //如果是哺乳状态, 设置一下哺乳猪群的信息
    private void updateBuruSowTrack(DoctorFarm farm) {
        doctorPigTrackDao.findByFarmIdAndStatus(farm.getId(), PigStatus.FEED.getKey()).stream()
                .filter(t -> Objects.equals(t.getIsRemoval(), IsOrNot.NO.getValue()) && Objects.equals(t.getPigType(), DoctorPig.PigSex.SOW.getKey()))
                .forEach(track -> {
                    track.setExtra(track.getExtra());
                    Map<String, Object> extraMap = track.getExtraMap();

                    DoctorPigTrack updateTrack = new DoctorPigTrack();
                    updateTrack.setId(track.getId());
                    if (extraMap.containsKey("farrowingPigletGroupId")) {
                        updateTrack.setGroupId(Long.valueOf(String.valueOf(extraMap.get("farrowingPigletGroupId"))));
                    }

                    //更新哺乳母猪信息
                    updateTrack.setUnweanQty(getIntegerDefault0(extraMap, "farrowingLiveCount"));
                    updateTrack.setWeanQty(getIntegerDefault0(extraMap, "partWeanPigletsCount"));
                    updateTrack.setFarrowQty(updateTrack.getUnweanQty() + updateTrack.getWeanQty());
                    updateTrack.setFarrowAvgWeight(getDoubleDefault0(extraMap, "birthNestAvg"));
                    updateTrack.setWeanAvgWeight(getDoubleDefault0(extraMap, "partWeanAvgWeight"));
                    doctorPigTrackDao.update(updateTrack);
                });
    }

    private static Integer getIntegerDefault0(Map<String, Object> extraMap, String key) {
        return extraMap.containsKey(key) ? Integer.valueOf(String.valueOf(extraMap.get(key))) : 0;
    }

    private static Double getDoubleDefault0(Map<String, Object> extraMap, String key) {
        return extraMap.containsKey(key) ? Double.valueOf(String.valueOf(extraMap.get(key))) : 0D;
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
        sow.setPigType(DoctorPig.PigSex.SOW.getKey());  //猪类是母猪
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
        sowEvent.setStatus(EventStatus.VALID.getValue());

        sowEvent.setEventAt(event.getEventAt());
        sowEvent.setKind(sow.getPigType());       // 猪类(公猪2,母猪1)
        sowEvent.setName(event.getEventName());
        sowEvent.setDesc(event.getEventDesc());
        sowEvent.setOutId(event.getEventOutId());
        sowEvent.setRemark(event.getRemark());
        sowEvent.setEventSource(SourceType.IMPORT.getValue());

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
            case TO_PREG:        //转入妊娠舍
                sowEvent.setExtra(JSON_MAPPER.toJson(getSowTranBarnExtra(event, barnMap)));
                break;
            case TO_MATING:      //转入配种舍
                sowEvent.setExtra(JSON_MAPPER.toJson(getSowTranBarnExtra(event, barnMap)));
                break;
            case TO_FARROWING:   //去分娩
                sowEvent.setExtra(JSON_MAPPER.toJson(getSowTranBarnExtra(event, barnMap)));
                break;
            case CHG_LOCATION:  //转舍
                sowEvent.setExtra(JSON_MAPPER.toJson(getSowTranBarnExtra(event, barnMap)));
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
                DoctorMatingDto mating = getSowMatingExtra(event, boarMap, subMap);
                sowEvent.setMattingDate(event.getEventAt());                //配种时间
                sowEvent.setExtra(JSON_MAPPER.toJson(mating));
                break;
            case PREG_CHECK:    //妊娠检查
                DoctorPregChkResultDto checkResult = getSowPregCheckExtra(event);
                sowEvent.setPregCheckResult(checkResult.getCheckResult());  //妊娠检查结果
                sowEvent.setCheckDate(event.getEventAt());                  //检查时间
                sowEvent.setExtra(JSON_MAPPER.toJson(checkResult));
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
                sowEvent.setFarrowWeight(event.getEventWeight());         //分娩总重(kg)
                sowEvent.setFarrowingDate(event.getEventAt());            //分娩时间
                sowEvent.setExtra(JSON_MAPPER.toJson(farrowing));
                break;
            case WEAN:          //断奶
                DoctorWeanDto wean = getSowWeanExtra(event);
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

    //拼接母猪转舍extra
    private DoctorChgLocationDto getSowTranBarnExtra(View_EventListSow event, Map<String, DoctorBarn> barnMap) {
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
        return transBarn;
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
        entry.setPigType(DoctorPig.PigSex.SOW.getKey());  //类型: 母猪
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
    private DoctorMatingDto getSowMatingExtra(View_EventListSow event, Map<String, DoctorPig> boarMap, Map<String, Long> subMap) {
        DoctorMatingDto mating = new DoctorMatingDto();
        mating.setMatingDate(event.getEventAt()); // 配种日期
        mating.setOperatorName(event.getStaffName()); // 配种人员
        mating.setOperatorId(subMap.get(event.getStaffName()));
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
    private DoctorWeanDto getSowWeanExtra(View_EventListSow event) {
        DoctorWeanDto wean = new DoctorWeanDto();
        wean.setPartWeanDate(event.getEventAt()); //断奶日期
        wean.setPartWeanRemark(event.getRemark());
        wean.setPartWeanPigletsCount(event.getWeanCount()); //断奶数量
        wean.setPartWeanAvgWeight(event.getWeanWeight());   //断奶平均重量
        return wean;
    }

    //拼接拼窝事件extra
    private DoctorFostersDto getSowFosterExtra(View_EventListSow event, Map<Integer, Map<String, DoctorBasic>> basicMap) {
        DoctorFostersDto foster = new DoctorFostersDto();
        foster.setFostersDate(DateUtil.toDateString(event.getEventAt()));   // 拼窝日期
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
        track.setStatus(status == null ? PigStatus.Entry.getKey() : status.getKey());
        track.setIsRemoval(sow.getIsRemoval());
        track.setWeight(card.getWeight());
        track.setOutFarmDate(DateUtil.toDate(card.getOutFarmDate()));
        track.setRemark(card.getRemark());
        track.setCurrentParity(card.getCurrentParity());
        DoctorPigEvent lastEvent = doctorPigEventDao.queryLastPigEventById(sow.getId());
        track.setCurrentEventId(notNull(lastEvent) ? lastEvent.getId() : 0L);

        if (notEmpty(events)) {
            //按照时间 asc 排序
            events = events.stream().sorted(Comparator.comparing(DoctorPigEvent::getEventAt)).collect(Collectors.toList());
            track.setExtra(JSON_MAPPER.toJson(getSowExtraMap(events)));   //extra字段保存当前胎次所有所有事件的extra

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

    //获取最后一个胎次的sowTrackExtraMap
    private static Map<String, Object> getSowExtraMap(List<DoctorPigEvent> events) {
        Map<String, Object> extraMap = Maps.newHashMap();
        for (DoctorPigEvent event : events) {
            if (Objects.equals(event.getType(), PigEvent.WEAN.getKey())) {
                extraMap.put("hasWeanToMating", true);
            }

            //配种事件说明是下一胎次, extra清掉重新搞
            if (Objects.equals(event.getType(), PigEvent.MATING.getKey())) {
                extraMap = Maps.newHashMap();
                extraMap.put("hasWeanToMating", false);
            }
            if (notEmpty(event.getExtra())) {
                extraMap.putAll(JSON_MAPPER.fromJson(event.getExtra(), JSON_MAPPER.createCollectionType(Map.class, String.class, Object.class)));

            }
        }
        return extraMap;
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
            } else if (Objects.equals(event.getType(), PigEvent.TO_MATING.getKey()) //转入配种舍
                    || Objects.equals(event.getType(), PigEvent.WEAN.getKey()) //断奶事件
                    || isNotPreg(event)) {
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
                    Objects.equals(event.getType(), PigEvent.WEAN.getKey())
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
        List<DoctorPig> pigs = boarCards.stream().map(card -> getBoar(card, farm, basicMap)).collect(Collectors.toList());
        if(!pigs.isEmpty()){
            doctorPigDao.creates(pigs);
        }

        //查出公猪, 转换成map
        Map<String, DoctorPig> boarMap = doctorPigDao.findPigsByFarmIdAndPigType(farm.getId(), DoctorPig.PigSex.BOAR.getKey()).stream()
                .collect(Collectors.toMap(DoctorPig::getOutId, v -> v));

        //2. 迁移DoctorPigEvent
        List<DoctorPigEvent> boarEvents = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, View_EventListBoar.class, "DoctorPigEvent-EventListBoar")).stream()
                .filter(loc -> isFarm(loc.getFarmOutId(), farm.getOutId()))
                .map(event -> getBoarEvent(event, boarMap, barnMap, basicMap, subMap, customerMap, changeReasonMap, vaccMap)).collect(Collectors.toList());
        if(!boarEvents.isEmpty()){
            doctorPigEventDao.creates(boarEvents);
        }

        //查出公猪事件, 按照公猪分组
        Map<Long, List<DoctorPigEvent>> boarEventMap = doctorPigEventDao.findByFarmIdAndKind(farm.getId(), DoctorPig.PigSex.BOAR.getKey())
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
        if(!boarTracks.isEmpty()){
            doctorPigTrackDao.creates(boarTracks);
        }

        boarTracks.forEach(boarTrack -> {
            //创建镜像
            DoctorPig boar = doctorPigDao.findById(boarTrack.getPigId());
            DoctorPigSnapshot pigSnapshot = DoctorPigSnapshot.builder()
                    .pigId(boar.getId())
                    .fromEventId(0L)
                    .toEventId(boarTrack.getCurrentEventId())
                    .toPigInfo(JsonMapperUtil.JSON_NON_DEFAULT_MAPPER.toJson(new DoctorPigSnapShotInfo(boar, boarTrack)))
                    .build();
            doctorPigSnapshotDao.create(pigSnapshot);
        });
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
        boar.setPigType(DoctorPig.PigSex.BOAR.getKey());  //猪类是公猪猪
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
        boar.setBoarType(boarType.getKey());
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
        boarEvent.setStatus(EventStatus.VALID.getValue());
        boarEvent.setEventSource(SourceType.IMPORT.getValue());

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
        entry.setBoarType(type == null ? null : type.getKey());
        entry.setBoarTypeName(event.getBoarType());
        entry.setPigType(DoctorPig.PigSex.BOAR.getKey());  //类型: 公猪
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
        DoctorPigEvent lastEvent = doctorPigEventDao.queryLastPigEventById(boar.getId());
        track.setCurrentEventId(notNull(lastEvent) ? lastEvent.getId() : 0L);

        if (notEmpty(events)) {
            //按照时间 asc 排序
            events = events.stream().sorted((a, b) -> a.getEventAt().compareTo(b.getEventAt())).collect(Collectors.toList());
            Map<String, Object> extraMap = Maps.newHashMap();
            events.forEach(event -> extraMap.putAll(JSON_MAPPER.fromJson(event.getExtra(), JSON_MAPPER.createCollectionType(Map.class, String.class, Object.class))));
            track.setExtra(JSON_MAPPER.toJson(extraMap));   //extra字段保存全部extra
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
        event.setStatus(EventStatus.VALID.getValue());
        event.setEventSource(SourceType.IMPORT.getValue());

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
        if(gainEvent.getWeight() == null || gainEvent.getQuantity() == null || gainEvent.getQuantity() == 0){
            event.setAvgWeight(0D);
        }else{
            event.setAvgWeight(gainEvent.getWeight() / gainEvent.getQuantity());
        }
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
                event.setInType(moveIn.getInType());
                event.setOtherBarnId(moveIn.getFromBarnId());
                event.setOtherBarnType(moveIn.getFromBarnType());
                event.setExtraMap(moveIn);
                event.setTransGroupType(getTransType(moveIn.getInType(), event.getPigType(), moveIn.getFromBarnType()).getValue());  //区分内转还是外转
                break;
            case CHANGE:
                DoctorChangeGroupEvent changeEvent = getChangeEvent(gainEvent, basicMap, changeReasonMap, customerMap);
                event.setExtraMap(changeEvent);
                event.setChangeTypeId(changeEvent.getChangeTypeId());
                event.setPrice(changeEvent.getPrice());
                event.setAmount(changeEvent.getAmount());

                //重量 均重重新计算
                if(event.getPrice() == 0L){
                    event.setWeight(0D);
                }else{
                    event.setWeight(event.getAmount() / event.getPrice() * 1D);
                }
                if(event.getWeight() == null || event.getQuantity() == null || event.getQuantity() == 0){
                    event.setAvgWeight(0D);
                }else{
                    event.setAvgWeight(event.getWeight() / event.getQuantity());
                }
                break;
            case TRANS_GROUP:
                DoctorTransGroupEvent transGroupEvent = getTranGroupEvent(gainEvent, basicMap, barnMap, groupMap, group);
                event.setOtherBarnId(transGroupEvent.getToBarnId());
                event.setOtherBarnType(transGroupEvent.getToBarnType());
                event.setExtraMap(transGroupEvent);
                event.setTransGroupType(getTransType(null, event.getPigType(), transGroupEvent.getToBarnType()).getValue());  //区分内转还是外转
                break;
            case TURN_SEED:
                DoctorTurnSeedGroupEvent turnSeed = getTurnSeedEvent(gainEvent, basicMap, barnMap, pigMap);
                event.setOtherBarnId(turnSeed.getToBarnId());
                event.setOtherBarnType(turnSeed.getToBarnType());
                event.setExtraMap(turnSeed);
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
            case TRANS_FARM: //转场肯定是外转, 相当于空降
                DoctorTransFarmGroupEvent transFarmEvent = getTranFarmEvent(gainEvent, basicMap, barnMap, groupMap, group);
                event.setOtherBarnId(transFarmEvent.getToBarnId());
                event.setOtherBarnType(transFarmEvent.getToBarnType());
                event.setExtraMap(transFarmEvent);
                event.setTransGroupType(DoctorGroupEvent.TransGroupType.OUT.getValue());
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
    private static DoctorGroupEvent.TransGroupType getTransType(Integer inType, Integer pigType, Integer toBarnType) {
        if (inType != null && !Objects.equals(inType, DoctorMoveInGroupEvent.InType.GROUP.getValue())) {
            return DoctorGroupEvent.TransGroupType.OUT;
        }
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
            turnSeed.setToBarnType(barn.getPigType());
        }

        return turnSeed;
    }

    //拼接猪群跟踪
    private DoctorGroupTrack getGroupTrack(DoctorGroup group, Proc_InventoryGain gain, List<DoctorGroupEvent> events) {
        DoctorGroupTrack groupTrack = new DoctorGroupTrack();
        groupTrack.setGroupId(group.getId());
        groupTrack.setSex(DoctorGroupTrack.Sex.MIX.getValue());

        //如果猪群已经关闭, 大部分的统计值可以置成0
        if (Objects.equals(group.getStatus(), DoctorGroup.Status.CLOSED.getValue())) {
            groupTrack.setAvgDayAge(gain == null ? 0 : gain.getAvgDayAge());    //关闭猪群的日龄
            return getCloseGroupTrack(groupTrack, group, events);
        }

        //未关闭的猪群, 拼接
        if (gain != null) {
            groupTrack.setAvgDayAge(gain.getAvgDayAge());
            groupTrack.setQuantity(MoreObjects.firstNonNull(gain.getQuantity(), 0));
            groupTrack.setBoarQty(gain.getQuantity() / 2);
            groupTrack.setSowQty(groupTrack.getQuantity() - groupTrack.getBoarQty());
            groupTrack.setBirthDate(DateTime.now().minusDays(groupTrack.getAvgDayAge() == null ? 1 : groupTrack.getAvgDayAge()).toDate());
        }
        DoctorGroupEvent lastEvent = events.stream().sorted((a, b) -> b.getEventAt().compareTo(a.getEventAt())).findFirst().orElse(null);
        groupTrack.setRelEventId(lastEvent == null ? null : lastEvent.getId());

        //更新产房仔猪
        return doctorGroupReportManager.updateGroupTrackReport(groupTrack, group.getPigType());
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
        return groupTrack;
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
            group.setStaffName(gain.getStaffName());
        }
        if (DoctorGroup.Status.CREATED.getValue() == group.getStatus()) {
            group.setCloseAt(null);
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

    private Integer parity;
    private String boarCode;
    private Integer statusBefore;
    private Integer statusAfter;
    private Integer quantity;
    private Integer quantityChange;
    private Boolean isWeanToMate;
    public void updateParityAndBoarCode(DoctorFarm farm) {
        List<DoctorPigEvent> doctorPigEvensList = doctorPigEventDao.list(ImmutableMap.of("farmId", farm.getId(), "type", PigEvent.ENTRY.getKey(), "kind", 1));
        updateParityAndBoarCodeByEntryEvents(doctorPigEvensList);
    }

    private void updateParityAndBoarCodeByEntryEvents(List<DoctorPigEvent> doctorPigEvensList) {
        doctorPigEvensList.forEach(doctorPigEvent -> {
            //log.info("update doctor_pig_events start: {}", doctorPigEvent);
            List<DoctorPigEvent> lists = doctorPigEventDao.queryAllEventsByPigIdForASC(doctorPigEvent.getPigId());
            parity = 1;
            boarCode = null;
            statusBefore = null;
            statusAfter = PigStatus.Entry.getKey();
            quantity = 1000; //一个胎次中分娩的活仔数, 默认1000,为了与quantityChange 不相等
            quantityChange = 0; //在一个胎次中仔猪变化的数量
            isWeanToMate = false; //判断是否是断奶到配种,断奶到配种才+1
            lists.stream()
                    .filter(doctorPigEvent1 -> doctorPigEvent1 != null && doctorPigEvent1.getType() != null)
                    .forEach(doctorPigEvent1 -> {
                        statusAfter = statusBefore;
                        switch (doctorPigEvent1.getType()){
                            case 6:
                                statusAfter = PigStatus.Removal.getKey();
                                break;
                            case 7:
                                if(!isNull(doctorPigEvent1.getExtraMap()) && !isNull(doctorPigEvent1.getExtraMap().get("parity")) && !Objects.equals("0", doctorPigEvent1.getExtraMap().get("parity").toString())){
                                    parity = Integer.valueOf(Objects.toString(doctorPigEvent1.getExtraMap().get("parity"))) ;
                                }
                                statusAfter = PigStatus.Entry.getKey();
                                break;
                            case 9:
                                if(isWeanToMate && doctorPigEvent1.getCurrentMatingCount() == 1){
                                    parity += 1;
                                }
                                boarCode = (isNull(doctorPigEvent1.getExtraMap()) || isNull(doctorPigEvent1.getExtraMap().get("matingBoarPigCode"))) ? null : Objects.toString(doctorPigEvent1.getExtraMap().get("matingBoarPigCode"));
                                statusAfter = PigStatus.Mate.getKey();
                                isWeanToMate = false;
                                break;
                            case 10:
                                if(statusBefore.equals(PigStatus.Wean.getKey())){
                                    statusAfter = PigStatus.Mate.getKey();
                                }else if(statusBefore.equals(PigStatus.KongHuai.getKey())){
                                    statusAfter = PigStatus.Entry.getKey();
                                }else if(statusBefore.equals(PigStatus.Pregnancy.getKey())){
                                    statusAfter = PigStatus.Pregnancy.getKey();
                                }else if(statusBefore.equals(PigStatus.Mate.getKey())){
                                    statusAfter = PigStatus.Mate.getKey();
                                }
                                break;
                            case 11:
                                if(!isNull(doctorPigEvent1.getExtraMap()) && !isNull(doctorPigEvent1.getExtraMap().get("checkResult"))){
                                    String checkResult = Objects.toString(doctorPigEvent1.getExtraMap().get("checkResult"));
                                    switch(checkResult){
                                        case "1":
                                            statusAfter = PigStatus.Pregnancy.getKey();
                                            break;
                                        case "2":
                                            statusAfter = PigStatus.KongHuai.getKey();
                                            break;
                                        case "3":
                                            statusAfter = PigStatus.KongHuai.getKey();
                                            break;
                                        case "4":
                                            statusAfter = PigStatus.KongHuai.getKey();
                                            break;
                                    }
                                }
                                break;
                            case 12:
                                statusAfter = PigStatus.Mate.getKey();
                                break;
                            case 14:
                                statusAfter = PigStatus.Farrow.getKey();
                                break;
                            case 15:
                                statusAfter = PigStatus.FEED.getKey();
                                quantity = doctorPigEvent1.getLiveCount();
                                quantityChange = 0;
                                break;
                            case 16:
                                boarCode = null;
                                quantityChange += MoreObjects.firstNonNull(doctorPigEvent1.getWeanCount(), 0);
                                if(quantity == quantityChange){
                                    statusAfter = PigStatus.Wean.getKey();
                                }
                                isWeanToMate = true;
                                break;
                            case 17:
                                quantityChange += Integer.parseInt(Objects.toString(doctorPigEvent1.getExtraMap().get("fostersCount")));
                                if(quantity == quantityChange){
                                    statusAfter = PigStatus.Wean.getKey();
                                }
                                break;
                            case 18:
                                quantityChange += Integer.parseInt(Objects.toString(doctorPigEvent1.getExtraMap().get("pigletsCount")));
                                if(quantity == quantityChange){
                                    statusAfter = PigStatus.Wean.getKey();
                                }
                                break;
                            case 19:
                                quantity += Objects.isNull(doctorPigEvent1.getExtraMap().get("fostersCount")) ? 0 : Integer.parseInt(Objects.toString(doctorPigEvent1.getExtraMap().get("fostersCount")));
                                break;
                            default:
                                break;
                        }
                        doctorPigEvent1.setPigStatusBefore(statusBefore);
                        doctorPigEvent1.setPigStatusAfter(statusAfter);
                        doctorPigEvent1.setParity(parity);
                        doctorPigEvent1.setBoarCode(boarCode);
                        statusBefore = statusAfter;
                    });
            Boolean result = doctorPigEventDao.updates(lists);
            if(!result){
                log.info("update parity boarCode fail: {}", lists);
            }
        });
    }


    public void updateExcelImportErrorPigEvents(DoctorFarm farm) {
        List<DoctorPigEvent> doctorPigEvensList = doctorPigEventDao.list(ImmutableMap.of("farmId", farm.getId(), "type", PigEvent.ENTRY.getKey(), "kind", 1, "createdAtEnd", "2017-03-20 23:59:59"));
        if (doctorPigEvensList.isEmpty()) {
            return;
        }
        List<List<DoctorPigEvent>> lists = Lists.partition(doctorPigEvensList, 1000);
        lists.forEach(list -> {
            for(DoctorPigEvent doctorPigEvent: list) {
                Map<String, Object> map = new HashMap<String, Object>();
                if(!isNull(doctorPigEvent.getExtraMap()) && !isNull(doctorPigEvent.getExtraMap().get("importParity"))){
                    continue;
                }
                if(!isNull(doctorPigEvent.getExtraMap()) && !isNull(doctorPigEvent.getExtraMap().get("parity")) ){
                    map = doctorPigEvent.getExtraMap();
                    map.put("importParity", map.get("parity"));
                    map.replace("parity", Integer.valueOf(map.get("parity").toString()) + 1);
                }else{
                    map.put("parity", 1);
                }

                doctorPigEvent.setExtraMap(map);
            }
            doctorPigEventDao.updates(list);
        });
    }



    public void updateFosterSowCode(DoctorFarm farm){
        List<DoctorPigEvent> doctorPigEvensList = doctorPigEventDao.list(ImmutableMap.of("farmId", farm.getId(), "type", PigEvent.FOSTERS.getKey(), "kind", 1));
        List<List<DoctorPigEvent>> lists = Lists.partition(doctorPigEvensList, 1000);
        lists.forEach(list->{
            list.forEach(doctorPigEvent -> {
                Map<String, Object> extra = doctorPigEvent.getExtraMap();
                if(extra.containsKey("fosterSowId") && !extra.containsKey("fosterSowCode")){
                    DoctorPig doctorPig = doctorPigDao.findById(Long.valueOf(Objects.toString(extra.get("fosterSowId"))));
                    if(!isNull(doctorPig)){
                        extra.put("fosterSowCode", doctorPig.getPigCode());
                    }
                }
                doctorPigEvent.setExtraMap(extra);
            });
            doctorPigEventDao.updates(list);
        });
    }

    public Response<Boolean> refreshPigStatus() {
        try {
            for (int i = 0;; i++) {
                Paging<DoctorPigTrack> trackPage = doctorPigTrackDao.paging(i, 1000, ImmutableMap.of("status", PigStatus.Entry.getKey()));
                trackPage.getData().forEach(doctorPigTrack -> {
                    if (!doctorPigEventDao.list(ImmutableMap.of("type", PigEvent.TO_MATING.getKey())).isEmpty()){
                        doctorPigTrack.setStatus(PigStatus.Wean.getKey());
                        doctorPigTrack.setUpdatedAt(new Date());
                        doctorPigTrackDao.update(doctorPigTrack);
                    }
                });
                if (trackPage.getData().size() < 1000){
                    break;
                }
            }
            return Response.ok(Boolean.TRUE);
        } catch (Exception e){
            log.error("refresh.pig.status.failed, cause{}", Throwables.getStackTraceAsString(e));
            return Response.fail("refresh.pig.status.failed");
        }
    }

    /**
     * 刷新npd
     */
    @Transactional
    public void flushNpd(Long farmId) {
        log.info("flush npd farmId:{}", farmId);
        Map<Long, List<DoctorPigEvent>> eventMap = doctorPigEventDao.findByFarmIdAndKind(farmId, 1).stream()
                .collect(Collectors.groupingBy(DoctorPigEvent::getPigId));
        eventMap.values().forEach(events -> {
            events = events.stream()
                    .sorted(Comparator.comparing(DoctorPigEvent::getEventAt).thenComparing(DoctorPigEvent::getId))
                    .collect(Collectors.toList());

            //更新事件的非生产天数
            updateNPD(events);

            //更新初配事件的是否已经分娩 和是否已经怀孕 的标志位
            updateFlag(events);

            //统计孕期 和 哺乳期
            updateDuring(events);

            //更新event
            events.forEach(doctorPigEventDao::update);
        });
        log.info("flush npd ok! farmId:{}", farmId);
    }

    /**
     * 刷分娩母猪track的group_id
     */
    @Transactional
    public void flushFarrowSowTrackGroupId(Long farmId) {
        log.info("flush farrrow sow track groupId, farmId:{}", farmId);

        //查出所有的groupId = null 的哺乳母猪, 找到产房的猪群
        doctorPigTrackDao.findByFarmIdAndStatus(farmId, PigStatus.FEED.getKey()).stream()
                .filter(track -> track.getGroupId() == null)
                .forEach(track -> {
                    DoctorGroup group = doctorGroupDao.findCurrentFarrowByBarnId(track.getCurrentBarnId());
                    if (group == null) {
                        log.error("farrow group not found, piginfo:{}", track);
                    } else {
                        DoctorPigTrack updateTrack = new DoctorPigTrack();
                        updateTrack.setId(track.getId());
                        updateTrack.setGroupId(group.getId());
                        doctorPigTrackDao.update(updateTrack);
                    }
                });
    }

    /**
     * 刷分娩事件的group_id
     */
    @Transactional
    public void flushFarrowEventGroupId(Long farmId) {
        log.info("flush farrow event groupId, farmId:{}", farmId);

        //查出所有的 groupId = null 的分娩事件, 更新一发
        doctorPigEventDao.findByFarmIdAndKindAndEventTypes(farmId,
                DoctorPig.PigSex.SOW.getKey(), Lists.newArrayList(PigEvent.FARROWING.getKey())).stream()
                .filter(event -> event.getGroupId() == null)
                .forEach(event -> {
                    DoctorGroup group = null;
                    DoctorFarrowingDto farrow = MAPPER.fromJson(event.getExtra(), DoctorFarrowingDto.class);
                    if (farrow != null && notEmpty(farrow.getGroupCode())) {
                        group = doctorGroupDao.findByFarmIdAndGroupCode(farmId, farrow.getGroupCode());
                    }
                    if (group == null) {
                        group = doctorGroupDao.findByFarmIdAndBarnIdAndDate(farmId, event.getBarnId(), event.getEventAt());
                    }

                    //更新group_id
                    updateEventGroupId(event, group);
                });
    }

    /**
     * 刷断奶事件的group_id
     */
    @Transactional
    public void flushWeanEventGroupId(Long farmId) {
        log.info("flush wean event groupId, farmId:{}", farmId);

        //查出所有的 groupId = null 的断奶事件, 更新一发
        doctorPigEventDao.findByFarmIdAndKindAndEventTypes(farmId,
                DoctorPig.PigSex.SOW.getKey(), Lists.newArrayList(PigEvent.WEAN.getKey())).stream()
                .filter(event -> event.getGroupId() == null)
                .forEach(event -> {
                    DoctorGroup group = doctorGroupDao.findByFarmIdAndBarnIdAndDate(farmId, event.getBarnId(), event.getEventAt());
                    if (group == null) {
                        DoctorPigEvent farrowEvent = doctorPigEventDao.findLastByTypeAndDate(event.getPigId(), event.getEventAt(), PigEvent.FARROWING.getKey());
                        if (farrowEvent != null && farrowEvent.getGroupId() != null) {
                            group = new DoctorGroup();
                            group.setId(farrowEvent.getGroupId());
                        }
                    }

                    //更新group_id
                    updateEventGroupId(event, group);
                });
    }

    private void updateEventGroupId(DoctorPigEvent event, DoctorGroup group) {
        if (group == null || group.getId() == null) {
            log.error("farrow event group not found, eventInfo:{}", event);
        } else {
            DoctorPigEvent updateEvent = new DoctorPigEvent();
            updateEvent.setId(event.getId());
            updateEvent.setGroupId(group.getId());
            doctorPigEventDao.update(updateEvent);
        }
    }

    public void updateParityAndBoarCodeByPigId(Long pigId) {
        List<DoctorPigEvent> doctorPigEvensList = doctorPigEventDao.list(ImmutableMap.of("pigId", pigId , "type", PigEvent.ENTRY.getKey()));
        updateParityAndBoarCodeByEntryEvents(doctorPigEvensList);
    }
}