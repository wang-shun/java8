package io.terminus.doctor.warehouse.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.warehouse.model.DoctorMaterialPriceInWareHouse;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 陈增辉
 * Desc: 仓库中各物料每次入库的剩余量Dao类
 * Date: 2016-08-15
 */
@Repository
public class DoctorMaterialPriceInWareHouseDao extends MyBatisDao<DoctorMaterialPriceInWareHouse> {

    public List<DoctorMaterialPriceInWareHouse> findByWareHouseId(Long wareHouseId){
        return sqlSession.selectList(sqlId("findByWareHouseId"), wareHouseId);
    }
}
