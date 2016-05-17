package io.terminus.doctor.warehouse.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
