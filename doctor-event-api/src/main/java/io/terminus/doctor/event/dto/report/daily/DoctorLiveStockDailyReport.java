package io.terminus.doctor.event.dto.report.daily;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 存栏日报
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Data
public class DoctorLiveStockDailyReport implements Serializable {
    private static final long serialVersionUID = 2428304354860017632L;

    /**
     * 后备母猪
     */
    private int houbeiSow;

    /**
     * 配怀母猪(已配种 + 怀孕)
     */
    private int peihuaiSow;

    /**
     * 哺乳母猪
     */
    private int buruSow;

    /**
     * 空怀母猪(断奶 + 返情 + 流产)
     */
    private int konghuaiSow;

    /**
     * 公猪
     */
    private int boar;

    /**
     * 产房仔猪
     */
    private int farrow;

    /**
     * 保育猪
     */
    private int nursery;

    /**
     * 育肥猪
     */
    private int fatten;

    public void addSowBoar(DoctorLiveStockDailyReport dto){
        this.houbeiSow += dto.getHoubeiSow();
        this.peihuaiSow += dto.getPeihuaiSow();
        this.buruSow += dto.getBuruSow();
        this.konghuaiSow += dto.getKonghuaiSow();
        this.boar += dto.getBoar();
    }
}
