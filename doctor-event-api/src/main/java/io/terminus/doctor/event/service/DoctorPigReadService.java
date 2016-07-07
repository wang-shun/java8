package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorPigInfoDetailDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.DoctorPigMessage;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;

import javax.validation.constraints.NotNull;
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
     * @param pigId
     * @return
     */
    Response<String> generateFostersCode(@NotNull(message = "input.pigId.empty") Long pigId);

    /**
     * 获取猪舍pig 信息内容
     * @param barnId
     * @return
     */
    Response<List<DoctorPigInfoDto>> queryDoctorPigInfoByBarnId(@NotNull(message = "input.barnId.empty") Long barnId);

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
     * @param farmId
     * @param pigCode
     * @return
     */
    Response<Boolean> validatePigCodeByFarmId(Long farmId, String pigCode);

    /**
     * 获取猪舍内的所有猪的猪类
     * @param barnId 猪舍id
     * @return 猪类set
     */
    Response<Set<Integer>> findPigStatusByBarnId(@NotNull(message = "barnId.not.null") Long barnId);
}
