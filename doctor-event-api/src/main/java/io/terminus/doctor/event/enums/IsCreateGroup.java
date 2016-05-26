package io.terminus.doctor.event.enums;

import lombok.Getter;

/**
 * Desc: 是否新建猪群
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */

public enum IsCreateGroup {
    NO(0, "否"),
    YES(1, "是");

    @Getter
    private Integer value;
    @Getter
    private String desc;

    IsCreateGroup(Integer value, String desc){
        this.value = value;
        this.desc = desc;
    }
}
