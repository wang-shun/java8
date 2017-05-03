package io.terminus.doctor.event.dto.report.daily;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by terminus on 2017/4/7.
 */
@Data
public class DoctorFatteningReport implements Serializable{
    private static final long serialVersionUID = -8474794056276694250L;
    /**
     * 存栏
     */
    private int fattenCount;
    /**
     * 转入
     */
    private int fattenIn;
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
     * 转后备
     */
    private int changeHoubei;
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
