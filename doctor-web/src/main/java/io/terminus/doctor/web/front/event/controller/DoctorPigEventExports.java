package io.terminus.doctor.web.front.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Throwables;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.boar.DoctorBoarConditionDto;
import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
import io.terminus.doctor.event.dto.event.group.*;
import io.terminus.doctor.event.dto.event.sow.*;
import io.terminus.doctor.event.dto.event.usual.*;
import io.terminus.doctor.event.enums.*;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorPigEventWriteService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.web.core.export.Exporter;
import io.terminus.doctor.web.front.event.dto.*;
import io.terminus.doctor.web.util.TransFromUtil;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlMapFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.notNull;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

/**
 * Created by terminus on 2017/3/20.
 */
@Slf4j
@Controller
@RequestMapping("/api/doctor/events/pig")
public class DoctorPigEventExports {

    private final DoctorPigReadService doctorPigReadService;

    private final DoctorPigEventReadService doctorPigEventReadService;

    private final DoctorPigEventWriteService doctorPigEventWriteService;

    private final UserReadService userReadService;

    private final DoctorGroupReadService doctorGroupReadService;

    private final TransFromUtil transFromUtil;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();
    private static final JsonMapperUtil JSON_MAPPER  = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER;

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Autowired
    private Exporter exporter;
    @Autowired
    public DoctorPigEventExports(DoctorPigReadService doctorPigReadService,
                                 DoctorPigEventReadService doctorPigEventReadService,
                                 DoctorPigEventWriteService doctorPigEventWriteService,
                                 UserReadService userReadService,
                                 DoctorGroupReadService doctorGroupReadService,
                                 TransFromUtil transFromUtil) {
        this.doctorPigReadService = doctorPigReadService;
        this.doctorPigEventReadService = doctorPigEventReadService;
        this.doctorPigEventWriteService = doctorPigEventWriteService;
        this.userReadService = userReadService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.transFromUtil = transFromUtil;
    }

    /**
     * 猪入场事件的导出报表的构建
     */
    public Paging<DoctorPigBoarInFarmExportDto> pagingInFarmExport(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPigBoarInFarmExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorFarmEntryDto farmEntryDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorFarmEntryDto.class);
                DoctorPigBoarInFarmExportDto dto = OBJECT_MAPPER.convertValue(farmEntryDto, DoctorPigBoarInFarmExportDto.class);
                if (doctorPigEventDetail.getExtraMap().containsKey("source")) {
                    dto.setSourceName(PigSource.from(dto.getSource()).getDesc());
                }
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setParity(doctorPigEventDetail.getParity());
                dto.setInitBarnName(doctorPigEventDetail.getBarnName());
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                return dto;
            }catch (Exception e){
                log.error("pagingInFarmExport error: {}", Throwables.getStackTraceAsString(e));
            }
            return new DoctorPigBoarInFarmExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 公猪采精事件事件的导出报表构建
     */
    public Paging<DoctorPigSemenExportDto> pagingSemenExport(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPigSemenExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorSemenDto semenDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorSemenDto.class);
                DoctorPigSemenExportDto dto = OBJECT_MAPPER.convertValue(semenDto, DoctorPigSemenExportDto.class);
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                return dto;
            }catch (Exception e){
                log.error("pagingSemenExport error: {}", Throwables.getStackTraceAsString(e));
            }
            return new DoctorPigSemenExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 转舍事件
     */
    public Paging<DoctorPigChangeBarnExportDto> pagingChangeBarn(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPigChangeBarnExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorChgLocationDto conditionDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorChgLocationDto.class);
                DoctorPigChangeBarnExportDto dto = OBJECT_MAPPER.convertValue(conditionDto, DoctorPigChangeBarnExportDto.class);
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                dto.setParity(doctorPigEventDetail.getParity());
                dto.setRemark(doctorPigEventDetail.getRemark());
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                if (doctorPigEventDetail.getPigStatusAfter() != null) {
                    dto.setPigStatusAfterName(PigStatus.from(doctorPigEventDetail.getPigStatusAfter()).getDesc());
                }
                return dto;
            }catch (Exception e){
                log.error("pagingChangeBarn error :{} fail", Throwables.getStackTraceAsString(e));
            }
            return new DoctorPigChangeBarnExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 公猪的疾病事件
     */
    public Paging<DoctorPigDiseaseExportDto> pagingDisease(Map<String, String> pigEventCriteria){
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPigDiseaseExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorDiseaseDto diseaseDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorDiseaseDto.class);
                DoctorPigDiseaseExportDto dto = OBJECT_MAPPER.convertValue(diseaseDto, DoctorPigDiseaseExportDto.class);
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                dto.setParity(doctorPigEventDetail.getParity());
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                return dto;
            }catch (Exception e){
                log.error("pagingDisease error: {}", Throwables.getStackTraceAsString(e));
            }
            return new DoctorPigDiseaseExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }

    /**
     * 猪的防疫事件报表模板
     */
    public Paging<DoctorPigVaccinationExportDto> pagingVaccination(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPigVaccinationExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorVaccinationDto vaccinationDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorVaccinationDto.class);
                DoctorPigVaccinationExportDto dto = OBJECT_MAPPER.convertValue(vaccinationDto, DoctorPigVaccinationExportDto.class);
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                return dto;
            }catch (Exception e){
                log.error("pagingVaccination error: {}", Throwables.getStackTraceAsString(e));
            }
            return new DoctorPigVaccinationExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 猪的离场事件
     */
    public Paging<DoctorPigRemoveExportDto> pagingRemove(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPigRemoveExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorRemovalDto removalDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorRemovalDto.class);
                DoctorPigRemoveExportDto dto = OBJECT_MAPPER.convertValue(removalDto, DoctorPigRemoveExportDto.class);
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setAmount(doctorPigEventDetail.getAmount());
                if (dto.getPrice() != null) {
                    dto.setPrice(dto.getPrice() / 100);
                }
                dto.setParity(doctorPigEventDetail.getParity());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                dto.setOperatorName(doctorPigEventDetail.getOperatorName());
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                return dto;
            }catch(Exception e){
                log.error("pagingRemove error: {}", Throwables.getStackTraceAsString(e));
            }
            return new DoctorPigRemoveExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 猪的配种事件
     */
    public Paging<DoctorPigMatingExportDto> pagingMating(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPigMatingExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorMatingDto matingDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorMatingDto.class);
                DoctorPigMatingExportDto dto = OBJECT_MAPPER.convertValue(matingDto, DoctorPigMatingExportDto.class);
                dto.setParity(doctorPigEventDetail.getParity());
                if (dto.getMatingType() != null) {
                    dto.setMatingTypeName(MatingType.from(dto.getMatingType()).getDesc());
                }
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                if (doctorPigEventDetail.getPigStatusAfter() != null) {
                    dto.setPigStatusAfterName(PigStatus.from(doctorPigEventDetail.getPigStatusAfter()).getDesc());
                }
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setOperatorName(doctorPigEventDetail.getOperatorName());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                return dto;
            }catch (Exception e){
                log.error("pagingMating error: {}", Throwables.getStackTraceAsString(e));
            }
            return new DoctorPigMatingExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }

    /**
     * 仔猪变动事件
     */
    public Paging<DoctorPigletsChgExportDto> pagingLetsChg(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPigletsChgExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorPigletsChgDto matingDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorPigletsChgDto.class);
                DoctorPigletsChgExportDto dto = OBJECT_MAPPER.convertValue(matingDto, DoctorPigletsChgExportDto.class);
                dto.setParity(doctorPigEventDetail.getParity());
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                return dto;
            }catch (Exception e){
                log.error("pagingLetsChg error: {}", Throwables.getStackTraceAsString(e));
            }
            return new DoctorPigletsChgExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 断奶事件
     */
    public Paging<DoctorWeanExportDto> pagingWean(Map<String, String> pigEventCriteria) {

        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorWeanExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorWeanDto weanDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorWeanDto.class);
                DoctorWeanExportDto dto = OBJECT_MAPPER.convertValue(weanDto, DoctorWeanExportDto.class);
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setParity(doctorPigEventDetail.getParity());
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                dto.setOperatorName(doctorPigEventDetail.getOperatorName());
                return dto;
            }catch (Exception e){
                log.error("pagingWean error :{} fail"+ Throwables.getStackTraceAsString(e));
            }
            return new DoctorWeanExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     *检查
     */
    public Paging<DoctorPregChkResultExportDto> pagingPregChkResult(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorPregChkResultExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorPregChkResultDto pregChkResultDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorPregChkResultDto.class);
                DoctorPregChkResultExportDto dto = OBJECT_MAPPER.convertValue(pregChkResultDto, DoctorPregChkResultExportDto.class);

                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setParity(doctorPigEventDetail.getParity());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                dto.setOperatorName(doctorPigEventDetail.getOperatorName());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                if (doctorPigEventDetail.getPregCheckResult() != null) {
                    dto.setCheckResultName(PregCheckResult.from(doctorPigEventDetail.getPregCheckResult()).getDesc());
                }
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                return dto;
            }catch (Exception e){
                log.error("pagingPregChkResult error: {}", Throwables.getStackTraceAsString(e));
            }
            return new DoctorPregChkResultExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);

    }
    /**
     *分娩
     */
    public Paging<DoctorFarrowingExportDto> pagingFarrowing( Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorFarrowingExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorFarrowingDto farrowingDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorFarrowingDto.class);
                DoctorFarrowingExportDto dto = OBJECT_MAPPER.convertValue(farrowingDto, DoctorFarrowingExportDto.class);
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setParity(doctorPigEventDetail.getParity());
                if (dto.getFarrowingType() != null) {
                    dto.setFarrowingTypeName(FarrowingType.from(dto.getFarrowingType()).getDesc());
                }
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                return dto;
            }catch (Exception e){
                log.error("pagingFarrowing error, cause:{}", Throwables.getStackTraceAsString(e));
            }
            return new DoctorFarrowingExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 拼窝
     */
    public Paging<DoctorFostersExportDto> pagingFosters(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorFostersExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorFostersDto fostersDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorFostersDto.class);
                DoctorFostersExportDto dto = OBJECT_MAPPER.convertValue(fostersDto, DoctorFostersExportDto.class);
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setParity(doctorPigEventDetail.getParity());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                dto.setOperatorName(doctorPigEventDetail.getOperatorName());
                return dto;
            }catch (Exception e){
                log.error("pagingFosters error :{} fail",Throwables.getStackTraceAsString(e));
            }
            return new DoctorFostersExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 公猪体况
     */
    public Paging<DoctorBoarConditionExportDto> pagingBoarCondition(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorBoarConditionExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorBoarConditionDto boarConditionDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorBoarConditionDto.class);
                DoctorBoarConditionExportDto dto = OBJECT_MAPPER.convertValue(boarConditionDto, DoctorBoarConditionExportDto.class);
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                dto.setOperatorName(doctorPigEventDetail.getOperatorName());
                return dto;
            }catch (Exception e){
                log.error("pagingBoarCondition error: {}", Throwables.getStackTraceAsString(e));
            }
            return new DoctorBoarConditionExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 母猪体况
     */
    public Paging<DoctorSowConditionExportDto> pagingsowCondition(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorSowConditionExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorConditionDto boarConditionDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorConditionDto.class);
                DoctorSowConditionExportDto dto = OBJECT_MAPPER.convertValue(boarConditionDto, DoctorSowConditionExportDto.class);
                dto.setCreatorName(doctorPigEventDetail.getCreatorName());
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setBarnName(doctorPigEventDetail.getBarnName());
                dto.setOperatorName(doctorPigEventDetail.getOperatorName());
                return dto;
            }catch (Exception e){
                log.error("pagingBoarCondition error: {}", Throwables.getStackTraceAsString(e));
            }
            return new DoctorSowConditionExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }
    /**
     * 猪的转场事件
     */
    public Paging<DoctorChgFarmExportDto> pagingChgFarm(Map<String, String> pigEventCriteria) {
        Paging<DoctorPigEvent> pigEventPaging = pigEventPaging(pigEventCriteria);
        List<DoctorChgFarmExportDto> list = pigEventPaging.getData().stream().map(doctorPigEventDetail -> {
            try {
                DoctorChgFarmDto chgFarmDto = JSON_MAPPER.fromJson(doctorPigEventDetail.getExtra(), DoctorChgFarmDto.class);
                DoctorChgFarmExportDto dto = OBJECT_MAPPER.convertValue(chgFarmDto, DoctorChgFarmExportDto.class);
                dto.setPigCode(doctorPigEventDetail.getPigCode());
                dto.setOperatorName(doctorPigEventDetail.getOperatorName());
                return dto;
            }catch (Exception e){
                log.error("pagingChgFarm error: {}", Throwables.getStackTraceAsString(e));
            }
            return new DoctorChgFarmExportDto();
        }).collect(toList());
        return new Paging<>(pigEventPaging.getTotal(), list);
    }

    /**
     * 新建猪群
     */
    public Paging<DoctorNewExportGroup> pagingNewGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorNewExportGroup> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            try {
                DoctorNewGroupEvent newGroupEvent = JSON_MAPPER.fromJson(doctorGroupEventDetail.getExtra(), DoctorNewGroupEvent.class);
                DoctorNewExportGroup exportData = BeanMapper.map(newGroupEvent, DoctorNewExportGroup.class);
                DoctorGroup group = RespHelper.or500(doctorGroupReadService.findGroupById(doctorGroupEventDetail.getGroupId()));
                exportData.setPigTypeName(PigType.from(doctorGroupEventDetail.getPigType()).getDesc());
                exportData.setBreedName(group.getBreedName());
                exportData.setStatus(DoctorGroup.Status.from(group.getStatus()).getDesc());
                exportData.setGroupCode(doctorGroupEventDetail.getGroupCode());
                exportData.setBarnName(doctorGroupEventDetail.getBarnName());
                exportData.setEventAt(doctorGroupEventDetail.getEventAt());
                exportData.setRemark(doctorGroupEventDetail.getRemark());
                exportData.setCreatorName(doctorGroupEventDetail.getCreatorName());
                return exportData;
            } catch (Exception e) {
                log.info("get.group.new.failed, eventId:{}", doctorGroupEventDetail.getId());
            }
            return new DoctorNewExportGroup();
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }

    /**
     *
     * @param groupEventCriteriaMap
     * @return
     * 转入猪群
     */
    public Paging<DoctorMoveInGroupExportDto> pagingMoveInGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorMoveInGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            try {
                DoctorMoveInGroupEvent moveInGroupEvent = JSON_MAPPER.fromJson(doctorGroupEventDetail.getExtra(), DoctorMoveInGroupEvent.class);
                DoctorMoveInGroupExportDto exportData = BeanMapper.map(moveInGroupEvent, DoctorMoveInGroupExportDto.class);
                exportData.setQuantity(doctorGroupEventDetail.getQuantity());
                exportData.setAvgDayAge(doctorGroupEventDetail.getAvgDayAge());
                exportData.setAvgWeight(doctorGroupEventDetail.getAvgWeight());
                exportData.setAmount(doctorGroupEventDetail.getAmount());
                exportData.setWeight(doctorGroupEventDetail.getWeight());
                exportData.setEventAt(doctorGroupEventDetail.getEventAt());
                exportData.setGroupCode(doctorGroupEventDetail.getGroupCode());
                exportData.setBarnName(doctorGroupEventDetail.getBarnName());
                exportData.setRemark(doctorGroupEventDetail.getRemark());
                exportData.setCreatorName(doctorGroupEventDetail.getCreatorName());
                exportData.setSource(isNull(moveInGroupEvent.getSource()) ? null : PigSource.from(moveInGroupEvent.getSource()).getDesc());
                exportData.setSex(isNull(moveInGroupEvent.getSex()) ? null : DoctorGroupTrack.Sex.from(moveInGroupEvent.getSex()).getDesc());
                return exportData;
            } catch (Exception e) {
                log.info("get.group.MoveIn.failed, eventId:{}", doctorGroupEventDetail.getId());
            }
            return new DoctorMoveInGroupExportDto();
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }

    /**
     * 猪群变动
     * @param groupEventCriteriaMap
     * @return
     */
    public Paging<DoctorChangeGroupExportDto> pagingChangeGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorChangeGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            try {
                DoctorChangeGroupEvent changeGroupEvent = JSON_MAPPER.fromJson(doctorGroupEventDetail.getExtra(), DoctorChangeGroupEvent.class);
                DoctorChangeGroupExportDto exportData = BeanMapper.map(changeGroupEvent, DoctorChangeGroupExportDto.class);
                exportData.setGroupCode(doctorGroupEventDetail.getGroupCode());
                exportData.setBarnName(doctorGroupEventDetail.getBarnName());
                exportData.setQuantity(doctorGroupEventDetail.getQuantity());
                exportData.setEventAt(doctorGroupEventDetail.getEventAt());
                exportData.setRemark(doctorGroupEventDetail.getRemark());
                exportData.setCreatorName(doctorGroupEventDetail.getCreatorName());
                return exportData;
            } catch (Exception e) {
                log.info("get.group.change.failed, eventId:{}", doctorGroupEventDetail.getId());
            }
            return new DoctorChangeGroupExportDto();
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }

    /**
     * 转群事件
     * @param groupEventCriteriaMap
     * @return
     */
    public Paging<DoctorChgFarmGroupExportDto> pagingChgFramGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorChgFarmGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            try {
                DoctorTransFarmGroupEvent transFarmGroupEvent = JSON_MAPPER.fromJson(doctorGroupEventDetail.getExtra(), DoctorTransFarmGroupEvent.class);
                DoctorChgFarmGroupExportDto exportData = BeanMapper.map(transFarmGroupEvent, DoctorChgFarmGroupExportDto.class);
                exportData.setGroupCode(doctorGroupEventDetail.getGroupCode());
                exportData.setBarnName(doctorGroupEventDetail.getBarnName());
                exportData.setFarmName(doctorGroupEventDetail.getFarmName());
                exportData.setEventAt(doctorGroupEventDetail.getEventAt());
                exportData.setQuantity(doctorGroupEventDetail.getQuantity());
                exportData.setWeight(doctorGroupEventDetail.getWeight());
                exportData.setRemark(doctorGroupEventDetail.getRemark());
                exportData.setCreatorName(doctorGroupEventDetail.getCreatorName());
                return exportData;
            } catch (Exception e) {
                log.info("get.group.chgFarm.failed, eventId:{}", doctorGroupEventDetail.getId());
            }
            return new DoctorChgFarmGroupExportDto();
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }

    /**
     * 猪群疾病事件
     * @param groupEventCriteriaMap
     * @return
     */
    public Paging<DoctorDiseaseGroupExportDto> pagingDiseaseGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorDiseaseGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            try {
                DoctorDiseaseGroupEvent diseaseGroupEvent = JSON_MAPPER.fromJson(doctorGroupEventDetail.getExtra(), DoctorDiseaseGroupEvent.class);
                DoctorDiseaseGroupExportDto exportData = BeanMapper.map(diseaseGroupEvent, DoctorDiseaseGroupExportDto.class);
                exportData.setGroupCode(doctorGroupEventDetail.getGroupCode());
                exportData.setBarnName(doctorGroupEventDetail.getBarnName());
                exportData.setEventAt(doctorGroupEventDetail.getEventAt());
                exportData.setRemark(doctorGroupEventDetail.getRemark());
                exportData.setCreatorName(doctorGroupEventDetail.getCreatorName());
                return exportData;
            } catch (Exception e) {
                log.info("get.group.disease.failed, eventId:{}", doctorGroupEventDetail.getId());
            }
            return new DoctorDiseaseGroupExportDto();
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }

    /**
     * 猪群免疫事件
     * @param groupEventCriteriaMap
     * @return
     */
    public Paging<DoctorVaccinationGroupExportDto> pagingVaccinationGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorVaccinationGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            try {
                DoctorAntiepidemicGroupEvent antiepidemicGroupEvent = JSON_MAPPER.fromJson(doctorGroupEventDetail.getExtra(), DoctorAntiepidemicGroupEvent.class);
                DoctorVaccinationGroupExportDto exportData = BeanMapper.map(antiepidemicGroupEvent, DoctorVaccinationGroupExportDto.class);
                exportData.setGroupCode(doctorGroupEventDetail.getGroupCode());
                exportData.setBarnName(doctorGroupEventDetail.getBarnName());
                exportData.setEventAt(doctorGroupEventDetail.getEventAt());
                exportData.setRemark(doctorGroupEventDetail.getRemark());
                exportData.setCreatorName(doctorGroupEventDetail.getCreatorName());
                return exportData;
            } catch (Exception e) {
                log.info("get.group.vaccination.failed, eventId:{}", doctorGroupEventDetail.getId());
            }
            return new DoctorVaccinationGroupExportDto();
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }

    /**
     * 转群事件
     * @param groupEventCriteriaMap
     * @return
     */
    public Paging<DoctorTransGroupExportDto> pagingTransGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorTransGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            try {
                DoctorTransGroupExportDto exportData = BeanMapper.map(doctorGroupEventDetail, DoctorTransGroupExportDto.class);
                DoctorTransGroupEvent transGroupEvent = JSON_MAPPER.fromJson(exportData.getExtra(), DoctorTransGroupEvent.class);
                exportData.setToBarnName(transGroupEvent.getToBarnName());
                exportData.setToGroupCode(transGroupEvent.getToGroupCode());
                exportData.setGroupCode(doctorGroupEventDetail.getGroupCode());
                exportData.setBarnName(doctorGroupEventDetail.getBarnName());
                exportData.setEventAt(doctorGroupEventDetail.getEventAt());
                exportData.setRemark(doctorGroupEventDetail.getRemark());
                exportData.setCreatorName(doctorGroupEventDetail.getCreatorName());
                return exportData;
            } catch (Exception e) {
                log.info("get.group.transGroup.failed, eventId:{}", doctorGroupEventDetail.getId());
            }
            return new DoctorTransGroupExportDto();
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }

    /**
     * 商品猪转种猪
     * @param groupEventCriteriaMap
     * @return
     */
    public Paging<DoctorTurnSeedGroupExportDto> pagingTurnSeedGroup(Map<String, String> groupEventCriteriaMap) {
        Paging<DoctorGroupEvent> paging = groupEventPaging(groupEventCriteriaMap);
        List<DoctorTurnSeedGroupExportDto> list = paging.getData().stream().map(doctorGroupEventDetail -> {
            try {
                DoctorTurnSeedGroupEvent seedGroupEvent = JSON_MAPPER.fromJson(doctorGroupEventDetail.getExtra(), DoctorTurnSeedGroupEvent.class);
                DoctorTurnSeedGroupExportDto exportData = BeanMapper.map(seedGroupEvent, DoctorTurnSeedGroupExportDto.class);
                exportData.setGroupCode(doctorGroupEventDetail.getGroupCode());
                exportData.setBarnName(doctorGroupEventDetail.getBarnName());
                exportData.setEventAt(doctorGroupEventDetail.getEventAt());
                exportData.setRemark(doctorGroupEventDetail.getRemark());
                exportData.setCreatorName(doctorGroupEventDetail.getCreatorName());
                return exportData;
            } catch (Exception e) {
                log.info("get.group.turnSeed.failed, eventId:{}", doctorGroupEventDetail.getId());
            }
            return new DoctorTurnSeedGroupExportDto();
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }

    /**
     *
     * @param groupEventCriteriaMap
     * @return
     */
    private Paging<DoctorGroupEvent> groupEventPaging(Map<String, String> groupEventCriteriaMap) {
        Map<String, Object> params = OBJECT_MAPPER.convertValue(groupEventCriteriaMap, JacksonType.MAP_OF_OBJECT);
        if (params == null || params.isEmpty()) {
            return Paging.empty();
        }
        params = Params.filterNullOrEmpty(params);
        if (params.get("eventTypes") != null) {
            params.put("types", Splitters.COMMA.splitToList((String) params.get("eventTypes")));
            params.remove("eventTypes");
        }
        if (StringUtils.isNotBlank((String) params.get("endDate"))) {
            params.put("endDate", new DateTime(params.get("endDate")).plusDays(1).minusMillis(1).toDate());
        }
        Response<Paging<DoctorGroupEvent>> pagingResponse = doctorGroupReadService.queryGroupEventsByCriteria(params, Integer.parseInt(groupEventCriteriaMap.get("pageNo")), Integer.parseInt(groupEventCriteriaMap.get("size")));
        if (!pagingResponse.isSuccess()) {
            return Paging.empty();
        }

        return pagingResponse.getResult();
    }

    private Paging<DoctorPigEvent> pigEventPaging(Map<String, String> groupEventCriteriaMap) {
        Map<String, Object> params = OBJECT_MAPPER.convertValue(groupEventCriteriaMap, JacksonType.MAP_OF_OBJECT);

        if (params == null || params.isEmpty()) {
            return Paging.empty();
        }
        params = Params.filterNullOrEmpty(params);
        if (params.get("eventTypes") != null) {
            params.put("types", Splitters.COMMA.splitToList((String) params.get("eventTypes")));
            params.remove("eventTypes");
        }
        if (StringUtils.isNotBlank((String) params.get("endDate"))) {
            params.put("endDate", new DateTime(params.get("endDate")).plusDays(1).minusMillis(1).toDate());
        }
        Response<Paging<DoctorPigEvent>> pigEventPagingResponse = doctorPigEventReadService.queryPigEventsByCriteria(params, Integer.parseInt(groupEventCriteriaMap.get("pageNo")), Integer.parseInt(groupEventCriteriaMap.get("size")));
        if (!pigEventPagingResponse.isSuccess()) {
            return Paging.empty();
        }
        return pigEventPagingResponse.getResult();
    }

    @RequestMapping(value = "/pigEventExport", method = RequestMethod.GET)
    public void eventExport(@RequestParam Map<String, String> eventCriteria, HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("event.export.starting");
            if (Strings.isNullOrEmpty(eventCriteria.get("kind"))) {
                return;
            }
            if (Objects.equals(eventCriteria.get("kind"), "1")) {
                exportSowEvents(eventCriteria, request, response);
            }
            if (Objects.equals(eventCriteria.get("kind"), "2")) {
                exportBoarEvents(eventCriteria, request, response);
            }
            if (Objects.equals(eventCriteria.get("kind"), "4")) {
                exportGroupEvents(eventCriteria, request, response);
            }

            log.info("event.export.ending");
        } catch (Exception e) {
            log.error("event.export.failed,cause:{}", Throwables.getStackTraceAsString(e));
        }
    }

    private void exportSowEvents(Map<String, String> eventCriteria, HttpServletRequest request, HttpServletResponse response) {
        switch (eventCriteria.get("eventTypes")) {
            case "7":
                //进场
                exporter.export("web-pig-sowInputFactory", eventCriteria, 1, 500, this::pagingInFarmExport, request, response);
                break;
            case "9":
                //配种
                exporter.export("web-pig-sowMating", eventCriteria, 1, 500, this::pagingMating, request, response);
                break;
            case "11":
                //妊娠检查
                exporter.export("web-pig-sowPregChkResult", eventCriteria, 1, 500, this::pagingPregChkResult, request, response);
                break;
            case "15":
                //分娩
                exporter.export("web-pig-sowFarrowing", eventCriteria, 1, 500, this::pagingFarrowing, request, response);
                break;
            case "16":
                //断奶
                exporter.export("web-pig-sowWean", eventCriteria, 1, 500, this::pagingWean, request, response);
                break;
            case "17":
                //拼窝
                exporter.export("web-pig-sowFosters", eventCriteria, 1, 500, this::pagingFosters, request, response);
                break;
            case "18":
                //仔猪变动
                exporter.export("web-pig-PigletsChg", eventCriteria, 1, 500, this::pagingLetsChg, request, response);
                break;
            case "1,12,14":
                //转舍
                exporter.export("web-pig-sowChangeBarn", eventCriteria, 1, 500, this::pagingChangeBarn, request, response);
                break;
            case "2":
                //转场
                exporter.export("web-pig-sowTransFarm", eventCriteria, 1, 500, this::pagingChgFarm, request, response);
                break;
            case "3":
                //体况
                exporter.export("web-pig-sowCondition", eventCriteria, 1, 500, this::pagingsowCondition, request, response);
                break;
            case "4":
                //疾病
                exporter.export("web-pig-sowDisease", eventCriteria, 1, 500, this::pagingDisease, request, response);
                break;
            case "5":
                //防疫
                exporter.export("web-pig-sowVaccination", eventCriteria, 1, 500, this::pagingVaccination, request, response);
                break;
            case "6":
                //离场
                exporter.export("web-pig-sowRemove", eventCriteria, 1, 500, this::pagingRemove, request, response);
                break;

        }
    }


    private void exportBoarEvents(Map<String, String> eventCriteria, HttpServletRequest request, HttpServletResponse response) {
        switch(PigEvent.from(Integer.parseInt(eventCriteria.get("eventTypes")))){
            case ENTRY:
                //进场
                exporter.export("web-pig-boarInputFactory", eventCriteria, 1, 500, this::pagingInFarmExport, request, response);
                break;
            case SEMEN:
                //采精
                exporter.export("web-pig-boarCollect", eventCriteria, 1, 500, this::pagingSemenExport, request, response);
                break;
            case CHG_LOCATION:
                exporter.export("web-pig-boarChangeBarn", eventCriteria, 1, 500, this::pagingChangeBarn, request, response);
                //转舍
                break;
            case CHG_FARM:
                exporter.export("web-pig-boarTransFarm", eventCriteria, 1, 500, this::pagingChgFarm, request, response);
                //转场
                break;
            case CONDITION:
                exporter.export("web-pig-boarCondition", eventCriteria, 1, 500, this::pagingBoarCondition, request, response);
                //体况
                break;
            case DISEASE:
                exporter.export("web-pig-boarDisease", eventCriteria, 1, 500, this::pagingDisease, request, response);
                //疾病
                break;
            case VACCINATION:
                exporter.export("web-pig-boarVaccination", eventCriteria, 1, 500, this::pagingVaccination, request, response);
                //防疫
                break;
            case REMOVAL:
                exporter.export("web-pig-boarRemove", eventCriteria, 1, 500, this::pagingRemove, request, response);
                //离场
                break;
        }
    }

    /**
     * 猪群导出
     * @param eventCriteria
     * @param request
     * @param response
     */
    private void exportGroupEvents(Map<String, String> eventCriteria, HttpServletRequest request, HttpServletResponse response) {
        switch(GroupEventType.from(Integer.parseInt(eventCriteria.get("eventTypes")))){
            case NEW:
                //新建
                exporter.export("web-group-new", eventCriteria, 1, 500, this::pagingNewGroup, request, response);
                break;
            case MOVE_IN:
                exporter.export("web-group-MoveIn", eventCriteria, 1, 500, this::pagingMoveInGroup, request, response);
                //转入
                break;
            case CHANGE:
                //猪群变动
                exporter.export("web-group-change", eventCriteria, 1, 500, this::pagingChangeGroup, request, response);
                break;
            case TRANS_GROUP:
                exporter.export("web-group-transGroup", eventCriteria, 1, 500, this::pagingTransGroup, request, response);
                //转群
                break;
            case TURN_SEED:
                //商品猪转种猪
                exporter.export("web-group-turnSeed", eventCriteria, 1, 500, this::pagingTurnSeedGroup, request, response);
                break;
            case LIVE_STOCK:
                //猪只存栏
//                exporter.export("web-group-event", eventCriteria, 1, 500, this::pagingTurnSeedGroup, request, response);
                break;
            case DISEASE:
                //疾病
                exporter.export("web-group-Disease", eventCriteria, 1, 500, this::pagingDiseaseGroup, request, response);
                break;
            case ANTIEPIDEMIC:
                //防疫
                exporter.export("web-group-accination", eventCriteria, 1, 500, this::pagingVaccinationGroup, request, response);
                break;
            case TRANS_FARM:
                //转场
                exporter.export("web-group-transFarm", eventCriteria, 1, 500, this::pagingChgFramGroup, request, response);
                break;
            case CLOSE:
                break;
        }
    }


}
