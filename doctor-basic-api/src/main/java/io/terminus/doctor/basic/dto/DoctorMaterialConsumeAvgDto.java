package io.terminus.doctor.basic.dto;

import io.terminus.doctor.common.enums.WareHouseType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 物料消耗信息统计方式Dto
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorMaterialConsumeAvgDto implements Serializable {

    private static final long serialVersionUID = 6393888957046257078L;

    /**
     * @see io.terminus.doctor.basic.model.DoctorMaterialConsumeAvg
     */

    // 猪场
    private Long farmId;
    private String farmName;

    // 仓库
    private Long wareHouseId;
    private String wareHouseName;

    // 管理员
    private Long managerId;
    private String managerName;

    // 原料
    private Long materialId;
    private String materialName;

    /**
     * @see WareHouseType
     */
    private Integer type;

    // 剩余消耗天数
    private Integer lotConsumeDay;

    // 剩余量
    private Double lotNumber;

    // 平均消耗数量
    private Long consumeAvgCount;

    // 消耗数量
    private Long consumeCount;

    // 消耗日期
    private Date consumeDate;
}
