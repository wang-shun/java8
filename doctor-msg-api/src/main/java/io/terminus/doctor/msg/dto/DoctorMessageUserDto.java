package io.terminus.doctor.msg.dto;

import io.terminus.doctor.msg.model.DoctorMessageUser;
import lombok.Data;

import java.util.List;

/**
 * Created by xiao on 16/10/11.
 */
@Data
public class DoctorMessageUserDto extends DoctorMessageUser{
    /**
     * 消息状态列表
     */
    private List<Integer> statuses;
}
