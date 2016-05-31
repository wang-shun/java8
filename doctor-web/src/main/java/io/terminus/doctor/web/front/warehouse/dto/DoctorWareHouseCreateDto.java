package io.terminus.doctor.web.front.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-05-30
 * Email:yaoqj@terminus.io
 * Descirbe: WareHouse 仓库创建的方式
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorWareHouseCreateDto implements Serializable{

    private static final long serialVersionUID = -2254236798030507493L;

    private String wareHouseName;   // 仓库名称

    private Long farmId;    // 猪场Id

    private Long managerId; // 原料Id

    private String address; // 地址信息

    /**
     * 不同的仓库类型，见枚举类型
     * @see io.terminus.doctor.warehouse.enums.WareHouseType
     */
    private Integer type;

    private Long creatorId;
}
