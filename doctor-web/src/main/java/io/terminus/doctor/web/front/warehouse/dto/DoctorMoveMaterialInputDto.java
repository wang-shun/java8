package io.terminus.doctor.web.front.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by xjn on 17/2/6.
 * 物料调拨信息输入
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorMoveMaterialInputDto implements Serializable{
    private static final long serialVersionUID = 8535603049536221096L;

    private Long farmId;
    private Long fromWareHouseId;
    private Long toWareHouseId;
    private Long materialId;
    private Double moveQuantity;
    private String eventAt;
}
