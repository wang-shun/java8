package io.terminus.doctor.web.front.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-05-31
 * Email:yaoqj@terminus.io
 * Descirbe: 对应的物料领用消耗录入信息
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorConsumeProviderInputDto implements Serializable{

    private static final long serialVersionUID = 3717428182117868904L;

    private Long farmId;       // 对应的猪场Id

    private Long wareHouseId;   //对应的仓库Id

    private Long materialId;    // 对应的物料Id

    private Long count;  //对应的数量

    private Long unitPrice; // 单价, 单位为"分"

    private Integer consumeDays;    // 对应的消耗日期

    private Long barnId;    //对应的消耗 猪舍Id

    private String barnName;    //对应的猪舍名称
}
