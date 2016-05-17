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

    private Long pigId;

    private Date semenDate;

    private Double weight;

    private Double dilutionRatio;

    private Double dilutionWeight;

    private Double density;

    private Double active;

    private Double ph;

    private Double total;

    private Double jxRatio;

    private String remark;
}
