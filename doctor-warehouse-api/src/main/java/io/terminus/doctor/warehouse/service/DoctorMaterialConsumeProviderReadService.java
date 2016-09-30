package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.warehouse.dto.MaterialCountAmount;
import io.terminus.doctor.warehouse.dto.MaterialEventReport;
import io.terminus.doctor.warehouse.dto.WarehouseEventReport;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;

import java.util.Date;
import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe: 领用调度事件读取信息
 */
public interface DoctorMaterialConsumeProviderReadService {

    Response<DoctorMaterialConsumeProvider> findById(Long id);

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
    Response<Paging<DoctorMaterialConsumeProvider>> page(Long farmId, Long warehouseId, Long materialId, Integer eventType, Integer materilaType,
                                                       Long staffId, String startAt, String endAt, Integer pageNo, Integer size);

    Response<List<DoctorMaterialConsumeProvider>> list(Long farmId, Long warehouseId, Long materialId, String materialName,
                                                       Integer eventType, List<Integer> eventTypes, Integer materilaType,
                                                       Long staffId, String startAt, String endAt);

    Response<Paging<MaterialCountAmount>> countAmount(Long farmId, Long warehouseId, Long materialId, Integer eventType, Integer materilaType,
                                              Long barnId, Long groupId, Long staffId, String startAt, String endAt, Integer pageNo, Integer size);

    /**
     * 对饲料消耗数量进行求和, 只计算事件类型为 DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER 的数据
     * 所有参数都可以为空
     * @param farmId
     * @param wareHouseId
     * @param materialId
     * @param staffId
     * @param barnId
     * @param groupId
     * @param startAt
     * @param endAt
     * @return
     */
    Response<Double> sumConsumeFeed(Long farmId, Long wareHouseId, Long materialId, Long staffId, Long barnId, Long groupId, String startAt, String endAt);

    /**
     * 查询仓库内各种物资在指定时间段内的出入库总量和金额
     * @param farmId
     * @param warehouseId
     * @param type
     * @param startAt
     * @param endAt
     * @return
     */
    Response<List<WarehouseEventReport>> warehouseEventReport(Long farmId, Long warehouseId, WareHouseType type, Long materialId, Date startAt, Date endAt);

    /**
     * 指定仓库在指定时间段内各种物料每天发生的各种事件的数量和金额
     * @param farmId
     * @param warehouseId
     * @param type
     * @param startAt
     * @param endAt
     * @return
     */
    Response<List<MaterialEventReport>> materialEventReport(Long farmId, Long warehouseId, WareHouseType type, Date startAt, Date endAt);
}
