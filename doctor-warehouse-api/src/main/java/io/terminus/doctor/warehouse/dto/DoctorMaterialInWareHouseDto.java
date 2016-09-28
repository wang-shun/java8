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

    private Long warehouseId;
    private String warehouseName;

    /**
     * @see io.terminus.doctor.common.enums.WareHouseType
     */
    private Integer type;

    private Double lotNumber;     //数量信息

    private String unitName;    // 单位名称

    private double currentAmount; // 当前金额

    private double inCount; // 入库数量

    private double inAmount; // 入库金额

    private double outCount; // 出库数量

    private double outAmount; // 出库金额

    public static DoctorMaterialInWareHouseDto buildDoctorMaterialInWareHouseInfo(DoctorMaterialInWareHouse inWareHouse){
        return DoctorMaterialInWareHouseDto.builder()
                .materialInWareHouseId(inWareHouse.getId())
                .materialId(inWareHouse.getMaterialId()).materialName(inWareHouse.getMaterialName())
                .warehouseId(inWareHouse.getWareHouseId()).warehouseName(inWareHouse.getWareHouseName())
                .type(inWareHouse.getType())
                .lotNumber(inWareHouse.getLotNumber())
                .unitName(inWareHouse.getUnitName())
                .build();
    }
}
