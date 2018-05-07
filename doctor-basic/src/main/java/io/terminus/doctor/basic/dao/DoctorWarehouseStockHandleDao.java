package io.terminus.doctor.basic.dao;

import com.google.common.collect.Maps;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import org.springframework.stereotype.Repository;

import java.util.*;

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

    public DoctorWarehouseStockHandle findByRelStockHandleId(Long id, int type) {
        int handle_sub_type = 0;
        //领料出库
        if (type == 2) {
            handle_sub_type = 13;
        }
        //配方出库
        if (type == 12) {
            handle_sub_type = 11;
        }
        //调拨出库
        if (type == 10) {
            handle_sub_type = 9;
        }
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("id", id);
        criteria.put("handle_sub_type", handle_sub_type);
        return this.sqlSession.selectOne(this.sqlId("findByRelStockHandleId"), criteria);
    }


    public List<DoctorWarehouseStockHandle> findRelStockHandle(Long stockHandleId) {
        return this.sqlSession.selectList(this.sqlId("findRelStockHandle"), stockHandleId);
    }

    public void updateHandleDateAndSettlementDate(Calendar handleDate, Date SettlementDate, Long stockHandleId) {

        Map<String, Object> params = new HashMap<>();
        params.put("stockHandleId", stockHandleId);
        params.put("handleDate", handleDate.getTime());
        params.put("settlementDate", SettlementDate);

        this.sqlSession.update(this.sqlId("updateHandleDateAndSettlementDate"), params);
    }
}
