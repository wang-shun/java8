package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 公猪存栏
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/14
 */
@Data
public class ReportBoarLiveStock implements Serializable {
    private static final long serialVersionUID = 7860287843347896819L;
    private Date sumat;           // 统计时间(天初)
    private int quantity;
}
