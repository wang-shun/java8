package io.terminus.doctor.warehouse.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenzenghui on 16/9/26.
 */
@Data
public class WarehouseEventReport implements Serializable {
    private static final long serialVersionUID = -4467785782657767887L;

    private Long warehouseId;
    /**
     * 事件类型
     * @see io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider.EVENT_TYPE
     */
    private Integer eventType;
    private Double count;
    private Double amount;
}
