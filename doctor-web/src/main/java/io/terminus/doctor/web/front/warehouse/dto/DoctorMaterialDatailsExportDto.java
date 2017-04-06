package io.terminus.doctor.web.front.warehouse.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/4/6.
 */
@Data
public class DoctorMaterialDatailsExportDto implements Serializable{

    private static final long serialVersionUID = 3423060897030227862L;
    /**
     * 物料名
     */
    private String materialName;
    /**
     * 类型
     */
    private Long type;
    /**
     * 物料类型
     */
    private String materialType;
    /**
     * 类型名
     */
    private String typeName;
    /**
     * 猪舍
     */
    private String barnName;
    /**
     * 时间
     */
    private Date updatedAt;
    /**
     * 数量
     */
    private Double number;
    /**
     * 价格
     */
    private Long price;
    /**
     * 金额
     */
    private Double priceSum;
    /**
     * 物料单位
     */
    private String unitName;
    /**
     * 猪群名
     */
    private String groupName;
    /**
     * 仓库名
     */
    private String wareHouseName;
    /**
     * 饲养员
     */
    private String people;
}
