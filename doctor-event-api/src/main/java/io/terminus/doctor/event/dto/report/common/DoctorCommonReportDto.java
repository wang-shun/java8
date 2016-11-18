package io.terminus.doctor.event.dto.report.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 猪场报表json字段(月报，周报公用)
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/12
 */
@Data
public class DoctorCommonReportDto implements Serializable {
    private static final long serialVersionUID = -2490312543838256507L;

    /**
     * 是否失败, true 失败(作为区分 0 与 未查询到结果)
     */
    private boolean fail;

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
    private int farrowJx;                     //产畸形数
    private int farrowMny;                    //木乃伊数
    private int farrowBlack;                  //产黑胎数
    private int farrowAll;                    //总产仔数
    private double farrowAvgHealth;           //窝均健仔数
    private double farrowAvgAll;              //窝均产仔数
    private double farrowAvgAlive;            //窝均活仔数
    private double farrowAvgWeak;             //窝均弱仔数
    private double farrowAvgWeight;           //分娩活仔均重(kg)

    /**
     * 断奶情况
     */
    private int weanSow;                      //断奶母猪数
    private int weanPiglet;                   //断奶仔猪数
    private double weanAvgWeight;             //断奶均重(kg)
    private double weanAvgCount;              //窝均断奶数
    private double weanAvgDayAge;             //断奶均日龄

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
    private int deadHoubei;                   //后备猪
    private double deadFarrowRate;            //产房死淘率
    private double deadNurseryRate;           //保育死淘率
    private double deadFattenRate;            //育肥死淘率

    /**
     * 其他
     */
    private double npd;                       //非生产天数
    private double psy;                       //psy
    private double mateInSeven;               //断奶7天配种率

    /**
     * 公猪生产成绩
     */
    private int boarMateCount;                    //配种次数
    private int boarFirstMateCount;               //首配母猪数
    private int boarSowPregCount;                 //受胎头数
    private int boarSowFarrowCount;               //产仔母猪头数
    private double boarFarrowAvgCount;            //平均产仔头数
    private double boarFarrowLiveAvgCount;        //平均产活仔数
    private double boarPregRate;                  //受胎率
    private double boarFarrowRate;                //分娩率

    /**
     * 存栏变动
     */
    private DoctorLiveStockChangeCommonReport liveStockChange;

    private String date;                      //统计月份 2016年08月, 供前台显示

    /**
     * 胎次分布
     */
    private List<DoctorStockStructureCommonReport> parityStockList;

    /**
     * 品类分布
     */
    private List<DoctorStockStructureCommonReport> breedStockList;
}
