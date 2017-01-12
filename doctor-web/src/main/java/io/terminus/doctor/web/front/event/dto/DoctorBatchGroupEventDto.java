package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xjn on 17/1/11.
 * 猪群批量事件封装
 */
@Data
public class DoctorBatchGroupEventDto implements Serializable{
    private static final long serialVersionUID = 7423411199173520837L;

    private Integer eventType;
    private List<DoctorGroupEventInputDto> inputList;
}
