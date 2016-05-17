package io.terminus.doctor.warehouse.dto;

import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe: 猪场仓库类型统计
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorFarmWareHouseTypeDto implements Serializable {

    private static final long serialVersionUID = 8660869818248687526L;

    private Long remainder; //剩余数量

    private Integer materialType;

    private String materialName;    //品类名称

    private Integer materialClass;  //  品种数据信息

    private String materialClassName;   //品种数据

    private Long consumeToday;  //当日领用记录

    private String manager; //管理人员

    public static DoctorFarmWareHouseTypeDto convertWareHouseFarmTypeDto(List<DoctorMaterialConsumeProvider> consumeProviderList){
        // TODO convert farm ware house type
        return null;
    }
}


