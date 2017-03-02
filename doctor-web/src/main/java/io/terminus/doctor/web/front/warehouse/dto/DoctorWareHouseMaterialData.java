package io.terminus.doctor.web.front.warehouse.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 17/3/2.
 * 仓库物料导出数据集
 */
@Data
public class DoctorWareHouseMaterialData implements Serializable{
    private static final long serialVersionUID = -1283436628908651718L;

    private String materialName;    // 物料名称

    private String providerFactoryName; //生产厂家

    private String unitName;    // 单位名称

    private Double inCount; // 入库数量

    private Double inAmount; // 入库金额

    private Double outCount; // 出库数量

    private Double outAmount; // 出库金额

    private Double monthBeginNumber;     //月初数量

    private Double monthBeginAmount; // 月初金额

    private Double lotNumber;     //当前数量

    private Double currentAmount; // 当前金额


}
