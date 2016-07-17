package io.terminus.doctor.warehouse.service;


import io.terminus.common.model.Response;

/**
 * Created by yaoqijun.
 * Date:2016-07-17
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public interface DoctorWareHouseTypeWriteService {

    /**
     * 初始化对应的猪场操作方式
     * @param farmId
     * @param farmName
     * @return
     */
    Response<Boolean> initDoctorWareHouseType(Long farmId, String farmName, Long userId, String userName);

}
