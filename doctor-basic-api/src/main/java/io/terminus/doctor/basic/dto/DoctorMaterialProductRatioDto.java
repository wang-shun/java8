package io.terminus.doctor.basic.dto;

import io.terminus.doctor.basic.model.FeedFormula;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-05-18
 * Email:yaoqj@terminus.io
 * Descirbe: 原料生产比例信息
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorMaterialProductRatioDto implements Serializable{

    private static final long serialVersionUID = 7187738042137376051L;

    private Long materialId; // 原料Id

    private Long farmId;    // 对应的猪场Id

    private FeedFormula.FeedProduce produce; // 原料配比信息
}
