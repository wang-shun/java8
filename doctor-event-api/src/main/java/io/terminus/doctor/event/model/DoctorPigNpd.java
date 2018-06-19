package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DoctorPigNpd implements Serializable {

    private Long id;    //Id

    private Long orgId; // 公司信息

    private Long farmId; // 猪场信息

    private Long pigId; // 猪Id

    private Date sumAt;//日期

    private Integer npd;//非生产天数

    private Integer pregnancy;//怀孕期

    private Integer lactation;//哺乳期

    private Date createdAt;//创建时间

    private Date updatedAt;//更新时间

}
