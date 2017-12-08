package io.terminus.doctor.basic.dao;

import io.terminus.common.mysql.dao.MyBatisDao;

import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-26 17:04:11
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseSkuDao extends MyBatisDao<DoctorWarehouseSku> {


    public String findLastCode(Long orgId, Integer type) {

        Map<String, Object> params = new HashMap<>(2);
        params.put("orgId", orgId);
        params.put("type", type);
        return this.sqlSession.selectOne(this.sqlId("findLastCode"), params);
    }

}
