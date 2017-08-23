package io.terminus.doctor.basic.service;

import io.terminus.doctor.basic.dto.WarehouseStockDto;

/**
 * Created by sunbo@terminus.io on 2017/8/10.
 */
public interface NewDoctorWarehouseWriterService {


     void handler(WarehouseStockDto warehouseStockDto);


}
