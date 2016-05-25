package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;

import javax.validation.constraints.NotNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public interface DoctorWareHouseWriteService {

    /**
     * 创建WareHouse
     * @param doctorWareHouse
     * @return
     */
    Response<Long> createWareHouse(@NotNull(message = "input.wareHouse.empty") DoctorWareHouse doctorWareHouse);

    /**
     * 修改warehouse 信息
     * @param wareHouse
     * @return
     */
//    Response<Boolean> updateWareHouse(DoctorWareHouse wareHouse);
}
