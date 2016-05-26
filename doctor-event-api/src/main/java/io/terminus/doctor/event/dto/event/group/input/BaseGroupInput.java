package io.terminus.doctor.event.dto.event.group.input;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 猪群时间录入信息基类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@Data
public class BaseGroupInput implements Serializable {
    private static final long serialVersionUID = 3142495945186975856L;

    protected Long farmId;

    protected Long groupId;

}
