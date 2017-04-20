package io.terminus.doctor.basic.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.basic.dto.BarnConsumeMaterialReport;
import io.terminus.doctor.basic.dto.MaterialCountAmount;
import io.terminus.doctor.basic.dto.MaterialEventReport;
import io.terminus.doctor.basic.dto.WarehouseEventReport;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;

import javax.validation.constraints.NotNull;
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
    Response<Paging<DoctorMaterialConsumeProvider>> page(Long farmId, Long warehouseId, Long materialId, Integer eventType, List<Integer> eventTypes, Integer materilaType,
                                                          Long staffId, String startAt, String endAt, Integer pageNo, Integer size);

    /**
     * 仓库事件能否回滚
     * @param eventId 事件id
     * @return 能否回滚
     */
    Response<Boolean> eventCanRollback(@NotNull(message = "eventId.not.empty") Long eventId);

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
     * @param startAt
     * @param endAt
     * @return
     */
    Response<List<WarehouseEventReport>> warehouseEventReport(Long farmId, Long warehouseId, Long materialId, String materialName,
                                                              Integer eventType, List<Integer> eventTypes, Integer materilaType,
                                                              Long staffId, String startAt, String endAt);

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

    /**
     * 以猪舍为维度统计物资领用情况
     * @param farmId
     * @param wareHouseId
     * @param materialId
     * @param materialName
     * @param type
     * @param barnId
     * @param staffId
     * @param creatorId
     * @param startAt
     * @param endAt
     * @return
     */
    Response<Paging<BarnConsumeMaterialReport>> barnConsumeMaterialReport(Long farmId, Long wareHouseId, Long materialId, String materialName,
                                                                        WareHouseType type, Long barnId, Long staffId, Long creatorId,
                                                                        String startAt, String endAt, Integer pageNo, Integer pageSize);
    /**
     * 根据事件的时间查询事件中物料的统计数据
     * @param startDate 事件开始时间
     * @param endDate 事件结束时间
     * @param farmId 公司Id
     */
    Response<List<DoctorMaterialConsumeProvider>> findMaterialConsume(Long farmId, Long wareHouseId, Long materialId, String materialName,
                                                                      Long barnId, Long materialType, String barnName, Long type, Date startDate, Date endDate ,Integer pageNo, Integer size);

    /**
     * 根据事件的时间查询事件中物料的统计数据
     * 带有分页
     * @param startDate 事件开始时间
     * @param endDate 事件结束时间
     * @param farmId 公司Id
     */
    Response<Paging<DoctorMaterialConsumeProvider>> pagingfindMaterialConsume(Long farmId, Long wareHouseId, Long materialId, Long groupId, String materialName,
                                                                              Long barnId, Long type, Date startDate, Date endDate ,Integer pageNo, Integer size);

    /**
     * 根据猪场id查询数据
     * @param farmId
     * @param type
     * @param barnId
     * @param startDate
     * @param endDate
     * @return
     */
    Response<List<DoctorMaterialConsumeProvider>> findMaterialProfit(Long farmId, Long type, Long barnId, Date startDate, Date endDate );

    /**
     *
     * @param farmId
     * @param groupId
     * @param materialId
     * @param type
     * @param wareHouseId
     * @param barnId
     * @param materialType
     * @param startDate
     * @param endDate
     * @return
     */
    Response<List<DoctorMaterialConsumeProvider>> findMaterialByGroupId(Long farmId, Long groupId, Long materialId, Long type, Long wareHouseId, Long barnId, Long materialType, Date startDate, Date endDate);

    /**
     * 根据groupID来获取批次数据统计的物料数据
     * @param farmId
     * @param type
     * @param barnId
     * @param startDate
     * @param endDate
     * @return
     */
    Response<List<DoctorMaterialConsumeProvider>> findMaterialByGroup(Long farmId, Long wareHouseId, Long materialId, List<Long> groupId, String materialName,
                                                                              Long barnId, Long type, Date startDate, Date endDate);
}
