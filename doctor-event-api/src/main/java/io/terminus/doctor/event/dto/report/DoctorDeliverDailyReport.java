package io.terminus.doctor.event.dto.report;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 分娩日报
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/19
 */
@Data
public class DoctorDeliverDailyReport implements Serializable {
    private static final long serialVersionUID = 2753759652989219311L;

    /**
     * 分娩窝数
     */
    private int nest;

    /**
     * 活仔数
     */
    private int live;

    /**
     * 健仔数
     */
    private int health;

    /**
     * 弱仔数
     */
    private int weak;

    /**
     * 死黑木畸
     */
    private int black;

    public void addDeliverCount(DoctorDeliverDailyReport dto){
        this.nest += dto.getNest();
        this.live += dto.getLive();
        this.health += dto.getHealth();
        this.weak += dto.getWeak();
        this.black += dto.getBlack();
    }
}
