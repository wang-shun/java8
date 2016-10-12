package io.terminus.doctor.msg.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.msg.model.DoctorHistoryMessage;

/**
 * Created by xiao on 16/10/11.
 */
public interface DoctorHistoryMessageWriteService {
    Response<Long> createHistoryMessage(DoctorHistoryMessage doctorHistoryMessage);

}
