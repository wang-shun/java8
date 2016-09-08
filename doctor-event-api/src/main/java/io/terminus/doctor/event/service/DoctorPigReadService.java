package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorPigInfoDetailDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.DoctorPigMessage;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public interface DoctorPigReadService {

    /**
     * 获取母猪数量
     * @param range
     * @see io.terminus.doctor.event.enums.DataRange
     * @param id
     * @return
     */
    Response<Long> queryPigCount(@NotNull(message = "input.range.empty")Integer range,
                                 @NotNull(message = "input.id.empty") Long id,
                                 @NotNull(message = "input.pigType.empty") Integer pigType);

    /**
     * 通过pigId 获取对应的详细信息
     * @param pigId
     * @return
     */
    Response<DoctorPigInfoDetailDto> queryPigDetailInfoByPigId(@NotNull(message = "input.pigId.empty") Long pigId, Integer eventSize);

    /**
     * 通过pigId 查询pig
     * @param pigId
     * @return
     */
    Response<DoctorPig> findPigById(Long pigId);

    /**
     * 通过doctorPig 信息分页查询
     * @param doctorPig
     * @param pageNo
     * @param pageSize
     * @return
     */
    Response<Paging<DoctorPigInfoDto>> pagingDoctorInfoDtoByPig(DoctorPig doctorPig, Integer pageNo, Integer pageSize);

    /**
     * 公猪的Track 信息分页查询
     * @param doctorPigTrack
     * @param pageNo
     * @param pageSize
     * @return
     */
    Response<Paging<DoctorPigInfoDto>> pagingDoctorInfoDtoByPigTrack(DoctorPigTrack doctorPigTrack, Integer pageNo, Integer pageSize);

    /**
     * 通过pigId 获取对应的 pigDto 信息内容
     * @param pigId
     * @return
     */
    Response<DoctorPigInfoDto> queryDoctorInfoDtoById(@NotNull(message = "input.pigId.empty") Long pigId);

    /**
     * 生成对应的窝号 年+月+胎次
     * @param farmId
     * @return
     */
    Response<String> generateFostersCode(@NotNull(message = "input.farmId.empty") Long farmId);

    /**
     * 获取猪舍pig 信息内容
     * @param barnId
     * @return
     */
    Response<List<DoctorPigInfoDto>> queryDoctorPigInfoByBarnId(@NotNull(message = "input.barnId.empty") Long barnId);

    /**
     * 查询当前猪舍里的猪只跟踪列表(注意是当前猪舍)
     * @param barnId 猪舍id
     * @return 猪只跟踪列表
     */
    Response<List<DoctorPigTrack>> findActivePigTrackByCurrentBarnId(@NotNull(message = "barnId.not.null") Long barnId);

    /**
     * 根据猪场id查询猪列表
     * @param farmId 猪场id
     * @return 猪列表
     */
    Response<List<DoctorPig>> findPigsByFarmId(@NotNull(message = "farmId.not.null") Long farmId);

    /**
     * 获取猪的track信息
     * @param pigId 猪id
     * @return
     */
    Response<DoctorPigTrack> findPigTrackByPigId(@NotNull(message = "input.pigId.empty") Long pigId);

    /**
     * 获取猪详情当前需要提醒的消息
     * @param pigId
     * @return
     */
    Response<List<DoctorPigMessage>> findPigMessageByPigId(@NotNull(message = "input.pigId.empty") Long pigId);

    /**
     * 校验 farmId pigCode 不存在
     * @param orgId
     * @param pigCode
     * @return
     */
    Response<Boolean> validatePigCodeByFarmId(Long orgId, String pigCode);

    /**
     * 获取猪舍内的所有猪的猪类
     * @param barnId 猪舍id
     * @return 猪类set
     */
    Response<Set<Integer>> findPigStatusByBarnId(@NotNull(message = "barnId.not.null") Long barnId);

    /**
     * 查询猪所在的猪舍信息
     * @param pigId 猪id
     * @return 猪舍信息
     */
    Response<DoctorBarn> findBarnByPigId(@NotNull(message = "pigId.not.null") Long pigId);

    /**
     * 查询母猪配种次数
     * @param pigId 猪id
     * @return 母猪配种次数
     */
    Response<Integer> getCountOfMating(@NotNull(message = "pigId.not.null")Long pigId);

    /**
     * 查询母猪第一次配种的时间
     * @param farmId 猪场id
     * @param pigId 猪id
     * @return 第一次配种的时间
     */
    Response<Date> getFirstMatingTime(@NotNull(message = "pigId.not.null")Long pigId, @NotNull(message = "farmId.not.null") Long farmId);
}
