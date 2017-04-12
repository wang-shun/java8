package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorDataFactor;

/**
 * Desc: 信用模型计算因子写服务接口
 * Mail: hehaiyang@terminus.io
 * Date: 2017/3/17
 */
public interface DoctorDataFactorWriteService {

    /**
     * 创建
     * @param doctorDataFactor
     * @return Boolean
     */
    Response<Long> create(DoctorDataFactor doctorDataFactor);

    /**
     * 更新
     * @param doctorDataFactor
     * @return Boolean
     */
    Response<Boolean> update(DoctorDataFactor doctorDataFactor);

    /**
     * 删除
     * @param id
     * @return Boolean
     */
    Response<Boolean> delete(Long id);

}