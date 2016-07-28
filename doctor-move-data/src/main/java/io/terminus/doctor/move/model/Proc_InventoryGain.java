package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 猪群存栏的存储过程
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/28
 */
@Data
public class Proc_InventoryGain implements Serializable {
    private static final long serialVersionUID = -6571990550994802911L;

    private String groupOutId;  //猪群outId

    private String farmOutId;   //猪场outId

    private Integer quantity;   //猪只数

    private Double avgWeight;   //均重

    private Integer avgDayAge;  //日龄
}
