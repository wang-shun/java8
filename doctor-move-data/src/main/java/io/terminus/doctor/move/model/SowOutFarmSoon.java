package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 母猪即将离场的状态转换
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/3
 */
@Data
public class SowOutFarmSoon implements Serializable {
    private static final long serialVersionUID = 6684173341227856985L;

    private String leaveType;
}
