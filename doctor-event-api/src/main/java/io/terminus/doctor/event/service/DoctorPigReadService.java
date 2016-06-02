package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorPigInfoDetailDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;

import javax.validation.constraints.NotNull;
import java.util.List;

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
    Response<DoctorPigInfoDetailDto> queryPigDetailInfoByPigId(@NotNull(message = "input.pigId.empty") Long pigId);

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
     * 获取猪舍pig 信息内容
     * @param barnId
     * @return
     */
    Response<List<DoctorPigInfoDto>> queryDoctorPigInfoByBarnId(@NotNull(message = "input.barnId.empty") Long barnId);
}
