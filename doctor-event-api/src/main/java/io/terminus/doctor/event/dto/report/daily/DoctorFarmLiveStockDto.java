package io.terminus.doctor.event.dto.report.daily;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by xjn on 17/4/26.
 * 猪场存栏dto
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorFarmLiveStockDto implements Serializable{

    private static final long serialVersionUID = -8788920096257520054L;

    private Long farmId;

    /**
     * 母猪数量
     */
    private Integer sow;

    /**
     * 公猪数量
     */
    private Integer boar;

    /**
     * 产房仔猪数
     */
    private Integer farrow;

    /**
     * 保育猪数
     */
    private Integer nursery;

    /**
     * 育肥猪数
     */
    private Integer fatten;

    /**
     * 后备猪数
     */
    private Integer houbei;

    /**
     * 配怀猪数
     */
    private Integer peihuai;
}
