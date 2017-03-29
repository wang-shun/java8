package io.terminus.doctor.common.enums;

import com.google.common.base.Objects;
import lombok.Getter;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 10:39 2017/3/29
 * 来源,表明是录入,excel导入,旧软件导入
 */

public enum SourceType {

    INPUT(0, "软件录入"),
    IMPORT(1, "导入数据"),
    MOVE(2, "迁移数据");

    @Getter
    private Integer value;
    @Getter
    private String desc;

    SourceType(Integer value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public static SourceType from(Integer value) {
        for (SourceType es : SourceType.values()) {
            if (Objects.equal(value, es.getValue())) {
                return es;
            }
        }
        return null;
    }
}
