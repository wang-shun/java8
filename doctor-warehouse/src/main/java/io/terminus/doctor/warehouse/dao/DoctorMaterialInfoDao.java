package io.terminus.doctor.warehouse.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-17
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Repository
public class DoctorMaterialInfoDao extends MyBatisDao<DoctorMaterialInfo>{

    public List<DoctorMaterialInfo> findByFarmId(Long farmId){
        return this.getSqlSession().selectList(sqlId("findByFarmId"), farmId);
    }

    public List<DoctorMaterialInfo> findByFarmIdType(Long farmId, Integer type){
        return this.getSqlSession().selectList(sqlId("findByFarmIdType"), ImmutableMap.of("farmId",farmId,"type",type));
    }

}
