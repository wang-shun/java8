package io.terminus.doctor.open.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 16/12/22.
 * 月报外部调用数据封装
 */
@Data
public class DoctorMonthlyReportOpen implements Serializable{
    private static final long serialVersionUID = 55475440288200390L;

    /**
     * 猪场名称
     */
    private String farmName;

    /**
     * 统计时间
     */
    private Date date;

    /**
     * 断奶情况
     */
    private int weanSow;                      //断奶母猪数
    private int weanPiglet;                   //断奶仔猪数
    private double weanAvgWeight;             //断奶均重(kg)
    private double weanAvgCount;              //窝均断奶数

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
     * 其他
     */
    private double npd;                       //非生产天数
    private double psy;                       //psy
    private double mateInSeven;               //断奶7天配种率

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

    /**
     * 销售情况
     */
    private int saleSow;                      //母猪
    private int saleBoar;                     //公猪
    private int saleNursery;                  //保育猪（产房+保育）
    private int saleFatten;                   //育肥猪
    private double saleAmount;                //销售金额汇总
    private double saleFarrow;                //仔猪销售数
    // TODO: 16/12/22  缺少字段  saleAmount saleFarrow purchaseAmount

    private double purchaseAmount;            //采购金额


}
