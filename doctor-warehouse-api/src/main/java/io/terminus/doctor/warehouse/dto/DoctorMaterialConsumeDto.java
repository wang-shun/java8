package io.terminus.doctor.warehouse.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe: 原料信息领用信息
 */
@Data
public class DoctorMaterialConsumeDto implements Serializable{

    private static final long serialVersionUID = -6771008689155023995L;

    private Integer materialTypeId; // 领用原料信息

    private String materialName;

    private Integer wareHouseId;    // 领用仓库信息

    private String wareHouseName;

    private Integer barnId; // barn Id

    private String barnName;    //宿舍名称

    private String feeder;  //饲养员信息

    private Long consumeCount;

    private Integer unitId;

    private String UnitName;    // 单位信息

    private Integer consumeDays; // 耗用天数

}
