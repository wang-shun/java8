package io.terminus.doctor.event.dto.event.group.input;

import io.terminus.doctor.event.model.DoctorGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by xjn on 17/1/11.
 * 新建猪群信息封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorNewGroupInputInfo implements Serializable{
    private static final long serialVersionUID = 4024828114824471210L;

    private DoctorGroup group;

    private DoctorNewGroupInput newGroupInput;
}
