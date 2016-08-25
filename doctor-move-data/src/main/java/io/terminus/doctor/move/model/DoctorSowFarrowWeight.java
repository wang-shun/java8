package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 分娩重量
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/24
 */
@Data
public class DoctorSowFarrowWeight implements Serializable {
    private static final long serialVersionUID = -6623273767651949808L;

    private String groupOutId;
    private Double farrowWeight; //分娩重量kg
}
