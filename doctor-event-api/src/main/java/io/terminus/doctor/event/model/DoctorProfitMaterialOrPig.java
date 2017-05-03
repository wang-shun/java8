package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/4/12.
 */
@Data
public class DoctorProfitMaterialOrPig implements Serializable{

    private static final long serialVersionUID = 7030712524797556958L;
    private Long farmId;
    /**
     * 饲料
     */
    private String feedTypeName;
    private Long feedTypeId;
    private Double feedAmount = 0.0;

    /**
     * 疫苗
     */
    private String vaccineTypeName;
    private Long vaccineTypeId;
    private Double vaccineAmount = 0.0;
    /**
     * 药品
     */
    private String medicineTypeName;
    private Long medicineTypeId;
    private Double medicineAmount = 0.0;
    /**
     * 消耗品
     */
    private String consumablesTypeName;
    private Long consumablesTypeId;
    private Double consumablesAmount = 0.0;
    /**
     * 原料
     */
    private String materialTypeName;
    private Long materialTypeId;
    private Double materialAmount = 0.0;

    /**
     * 猪类型
     */
    private String pigTypeName;
    private String pigTypeNameId;
    private Double amountPig;
    private Double amountYearPig = 0.0;
    /**
     * 时间
     */
    private Date sumTime;
    private String refreshTime;
    /**
     *
     */
    private Double amountYearMaterial = 0.0;
}
