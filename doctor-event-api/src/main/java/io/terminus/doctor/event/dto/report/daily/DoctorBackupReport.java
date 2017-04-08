package io.terminus.doctor.event.dto.report.daily;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by terminus on 2017/4/7.
 */
@Data
public class DoctorBackupReport implements Serializable{
    private static final long serialVersionUID = 4291047218557067691L;
    /**
     * 存栏
     */
    private int backupCount;
    /**
     * 转入
     */
    private int backupIn;
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
     * 转种猪
     */
    private int changeSeed;
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
