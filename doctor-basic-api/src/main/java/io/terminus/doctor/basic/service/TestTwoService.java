package io.terminus.doctor.basic.service;


import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;

public interface TestTwoService {   //方法

    Response  select(Integer id);//查询

    Response  add(DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle);//增加

    Response delete(long id);//删除

    Response update(DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle);//更新

}
