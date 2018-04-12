package io.terminus.doctor.basic.dao;

import io.terminus.doctor.basic.model.DoctorWarehouseOrgSettlement;
import io.terminus.common.mysql.dao.MyBatisDao;

import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-04-12 16:23:41
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseOrgSettlementDao extends MyBatisDao<DoctorWarehouseOrgSettlement> {


    public boolean isSettled(Long orgId, Date settlementDate) {
        Map<String, Object> params = new HashMap<>();

        params.put("orgId", orgId);
        params.put("settlementDate", settlementDate);

        long count = this.sqlSession.selectOne(this.sqlId("countBySettlementDate"), params);
        return count > 0;
    }

}
