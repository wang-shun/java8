package io.terminus.doctor.event.dto;

import io.terminus.doctor.event.model.DoctorGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪群查询条件
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorGroupSearchDto extends DoctorGroup implements Serializable {
    private static final long serialVersionUID = -7372130840721095447L;

    private Date startOpenAt;   //建群开始时间

    private Date endOpenAt;     //建群结束时间

    private Date startCloseAt;  //关群开始时间

    private Date endCloseAt;    //关群结束时间
}
