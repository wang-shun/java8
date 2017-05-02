package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.PigScoreApply;

/**
 * Desc: 猪场评分功能申请
 * Mail: hehaiyang@terminus.io
 * Date: 2017/05/02
 */
public interface PigScoreApplyWriteService {

    /**
     * 创建
     * @param pigScoreApply
     * @return Boolean
     */
    Response<Long> create(PigScoreApply pigScoreApply);

    /**
     * 更新
     * @param pigScoreApply
     * @return Boolean
     */
    Response<Boolean> update(PigScoreApply pigScoreApply);

    /**
     * 删除
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);

}