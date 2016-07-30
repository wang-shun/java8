package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by chenzenghui on 16/7/27.
 */
@Data
public class View_EventListGain implements Serializable {
    private static final long serialVersionUID = 5363694163735495420L;
    private String diseaseName;        // 疾病名称
    private Date birthDate;
    private Date eventAt;
    private String eventTypeName;
    private String changTypeName;      // 猪群变动的变动类型名称
    private String changeReasonName;   // 变动原因名称
    private String inTypeName;         //转入猪群事件的转入类型
    private Double weight;
    private Double avgWeight;
    private Long price;
    private Long amount;
    private String customer;
    private String context;            //此字段有多个意思
    private Integer isAuto;            //是否是自动生成的事件
    private String sexName;
    private String breed;
    private String source;             // 来源或其他乱七八糟的值
    private Integer quantity;
    private Integer sowQty;
    private Integer boarQty;
    private Integer avgDayAge;
    private String staffName;
    private String groupOutId;         // 管理的猪群OID
    private String groupEventOutId;    // 猪群事件的OID
    private String barnOutId;          // 事件发生的猪舍OID
    private String toBarnOutId;        // 转入猪舍的OID, 不一定是在猪舍oid, 也可能是猪群oid
    private String notDisease;         // 不是疾病, 可能有多个值
    private String toGroupOutId;       // 猪群转群的 目标猪群outId
    private String eventDesc;          // 事件详情
    private String remark;
}

/*
防疫：
Disease: 疫苗名称
Treatment: 防疫结果
防疫人员直接取 staffName

疾病：
Disease: 疾病名称的ColID 关联 TB_FieldValue
疾病人员直接取 staffName

新建猪群：
来源：可能为空，如果为空，默认本场

转入猪群：
SourceGainOID 来源猪群
来源猪舍从来源猪群取

猪群变动：
ChgType: 变动类型id
ChgReason:变动原因名称
如果ChgType.name=转出，ChgReason=群间转移， 说明这是转群事件！！ sql已经转换
  群间转移（转群）：Treament: 转入猪舍， OutDest: 转入猪群
  选后备（？）: Treament: 转入猪舍， OutDest: 转入猪群

猪群转群：
TreatMent: 是否新建猪群
OutDestnation: 转入猪舍
SourceGainOID: 转入猪群

商品猪转为种猪:
LitterId: 转种猪的name = pigCode
birthDate:
Treament: 转入猪舍
*/

