package io.terminus.doctor.warehouse.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe: 用户供给物料信息 TODO 具体界面信息待定
 */
@Data
public class DoctorMaterialProviderDto implements Serializable{

    private static final long serialVersionUID = -6771008689155023995L;

    private Long farmId;    //对应的猪场信息

    private Long materialTypeId; // 领用原料信息

    private String materialName;

    private Long wareHouseId;    // 领用仓库信息

    private String wareHouseName;

    private Integer barnId; // barn Id

    private String barnName;    //宿舍名称

    private String feeder;  //饲养员信息

    private Long consumeCount;

    private Integer unitId;

    private String UnitName;    // 单位信息
}
