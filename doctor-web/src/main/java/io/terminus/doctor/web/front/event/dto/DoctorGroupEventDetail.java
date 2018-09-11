package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.model.DoctorGroupEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by xjn on 17/2/5.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorGroupEventDetail extends DoctorGroupEvent implements Serializable {
    private static final long serialVersionUID = 3941077199183497746L;

    private Boolean isRollback;

    // 猪群合计 （陈娟 2018-09-05）
    // 总猪只数
    private Integer sumQuantity;

    // 总重，
    private Double sumWeight;

    // 总价值，
    private Long sumAmount;

    // 公猪数，
    private Integer sumBoarQty;

    // 母猪数，
    private Integer sumSowQty;

    // 公猪数，
    private Integer boarQty;

    // 母猪数，
    private Integer sowQty;

}
