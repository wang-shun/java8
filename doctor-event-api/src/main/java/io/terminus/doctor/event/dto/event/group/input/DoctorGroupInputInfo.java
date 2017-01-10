package io.terminus.doctor.event.dto.event.group.input;

import io.terminus.doctor.event.dto.DoctorGroupDetail;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 17/1/10.
 * 封装猪群事件相关信息
 */
@Data
public class DoctorGroupInputInfo implements Serializable {
    private static final long serialVersionUID = 606898319571669386L;

    /**
     * 猪群信息
     */
    private DoctorGroupDetail groupDetail;

    /**
     * 猪群事件输入信息
     */
    private BaseGroupInput input;
}
