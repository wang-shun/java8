package io.terminus.doctor.msg.dto;

import io.terminus.doctor.msg.model.DoctorMessage;
import lombok.Data;

import java.util.List;

/**
 * Created by xiao on 16/9/18.
 */
@Data
public class DoctorMessageSearchDto extends DoctorMessage {
    private static final long serialVersionUID = 1616433046730343439L;

    /**
     * 消息状态列表
     */
    private List<Integer> statuses;
    /**
     * 消息类型列表
     */
    private List<Integer> types;

    /**
     * 消息ids
     */
    private List<Long> ids;

    private String eventStartAt;

    private String eventEndAt;

    private String otherStartAt;

    private String otherEndAt;

}
