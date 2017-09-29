package io.terminus.doctor.basic.manager;

import io.terminus.doctor.basic.dao.DoctorWarehouseStockHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockHandlerDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;

/**
 * Created by sunbo@terminus.io on 2017/9/12.
 */
@Component
public class DoctorWarehouseStockHandleManager {
    @Autowired
    private DoctorWarehouseStockHandleDao doctorWarehouseStockHandleDao;

//    @Transactional(propagation = Propagation.NESTED)
    public void handle(Long warehouseId, Long farmId, Calendar handleDate, String serialNo) {

        DoctorWarehouseStockHandle handle = new DoctorWarehouseStockHandle();
        handle.setFarmId(farmId);
        handle.setWarehouseId(warehouseId);
        handle.setHandleDate(handleDate.getTime());
        handle.setSerialNo(serialNo);
        doctorWarehouseStockHandleDao.create(handle);
    }
}
