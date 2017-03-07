package io.terminus.doctor.basic.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.model.DoctorMaterialInWareHouse;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-04-25
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Repository
public class DoctorMaterialInWareHouseDao extends MyBatisDao<DoctorMaterialInWareHouse>{

    public List<DoctorMaterialInWareHouse> queryByFarmAndWareHouseId(Long farmId, Long wareHouseId){
        return this.getSqlSession().selectList(sqlId("queryByFarmAndWareHouseId"), ImmutableMap.of("farmId",farmId, "wareHouseId",wareHouseId));
    }

    /**
     * 获取仓库中物料信息 数量
     * @param farmId
     * @param wareHouseId
     * @param materialId
     * @return
     */
    public DoctorMaterialInWareHouse queryByFarmHouseMaterial(Long farmId, Long wareHouseId, Long materialId){
        return this.getSqlSession().selectOne("queryByFarmHouseMaterial",ImmutableMap.of("farmId",farmId,"wareHouseId",wareHouseId,"materialId",materialId));
    }

    /**
     * 列举猪场 物料类型的列表
     * @param farmId
     * @param type
     * @return
     */
    public List<DoctorMaterialInWareHouse> queryByFarmType(Long farmId, Integer type){
        return this.getSqlSession().selectList(sqlId("queryByFarmType"), ImmutableMap.of("farmId",farmId, "type", type));
    }

    /**
     * 通过猪场物料， 获取不同的仓库存储内容
     * @param farmId
     * @param materialId
     * @return
     */
    public List<DoctorMaterialInWareHouse> queryByFarmMaterial(Long farmId, Long materialId){
        return this.getSqlSession().selectList(sqlId("queryByFarmMaterial"), ImmutableMap.of("farmId",farmId, "materialId", materialId));
    }

    /**
     * 修改对应的原料信息信息
     * @param params    MaterialId 原料Id, 修改 MaterialName, unit, unitName 内容的修改方式
     */
    public boolean updateMaterialInfoByMaterialId(Map<String, Object> params){
        return this.getSqlSession().update(sqlId("updateMaterialInfoByMaterialId"), params) >= 0;
    }

    /**
     * 查找仓库ID和物料Id所对应的物料计量单位
     * @param farmId
     * @param materialId 物料ID
     * @param wareHouseId 仓库ID
     * @return
     */
    public DoctorMaterialInWareHouse findMaterialUnit(Long farmId, Long materialId, Long wareHouseId ) {
        return this.getSqlSession().selectOne(sqlId("findMaterialUnit"), ImmutableMap.of("farmId", farmId, "materialId", materialId, "wareHouseId", wareHouseId));
    }
}
