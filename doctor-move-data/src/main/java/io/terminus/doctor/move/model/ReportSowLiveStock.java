package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/14
 */
@Data
public class ReportSowLiveStock implements Serializable {
    private static final long serialVersionUID = -1693369264576304905L;

    private int buruSow;      // 哺乳母猪
    private int peihuaiSow;   // 配怀母猪
    private int konghuaiSow;  // 空怀空怀母猪
    private Date sumat;           // 统计时间(天初)
}
