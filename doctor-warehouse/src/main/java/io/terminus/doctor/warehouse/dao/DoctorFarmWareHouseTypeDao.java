package io.terminus.doctor.warehouse.dao;

import com.google.common.collect.Maps;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;
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
public class DoctorFarmWareHouseTypeDao extends MyBatisDao<DoctorFarmWareHouseType> {

    public List<DoctorFarmWareHouseType> findByFarmId(Long farmId){
        return this.getSqlSession().selectList(sqlId("findByFarmId"), farmId);
    }

    public DoctorFarmWareHouseType findByFarmIdAndType(Long farmId, Integer type){
        Map<String,Object> params = Maps.newHashMap();
        params.put("farmId", farmId);
        params.put("type", type);
        return this.getSqlSession().selectOne(sqlId("findByFarmIdAndType"), params);
    }

    public void updateAll(DoctorFarmWareHouseType model){
        sqlSession.update(sqlId("updateAll"), model);
    }
}
