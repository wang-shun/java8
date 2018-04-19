package io.terminus.doctor.basic.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-17
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Repository
public class DoctorWareHouseDao extends MyBatisDao<DoctorWareHouse>{

    public List<DoctorWareHouse> findByFarmId(Long farmId){
        return this.getSqlSession().selectList(sqlId("findByFarmId"), farmId);
    }
    public List<DoctorWareHouse> getWarehouseByType(DoctorWareHouse criteria,Integer pageNum, Integer pageSize){
        Map<String, Object> params = new HashMap<>();
        params.put("criteria", criteria);
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        return this.getSqlSession().selectList(sqlId("getWarehouseByType"),params);
    }
}
