package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.model.DoctorGroupBatchSummary;

import javax.validation.Valid;

/**
 * Desc: 猪群批次总结表读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-09-13
 */

public interface DoctorGroupBatchSummaryReadService {

    /**
     * 通过猪群明细实时获取批次总结
     * @param groupDetail 猪群明细
     * @param fcrFeed     料肉比的饲料用量
     * @return 猪群批次总结
     */
    Response<DoctorGroupBatchSummary> getSummaryByGroupDetail(DoctorGroupDetail groupDetail, Double fcrFeed);

    /**
     * 分页查询猪群批次总结
     * @param searchDto 查询dto
     * @param pageSize  分页大小
     * @param pageNo    页码
     * @return 批次总结
     */
    Response<Paging<DoctorGroupBatchSummary>> pagingGroupBatchSummary(@Valid DoctorGroupSearchDto searchDto, Integer pageNo, Integer pageSize);

}
