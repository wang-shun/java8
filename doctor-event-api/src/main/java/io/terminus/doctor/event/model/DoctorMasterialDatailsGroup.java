package io.terminus.doctor.event.model;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 猪群领用Dto
 * Created by terminus on 2017/4/17.
 */
@Data
public class DoctorMasterialDatailsGroup implements Serializable{

    private static final long serialVersionUID = -7816407708068074095L;
    private Long farmId;
    /**
     * 物料Id
     */
    private Long materialId;
    /**
     * 物料名
     */
    private String materialName;
    /**
     * 类型
     */
    private Integer type;
    /**
     * 物料类型
     */
    private Integer materialType;
    /**
     * 类型名
     */
    private String typeName;
    /**
     * 猪舍
     */
    private String barnName;
    /**
     * 猪舍Id
     */
    private Long barnId;
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
     * 猪群ID
     */
    private Long groupId;
    /**
     * 仓库名
     */
    private String wareHouseName;
    /**
     * 仓库Id
     */
    private Long wareHouseId;
    /**
     * 饲养员
     */
    private String people;
    /**
     * 建群日期
     */
    private Date openAt;
    /**
     * 关闭日期
     */
    private Date closeAt;

    /**
     * 刷新日期
     */
    private Date flushDate;

}
