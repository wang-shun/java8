package io.terminus.doctor.event.dto.report;

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

    private Integer houbeiSow;

    private Integer peihuaiSow;

    private Integer buruSow;

    private Integer konghuaiSow;

    private Integer boar;

    private Integer farrow;

    private Integer nursery;

    private Integer fatten;
}
