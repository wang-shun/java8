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
 * Descirbe: 断奶事件
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWeanDto implements Serializable {

    private static final long serialVersionUID = -8375069125388951376L;

    private Date weanDate;  // 断奶日期

    private Integer pigletsCount;   // 断奶数量

    private Double avgWeight;   //平均断奶重量

    private String weanRemark;  //断奶标识

    private Integer qualifiedCount; // 合格数量

    private Integer notQualifiedCount; //不合格的数量
}
