package io.terminus.doctor.user.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dto.FarmCriteria;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorFarmInformation;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Desc: 猪场信息读接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */

public interface DoctorFarmReadService {

    /**
     * 根据猪场id查询猪场信息
     * @param farmId 猪场id
     * @return 猪场信息
     */
    Response<DoctorFarm> findFarmById(Long farmId);

    /**
     * 根据用户id查询猪场信息列表
     * @param userId 用户id
     * @return 猪场信息列表
     */
    Response<List<DoctorFarm>> findFarmsByUserId(Long userId);

    /**
     * 根据用户id查询有权限的猪场的id
     * @param userId 用户id
     * @return 猪场id
     */
    Response<List<Long>> findFarmIdsByUserId(Long userId);

    /**
     * 查询所有猪场
     * @return 所有猪场
     */
    Response<List<DoctorFarm>> findAllFarms();

    /**
     * 根据公司id查询猪场信息列表(子公司)
     * @param orgId 公司id
     * @return 猪场信息列表
     */
    Response<List<DoctorFarm>> findFarmsByOrgId(@NotNull(message = "orgId.not.null") Long orgId);

    /**
     * 查询猪场
     * @param orgId 公司id
     * @param isIntelligent 是否是时智能猪场
     * @return
     */
    Response<List<DoctorFarm>> findFarmsBy(Long orgId, Integer isIntelligent);
    /**
     * 根据farmIds 查询猪场
     * @param ids 猪场ids
     * @return 猪场列表
     */
    Response<List<DoctorFarm>> findFarmsByIds(List<Long> ids);

    /**
     * 分页查询猪场
     * @param farmCriteria
     * @return
     */
    Response<Paging<DoctorFarm>> pagingFarm(FarmCriteria farmCriteria, Integer pageNo, Integer pageSize);

    /**
     * 根据猪场编号查询猪场
     * @param number 猪场编号
     * @return
     */
    Response<DoctorFarm> findByNumber(String number);

    //  ---------------------  新增代码  2018年8月28日17:50:56 ----------------------
    Response<List<DoctorFarmInformation>> findSubordinatePig();


}
