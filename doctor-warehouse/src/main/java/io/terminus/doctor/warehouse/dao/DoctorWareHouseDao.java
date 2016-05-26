package io.terminus.doctor.warehouse.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
