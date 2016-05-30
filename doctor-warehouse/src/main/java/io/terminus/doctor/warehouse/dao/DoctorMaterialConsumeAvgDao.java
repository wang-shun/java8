package io.terminus.doctor.warehouse.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeAvg;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    public List<DoctorMaterialConsumeAvg> queryByFarmIdAndType(Long farmId, Integer type){
        return this.getSqlSession().selectList(sqlId("queryByFarmIdAndType"), ImmutableMap.of("farmId",farmId, "type", type));
    }
}
