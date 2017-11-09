package io.terminus.doctor.basic.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dao.DoctorWarehouseVendorDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseVendor;
import io.terminus.doctor.common.exception.InvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by sunbo@terminus.io on 2017/10/31.
 */
@Component
public class DoctorVendorManager {

    @Autowired
    private DoctorWarehouseVendorDao doctorWarehouseVendorDao;

    /**
     * @param id
     * @return
     * @throws InvalidException 如果没有找到
     */
    public DoctorWarehouseVendor findById(Long id) throws InvalidException {
        DoctorWarehouseVendor vendor = doctorWarehouseVendorDao.findById(id);
        if (null == vendor)
            throw new InvalidException("doctor.vendor.not.found", id);

        return vendor;
    }

}
