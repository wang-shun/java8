package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;

import java.util.Date;
import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe: 领用调度事件读取信息
 */
public interface DoctorMaterialConsumeProviderReadService {

    /**
     * 获取日期范围内领用事件信息
     * @param farmId
     * @param beginDate
     * @param endDate
     * @return
     */
    Response<List<DoctorMaterialConsumeProvider>> queryDoctorMaterialConsumerEvent(String farmId, Date beginDate, Date endDate);

}
