package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-11 16:16:32
 * Created by [ your name ]
 */
@Data
public class DoctorReportMaterial implements Serializable {

    private static final long serialVersionUID = 9007113088301467279L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 
     */
    private Date sumAt;
    
    /**
     * 
     */
    private String sumAtName;
    
    /**
     * 日期类型，日周月季年
     * @see io.terminus.doctor.event.enums.DateDimension
     */
    private Integer dateType;
    
    /**
     * 组织ID
     */
    private Long orzId;
    
    /**
     * 组织名称
     */
    private String orzName;
    
    /**
     * 组织类型，猪场，公司，集团
     * @see io.terminus.doctor.event.enums.OrzDimension
     */
    private Integer orzType;
    
    /**
     * 后备饲料金额
     */
    private BigDecimal houbeiFeedAmount;
    
    /**
     * 后备饲料数量
     */
    private Integer houbeiFeedQuantity;
    
    /**
     * 后备原料金额
     */
    private BigDecimal houbeiMaterialAmount;
    
    /**
     * 后备原料数量
     */
    private Integer houbeiMaterialQuantity;
    
    /**
     * 后备疫苗金额
     */
    private BigDecimal houbeiVaccinationAmount;
    
    /**
     * 后备兽药金额
     */
    private BigDecimal houbeiMedicineAmount;
    
    /**
     * 后备消耗品金额
     */
    private BigDecimal houbeiConsumeAmount;
    
    /**
     * 配怀饲料金额
     */
    private BigDecimal peihuaiFeedAmount;
    
    /**
     * 配怀饲料数量
     */
    private Integer peihuaiFeedQuantity;
    
    /**
     * 配怀原料金额
     */
    private BigDecimal peihuaiMaterialAmount;
    
    /**
     * 配怀原料数量
     */
    private Integer peihuaiMaterialQuantity;
    
    /**
     * 配怀疫苗金额
     */
    private BigDecimal peihuaiVaccinationAmount;
    
    /**
     * 配怀兽药金额
     */
    private BigDecimal peihuaiMedicineAmount;
    
    /**
     * 配怀消耗品金额
     */
    private BigDecimal peihuaiConsumeAmount;
    
    /**
     * 产房母猪饲料金额
     */
    private BigDecimal sowFeedAmount;
    
    /**
     * 产房母猪饲料数量
     */
    private Integer sowFeedQuantity;
    
    /**
     * 产房母猪原料金额
     */
    private BigDecimal sowMaterialAmount;
    
    /**
     * 产房母猪原料数量
     */
    private Integer sowMaterialQuantity;
    
    /**
     * 产房母猪疫苗金额
     */
    private BigDecimal sowVaccinationAmount;
    
    /**
     * 产房母猪兽药金额
     */
    private BigDecimal sowMedicineAmount;
    
    /**
     * 产房母猪消耗品金额
     */
    private BigDecimal sowConsumeAmount;
    
    /**
     * 产房仔猪饲料金额
     */
    private BigDecimal pigletFeedAmount;
    
    /**
     * 产房仔猪饲料数量
     */
    private Integer pigletFeedQuantity;
    
    /**
     * 产房仔猪原料金额
     */
    private BigDecimal pigletMaterialAmount;
    
    /**
     * 产房仔猪原料数量
     */
    private Integer pigletMaterialQuantity;
    
    /**
     * 产房仔猪疫苗金额
     */
    private BigDecimal pigletVaccinationAmount;
    
    /**
     * 产房仔猪兽药金额
     */
    private BigDecimal pigletMedicineAmount;
    
    /**
     * 产房仔猪消耗品金额
     */
    private BigDecimal pigletConsumeAmount;
    
    /**
     * 保育饲料金额
     */
    private BigDecimal baoyuFeedAmount;
    
    /**
     * 保育饲料数量
     */
    private Integer baoyuFeedQuantity;
    
    /**
     * 保育原料金额
     */
    private BigDecimal baoyuMaterialAmount;
    
    /**
     * 保育原料数量
     */
    private Integer baoyuMaterialQuantity;
    
    /**
     * 保育疫苗金额
     */
    private BigDecimal baoyuVaccinationAmount;
    
    /**
     * 保育兽药金额
     */
    private BigDecimal baoyuMedicineAmount;
    
    /**
     * 保育消耗品金额
     */
    private BigDecimal baoyuConsumeAmount;
    
    /**
     * 育肥饲料金额
     */
    private BigDecimal yufeiFeedAmount;
    
    /**
     * 育肥饲料数量
     */
    private Integer yufeiFeedQuantity;
    
    /**
     * 育肥原料金额
     */
    private BigDecimal yufeiMaterialAmount;
    
    /**
     * 育肥原料数量
     */
    private Integer yufeiMaterialQuantity;
    
    /**
     * 育肥疫苗金额
     */
    private BigDecimal yufeiVaccinationAmount;
    
    /**
     * 育肥兽药金额
     */
    private BigDecimal yufeiMedicineAmount;
    
    /**
     * 育肥消耗品金额
     */
    private BigDecimal yufeiConsumeAmount;
    
    /**
     * 公猪饲料金额
     */
    private BigDecimal boarFeedAmount;
    
    /**
     * 公猪饲料数量
     */
    private Integer boarFeedQuantity;
    
    /**
     * 公猪原料金额
     */
    private BigDecimal boarMaterialAmount;
    
    /**
     * 公猪原料数量
     */
    private Integer boarMaterialQuantity;
    
    /**
     * 公猪疫苗金额
     */
    private BigDecimal boarVaccinationAmount;
    
    /**
     * 公猪兽药金额
     */
    private BigDecimal boarMedicineAmount;
    
    /**
     * 公猪消耗品金额
     */
    private BigDecimal boarConsumeAmount;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}