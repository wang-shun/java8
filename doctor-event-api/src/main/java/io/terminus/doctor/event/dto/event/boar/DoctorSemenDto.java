package io.terminus.doctor.event.dto.event.boar;

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
 * Descirbe:
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorSemenDto implements Serializable{

    private static final long serialVersionUID = 3227572350710428642L;

    private Date semenDate;     //采精日期

    private Double semenWeight; //采精重量

    private Double dilutionRatio;   // 稀释倍数

    private Double dilutionWeight;  //稀释后重量

    private Double semenDensity;    //精液密度

    private Double semenActive; //精液活力

    private Double semenPh; //精液PH

    private Double semenTotal;  //精液总量

    private Double semenJxRatio;    //精液畸形率

    private String semenRemark; //精液备注（非必填）
}
