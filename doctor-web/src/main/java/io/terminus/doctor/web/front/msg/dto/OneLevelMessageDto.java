package io.terminus.doctor.web.front.msg.dto;

import io.terminus.doctor.msg.model.DoctorMessageRule;
import lombok.Data;
import lombok.experimental.Builder;

/**
 * Created by xiao on 16/8/31.
 */
@Data
@Builder
public class OneLevelMessageDto {
    /**
     * 猪只数
     */
    private Long pigCount;

    private DoctorMessageRule doctorMessageRule;

}
