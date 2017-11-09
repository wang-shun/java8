package io.terminus.doctor.basic.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.enums.WarehouseVendorDeleteFlag;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseVendor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-26 15:57:14
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseVendorDao extends MyBatisDao<DoctorWarehouseVendor> {


    public DoctorWarehouseVendor findByName(String name) {

        Map<String, Object> params = new HashMap<>();
        params.put("status", WarehouseVendorDeleteFlag.NORMAL.getValue());
        params.put("name", name);

        List<DoctorWarehouseVendor> vendors = this.list(params);
        if (vendors.isEmpty())
            return null;

        return vendors.get(0);
    }

}
