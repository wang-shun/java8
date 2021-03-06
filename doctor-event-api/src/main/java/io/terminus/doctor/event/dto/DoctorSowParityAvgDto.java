package io.terminus.doctor.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by xjn on 16/12/20.
 * 母猪胎次总结各种数据平均值
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSowParityAvgDto implements Serializable{
    private static final long serialVersionUID = 3691167155073069630L;

    private double avgPigLetCount;    //平均产仔数量

    private double avgWeanCount;

    private double avgBirthWeight;  //所有出生的平均出生均重

    private double avgWeanWeight;

    private double avgHealthCount;    //平均健崽数量

    private double avgWeakCount;  // 平均弱仔数量

    private double avgDeadCount;  //平均死亡数量

    private double avgMujiCount;  //平均木畸黑数量
}
