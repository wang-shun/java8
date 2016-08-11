package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by chenzenghui on 16/7/27.
 */
@Data
public class View_EventListSow implements Serializable {
    private static final long serialVersionUID = 8185298029467518284L;
    private String eventName;       // 转换事件的名称, 以适应新版的事件
    private String sowOutId;
    private String farmOutId;
    private String pigCode;
    private String pigFatherCode;
    private String pigMotherCode;
    private Integer source;
    private Date birthDate;
    private Date inFarmDate;      // 进场日期
    private Integer inFarmDayAge;    // 进场日龄
    private Integer firstParity;     // 初始胎次
    private Integer currentParity;   // 当前胎次(相对于now而言)
    private Integer parity;          // 事件发生时的胎次(相对于事件发生日期)
    private Integer leftCount;
    private Integer rightCount;
    private String breed;
    private String genetic;
    private String eventOutId;
    private Date eventAt;
    private String eventDesc;       // 事件描述
    private String remark;          // 事件备注
    private String barnOutId;       // 事件发生猪舍
    private Long price;             // 单价(分)
    private Long amount;            // 总额(分)
    private String boarCode;        // 配种事件的公猪code
    private String serviceType;     // 配种/分娩事件复用字段, 意义不同
    private String staffName;
    private Date farrowDate;        // 预产日期
    private String pregCheckResult; // 妊娠检查/断奶复用字段
    private String litterId;        // 分娩事件字段, 其他事件均为0, 需要join
    private String farrowType;      // 分娩/断奶复用字段, 分娩类型, 断奶转入猪舍名?
    private Integer needHelp;       // 是否需要帮助
    private Integer isSingleManage; // 是否个体管理
    private Double eventWeight;
    private Integer allCount;        // 分娩总数(活仔数)
    private Integer healthyCount;    // 健仔数
    private Integer deadCount;       // 死胎数
    private Integer mummyCount;      // 木乃伊数
    private Integer jxCount;         // 畸形数
    private Integer weakCount;       // 弱仔数
    private Integer blackCount;      // 黑胎数
    private Integer chgCount;
    private String chgType;         // 仔猪变动/断奶/分娩 复用字段
    private String chgReason;       // 转舍/拼窝/离场/妊娠检查/被拼窝/断奶/仔猪变动/分娩 复用字段, 真他妈屌啊!
    private String customer;        // 分娩事件要用到
    private String nurseSow;        // 拼窝/分娩 复用字段
    private String toBarnOutId;     // 进场/转舍/分娩/被拼窝/断奶 复用事件 一般是outId或猪舍名称(分娩)
    private String disease;         // 防疫(疫苗名称)/仔猪变动/分娩/断奶/疾病(TB_FieldValue的ColID) 复用字段
    private String treatment;       // 防疫(防疫结果)/拼窝/被拼窝/转舍 复用字段
    private Integer netInCount;
    private Integer netOutCount;
    private Integer score;           // 体况得分
    private Double backFat;          // 背镖
    private Integer weanCount;       // 断奶数量
    private Double weanWeight;       // 断奶重量
    private String nestCode;         // 断奶重量
    private String diseaseName;      // 疾病事件的疾病名称
    private String changeTypeName;   // 变动类型名称
    private String fosterReasonName; // 寄养原因名称
    private String toGroupCode;      // 分娩转入猪群code
}
