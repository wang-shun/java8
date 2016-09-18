package io.terminus.doctor.event.dto.report.monthly;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 存栏变动月报
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/16
 */
@Data
public class DoctorLiveStockChangeMonthlyReport implements Serializable {
    private static final long serialVersionUID = 1000955498510651232L;

    /**
     * 后备舍
     */
    private int houbeiBegin;               //期初
    private int houbeiIn;                  //转入
    private int houbeiToSeed;              //转种猪
    private int houbeiDead;                //死淘
    private int houbeiSale;                //销售
    private double houbeiFeedCount;        //饲料数量(重量)
    private double houbeiFeedAmount;       //饲料金额
    private double houbeiDrugAmount;       //药品金额
    private double houbeiVaccineAmount;    //疫苗金额
    private double houbeiConsumerAmount;   //易耗品金额

    /**
     * 配怀舍
     */
    private int peiHuaiBegin;              //期初
    private int peiHuaiToFarrow;           //转产房
    private int peiHuaiIn;                 //进场
    private int peiHuaiWeanIn;             //断奶转入 = 断奶转出
    private int peiHuaiDead;               //死淘
    private double peiHuaiFeedCount;       //饲料数量(重量)
    private double peiHuaiFeedAmount;      //饲料金额
    private double peiHuaiDrugAmount;      //药品金额
    private double peiHuaiVaccineAmount;   //疫苗金额
    private double peiHuaiConsumerAmount;  //易耗品金额

    /**
     * 产房母猪
     */
    private int farrowSowBegin;            //期初
    private int farrowSowIn;               //转入 = 转产房
    private int farrowSowWeanOut;          //断奶转出 = 断奶转入
    private int farrowSowDead;             //死淘
    private double farrowSowFeedCount;     //饲料数量(重量)
    private double farrowSowFeedAmount;    //饲料金额
    private double farrowSowDrugAmount;    //药品金额
    private double farrowSowVaccineAmount; //疫苗金额
    private double farrowSowConsumerAmount;//易耗品金额

    /**
     * 产房仔猪
     */
    private int farrowBegin;               //期初
    private int farrowIn;                  //转入
    private int farrowToNursery;           //转保育
    private int farrowDead;                //死淘
    private int farrowSale;                //销售
    private double farrowFeedCount;        //饲料数量(重量)
    private double farrowFeedAmount;       //饲料金额
    private double farrowDrugAmount;       //药品金额
    private double farrowVaccineAmount;    //疫苗金额
    private double farrowConsumerAmount;   //易耗品金额

    /**
     * 保育猪
     */
    private int nurseryBegin;              //期初
    private int nurseryIn;                 //转入
    private int nurseryToFatten;           //转育肥
    private int nurseryDead;               //死淘
    private int nurserySale;               //销售
    private double nurseryFeedCount;       //饲料数量(重量)
    private double nurseryFeedAmount;      //饲料金额
    private double nurseryDrugAmount;      //药品金额
    private double nurseryVaccineAmount;   //疫苗金额
    private double nurseryConsumerAmount;  //易耗品金额

    /**
     * 育肥猪
     */
    private int fattenBegin;               //期初
    private int fattenIn;                  //转入
    private int fattenDead;                //死淘
    private int fattenSale;                //销售
    private double fattenFeedCount;        //饲料数量(重量)
    private double fattenFeedAmount;       //饲料金额
    private double fattenDrugAmount;       //药品金额
    private double fattenVaccineAmount;    //疫苗金额
    private double fattenConsumerAmount;   //易耗品金额
}
