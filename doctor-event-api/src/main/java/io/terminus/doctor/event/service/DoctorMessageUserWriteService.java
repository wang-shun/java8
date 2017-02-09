package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorMessageUser;

import javax.validation.constraints.NotNull;
import java.util.List;

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

    /**
     * 删除关联关系根据messageId
     * @param messageId
     * @return
     */
    Response<Boolean> deleteByMessageId(@NotNull(message = "messageId is null") Long messageId);

    /**
     * 批量删除
     * @param messageIds
     * @return
     */
    Response<Boolean> deletesByMessageIds(List<Long> messageIds);
}
