package io.terminus.doctor.msg.dto;

import lombok.Data;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by xjn on 17/1/22.
 */
@Data
@Builder
public class DoctorSuggestBarn implements Serializable {
    private static final long serialVersionUID = -4436485863975452533L;

    private Long barnId;
    private String barnName;
}
