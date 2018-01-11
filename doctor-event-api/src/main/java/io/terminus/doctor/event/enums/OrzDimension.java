package io.terminus.doctor.event.enums;

import lombok.Getter;

/**
 * Created by xjn on 18/1/11.
 * email:xiaojiannan@terminus.io
 * 组织维度
 */
public enum OrzDimension {
    CLIQUE(1, "集团"),
    ORG(2, "公司"),
    FARM(3, "猪场");

    @Getter
    private Integer value;
    @Getter
    private String name;

    OrzDimension(Integer value, String name) {
        this.value = value;
        this.name = name;
    }
}
