package io.terminus.doctor.basic.dao;

import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseItemOrg;
import io.terminus.common.mysql.dao.MyBatisDao;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-11-02 22:15:38
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseItemOrgDao extends MyBatisDao<DoctorWarehouseItemOrg> {


    public List<DoctorWarehouseItemOrg> findByOrg(Long orgId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orgId", orgId);
        return this.list(params);
    }

    public void deleteByOrg(Long orgId) {
        this.sqlSession.delete(sqlId("deleteByOrg"), orgId);
    }

}
