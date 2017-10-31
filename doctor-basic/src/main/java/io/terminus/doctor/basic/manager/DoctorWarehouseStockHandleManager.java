package io.terminus.doctor.basic.manager;

import io.terminus.doctor.basic.dao.DoctorWarehouseStockHandleDao;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;

/**
 * Created by sunbo@terminus.io on 2017/9/12.
 */
@Component
public class DoctorWarehouseStockHandleManager {
    @Autowired
    private DoctorWarehouseStockHandleDao doctorWarehouseStockHandleDao;

    //    @Transactional(propagation = Propagation.NESTED)
    public void handle(AbstractWarehouseStockDto stockDto, DoctorWareHouse wareHouse, String serialNo, WarehouseMaterialHandleType handleType) {

        DoctorWarehouseStockHandle handle = new DoctorWarehouseStockHandle();
        handle.setFarmId(stockDto.getFarmId());
        handle.setWarehouseId(stockDto.getWarehouseId());
        handle.setWarehouseName(wareHouse.getWareHouseName());

        handle.setOperatorId(stockDto.getOperatorId());
        handle.setOperatorName(stockDto.getOperatorName());

        handle.setHandleDate(stockDto.getHandleDate().getTime());
        handle.setSerialNo(serialNo);

        handle.setHandleType(handleType.getValue());
        handle.setHandleSubType(handleType.getValue());

        doctorWarehouseStockHandleDao.create(handle);
    }
}
