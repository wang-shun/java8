package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorBarnCountForPigTypeDto;
import io.terminus.doctor.event.model.DoctorBarn;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Desc: 猪舍表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorBarnReadService {

    /**
     * 根据id查询猪舍表
     * @param barnId 主键id
     * @return 猪舍表
     */
    Response<DoctorBarn> findBarnById(Long barnId);

    /**
     * 根据farmId查询猪舍表
     * @param farmId 猪场id
     * @return 猪舍表
     */
    Response<List<DoctorBarn>> findBarnsByFarmId(@NotNull(message = "farmId.not.null") Long farmId);

    /**
     * 根据farmIds查询猪舍表
     * @param farmIds 猪场id
     * @return 猪舍表
     */
    Response<List<DoctorBarn>> findBarnsByFarmIds(List<Long> farmIds);

    /**
     * 根据一些枚举条件查询猪舍
     * @param farmId  猪场id
     * @param pigTypes 猪类
     * @see io.terminus.doctor.common.enums.PigType
     * @param canOpenGroup 能否新建猪群
     * @see io.terminus.doctor.event.model.DoctorBarn.CanOpenGroup
     * @param status 猪舍状态
     * @see io.terminus.doctor.event.model.DoctorBarn.Status
     * @return 猪舍列表
     */
    Response<List<DoctorBarn>> findBarnsByEnums(@NotNull(message = "farmId.not.null") Long farmId,
                                                List<Integer> pigTypes, Integer canOpenGroup, Integer status);

    /**
     * 查询当前猪舍的存栏量
     * @param barnId 猪舍id
     * @return 存栏量
     */
    Response<Integer> countPigByBarnId(@NotNull(message = "barnId.not.null") Long barnId);

    /**
     * 查询当前猪舍的猪群猪数量
     * @param barnId 猪舍id
     * @return 存栏量
     */
    Response<Integer> pigGroupCountByBarnId(@NotNull(message = "barnId.not.null") Long barnId);

    /**
     * 查询当前猪舍的猪数量
     * @param barnId 猪舍id
     * @return 存栏量
     */
    Response<Integer> pigCountByBarnId(@NotNull(message = "barnId.not.null") Long barnId);

    /**
     * 根据外部编码查询猪舍
     * @param outId 外部编码
     * @return 猪舍
     */
    Response<DoctorBarn> findBarnByOutId(@NotEmpty(message = "outId.not.empty") String outId);

    /**
     * 根据当前猪舍查询可以转入的猪舍
     * @param farmId  转入的猪场id
     * @param groupId  当前猪群id
     * @return  可以转入的猪舍
     */
    Response<List<DoctorBarn>> findAvailableBarns(@NotNull(message = "farmId.can.not.be.null") Long farmId,
                                                  @NotNull(message = "groupId.can.not.be.null") Long groupId);
    /**
     * 统计每种猪舍类型猪舍数量
     * @param criteria 查询条件
     * @return 每种类型猪舍数量
     */
    Response<DoctorBarnCountForPigTypeDto> countForTypes(Map<String, Object> criteria);
}
