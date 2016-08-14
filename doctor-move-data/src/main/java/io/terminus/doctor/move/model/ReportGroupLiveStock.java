package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 阶段仔猪存栏
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/14
 */
@Data
public class ReportGroupLiveStock implements Serializable {
    private static final long serialVersionUID = 7860287843347896819L;

    private Integer farrowCount;  // 产房仔猪
    private Integer nurseryCount; // 保育猪
    private Integer fattenCount;  // 育肥猪
    private Date sumat;           // 统计时间(天初)

}
