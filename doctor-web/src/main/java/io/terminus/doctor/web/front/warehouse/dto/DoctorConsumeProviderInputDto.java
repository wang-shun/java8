package io.terminus.doctor.web.front.warehouse.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-05-31
 * Email:yaoqj@terminus.io
 * Descirbe: 对应的物料领用消耗录入信息
 */
@Data
public class DoctorConsumeProviderInputDto implements Serializable{

    private static final long serialVersionUID = 3717428182117868904L;

    private Long farmId;       // 对应的猪场Id

    private Long wareHouseId;   //对应的仓库Id

    private Long materialId;    // 对应的物料Id

    private Long barnId;    //对应的消耗 猪舍Id

    private Long feederId;  //对应的饲养员Id

    private Long count;  //对应的数量

    private Integer consumeDays;    // 对应的消耗日期
}
