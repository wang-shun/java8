package io.terminus.doctor.move.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.util.Date;

/**
 * Created by xjn on 17/8/25.
 * Excel中猪页对应pojo
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorImportPigEvent {
    private String pigCode;               //猪耳号
    private Date eventAt;                 //事件时间
    private String eventName;             //事件名称
    private String barnName;              //事件发生猪舍
    private String pigSex;                //性别
    private String remark;                //备注
    private Date birthday;                //出生日期
    private String source;                //进场来源
    private String breedName;             //品种
    private String breedTypeName;         //品系
    private Integer parity;               //母猪进场胎次
    private String boarType;              //公猪进场类型
    private String mateType;              //配种类型
    private String mateBoarCode;          //配种公猪号
    private String mateOperator;          //配种员
    private String pregCheckResult;       //妊娠检查结果
    private String toBarnName;            //去分娩产房猪舍名
    private String farrowingType;         //分娩类型
    private Double birthNestAvg;          //出生窝重
    private Integer healthyCount;         //健仔数量(断奶事件校验对应的母猪状态信息)
    private Integer weakCount;            //弱仔数量
    private Integer jixingCount;          //畸形
    private Integer deadCount;            //死仔
    private Integer mummyCount;           //木乃伊
    private Integer blackCount;           //黑胎
    private Integer partWeanPigletsCount; //部分断奶数量
    private Double partWeanAvgWeight;     //断奶平均重量
    private String weanToBarn;            //断奶转入猪舍

}
