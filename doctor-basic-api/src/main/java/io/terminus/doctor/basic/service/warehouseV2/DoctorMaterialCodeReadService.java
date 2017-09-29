package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouseV2.DoctorMaterialCode;

/**
 * Created by sunbo@terminus.io on 2017/9/11.
 */
public interface DoctorMaterialCodeReadService {


    Response<DoctorMaterialCode> find(Long warehouseId, Long materialId, String vendorName);

}
