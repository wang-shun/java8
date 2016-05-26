package io.terminus.doctor.event.dto.event.group;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 关闭猪群事件
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorCloseGroupEvent extends BaseGroupEvent implements Serializable {
    private static final long serialVersionUID = -6906331122798097306L;

    /**
     * 关闭日期
     */
    private Date closeAt;
}
