package io.terminus.doctor.web.front.msg.dto;

import io.terminus.doctor.event.model.DoctorMessageRule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by xiao on 16/8/31.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OneLevelMessageDto implements Serializable {
    private static final long serialVersionUID = 1350746532852433257L;
    /**
     * 猪只数
     */
    private Integer pigCount;

    private DoctorMessageRule doctorMessageRule;

}
