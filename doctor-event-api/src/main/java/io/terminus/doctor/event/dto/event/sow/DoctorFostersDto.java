package io.terminus.doctor.event.dto.event.sow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 仔猪拼窝事件(自动生成代哺的事件)
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorFostersDto implements Serializable{

    private static final long serialVersionUID = 2998287596879859648L;

    private Date fostersDate;   // 拼窝日期

    private Integer fostersCount;   //  拼窝数量

    private Integer sowFostersCount;    // 拼窝母猪数量

    private Integer boarFostersCount;   // 拼窝公猪数量

    private Double fosterTotalWeight;   //拼窝总重量

    private Long fosterReason;  //寄养原因

    private Long fosterSowId;   // 拼窝母猪Id

    private String fosterRemark;    // 拼窝标识
}
