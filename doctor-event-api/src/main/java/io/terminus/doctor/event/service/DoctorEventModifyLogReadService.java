package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorEventModifyRequestDto;
import io.terminus.doctor.event.model.DoctorEventModifyLog;

/**
 * Code generated by terminus code gen
 * Desc: 读服务
 * Date: 2017-04-05
 */

public interface DoctorEventModifyLogReadService {

    /**
     * 根据id查询
     * @param doctorEventModifyLogId 主键id
     * @return 
     */
    Response<DoctorEventModifyLog> findDoctorEventModifyLogById(Long doctorEventModifyLogId);

    /**
     * 根据id查询
     * @param requestId 请求
     * @return 封装dto
     */
    Response<DoctorEventModifyRequestDto> findRequestDto(Long requestId);

    /**
     * 分页查询编辑记录
     * @param modifyLog 编辑
     * @param pageNo 页码
     * @param pageSize 分页大小
     * @return 分页结果
     */
    Response<Paging<DoctorEventModifyRequestDto>>  pageModifyLog(DoctorEventModifyLog modifyLog, Integer pageNo, Integer pageSize);
}
