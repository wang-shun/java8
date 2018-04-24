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
        if (type == 6) {
            handle_sub_type = 2;
        }
        if (type == 8) {
            handle_sub_type = 3;
        }
        if (type == 9) {
            handle_sub_type = 5;
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
        params.put("year", handleDate.get(Calendar.YEAR));
        params.put("month", handleDate.get(Calendar.MONTH) + 1);
        params.put("settlementDate", SettlementDate);

        this.sqlSession.update(this.sqlId("updateHandleDateAndSettlementDate"), params);
    }
}
