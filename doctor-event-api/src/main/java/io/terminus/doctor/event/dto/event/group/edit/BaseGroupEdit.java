package io.terminus.doctor.event.dto.event.group.edit;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 猪群事件编辑基类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/30
 */
@Data
public class BaseGroupEdit implements Serializable {
    private static final long serialVersionUID = 8227448334200571492L;

    /**
     * 备注
     */
    private String remark;
}
