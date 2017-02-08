package io.terminus.doctor.web.front.warehouse.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 17/2/6.
 * 物料盘点输入信息
 */
@Data
public class DoctorInventoryInputDto implements Serializable{
    private static final long serialVersionUID = -1695708159501143707L;

    private Long farmId;
    private Long wareHouseId;
    private Long materialId;
    private Double count;
    private String eventAt;
}
