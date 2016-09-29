package io.terminus.doctor.warehouse.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenzenghui on 16/9/27.
 */
@Data
public class MaterialEventReport implements Serializable {
    private static final long serialVersionUID = 8780980311921067822L;

    private Long materialId;
    private String materialName;
    private String eventTime;
    /**
     * 事件类型
     * @see io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider.EVENT_TYPE
     */
    private Integer eventType;
    private Double eventCount;
    private Double amount;
}
