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


}