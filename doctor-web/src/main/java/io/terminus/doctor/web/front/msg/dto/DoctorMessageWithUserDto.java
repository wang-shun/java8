package io.terminus.doctor.web.front.msg.dto;

import io.terminus.doctor.event.model.DoctorMessage;
import io.terminus.doctor.event.model.DoctorMessageUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * 消息表与消息用户关联表
 * Created by xiao on 16/10/12.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorMessageWithUserDto implements Serializable{
    private static final long serialVersionUID = 2791523246210624657L;

    private DoctorMessage doctorMessage;

    private DoctorMessageUser doctorMessageUser;

}
