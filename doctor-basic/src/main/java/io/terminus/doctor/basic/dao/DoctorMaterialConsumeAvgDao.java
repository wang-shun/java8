package io.terminus.doctor.basic.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeAvg;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-17
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Repository
public class DoctorMaterialConsumeAvgDao extends MyBatisDao<DoctorMaterialConsumeAvg>{

    /**
     * 获取公司物料信息
     * @param farmId
     * @param wareHouseId
     * @param materialId
     * @return
     */
    public DoctorMaterialConsumeAvg queryByIds(Long farmId, Long wareHouseId, Long materialId){
        return this.getSqlSession().selectOne(sqlId("queryByIds"), ImmutableMap.of("farmId",farmId, "wareHouseId", wareHouseId, "materialId", materialId));
    }

    /**
     * 通过 farmIds, warehouseId, materialId 获取结果
     * @param params
     * @return
     */
    public List<DoctorMaterialConsumeAvg> queryByIds(Map<String,Object> params){
        return this.getSqlSession().selectList(sqlId("queryByIds"), params);
    }

    public List<DoctorMaterialConsumeAvg> queryByFarmIdAndType(Long farmId, Integer type){
        return this.getSqlSession().selectList(sqlId("queryByFarmIdAndType"), ImmutableMap.of("farmId",farmId, "type", type));
    }

    public DoctorMaterialConsumeAvg findLastByFarmId(Long farmId){
        return sqlSession.selectOne(sqlId("findLastByFarmId"), farmId);
    }

    public void updateAll(DoctorMaterialConsumeAvg model){
        sqlSession.update(sqlId("updateAll"), model);
    }
}
