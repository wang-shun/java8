package io.terminus.doctor.warehouse.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;
import org.springframework.stereotype.Repository;

import java.util.List;

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

}
