package io.terminus.doctor.event.dto.event.usual;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 离场事件信息
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorRemovalDto implements Serializable{

    private static final long serialVersionUID = -5166658905616894350L;

    private Long chgTypeId; // 变动类型Id basic 数据信息

    private Long chgTypeName;   // 变动名称

    private Long chgReasonId;   // 变动原因 Id

    private Long chgReasonName; // 变动原因

    private Long toBarnId;  // 转入事件信息

    private Double weight;  // 重量

    private Double price;  // 价格

    private Double sum; // 总量

    private Long customerId;    // 客户Id

    private String remark;  //注解
}
