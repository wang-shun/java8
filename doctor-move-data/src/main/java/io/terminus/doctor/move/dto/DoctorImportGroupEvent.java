package io.terminus.doctor.move.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.util.Date;

/**
 * Created by xjn on 17/8/28.
 * Excel导入猪群事件
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorImportGroupEvent {

    private Integer lineNumber;            //所在Excel耳号
    private String groupCode;             //猪群code
    private Date eventAt;                 //事件时间
    private String eventName;             //事件名称
    private String remark;                //备注
    private String newBarnName;           //新建猪群所属猪舍猪舍
    private String sexName;               //性别
    private String breedName;             //品种
    private String geneticName;           //品系
    private String source;                //来源
    private String inTypeName;            //转移类型
    private Integer quantity;             //数量
    private Integer avgDayAge;            //平均日龄
    private Double avgWeight;             //平均体重
    private Long origin;                //原值

}
