package io.terminus.doctor.basic.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.basic.model.DoctorMaterialPriceInWareHouse;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * 陈增辉
 * Desc: 仓库中各物料每次入库的剩余量Dao类
 * Date: 2016-08-15
 */
@Repository
public class DoctorMaterialPriceInWareHouseDao extends MyBatisDao<DoctorMaterialPriceInWareHouse> {

    /**
     * 查询指定仓库中指定的物料, 每次入库的剩余量
     * 按照入库时间升序排序
     * @param wareHouseId 仓库id
     * @param materialId 物料id
     */
    public List<DoctorMaterialPriceInWareHouse> findByWareHouseAndMaterialId(Long wareHouseId, Long materialId){
        return sqlSession.selectList(sqlId("findByWareHouseAndMaterialId"), ImmutableMap.of("wareHouseId", wareHouseId, "materialId", materialId));
    }

    /**
     * 查找某次入库的剩余量
     * @param providerId
     * @return
     */
    public DoctorMaterialPriceInWareHouse findByProviderId(Long providerId){
        return sqlSession.selectOne(sqlId("findByProviderId"), providerId);
    }

    /**
     * 查询各仓库当前库存的价值, 单位是“分”
     * 所有参数都可为空
     * @param farmId
     * @param warehouseId
     * @return key = warehouseId, value = 价值(分)
     */
    public Map<Long, Double> stockAmount(Long farmId, Long warehouseId, WareHouseType type){
        Map<String, Object> param = MapBuilder.<String, Object>of()
                .put("farmId", farmId)
                .put("warehouseId", warehouseId)
                .map();
        if(type != null){
            param.put("type", type.getKey());
        }

        List<Map<String, Object>> query = sqlSession.selectList(sqlId("stockAmount"), param);

        Map<Long, Double> result = new HashMap<>();
        query.stream().forEach( map -> {
            Long houseId = Long.parseLong(Objects.toString(map.get("ware_house_id")));
            Double amount = Double.valueOf(Objects.toString(map.get("amount")));
            result.put(houseId, amount);
        });
        return result;
    }

    /**
     * 仓库当前库存信息
     * @param farmId
     * @param warehouseId
     * @param type
     * @return
     */
    public Map<String, Object> currentStockInfo(Long farmId, Long warehouseId, Integer type){
        Map map = new HashMap();

        map = sqlSession.selectOne(sqlId("stockAmount"), ImmutableMap.of("farmId", farmId, "warehouseId", warehouseId, "type", type));

        return map;
    }

    public List<DoctorMaterialPriceInWareHouse> findMaterialDatas(Long farmId, Long materialId, Long wareHouseId, Date endDate) {

        return sqlSession.selectList(sqlId("findMaterialData"), ImmutableMap.of("farmId", farmId, "materialId", materialId, "wareHouseId", wareHouseId, "endDate", endDate));
    }

}
