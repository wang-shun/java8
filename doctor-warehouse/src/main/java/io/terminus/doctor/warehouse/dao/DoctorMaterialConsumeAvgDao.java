package io.terminus.doctor.warehouse.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeAvg;
import org.springframework.stereotype.Repository;

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
        return this.getSqlSession().selectOne("queryByIds", ImmutableMap.of("farmId",farmId, "wareHouseId", wareHouseId, "materialId", materialId));
    }

}
