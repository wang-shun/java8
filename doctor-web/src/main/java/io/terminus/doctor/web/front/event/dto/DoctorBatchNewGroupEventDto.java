package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xjn on 17/1/11.
 * 批量新建猪场输入
 */
@Data
public class DoctorBatchNewGroupEventDto implements Serializable{

    private static final long serialVersionUID = -8311680984992615474L;

    private List<DoctorNewGroupInput> newGroupInputList;
}
