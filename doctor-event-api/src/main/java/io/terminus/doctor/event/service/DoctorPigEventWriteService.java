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
     * 公猪， 母猪 进仓事件信息
     * @param doctorBasicInputInfoDto
     * @param doctorFarmEntryDto
     * @param pigType 猪类型
     * @return
     */
    Response<Boolean> pigEntryEvent(@NotNull(message = "input.basic.empty") DoctorBasicInputInfoDto doctorBasicInputInfoDto,
                                    @NotNull(message = "input.dto.empty") DoctorFarmEntryDto doctorFarmEntryDto,
                                    @NotNull(message = "input.pigType.empty") Integer pigType);

    /**
     * 猪只疾病事件录入
     * @param doctorDiseaseDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Boolean> diseaseEvent(@NotNull(message = "input.dto.empty") DoctorDiseaseDto doctorDiseaseDto,
                                   @NotNull(message = "input.basic.empty") DoctorBasicInputInfoDto doctorBasicInputInfoDto,
                                   @NotNull(message = "input.pigType.empty") Integer pigType);

    /**
     * 防疫事件信息
     * @param doctorVaccinationDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Boolean> vaccinationEvent(DoctorVaccinationDto doctorVaccinationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 体况事件信息
     * @param doctorConditionDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Boolean> conditionEvent(DoctorConditionDto doctorConditionDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 转舍事件信息
     * @param doctorChgLocationDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Boolean> chgLocationEvent(DoctorChgLocationDto doctorChgLocationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 转场事件信息
     * @param doctorChgFarmDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Boolean> chgFarmEvent(DoctorChgFarmDto doctorChgFarmDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 离场事件
     * @param doctorRemovalDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Boolean> removalEvent(DoctorRemovalDto doctorRemovalDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 公猪采精事件录入
     * @param doctorSemenDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Boolean> boarSemenEvent(DoctorSemenDto doctorSemenDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 母猪配种事件信息录入
     * @param doctorMatingDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Boolean> sowMatingEvent(DoctorMatingDto doctorMatingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 母猪妊娠事件
     * @param doctorPregChkResultDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Boolean> sowPregCheckEvent(DoctorPregChkResultDto doctorPregChkResultDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 母猪分娩事件
     * @param doctorFarrowingDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Boolean> sowFarrowingEvent(DoctorFarrowingDto doctorFarrowingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 仔猪变动事件信息
     * @param doctorPigletsChgDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Boolean> sowPigletsChgEvent(DoctorPigletsChgDto doctorPigletsChgDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 拼窝事件信息
     * @param doctorFostersDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Boolean> sowFostersEvent(DoctorFostersDto doctorFostersDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 仔猪窝重事件
     * @param doctorLitterWeightDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Boolean> sowLitterWeightEvent(DoctorLitterWeightDto doctorLitterWeightDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 部分断奶母猪
     * @param doctorPartWeanDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Boolean> sowPartWeanEvent(DoctorPartWeanDto doctorPartWeanDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 母猪断奶事件
     * @param doctorWeanDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Boolean> sowWeanEvent(DoctorWeanDto doctorWeanDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);
}
