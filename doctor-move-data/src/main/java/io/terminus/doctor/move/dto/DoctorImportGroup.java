package io.terminus.doctor.move.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.util.Date;

/**
 * Created by xjn on 17/8/28.
 * 导入猪群页
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorImportGroup {
    private Integer lineNumber;            //所在Excel耳号
    private String groupCode;             //猪群号
    private String barnName;              //猪舍
    private String sex;                   //性别
    private Integer liveStock;            //存栏
    private Integer avgDayAge;            //平均日龄
    private Double avgWeight;             //平均体重
    private Date newGroupDate;            //建群日期
}
