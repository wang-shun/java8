package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe: 公司原料信息修改
 */
public interface DoctorMaterialInfoWriteService {

    Response<Boolean> createMaterialInfoFarm(DoctorMaterialInfo doctorMaterialInfo);

    Response<Boolean> updateMaterialInfoFarm(DoctorMaterialInfo doctorMaterialInfo);
}
