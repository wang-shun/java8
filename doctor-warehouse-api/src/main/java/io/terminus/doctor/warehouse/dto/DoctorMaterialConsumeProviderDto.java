package io.terminus.doctor.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe: 物料的领用消耗基本数据信息
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorMaterialConsumeProviderDto implements Serializable{

    private static final long serialVersionUID = -6771008689155023995L;

    /**
     * 操作类型
     * @see io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider.EVENT_TYPE
     */
    private Integer actionType;

    private Integer type;   //领取物料类型

    private Long farmId;    //对应的猪场信息

    private String farmName;    //猪场名称

    private Long materialTypeId; // 领用原料信息

    private String materialName;

    private Long wareHouseId;    // 领用仓库信息

    private String wareHouseName;

    private Long barnId; // barn Id

    private String barnName;    //宿舍名称

    private Long staffId;

    private String staffName;  //饲养员信息

    private Long count;

    private Long unitId;

    private String unitName;    // 单位信息

    private Integer consumeDays; // 耗用天数
}
