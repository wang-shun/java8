package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by chenzenghui on 16/7/27.
 */
@Data
public class View_EventListBoar implements Serializable {
    private static final long serialVersionUID = 5918176765258406604L;
    private String farmOutId;
    private String groupOutId;
    private String pigCode;
    private String pigFatherCode;
    private String pigMotherCode;
    private Date birthDate;
    private String birthWeight;     // 进场重量
    private Date inFarmDate;        // 进场日期
    private Integer inFarmDayAge;   // 进场日龄
    private String boarType;        // 公猪类型
    private String customer;
    private Integer source;
    private String breed;
    private String genetic;
    private String eventOutId;
    private Date eventAt;
    private String eventName;       // 事件名称 转换成枚举里需要的值
    private String eventDesc;
    private String remark;          // 采精的remark和event的remark
    private String barnOutId;       // 事件发生猪舍outId
    private Long price;
    private Long amount;
    private Double dilutionRatio;   // 稀释倍数
    private Double dilutionWeight;  // 稀释后重量
    private Double semenDensity;    // 精液密度
    private Double semenActive;     // 精液活力
    private Double semenPh;         // 精液pH
    private Integer score;          // 得分
    private Integer scoreHuoli;     // 活力得分
    private Integer scoreMidu;      // 密度得分
    private Integer scoreXingtai;   // 形态得分
    private Integer scoreShuliang;  // 数量得分
    private Double semenJxRatio;    // 畸形率  todo: 其他采精事件相关字段需要确认
    private Double eventWeight;
    private String chgType;         // 变动(当是猪群转出事件时才有值)
    private String chgReason;       // 变动原因(当是疾病事件时 为疾病人员名称)
    private String toBarnOutId;     // 进场事件: 进场猪舍outId, 转舍的目的地
    private String vaccName;        // 疫苗名称
    private String diseaseName;     // 疾病名称
}
/*
疾病事件:
Disease: 疾病名称, TB_FieldValue的ColID
ChgReason: 疾病人员名称

防疫:
Disease: 疫苗名称,

转舍:
OutDest: 目的地的outId

进场:
OutDest: 进场猪舍outId

 */