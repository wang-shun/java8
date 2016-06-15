package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.boar.DoctorSemenDto;
import io.terminus.doctor.event.dto.event.sow.DoctorAbortionDto;
import io.terminus.doctor.event.dto.event.sow.DoctorFarrowingDto;
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

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

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
    Response<Long> rollBackPigEvent(@NotNull(message = "input.pigEventId.empty") Long pigEventId,
                                    @NotNull(message = "input.reverterPigType") Integer revertPigType,
                                    Long staffId, String staffName);

    /**
     * 公猪， 母猪 进仓事件信息
     * @param doctorBasicInputInfoDto
     * @param doctorFarmEntryDto
     * @return
     */
    Response<Long> pigEntryEvent(@NotNull(message = "input.basic.empty") DoctorBasicInputInfoDto doctorBasicInputInfoDto,
                                    @NotNull(message = "input.dto.empty") DoctorFarmEntryDto doctorFarmEntryDto);

    /**
     * 猪只疾病事件录入
     * @param doctorDiseaseDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> diseaseEvent(@NotNull(message = "input.dto.empty") DoctorDiseaseDto doctorDiseaseDto,
                                   @NotNull(message = "input.basic.empty") DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 批量创建疾病数据信息
     * @param doctorDiseaseDto 相同的疾病信息数据
     * @param basicInputInfoDto 基础信息， barnId 下每头猪创建疾病信息内容
     * @return 创建事件列表信息
     */
    Response<Boolean> diseaseEvents(@NotNull(message = "input.dto.empty") DoctorDiseaseDto doctorDiseaseDto,
                                    @NotNull(message = "input.basic.empty") DoctorBasicInputInfoDto basicInputInfoDto);

    /**
     * 防疫事件信息
     * @param doctorVaccinationDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> vaccinationEvent(@NotNull(message = "input.dto.empty") DoctorVaccinationDto doctorVaccinationDto,
                                        @NotNull(message = "input.basic.empty") DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 批量创建免疫事件信息
     * @param doctorVaccinationDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Boolean> vaccinationEvents(@NotNull(message = "input.dto.empty") DoctorVaccinationDto doctorVaccinationDto,
                                        @NotNull(message = "input.basic.empty") DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 体况事件信息
     * @param doctorConditionDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> conditionEvent(@NotNull(message = "input.dto.empty") DoctorConditionDto doctorConditionDto,
                                     @NotNull(message = "input.basic.empty") DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 转舍事件信息
     * @param doctorChgLocationDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> chgLocationEvent(@NotNull(message = "input.dto.empty") DoctorChgLocationDto doctorChgLocationDto,
                                       @NotNull(message = "input.basic.empty") DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 流产事件录入
     * @param doctorAbortionDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> abortionEvent(@NotNull(message = "input.dto.empty") DoctorAbortionDto doctorAbortionDto,
                                 @NotNull(message = "input.basic.empty") DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 转场事件信息
     * @param doctorChgFarmDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> chgFarmEvent(DoctorChgFarmDto doctorChgFarmDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 离场事件
     * @param doctorRemovalDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> removalEvent(DoctorRemovalDto doctorRemovalDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 公猪采精事件录入
     * @param doctorSemenDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> boarSemenEvent(DoctorSemenDto doctorSemenDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 母猪配种事件信息录入
     * @param doctorMatingDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> sowMatingEvent(DoctorMatingDto doctorMatingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 对应的母猪转舍事件处理
     * @param doctorChgLocationDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> chgSowLocationEvent(DoctorChgLocationDto doctorChgLocationDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 母猪妊娠事件
     * @param doctorPregChkResultDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> sowPregCheckEvent(DoctorPregChkResultDto doctorPregChkResultDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 母猪分娩事件
     * @param doctorFarrowingDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> sowFarrowingEvent(DoctorFarrowingDto doctorFarrowingDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 仔猪变动事件信息
     * @param doctorPigletsChgDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> sowPigletsChgEvent(DoctorPigletsChgDto doctorPigletsChgDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 部分断奶母猪
     * @param doctorPartWeanDto
     * @param doctorBasicInputInfoDto
     * @return
     */
    Response<Long> sowPartWeanEvent(DoctorPartWeanDto doctorPartWeanDto, DoctorBasicInputInfoDto doctorBasicInputInfoDto);

    /**
     * 不同的母猪的信息录入方式
     * @param basics  不同的Pig
     * @param extra 扩展信息
     * @return
     */
    Response<Boolean> sowPigsEventCreate(List<DoctorBasicInputInfoDto> basics, Map<String,Object> extra);

    /**
     * casual 事件信息 创建
     * @param basics
     * @param extra
     * @return
     */
    Response<Boolean> casualPigsEventCreate(List<DoctorBasicInputInfoDto> basics, Map<String,Object> extra);
}
