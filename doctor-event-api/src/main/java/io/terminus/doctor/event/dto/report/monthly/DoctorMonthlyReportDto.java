package io.terminus.doctor.event.dto.report.monthly;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 猪场月报json字段
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/12
 */
@Data
public class DoctorMonthlyReportDto implements Serializable {
    private static final long serialVersionUID = -2490312543838256507L;

    /**
     * 配种情况
     */
    private int mateHoubei;                   //配后备
    private int mateWean;                     //配断奶
    private int mateFanqing;                  //配返情
    private int mateAbort;                    //配流产
    private int mateNegtive;                  //配阴性
    private double mateEstimatePregRate;      //估算受胎率
    private double mateRealPregRate;          //实际受胎率
    private double mateEstimateFarrowingRate; //估算配种分娩率
    private double mateRealFarrowingRate;     //实际配种分娩率

    /**
     * 妊娠检查情况
     */
    private int checkPositive;                //妊娠检查阳性
    private int checkFanqing;                 //返情
    private int checkAbort;                   //流产
    private int checkNegtive;                 //妊娠检查阴性

    /**
     * 分娩情况
     */
    private int farrowEstimateParity;         //预产胎数
    private int farrowNest;                   //分娩窝数
    private int farrowAlive;                  //产活仔数
    private int farrowHealth;                 //产键仔数
    private int farrowWeak;                   //产弱仔数
    private int farrowDead;                   //产死仔数
    private int farrowMny;                    //木乃伊数
    private int farrowAll;                    //总产仔数
    private double farrowAvgHealth;           //窝均健仔数
    private double farrowAvgAll;              //窝均产仔数
    private double farrowAvgAlive;            //窝均活仔数

    /**
     * 断奶情况
     */
    private int weanSow;                      //断奶母猪数
    private int weanPiglet;                   //断奶仔猪数
    private double weanAvgWeight;             //断奶均重
    private double weanAvgCount;              //窝均断奶数

    /**
     * 销售情况
     */
    private int saleSow;                      //母猪
    private int saleBoar;                     //公猪
    private int saleNursery;                  //保育猪（产房+保育）
    private int saleFatten;                   //育肥猪

    /**
     * 死淘情况
     */
    private int deadSow;                      //母猪
    private int deadBoar;                     //公猪
    private int deadFarrow;                   //产房仔猪
    private int deadNursery;                  //保育猪
    private int deadFatten;                   //育肥猪
    private double deadFarrowRate;            //产房死淘率
    private double deadNurseryRate;           //保育死淘率
    private double deadFattenRate;            //育肥死淘率
}
