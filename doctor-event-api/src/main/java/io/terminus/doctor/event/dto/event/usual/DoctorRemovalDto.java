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

    private Long pigId;

    private Long chgTypeId;

    private Long chgTypeName;

    private Long chgReasonId;

    private Long chgReasonName;

    private Long toBarnId;  // 转入事件信息

    private Double weight;

    private Double price;

    private Double sum;

    private Long customerId;

    private String remark;
}
