package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorEventModifyRequestDto;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by xjn on 17/3/10.
 * 编辑请求读service
 */
public interface DoctorEventModifyRequestReadService {
    /**
     * 根据id获取事件编辑请求
     * @param requestId id
     * @return 事件编辑请求
     */
    Response<DoctorEventModifyRequest> findById(@NotNull(message = "requestId.not.null") Long requestId);

    /**
     * 分页查询
     * @param modifyRequest 查询条件
     * @return 分页结果
     */
    Response<Paging<DoctorEventModifyRequestDto>> pagingRequest(DoctorEventModifyRequest modifyRequest, Integer pageNo, Integer pageSize);

    /**
     * 更具id获取dto
     * @param requestId
     * @return
     */
    Response<DoctorEventModifyRequestDto> findDtoById(@NotNull(message = "requestId.not.null") Long requestId);

    /**
     * 查询所需状态的编辑请求
     * @param status 请求状态
     * @return 请求列表
     */
    Response<List<DoctorEventModifyRequest>> listByStatus(@NotNull(message = "status.not.null")Integer status);
}
