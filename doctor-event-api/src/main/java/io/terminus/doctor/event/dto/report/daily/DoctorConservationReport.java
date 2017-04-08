package io.terminus.doctor.event.dto.report.daily;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by terminus on 2017/4/7.
 */
@Data
public class DoctorConservationReport implements Serializable{
    private static final long serialVersionUID = -7241647415493283967L;

    /**
     * 存栏
     */
    private int nursery;
    /**
     * 转入
     */
    private int nurseryIn;
    /**
     * 死亡
     */
    private int dead;
    /**
     * 淘汰
     */
    private int eliminate;
    /**
     * 销售
     */
    private int sales;
    /**
     * 转育肥
     */
    private int changeFattening;
    /**
     * 转场
     */
    private int changeFarm;
    /**
     * 其他
     */
    private int other;
    /**
     * 饲料数量
     */
    private double material;
    /**
     * 饲料金额
     */
    private double materialPrice;
    /**
     * 药品金额
     */
    private double medicinePrice;
    /**
     * 疫苗金额
     */
    private double vaccinePrice;
    /**
     * 易耗品金额
     */
    private double consumablesPrice;

}
