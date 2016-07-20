package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-07-19
 * Email:yaoqj@terminus.io
 * Descirbe: 统计对应的母猪不同状态
 */
@Data
public class DoctorPigStatusCount implements Serializable {

    private static final long serialVersionUID = -8682281373701625098L;

    private Integer status;

    private Integer count;

}
