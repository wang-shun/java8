package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.DoctorRollbackDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.event.ListenedPigEvent;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-19
 * Email:yaoqj@terminus.io
 * Descirbe: 猪事件信息录入方式
 */
@Service
@Slf4j
@RpcProvider
public class DoctorPigEventWriteServiceImpl implements DoctorPigEventWriteService {

    private final DoctorPigEventManager doctorPigEventManager;
    private final CoreEventDispatcher coreEventDispatcher;
    private final DoctorPigEventDao doctorPigEventDao;

    @Autowired
    public DoctorPigEventWriteServiceImpl(DoctorPigEventManager doctorPigEventManager,
                                          CoreEventDispatcher coreEventDispatcher,
                                          DoctorPigEventDao doctorPigEventDao) {
        this.doctorPigEventManager = doctorPigEventManager;
        this.coreEventDispatcher = coreEventDispatcher;
        this.doctorPigEventDao = doctorPigEventDao;
    }

//    @Override
//    public Response<Long> pigEntryEvent(DoctorBasicInputInfoDto doctorBasicInputInfoDto, DoctorFarmEntryDto doctorFarmEntryDto) {
//        try {
//            // validate 左右乳头数量大于0
//            if (Objects.equals(doctorFarmEntryDto.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())) {
//                checkState(isNull(doctorFarmEntryDto.getLeft()) || doctorFarmEntryDto.getLeft() >= 0, "input.sowLeft.error");
//                checkState(isNull(doctorFarmEntryDto.getRight()) || doctorFarmEntryDto.getRight() >= 0, "input.sowRight.error");
//            }
//
//            Map<String, Object> extra = Maps.newHashMap();
//            BeanMapper.copy(doctorFarmEntryDto, extra);
//            Map<String, Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, extra);
//
//            publishEvent(result);
//            publishBarnEvent(doctorFarmEntryDto.getBarnId());
//            return Response.ok(Params.getWithConvert(result, "doctorPigId", a -> Long.valueOf(a.toString())));
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (IllegalStateException e) {
//            log.error("pig entry event illegal state fail, basicInfo:{}, doctorFarmEntryDto:{}, cause:{}",
//                    doctorBasicInputInfoDto, doctorFarmEntryDto, Throwables.getStackTraceAsString(e));
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("pig entry event create fail,basicInfo:{}, doctorFarmEntryDto:{}, cause:{}",
//                    doctorBasicInputInfoDto, doctorFarmEntryDto,
//                    Throwables.getStackTraceAsString(e));
//            return Response.fail("create.entryEvent.fail");
//        }
//    }
//
//    @Override
//    public Response<List<Long>> batchPigEntryEvent(List<DoctorPigEntryEventDto> doctorPigEntryEventDtos) {
//        //校验数据
//        try {
//            for (DoctorPigEntryEventDto doctorPigEntryEventDto : doctorPigEntryEventDtos) {
//                DoctorFarmEntryDto doctorFarmEntryDto = doctorPigEntryEventDto.getDoctorFarmEntryDto();
//                // validate 左右乳头数量大于0
//                if (Objects.equals(doctorFarmEntryDto.getPigType(), DoctorPig.PIG_TYPE.SOW.getKey())) {
//                    checkState(isNull(doctorFarmEntryDto.getLeft()) || doctorFarmEntryDto.getLeft() >= 0, "input.sowLeft.error");
//                    checkState(isNull(doctorFarmEntryDto.getRight()) || doctorFarmEntryDto.getRight() >= 0, "input.sowRight.error");
//                }
//            }
//            List<Map<String, Object>> result = doctorPigEventManager.createCasualPigEvent(doctorPigEntryEventDtos);
//            List<Long> pigIds= Lists.newArrayList();
//            result.forEach(map -> {
//                publishEvent(map);
//                publishBarnEvent(Params.getWithConvert(map, "barnId", a -> Long.valueOf(a.toString())));
//                pigIds.add(Params.getWithConvert(map, "doctorPigId", a -> Long.valueOf(a.toString())));
//            });
//            return Response.ok(pigIds);
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (IllegalStateException e) {
//            log.error("pig entry event illegal state fail, doctorPigEntryEventDtos:{}, cause:{}",
//                    doctorPigEntryEventDtos, Throwables.getStackTraceAsString(e));
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("pig entry event create fail,doctorPigEntryEventDtos:{}, cause:{}",
//                    doctorPigEntryEventDtos,
//                    Throwables.getStackTraceAsString(e));
//            return Response.fail("create.entryEvent.fail");
//        }
//    }
//
//    @Override
//    public Response<Long> diseaseEvent(DoctorDiseaseDto doctorDiseaseDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
//        try {
//            doctorDiseaseDto.setDiseaseStaff(doctorBasicInputInfoDto.getStaffName());
//
//            Map<String, Object> dto = Maps.newHashMap();
//            BeanMapper.copy(doctorDiseaseDto, dto);
//
//            Map<String, Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
//            return Response.ok(Params.getWithConvert(result, "doctorEventId", a -> Long.valueOf(a.toString())));
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("disease event create fail, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("create.diseaseEvent.fail");
//        }
//    }
//
//    @Override
//    @Deprecated
//    public Response<Boolean> diseaseEvents(DoctorDiseaseDto doctorDiseaseDto, DoctorBasicInputInfoDto basicInputInfoDto) {
//        try {
//            Response<List<DoctorPigInfoDto>> listResponse = this.doctorPigReadService.queryDoctorPigInfoByBarnId(basicInputInfoDto.getBarnId());
//            checkState(listResponse.isSuccess(), "query.pigByBarnId.fail");
//
//            List<DoctorBasicInputInfoDto> basicInputInfoDtos = listResponse.getResult().stream()
//                    .map(dto -> basicInputInfoDto.buildSameBarnPigInfo(dto.getId(), dto.getPigType(), dto.getPigCode())).collect(Collectors.toList());
//
//            Map<String, Object> beans = Maps.newHashMap();
//            BeanMapper.copy(doctorDiseaseDto, beans);
//
//            doctorPigEventManager.createCasualPigEvents(basicInputInfoDtos, beans);
//            return Response.ok(Boolean.TRUE);
//        } catch (IllegalStateException se) {
//            log.warn("illegal state, create events disease fail, cause:{}", Throwables.getStackTraceAsString(se));
//            return Response.fail(se.getMessage());
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("create diseases events fail, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("create.diseaseEvents.fail");
//        }
//    }
//
//    @Override
//    public Response<Long> vaccinationEvent(DoctorVaccinationDto doctorVaccinationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
//        try {
//            doctorVaccinationDto.setVaccinationStaffId(doctorBasicInputInfoDto.getStaffId());
//            doctorVaccinationDto.setVaccinationStaffName(doctorBasicInputInfoDto.getStaffName());
//
//            Map<String, Object> dto = Maps.newHashMap();
//            BeanMapper.copy(doctorVaccinationDto, dto);
//
//            Map<String, Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
//            publishEvent(result);
//            return Response.ok(Params.getWithConvert(result, "doctorEventId", a -> Long.valueOf(a.toString())));
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("create.vaccination.fail");
//        }
//    }
//
//    @Override
//    @Deprecated
//    public Response<Boolean> vaccinationEvents(DoctorVaccinationDto doctorVaccinationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
//        try {
//            Response<List<DoctorPigInfoDto>> listResponse = this.doctorPigReadService.queryDoctorPigInfoByBarnId(doctorBasicInputInfoDto.getBarnId());
//            checkState(listResponse.isSuccess(), "query.vaccinations.error");
//
//            List<DoctorBasicInputInfoDto> basicInputInfoDtos = listResponse.getResult().stream()
//                    .map(dto -> doctorBasicInputInfoDto.buildSameBarnPigInfo(dto.getId(), dto.getPigType(), dto.getPigCode())).collect(Collectors.toList());
//
//            Map<String, Object> beans = Maps.newHashMap();
//            BeanMapper.copy(doctorVaccinationDto, beans);
//
//            doctorPigEventManager.createCasualPigEvents(basicInputInfoDtos, beans);
//            return Response.ok(Boolean.TRUE);
//        } catch (IllegalStateException se) {
//            log.warn("illegal state fail, cause:{}", Throwables.getStackTraceAsString(se));
//            return Response.fail(se.getMessage());
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("vaccination events create fail, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("vaccination.createEvents.fail");
//        }
//    }
//
//    @Override
//    public Response<Long> conditionEvent(DoctorConditionDto doctorConditionDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
//        try {
//            Map<String, Object> dto = Maps.newHashMap();
//            BeanMapper.copy(doctorConditionDto, dto);
//
//            Map<String, Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
//            publishEvent(result);
//            return Response.ok(Params.getWithConvert(result, "doctorEventId", a -> Long.valueOf(a.toString())));
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("condition event create fail, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("create.condition.event.fail");
//        }
//    }
//
//    @Override
//    public Response<Long> chgLocationEvent(DoctorChgLocationDto doctorChgLocationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
//        try {
//            Map<String, Object> dto = Maps.newHashMap();
//            BeanMapper.copy(doctorChgLocationDto, dto);
//
//            Map<String, Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
//            publishEvent(result);
//            publishBarnEvent(doctorChgLocationDto.getChgLocationFromBarnId());
//            publishBarnEvent(doctorChgLocationDto.getChgLocationToBarnId());
//            return Response.ok(Params.getWithConvert(result, "doctorEventId", a -> Long.valueOf(a.toString())));
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("create.chgLocationEvent.fail");
//        }
//    }
//
//    @Override
//    public Response<Long> chgFarmEvent(DoctorChgFarmDto doctorChgFarmDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
//        try {
//            Map<String, Object> dto = Maps.newHashMap();
//            BeanMapper.copy(doctorChgFarmDto, dto);
//
//            Map<String, Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
//            publishBarnEvent(doctorChgFarmDto.getFromBarnId());
//            publishBarnEvent(doctorChgFarmDto.getToBarnId());
//            publishEvent(result);
//            return Response.ok(Params.getWithConvert(result, "doctorEventId", a -> Long.valueOf(a.toString())));
//        } catch (IllegalStateException e) {
//            log.error("chg farm event illegal state, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail(e.getMessage());
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("create.chgFarmEvent.fail");
//        }
//    }
//
//    @Override
//    public Response<Long> removalEvent(DoctorRemovalDto doctorRemovalDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
//        try {
//            Map<String, Object> dto = Maps.newHashMap();
//            BeanMapper.copy(doctorRemovalDto, dto);
//
//            Map<String, Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
//            publishEvent(result);
//            publishBarnEvent(doctorBasicInputInfoDto.getBarnId());
//            return Response.ok(Params.getWithConvert(result, "doctorEventId", a -> Long.valueOf(a.toString())));
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("create.removalEvent.fail");
//        }
//    }
//
//    @Override
//    public Response<Long> boarSemenEvent(DoctorSemenDto doctorSemenDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
//        try {
//            Map<String, Object> dto = Maps.newHashMap();
//            BeanMapper.copy(doctorSemenDto, dto);
//
//            Map<String, Object> result = doctorPigEventManager.createCasualPigEvent(doctorBasicInputInfoDto, dto);
//            return Response.ok(Params.getWithConvert(result, "doctorEventId", a -> Long.valueOf(a.toString())));
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("create.semenEvent.fail");
//        }
//    }
//
//    @Override
//    public Response<Long> sowMatingEvent(DoctorMatingDto doctorMatingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
//        try {
//            Map<String, Object> dto = Maps.newHashMap();
//            BeanMapper.copy(doctorMatingDto, dto);
//
//            Map<String, Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
//            publishEvent(result);
//            return Response.ok(Params.getWithConvert(result, "doctorEventId", a -> Long.valueOf(a.toString())));
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("create.matingEvent.fail");
//        }
//    }
//
//    @Override
//    public Response<Long> chgSowLocationEvent(DoctorChgLocationDto doctorChgLocationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
//        try {
//            Map<String, Object> dto = Maps.newHashMap();
//            BeanMapper.copy(doctorChgLocationDto, dto);
//
//            doctorChgLocationDto.setChgLocationFromBarnId(doctorBasicInputInfoDto.getBarnId());
//            doctorChgLocationDto.setChgLocationFromBarnName(doctorBasicInputInfoDto.getBarnName());
//
//            Map<String, Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
//            publishEvent(result);
//            publishBarnEvent(doctorChgLocationDto.getChgLocationFromBarnId());
//            publishBarnEvent(doctorChgLocationDto.getChgLocationToBarnId());
//            return Response.ok(Params.getWithConvert(result, "doctorEventId", a -> Long.valueOf(a.toString())));
//        } catch (IllegalStateException e) {
//            log.error("change sow location event illegal status, cause:{}", e.getMessage());
//            return Response.fail(e.getMessage());
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("change sow location event create fail, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("create.chgLocation.fail");
//        }
//    }
//
//    @Override
//    public Response<Long> sowPregCheckEvent(DoctorPregChkResultDto doctorPregChkResultDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
//        try {
//            Map<String, Object> dto = Maps.newHashMap();
//            BeanMapper.copy(doctorPregChkResultDto, dto);
//
//            Map<String, Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
//            publishEvent(result);
//            return Response.ok(Params.getWithConvert(result, "doctorEventId", a -> Long.valueOf(a.toString())));
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("create.sowPregCheckEvent.fail");
//        }
//    }
//
//    @Override
//    public Response<Long> sowFarrowingEvent(DoctorFarrowingDto doctorFarrowingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
//        try {
//            // validate count
//            Integer liveCount = doctorFarrowingDto.getFarrowingLiveCount();
//            Integer healthCount = doctorFarrowingDto.getHealthCount();
//            Integer weakCount = doctorFarrowingDto.getWeakCount();
//            checkState(Objects.equals(liveCount, healthCount + weakCount), "validate.farrowingCount.fail");
//
//            // 校验健仔的数量
//            checkState(healthCount >= 0 && healthCount <= 25, "sowFarrow.healthCount.error");
//            // 校验活仔的数量
//            checkState(liveCount >= healthCount && liveCount <= 25, "sowFarrow.liveCount.error");
//
//            // 校验对应的公猪, 母猪的数量信息
//            if (!Objects.isNull(doctorFarrowingDto.getLiveBoarCount()) || !Objects.isNull(doctorFarrowingDto.getLiveSowCount())) {
//
//                Integer sowCount = MoreObjects.firstNonNull(doctorFarrowingDto.getLiveSowCount(), 0);
//                Integer boarCount = MoreObjects.firstNonNull(doctorFarrowingDto.getLiveBoarCount(), 0);
//
//                checkState(Objects.equals(doctorFarrowingDto.getFarrowingLiveCount(), sowCount + boarCount),
//                        "validate.farrowingCount.fail");
//            }
//
//            Map<String, Object> dto = Maps.newHashMap();
//            BeanMapper.copy(doctorFarrowingDto, dto);
//
//            Map<String, Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
//            publishEvent(result);
//            return Response.ok(Params.getWithConvert(result, "doctorEventId", a -> Long.valueOf(a.toString())));
//        } catch (IllegalStateException e) {
//            log.error("illegal state validate farrow count error, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail(e.getMessage());
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("vaccination event create fail, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("create.farrowing.fail");
//        }
//    }
//
//    @Override
//    @Deprecated
//    public Response<Long> sowPigletsChgEvent(DoctorPigletsChgDto doctorPigletsChgDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
//        try {
//            Map<String, Object> dto = Maps.newHashMap();
//            BeanMapper.copy(doctorPigletsChgDto, dto);
//
//            Map<String, Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
//            return Response.ok(Params.getWithConvert(result, "doctorEventId", a -> Long.valueOf(a.toString())));
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (IllegalStateException e) {
//            log.error("illegal state piglet chg event create, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("piglets event create fail, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("create.piglets.fail");
//        }
//    }
//
//    @Override
//    public Response<Long> sowPartWeanEvent(DoctorPartWeanDto doctorPartWeanDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto) {
//        try {
//            Map<String, Object> dto = Maps.newHashMap();
//            BeanMapper.copy(doctorPartWeanDto, dto);
//
//            if (!Objects.isNull(doctorPartWeanDto.getQualifiedCount()) || !Objects.isNull(doctorPartWeanDto.getNotQualifiedCount())) {
//                checkState(Objects.equals(doctorPartWeanDto.getPartWeanPigletsCount(),
//                        MoreObjects.firstNonNull(doctorPartWeanDto.getQualifiedCount(), 0) +
//                                MoreObjects.firstNonNull(doctorPartWeanDto.getNotQualifiedCount(), 0)
//                ), "partWean.qualified.error");
//            }
//
//            Map<String, Object> result = doctorPigEventManager.createSowPigEvent(doctorBasicInputInfoDto, dto);
//            publishEvent(result);
//            return Response.ok(Params.getWithConvert(result, "doctorEventId", a -> Long.valueOf(a.toString())));
//        } catch (IllegalStateException e) {
//            log.error("part wean event illegal state, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail(e.getMessage());
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("part wean event create fail, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("create.partWean.fail");
//        }
//    }
//
//    @Override
//    public Response<Boolean> sowPigsEventCreate(List<DoctorBasicInputInfoDto> basics, Map<String, Object> extra) {
//        try {
//            // 批量信息创建
//            Map<String, Object> result = doctorPigEventManager.createSowEvents(basics, extra);
//
//            publishEvent(result);
//            return Response.ok(Boolean.TRUE);
//        } catch (ServiceException | IllegalStateException | IllegalArgumentException e) {
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("sow pigs event creates fail, cause:{}", Throwables.getStackTraceAsString(e));
//            return Response.fail("create.sowPigsEvent.fail");
//        }
//    }
//
//    @Override
//    public Response<Boolean> casualPigsEventCreate(List<DoctorBasicInputInfoDto> basics, Map<String, Object> extra) {
//        try {
//            // 批量信息创建
//            Map<String, Object> result = doctorPigEventManager.createCasualPigEvents(basics, extra);
//
//            publishEvent(result);
//            return Response.ok(Boolean.TRUE);
//        } catch (ServiceException e) {
//            return Response.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("casual events pigs event create fail, basics:{}, extra:{}, cause:{}", basics, extra, Throwables.getStackTraceAsString(e));
//            return Response.fail("create.casualPigsEvent.fail");
//        }
//    }


    @Override
    public Response<Boolean> pigEventHandle(BasePigEventInputDto inputDto, DoctorBasicInputInfoDto basic) {
        try {
            List<DoctorRollbackDto> rollbackDtos = doctorPigEventManager.eventHandle(inputDto, basic);
            doctorPigEventManager.checkAndPublishEvent(rollbackDtos);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("pig.event.handle.failed, cause by :{}", Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        }
    }

    @Override
    public Response<Boolean> batchPigEventHandle(List<BasePigEventInputDto> inputDtos, DoctorBasicInputInfoDto basic) {
        try {
            List<DoctorRollbackDto> rollbackDtos = doctorPigEventManager.batchEventsHandle(inputDtos, basic);
            doctorPigEventManager.checkAndPublishEvent(rollbackDtos);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("batch.pig.event.handle.failed, cause by :{}", Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        }
    }

    @Deprecated
    @Override
    public Response<Boolean> createPigEvent(DoctorPigEvent doctorPigEvent) {
        try {
            doctorPigEventDao.create(doctorPigEvent);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("create.pig.event.failed, cause{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.pig.event.failed");
        }
    }

    @Deprecated
    @Override
    public Response<Boolean> updatePigEvents(DoctorPigEvent doctorPigEvent) {
        try {
            doctorPigEventDao.updatePigEvents(doctorPigEvent);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("update.pig.event.failed, cause{}", Throwables.getStackTraceAsString(e));
            return Response.fail("update.pig.event.failed");
        }
    }

    /**
     * 推送对应的事件信息
     *
     * @param results
     */
    private void publishEvent(Map<String, Object> results) {
        try {
            if ("single".equals(results.get("contextType"))) {
                ListenedPigEvent listenedPigEvent = new ListenedPigEvent();
                listenedPigEvent.setPigId(Long.parseLong(results.get("doctorPigId").toString()));
                listenedPigEvent.setPigEventId(Params.getWithConvert(results, "doctorEventId", a -> Long.valueOf(a.toString())));
                listenedPigEvent.setEventType((Integer) results.get("type"));
                coreEventDispatcher.publish(listenedPigEvent);
            } else {
                results.keySet().forEach(pigId -> {
                    Map<String, Object> map = (Map<String, Object>) results.get(pigId);
                    ListenedPigEvent listenedPigEvent = new ListenedPigEvent();
                    listenedPigEvent.setPigId((Long.parseLong(pigId)));
                    listenedPigEvent.setPigEventId(Params.getWithConvert(map, "doctorEventId", a -> Long.valueOf(a.toString())));
                    listenedPigEvent.setEventType((Integer) map.get("type"));
                    coreEventDispatcher.publish(listenedPigEvent);
                });
            }
        } catch (Exception e) {
            log.error("failed to publish pig event, cause:{}", Throwables.getStackTraceAsString(e));
        }

        try {
            // 向zk发送刷新消息的事件
            //publisher.publish(DataEvent.toBytes(DataEventType.PigEventCreate.getKey(), new DoctorZkPigEvent(results)));
        } catch (Exception e) {
            log.error(Throwables.getStackTraceAsString(e));
        }
    }
}
