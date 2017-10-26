package io.terminus.doctor.basic.dao;

import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseVendorOrg;
import io.terminus.common.mysql.dao.MyBatisDao;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-26 16:16:34
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseVendorOrgDao extends MyBatisDao<DoctorWarehouseVendorOrg> {


    public Optional<DoctorWarehouseVendorOrg> findByOrgAndVendor(Long vendorId, Long orgId) {
        List<DoctorWarehouseVendorOrg> vendorOrgs = this.list(DoctorWarehouseVendorOrg.builder()
                .orgId(orgId)
                .vendorId(vendorId)
                .build());
        if (null == vendorOrgs || vendorOrgs.isEmpty())
            return Optional.empty();
        return Optional.ofNullable(vendorOrgs.get(0));
    }

    public List<DoctorWarehouseVendorOrg> findByOrg(Long orgId) {
        return this.list(DoctorWarehouseVendorOrg.builder()
                .orgId(orgId)
                .build());
    }

}
