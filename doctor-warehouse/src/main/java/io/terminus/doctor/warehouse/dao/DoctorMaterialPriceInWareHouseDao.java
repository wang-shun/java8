package io.terminus.doctor.warehouse.dao;

import com.google.common.collect.ImmutableMap;
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

    /**
     * 查询指定仓库中指定的物料, 每次入库的剩余量
     * 按照入库时间升序排序
     * @param wareHouseId 仓库id
     * @param materialId 物料id
     */
    public List<DoctorMaterialPriceInWareHouse> findByWareHouseAndMaterialId(Long wareHouseId, Long materialId){
        return sqlSession.selectList(sqlId("findByWareHouseAndMaterialId"), ImmutableMap.of("wareHouseId", wareHouseId, "materialId", materialId));
    }

    /**
     * 查找某次入库的剩余量
     * @param providerId
     * @return
     */
    public DoctorMaterialPriceInWareHouse findByProviderId(Long providerId){
        return sqlSession.selectOne(sqlId("findByProviderId"), providerId);
    }
}
