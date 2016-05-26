package io.terminus.doctor.event.dto.event.group;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 猪群事件基类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@Data
public class BaseGroupEvent implements Serializable {
    private static final long serialVersionUID = 8577777699827163897L;

    /**
     * 事件类型
     * @see io.terminus.doctor.event.enums.GroupEventType
     */
    protected Integer type;
}
