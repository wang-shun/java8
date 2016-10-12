package io.terminus.doctor.msg.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.msg.model.DoctorMessageUser;

import javax.validation.constraints.NotNull;

/**
 * Created by xiao on 16/10/11.
 */
public interface DoctorMessageUserWriteService {
    /**
     * 创建doctormessageUser
     * @param doctorMessageUser
     * @return
     */
    Response<Long> createDoctorMessageUser(DoctorMessageUser doctorMessageUser);

    /**
     * 更新
     * @param doctorMessageUser
     * @return
     */
    Response<Boolean> updateDoctorMessageUser(DoctorMessageUser doctorMessageUser);

    Response<Boolean> deleteByMessageId(@NotNull(message = "messageId is null") Long messageId);
}
