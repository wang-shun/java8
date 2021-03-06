package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 11:13 2017/4/21
 */
@Data
public class DoctorDailyReportSum extends DoctorBaseReport implements Serializable{
    private static final long serialVersionUID = -2034574451691997851L;


    private double farrowAvgLive;      //窝均活仔
    private double farrowAvgHealth;    //窝均健仔
    private double farrowAvgWeak;      //窝均弱仔

    private double avgWeanCount;       //窝均断奶

    private Integer preFarrowCount;     //预产胎数

}
