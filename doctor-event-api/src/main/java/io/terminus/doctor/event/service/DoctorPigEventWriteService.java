package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFostersDto;
import io.terminus.doctor.event.dto.event.sow.DoctorLitterWeightDto;
import io.terminus.doctor.event.dto.event.sow.DoctorMatingDto;
import io.terminus.doctor.event.dto.event.sow.DoctorPartWeanDto;
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

import javax.validation.constraints.NotNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 猪事件录入信息表
 */
public interface DoctorPigEventWriteService {

    /**
     * 回滚 pig event 事件信息
     * @param pigEventId
     * @return
     */
    Response<Boolean> rollBackPigEvent(@NotNull(message = "input.pigEventId.empty") Long pigEventId);

    /**
     * 公猪， 母猪 进仓事件信息
     * @param doctorBasicInputInfoDto
     * @param doctorFarmEntryDto
     * @param pigType 猪类型
     * @return
     */
    Response<Long> pigEntryEvent(@NotNull(message = "input.basic.empty") DoctorBasicInputInfoDto doctorBasicInputInfoDto,
                                    @NotNull(message = "input.dto.empty") DoctorFarmEntryDto doctorFarmEntryDto,
                                    @NotNull(message = "input.pigType.empty") Integer pigType);

    /**
     * 猪只疾病事件录入
     * @param doctorDiseaseDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> diseaseEvent(@NotNull(message = "input.dto.empty") DoctorDiseaseDto doctorDiseaseDto,
                                   @NotNull(message = "input.basic.empty") DoctorBasicInputInfoDto doctorBasicInputInfoDto,
                                   @NotNull(message = "input.pigType.empty") Integer pigType);

    /**
     * 防疫事件信息
     * @param doctorVaccinationDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> vaccinationEvent(@NotNull(message = "input.dto.empty") DoctorVaccinationDto doctorVaccinationDto,
                                       @NotNull(message = "input.basic.empty") DoctorBasicInputInfoDto doctorBasicInputInfoDto,
                                       @NotNull(message = "input.pigType.empty") Integer pigType);

    /**
     * 体况事件信息
     * @param doctorConditionDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> conditionEvent(@NotNull(message = "input.dto.empty") DoctorConditionDto doctorConditionDto,
                                     @NotNull(message = "input.basic.empty") DoctorBasicInputInfoDto doctorBasicInputInfoDto,
                                     @NotNull(message = "input.pigType.empty") Integer pigType);

    /**
     * 转舍事件信息
     * @param doctorChgLocationDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> chgLocationEvent(@NotNull(message = "input.dto.empty") DoctorChgLocationDto doctorChgLocationDto,
                                       @NotNull(message = "input.basic.empty") DoctorBasicInputInfoDto doctorBasicInputInfoDto,
                                       @NotNull(message = "input.pigType.empty") Integer pigType);

    /**
     * 转场事件信息
     * @param doctorChgFarmDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> chgFarmEvent(DoctorChgFarmDto doctorChgFarmDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType);

    /**
     * 离场事件
     * @param doctorRemovalDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> removalEvent(DoctorRemovalDto doctorRemovalDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType);

    /**
     * 公猪采精事件录入
     * @param doctorSemenDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> boarSemenEvent(DoctorSemenDto doctorSemenDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType);

    /**
     * 母猪配种事件信息录入
     * @param doctorMatingDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> sowMatingEvent(DoctorMatingDto doctorMatingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType);

    /**
     * 母猪妊娠事件
     * @param doctorPregChkResultDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> sowPregCheckEvent(DoctorPregChkResultDto doctorPregChkResultDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType);

    /**
     * 母猪分娩事件
     * @param doctorFarrowingDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> sowFarrowingEvent(DoctorFarrowingDto doctorFarrowingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType);

    /**
     * 仔猪变动事件信息
     * @param doctorPigletsChgDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> sowPigletsChgEvent(DoctorPigletsChgDto doctorPigletsChgDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType);

    /**
     * 拼窝事件信息
     * @param doctorFostersDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> sowFostersEvent(DoctorFostersDto doctorFostersDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType);

    /**
     * 仔猪窝重事件
     * @param doctorLitterWeightDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> sowLitterWeightEvent(DoctorLitterWeightDto doctorLitterWeightDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType);

    /**
     * 部分断奶母猪
     * @param doctorPartWeanDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> sowPartWeanEvent(DoctorPartWeanDto doctorPartWeanDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType);

    /**
     * 母猪断奶事件
     * @param doctorWeanDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> sowWeanEvent(DoctorWeanDto doctorWeanDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto, Integer pigType);
}
