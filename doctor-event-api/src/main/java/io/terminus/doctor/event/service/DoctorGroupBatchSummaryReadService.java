package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.model.DoctorGroupBatchSummary;

import javax.validation.constraints.NotNull;

/**
 * Desc: 猪群批次总结表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-09-13
 */

public interface DoctorGroupBatchSummaryReadService {

    /**
     * 根据id查询猪群批次总结表
     * @param groupBatchSummaryId 主键id
     * @return 猪群批次总结表
     */
    Response<DoctorGroupBatchSummary> findGroupBatchSummaryById(@NotNull(message = "groupBatchSummaryId.not.null") Long groupBatchSummaryId);

    /**
     * 通过猪群明细实时获取批次总结
     * @param groupDetail 猪群明细
     * @return 猪群批次总结
     */
    Response<DoctorGroupBatchSummary> getSummaryByGroupDetail(DoctorGroupDetail groupDetail);

}
