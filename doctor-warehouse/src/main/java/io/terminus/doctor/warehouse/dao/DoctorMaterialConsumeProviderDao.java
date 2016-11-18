package io.terminus.doctor.warehouse.dao;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Paging;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.Constants;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.warehouse.dto.BarnConsumeMaterialReport;
import io.terminus.doctor.warehouse.dto.MaterialCountAmount;
import io.terminus.doctor.warehouse.dto.MaterialEventReport;
import io.terminus.doctor.warehouse.dto.WarehouseEventReport;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DoctorMaterialConsumeProviderDao extends MyBatisDao<DoctorMaterialConsumeProvider>{

    /**
     * 查询指定仓库最近一次事件
     * @param wareHouseId 仓库id, 不可为空
     * @param materialId  物料id, 可为空
     * @param eventType 事件类型, 可为空
     * @return
     */
    public DoctorMaterialConsumeProvider findLastEvent(Long wareHouseId, Long materialId, DoctorMaterialConsumeProvider.EVENT_TYPE eventType){
        Map<String, Object> param = new HashMap<>();
        param.put("wareHouseId", wareHouseId);
        if(eventType != null){
            param.put("eventType", eventType.getValue());
        }
        if(materialId != null){
            param.put("materialId", materialId);
        }
        return sqlSession.selectOne(sqlId("findLastEvent"), ImmutableMap.copyOf(param));
    }

    public Paging<MaterialCountAmount> countAmount(Integer offset, Integer limit, Map<String, Object> criteria){
        if (criteria == null) {    //如果查询条件为空
            criteria = Maps.newHashMap();
        }
        // get total count
        Long total = sqlSession.selectOne(sqlId("countCountAmount"), criteria);
        if (total <= 0){
            return new Paging<>(0L, Collections.<MaterialCountAmount>emptyList());
        }
        criteria.put(Constants.VAR_OFFSET, offset);
        criteria.put(Constants.VAR_LIMIT, limit);
        // get data
        List<MaterialCountAmount> datas = sqlSession.selectList(sqlId("pageCountAmount"), criteria);
        return new Paging<>(total, datas);
    }

    public Map<Long, Double> sumEventCount(Long wareHouseId, List<Integer> eventTypes){
        Map<String ,Object> param = new HashMap<>();
        param.put("wareHouseId", wareHouseId);
        if(eventTypes != null && !eventTypes.isEmpty()){
            param.put("eventTypes", eventTypes);
        }
        Map<Long, Double> result = new HashMap<>();
        Map<Long, Map<String, Object>> query = sqlSession.selectMap(sqlId("sumEventCount"), param, "material_id");
        for(Map.Entry<Long, Map<String, Object>> entry : query.entrySet()){
            Long materialId = entry.getKey();
            Double consumeTotal = Double.valueOf(entry.getValue().get("consumeTotal").toString());
            result.put(materialId, consumeTotal);
        }
        return result;
    }

    /**
     * 对饲料消耗数量进行求和, 只计算事件类型为 DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER 的数据
     * @param criteria 过滤字段
     * @return
     */
    public Double sumConsumeFeed(Map<String, Object> criteria){
        criteria.put("eventType", DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue());
        criteria.put("type", WareHouseType.FEED.getKey());
        return sqlSession.selectOne(sqlId("sumConsumeFeed"), criteria);
    }

    /**
     * 查询仓库内各种物资在指定时间段内的出入库总量和金额
     * @return
     */
    public List<WarehouseEventReport> warehouseEventReport(Map<String, Object> criteria){
        return sqlSession.selectList(sqlId("warehouseEventReport"), criteria);
    }

    /**
     * 指定仓库在指定时间段内各种物料每天发生的各种事件的数量和金额
     * @param farmId 猪场id
     * @param warehouseId 仓库id
     * @param startAt
     * @param endAt
     * @return
     */
    public List<MaterialEventReport> materialEventReport(Long farmId, Long warehouseId, WareHouseType type, Date startAt, Date endAt){
        Map<String, Object> param = MapBuilder.<String, Object>of()
                .put("farmId", farmId)
                .put("wareHouseId", warehouseId)
                .put("startAt", startAt)
                .put("endAt", endAt)
                .map();
        if(type != null){
            param.put("type", type.getKey());
        }
        return sqlSession.selectList(sqlId("materialEventReport"), ImmutableMap.copyOf(Params.filterNullOrEmpty(param)));
    }

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
    public Paging<BarnConsumeMaterialReport> barnReport(Long farmId, Long wareHouseId, Long materialId, String materialName,
                                                      WareHouseType type, Long barnId, Long staffId, Long creatorId,
                                                      String startAt, String endAt, Integer offset, Integer limit){
        if(farmId == null){
            throw new ServiceException("farmId.not.null");
        }
        Map<String, Object> param = MapBuilder.<String, Object>of()
                .put("farmId", farmId)
                .put("wareHouseId", wareHouseId)
                .put("startAt", startAt)
                .put("endAt", endAt)
                .put("materialId", materialId)
                .put("materialName", materialName)
                .put("barnId", barnId)
                .put("staffId", staffId)
                .put("creatorId", creatorId)
                .put("offset", offset)
                .put("limit", limit)
                .map();
        if(type != null){
            param.put("type", type.getKey());
        }
        param = ImmutableMap.copyOf(Params.filterNullOrEmpty(param));
        long total = sqlSession.selectOne(sqlId("countBarnReport"), param);
        if (total <= 0){
            return new Paging<>(0L, Collections.<BarnConsumeMaterialReport>emptyList());
        }
        List<BarnConsumeMaterialReport> datas = sqlSession.selectList(sqlId("pageBarnReport"), param);
        return new Paging<>(total, datas);
    }
}
