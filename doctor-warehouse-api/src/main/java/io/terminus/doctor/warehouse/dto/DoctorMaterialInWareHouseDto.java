package io.terminus.doctor.warehouse.dto;

import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-05-30
 * Email:yaoqj@terminus.io
 * Descirbe:对应的物料信息管理方式
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorMaterialInWareHouseDto implements Serializable{

    private static final long serialVersionUID = 8244463624009674472L;

    private Long materialInWareHouseId; //对应的仓库原料的Id

    private Long materialId;    // 原料Id

    private String materialName;    // 原料名称

    private Double lotNumber;     //数量信息

    private String unitName;    // 单位名称

    private Long staffId;   // 人员Id

    private String staffName;   //人员姓名

    private String realName;    // 人员真实姓名

    public static DoctorMaterialInWareHouseDto buildDoctorMaterialInWareHouseInfo(DoctorMaterialInWareHouse inWareHouse,
                                                                                  DoctorWareHouse doctorWareHouse){
        return DoctorMaterialInWareHouseDto.builder().materialInWareHouseId(inWareHouse.getId())
                .materialId(inWareHouse.getMaterialId()).materialName(inWareHouse.getMaterialName())
                .lotNumber(inWareHouse.getLotNumber()).unitName(inWareHouse.getUnitName())
                .staffId(doctorWareHouse.getManagerId()).staffName(doctorWareHouse.getManagerName())
                .build();
    }
}
