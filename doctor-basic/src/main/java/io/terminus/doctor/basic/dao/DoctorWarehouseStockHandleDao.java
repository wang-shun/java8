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

    //得到单位名称
    public String getNameByUnit(Long relStockHandleId) {
        String unitName = this.sqlSession.selectOne(this.sqlId("getNameByUnit"), relStockHandleId);
        return unitName;
    }

    //得到配方入库仓库
    public DoctorWarehouseStockHandle findwarehouseName(Long RelId) {
        DoctorWarehouseStockHandle findwarehouseName = this.sqlSession.selectOne(this.sqlId("findwarehouseName"), RelId);
        return findwarehouseName;
    }

    public DoctorWarehouseStockHandle findByRelStockHandleId(Long id, int type) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("id", id);
        return this.sqlSession.selectOne(this.sqlId("findByRelStockHandleId"), criteria);
    }

    public List<DoctorWarehouseStockHandle> findByRelStockHandleIds(Long id) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("id", id);
        return this.sqlSession.selectList(this.sqlId("findByRelStockHandleIds"), criteria);
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
