package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xjn on 17/1/9.
 * 批量事件信息
 */
@Data
public class DoctorBatchPigEventDto implements Serializable{
    private static final long serialVersionUID = 8504694341062121152L;

    private Long farmId;

    private Integer eventType;

    private Integer pigType;

    private List<String> inputJsonList;

}
