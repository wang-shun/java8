package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorGroupBatchSummary;

/**
 * Desc: 猪群批次总结表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-09-13
 */

public interface DoctorGroupBatchSummaryWriteService {

    /**
     * 创建DoctorGroupBatchSummary
     * @param groupBatchSummary 猪群批次总结表实例
     * @return 主键id
     */
    Response<Long> createGroupBatchSummary(DoctorGroupBatchSummary groupBatchSummary);

    /**
     * 更新DoctorGroupBatchSummary
     * @param groupBatchSummary 猪群批次总结表实例
     * @return 是否成功
     */
    Response<Boolean> updateGroupBatchSummary(DoctorGroupBatchSummary groupBatchSummary);

    /**
     * 根据主键id删除DoctorGroupBatchSummary
     * @param groupBatchSummaryId 猪群批次总结表实例主键id
     * @return 是否成功
     */
    Response<Boolean> deleteGroupBatchSummaryById(Long groupBatchSummaryId);
}