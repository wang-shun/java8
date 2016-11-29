package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;

/**
 * Created by yaoqijun.
 * Date:2016-07-20
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Deprecated
public interface DoctorDailyPigReportWriteService {

    /**
     * 修改对应的DoctorPigDaily 对应的日常信息信息
     * @param pigEventId
     * @return
     */
    @Deprecated
    Response<Boolean> updateDailyPigReportInfo(Long pigEventId);

}
