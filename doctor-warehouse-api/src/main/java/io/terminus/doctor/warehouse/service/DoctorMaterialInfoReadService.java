package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public interface DoctorMaterialInfoReadService {

    /**
     * 获取对应猪场的原料信息
     * @param farmId
     * @return
     */
    Response<List<DoctorMaterialInfo>> queryMaterialInfos(String farmId);

}
