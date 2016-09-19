package io.terminus.doctor.warehouse.dao;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.Constants;
import io.terminus.doctor.warehouse.dto.MaterialCountAmount;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import org.springframework.stereotype.Repository;

import java.util.Collections;
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

    public Map<Long, Double> countConsumeTotal(Long wareHouseId){
        Map<Long, Double> result = new HashMap<>();
        Map<Long, Map<String, Object>> query = sqlSession.selectMap(sqlId("countConsumeTotal"), wareHouseId, "material_id");
        for(Map.Entry<Long, Map<String, Object>> entry : query.entrySet()){
            Long materialId = entry.getKey();
            Double consumeTotal = Double.valueOf(entry.getValue().get("consumeTotal").toString());
            result.put(materialId, consumeTotal);
        }
        return result;
    }
}
