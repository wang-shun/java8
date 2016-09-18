package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe: 领用调度事件读取信息
 */
public interface DoctorMaterialConsumeProviderReadService {

    /**
     * 分页查询仓库历史出入记录
     * @param warehouseId 仓库id
     * @param materialId 物料id
     * @param eventType 事件类型
     *                  @see DoctorMaterialConsumeProvider.EVENT_TYPE
     * @param materilaType 物料(仓库)类型
     *                     @see io.terminus.doctor.common.enums.WareHouseType
     * @param staffId 事件员工id
     * @param startAt 开始日期范围
     * @param endAt 结束日期范围
     * @param pageNo 第几页
     * @param size 每页数量
     * @return
     */
    Response<Paging<DoctorMaterialConsumeProvider>> page(Long warehouseId, Long materialId, Integer eventType, Integer materilaType,
                                                       Long staffId, String startAt, String endAt, Integer pageNo, Integer size);

}
