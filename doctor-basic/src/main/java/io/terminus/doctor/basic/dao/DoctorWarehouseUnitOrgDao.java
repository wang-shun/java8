package io.terminus.doctor.basic.dao;

import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseUnitOrg;
import io.terminus.common.mysql.dao.MyBatisDao;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-30 16:08:20
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseUnitOrgDao extends MyBatisDao<DoctorWarehouseUnitOrg> {

    public List<DoctorWarehouseUnitOrg> findByOrg(Long orgId) {
        return this.list(DoctorWarehouseUnitOrg.builder()
                .orgId(orgId)
                .build());
    }


    public Optional<DoctorWarehouseUnitOrg> findByOrgAndUnit(Long orgId, Long unitId) {
        List<DoctorWarehouseUnitOrg> unitOrgs = this.list(DoctorWarehouseUnitOrg.builder()
                .orgId(orgId)
                .unitId(unitId)
                .build());
        if (unitOrgs.isEmpty())
            return Optional.empty();
        return Optional.ofNullable(unitOrgs.get(0));
    }

    public void deleteByOrg(Long orgId) {
        this.sqlSession.delete(sqlId("deleteByOrg"), orgId);
    }

}
