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
 * Descirbe: 部分断奶事件录入
 * (每次断奶都是部分断奶, 旧软件的最后一次断奶会触发转舍事件, 新软件不会触发)
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorPartWeanDto implements Serializable{

    private static final long serialVersionUID = 252972605944533095L;

    private Date partWeanDate; //断奶日期

    private Integer partWeanPigletsCount; //部分断奶数量

    private Double partWeanAvgWeight;   //断奶平均重量

    private String partWeanRemark;  //部分断奶标识

    private Integer qualifiedCount; // 合格数量

    private Integer notQualifiedCount; //不合格的数量
}
