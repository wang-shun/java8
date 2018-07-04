package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;

/**
 * @ClassName TestService
 * @Description TODO
 * @Author Danny
 * @Date 2018/7/4 10:55
 */
public interface TestService {

    Response select(Integer id);

    Response add(DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle);

    Response update(DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle);

    Response delete(Long id);

}
