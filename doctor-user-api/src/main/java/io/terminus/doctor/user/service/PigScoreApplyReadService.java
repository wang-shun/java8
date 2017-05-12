package io.terminus.doctor.user.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.PigScoreApply;

import java.util.Map;
import java.util.List;

/**
 * Desc: 猪场评分功能申请
 * Mail: hehaiyang@terminus.io
 * Date: 2017/05/02
 */
public interface PigScoreApplyReadService {

    /**
     * 查询
     * @param id
     * @return pigScoreApply
     */
    Response<PigScoreApply> findById(Long id);

    /**
     * 分页
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<PigScoreApply>
     */
    Response<Paging<PigScoreApply>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

   /**
    * 列表
    * @param criteria
    * @return List<PigScoreApply>
    */
    Response<List<PigScoreApply>> list(Map<String, Object> criteria);

    Response<PigScoreApply> findByFarmIdAndUserId(Long farmId, Long userId);
}