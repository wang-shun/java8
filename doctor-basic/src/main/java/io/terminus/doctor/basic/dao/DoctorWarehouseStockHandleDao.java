package io.terminus.doctor.basic.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import org.springframework.stereotype.Repository;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-31 14:49:19
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseStockHandleDao extends MyBatisDao<DoctorWarehouseStockHandle> {

    //得到配方入库仓库
    public String findwarehouseName(Long RelId) {
        String warehouseName = this.sqlSession.selectOne(this.sqlId("findwarehouseName"), RelId);
        return warehouseName;
    }
}
