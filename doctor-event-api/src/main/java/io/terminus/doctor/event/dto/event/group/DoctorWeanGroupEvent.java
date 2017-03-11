package io.terminus.doctor.event.dto.event.group;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 13:17 17/3/11
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorWeanGroupEvent extends BaseGroupEvent implements Serializable {
    private static final long serialVersionUID = 6521063134132667198L;

    private Date partWeanDate; //断奶日期

    private Integer partWeanPigletsCount; //部分断奶数量

    private Double partWeanAvgWeight;   //断奶平均重量

    private String partWeanRemark;  //部分断奶标识

    private Integer qualifiedCount; // 合格数量

    private Integer notQualifiedCount; //不合格的数量
}
