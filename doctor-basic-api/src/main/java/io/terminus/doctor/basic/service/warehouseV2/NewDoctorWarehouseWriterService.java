package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockDto;

/**
 * Created by sunbo@terminus.io on 2017/8/10.
 */
public interface NewDoctorWarehouseWriterService {


     void handler(WarehouseStockDto warehouseStockDto);


}
